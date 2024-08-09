package uk.gov.hmcts.reform.finrem.caseorchestration.handler.documentremoval;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentToKeepCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentremoval.DocumentRemovalService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DocumentRemovalAboutToSubmitHandler extends FinremCallbackHandler {

    private final ObjectMapper objectMapper;
    private final DocumentRemovalService documentRemovalService;

    @Autowired
    public DocumentRemovalAboutToSubmitHandler(FinremCaseDetailsMapper mapper,
                                               DocumentRemovalService documentRemovalService,
                                               GenericDocumentService genericDocumentService) {
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

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();

        List<JsonNode> documentNodes = new ArrayList<>();
        List<DocumentToKeepCollection> documentsUserWantsToKeepList = caseData.getDocumentToKeepCollection();
        JsonNode root = objectMapper.valueToTree(caseData);

        // Gets the case data as a node tree
        documentRemovalService.retrieveDocumentNodes(root, documentNodes);

        // Removes duplicates from the node tree - bundle this with the above function when refactored.
        documentNodes = documentNodes.stream().distinct().toList();

        // Uses the node tree to rebuild the same documents collection, that we use to display to the user after about-to-start
        List<DocumentToKeepCollection> allExistingDocumentsList = documentRemovalService.buildCaseDocumentList(documentNodes);

        // Uses and compares collections to see what file(s) the user wants removed
        ArrayList<DocumentToKeepCollection> documentsUserWantsDeletedList = new ArrayList<>(allExistingDocumentsList);
        // documentsUserWantsDeletedCollection is the difference between a list of allExistingDocumentsCollection and documentsUserWantsToKeepList
        documentsUserWantsDeletedList.removeAll(documentsUserWantsToKeepList);

        //PROVE whether CRUD needed to delete things - see if this extends to files.  As this goes through CCD AM
//        documentsUserWantsDeletedList.forEach( documentToDeleteCollection ->
//                documentRemovalService.deleteDocument(
//                        documentToDeleteCollection.getValue(), userAuthorisation));

        documentsUserWantsDeletedList.forEach( documentToDeleteCollection ->
                documentRemovalService.updateNodeForDocumentToDelete(
                        root, documentToDeleteCollection.getValue()));


        documentRemovalService.removeDocumentToRemoveCollection(root);

        FinremCaseData amendedCaseData =
                documentRemovalService.buildAmendedCaseDataFromRootNode(root, caseDetails.getId(), objectMapper);

        // Put in better logging, and try catch exception handling and custom exception for it all.

        // Refactor to use the Mapper bean already in COS?

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(amendedCaseData).build();
    }

}
