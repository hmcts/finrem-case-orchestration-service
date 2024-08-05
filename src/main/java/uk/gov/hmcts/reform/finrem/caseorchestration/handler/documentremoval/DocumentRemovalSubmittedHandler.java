package uk.gov.hmcts.reform.finrem.caseorchestration.handler.documentremoval;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
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

import static uk.gov.hmcts.reform.finrem.caseorchestration.service.documentremoval.DocumentRemovalService.DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.documentremoval.DocumentRemovalService.DOCUMENT_URL;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DocumentRemovalSubmittedHandler extends FinremCallbackHandler {

    private final ObjectMapper objectMapper;
    private final DocumentRemovalService documentRemovalService;

    public DocumentRemovalSubmittedHandler(FinremCaseDetailsMapper mapper,  DocumentRemovalService documentRemovalService) {
        super(mapper);
        this.documentRemovalService = documentRemovalService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
                && (EventType.REMOVE_CASE_DOCUMENT.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest, String userAuthorisation) {

        List<JsonNode> documentNodes = new ArrayList<>();

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();

        List<DocumentToRemoveCollection> documentsUserWantsToKeepCollection = caseData.getDocumentToRemoveCollection();

        JsonNode root = objectMapper.valueToTree(caseData);
        documentRemovalService.retrieveDocumentNodes(root, documentNodes);

        documentNodes = documentNodes.stream().distinct().toList();

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

        ArrayList<DocumentToRemoveCollection> documentsUserWantsDeletedCollection = new ArrayList<>(allExistingDocumentsCollection);
        documentsUserWantsDeletedCollection.removeAll(documentsUserWantsToKeepCollection);

        //Upload a new 'document deleted file'
        String deletedDocId = "a guid";
        //Todo

        // Update root so that the document details are redacted for each document that needs to be deleted.
        documentsUserWantsDeletedCollection.forEach( documentToDelete ->
                documentRemovalService.updateNodeForDocumentToDelete(root, documentToDelete.getValue(), deletedDocId));

        // Build case data with contents of documentsUserWantsDeletedCollection removed. File id replaced.



        // Remove DocumentToRemoveCollection
        //Todo

        // then make a call to the help class to delete the doc from doc store - if no such call already exists.
        //Todo

        // Put in better logging

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).build();
    }

}

