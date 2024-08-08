package uk.gov.hmcts.reform.finrem.caseorchestration.handler.documentremoval;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentremoval.DocumentRemovalService;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.documentremoval.DocumentRemovalService.DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.documentremoval.DocumentRemovalService.DOCUMENT_URL;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DocumentRemovalAboutToSubmitHandler extends FinremCallbackHandler {

    private final ObjectMapper objectMapper;
    private final DocumentRemovalService documentRemovalService;
    private final GenericDocumentService genericDocumentService;

    @Autowired
    public DocumentRemovalAboutToSubmitHandler(FinremCaseDetailsMapper mapper,
                                               DocumentRemovalService documentRemovalService,
                                               GenericDocumentService genericDocumentService) {
        super(mapper);
        this.documentRemovalService = documentRemovalService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.genericDocumentService = genericDocumentService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
                && (EventType.REMOVE_CASE_DOCUMENT.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest, String userAuthorisation) {

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();

        List<JsonNode> documentNodes = new ArrayList<>();
        List<DocumentToRemoveCollection> documentsUserWantsToKeepList = caseData.getDocumentToRemoveCollection();
        JsonNode root = objectMapper.valueToTree(caseData);

        // Gets the case data as a node tree
        documentRemovalService.retrieveDocumentNodes(root, documentNodes);

        // Removes duplicates from the node tree - bundle this with the above function when refactored.
        documentNodes = documentNodes.stream().distinct().toList();

        // Uses the node tree to rebuild the same documents collection, that we use to display to the user after about-to-start
        List<DocumentToRemoveCollection> allExistingDocumentsList = documentRemovalService.buildCaseDocumentList(documentNodes);

        // Uses and compares collections to see what file(s) the user wants removed
        ArrayList<DocumentToRemoveCollection> documentsUserWantsDeletedList = new ArrayList<>(allExistingDocumentsList);
        // documentsUserWantsDeletedCollection is the difference between a list of allExistingDocumentsCollection and documentsUserWantsToKeepList
        documentsUserWantsDeletedList.removeAll(documentsUserWantsToKeepList);

        // ACs require uploading a new document deleted file.  UploadDocumentContestedAboutToSubmitHandler indicates
        // that EXUI handles the actual doc upload.  The handler validates that urls, sorts and categorises.
        // DFR doesn't do this yet, but we could do via template/docmosis (GenerateDocumentService.java)*.
        // For now this changes to a valid anonymous stores and GUID so that the document can be accessed.
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        JsonNode newNode = documentRemovalService.buildNewNodeForDeletedFile(
                objectMapper,
                "http://redacted_store/documents/00000000-0000-0000-0000-000000000000",
                String.format("Document removed - %s", LocalDateTime.now().format(dtf)),
                "http://redacted_store/documents/00000000-0000-0000-0000-000000000000/binary"
        );

        //PROVE whether CRUD needed to delete things - see if this extends to files.  As this goes through CCD AM
        documentsUserWantsDeletedList.forEach( documentToDeleteCollection ->
                documentRemovalService.deleteDocument(
                        documentToDeleteCollection.getValue(), userAuthorisation));

        documentsUserWantsDeletedList.forEach( documentToDeleteCollection ->
                documentRemovalService.updateNodeForDocumentToDelete(
                        root, newNode, documentToDeleteCollection.getValue()));


        documentRemovalService.removeDocumentToRemoveCollection(root);

        FinremCaseData amendedCaseData =
                documentRemovalService.buildAmendedCaseDataFromRootNode(root, caseDetails.getId(), objectMapper);

        // Put in better logging, and try catch exception handling and custom exception for it all.

        // Refactor to use the Mapper bean already in COS?

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(amendedCaseData).build();
    }

}
