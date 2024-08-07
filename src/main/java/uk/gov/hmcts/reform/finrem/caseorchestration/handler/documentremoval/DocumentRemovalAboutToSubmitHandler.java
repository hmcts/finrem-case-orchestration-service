package uk.gov.hmcts.reform.finrem.caseorchestration.handler.documentremoval;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.DocumentDeleteException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentToRemove;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentToRemoveCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentremoval.DocumentRemovalService;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.documentremoval.DocumentRemovalService.DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.documentremoval.DocumentRemovalService.DOCUMENT_URL;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DocumentRemovalAboutToSubmitHandler extends FinremCallbackHandler {

    private final ObjectMapper objectMapper;
    private final DocumentRemovalService documentRemovalService;

    public DocumentRemovalAboutToSubmitHandler(FinremCaseDetailsMapper mapper, DocumentRemovalService documentRemovalService) {
        super(mapper);
        this.documentRemovalService = documentRemovalService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
                && (EventType.REMOVE_CASE_DOCUMENT.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest, String userAuthorisation) {

        List<JsonNode> documentNodes = new ArrayList<>();

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();

        List<DocumentToRemoveCollection> documentsUserWantsToKeepCollection = caseData.getDocumentToRemoveCollection();

        JsonNode root = objectMapper.valueToTree(caseData);

        // Gets the case data as a node tree
        documentRemovalService.retrieveDocumentNodes(root, documentNodes);

        // Removes duplicates from the node tree
        documentNodes = documentNodes.stream().distinct().toList();

        // Uses the node tree to rebuild the same documents collection, that we use to display to the user after about-to-submit
        List<DocumentToRemoveCollection> allExistingDocumentsCollection = new ArrayList<>();
        for (JsonNode documentNode : documentNodes) {
            String docUrl = documentNode.get(DOCUMENT_URL).asText();
            String[] documentUrlAsArray = docUrl.split("/");
            String docId = documentUrlAsArray[documentUrlAsArray.length-1];

            allExistingDocumentsCollection.add(
                    DocumentToRemoveCollection.builder()
                            .value(DocumentToRemove.builder()
                                    .documentToRemoveUrl(docUrl)
                                    .documentToRemoveName(documentNode.get(DOCUMENT_FILENAME).asText())
                                    .documentToRemoveId(docId)
                                    .build())
                            .build());
        }

        // Uses and compares collections to see what file(s) the user wants removed
        ArrayList<DocumentToRemoveCollection> documentsUserWantsDeletedCollection = new ArrayList<>(allExistingDocumentsCollection);
        // documentsUserWantsDeletedCollection is the difference between allExistingDocumentsCollection and documentsUserWantsToKeepCollection
        documentsUserWantsDeletedCollection.removeAll(documentsUserWantsToKeepCollection);

        //Upload a new 'document deleted file' Hardcoded at the moment.
        // todo

        // Update root so that the document details are redacted for each document that needs to be deleted.
        JsonNode newNode = documentRemovalService.buildNewNodeForDeletedFile(objectMapper,"url", "filename", "binary");
        documentsUserWantsDeletedCollection.forEach( documentToDelete ->
                documentRemovalService.updateNodeForDocumentToDelete(root, newNode, documentToDelete.getValue()));

        // Clears out the document collection from the root node, so that it isn't part of the final CCD data.
        documentRemovalService.removeDocumentToRemoveCollection(root);

        // rebuild case data with file data redacted.  Does this from the root node with the required updates.
        FinremCaseData amendedCaseData;
        try {
            amendedCaseData = objectMapper.treeToValue(root, FinremCaseData.class);
        } catch (Exception e) {
            log.error(format("Error building amendedCaseData for case id %s after deleting document", caseDetails.getId()), e);
            throw new DocumentDeleteException(e.getMessage(), e);
        }

        // then make a call to the help class to delete the doc from doc store - if no such call already exists.
        //Todo

        // Put in better logging, and try catch exception handling and custom exception for it all.

        // Refactor to use the Mapper bean already in COS?

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(amendedCaseData).build();
    }

}
