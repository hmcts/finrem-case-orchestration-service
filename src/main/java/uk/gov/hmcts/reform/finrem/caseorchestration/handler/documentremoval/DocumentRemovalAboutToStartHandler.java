package uk.gov.hmcts.reform.finrem.caseorchestration.handler.documentremoval;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentToRemove;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentToRemoveCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentremoval.DocumentRemovalService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DocumentRemovalAboutToStartHandler extends FinremCallbackHandler {

    static final String DOCUMENT_URL = "document_url";
    static final String DOCUMENT_FILENAME = "document_filename";
    static final String DOCUMENT_TIME_STAMP = "document_timestamp";

    private final ObjectMapper objectMapper;
    private final DocumentRemovalService documentRemovalService;


    public DocumentRemovalAboutToStartHandler(FinremCaseDetailsMapper mapper, DocumentRemovalService documentRemovalService) {
        super(mapper);
        this.documentRemovalService = documentRemovalService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && (EventType.REMOVE_CASE_DOCUMENT.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest, String userAuthorisation) {
        List<JsonNode> documentNodes = new ArrayList<>();
        List<DocumentToRemoveCollection> documentsCollection = new ArrayList<>();

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();

        JsonNode root = objectMapper.valueToTree(caseData);
        documentRemovalService.retrieveDocumentNodes(root, documentNodes);

        // TODO: Sort by document upload_timestamp if provided with document node.
        documentNodes = documentNodes.stream().distinct().toList();

        for (JsonNode documentNode : documentNodes) {
            String docUrl = documentNode.get(DOCUMENT_URL).asText();
            String[] documentUrlAsArray = docUrl.split("/");
            String docId = documentUrlAsArray[documentUrlAsArray.length-1];

            documentsCollection.add(
                DocumentToRemoveCollection.builder()
                    .value(DocumentToRemove.builder()
                        .documentToRemoveUrl(docUrl)
                        .documentToRemoveName(documentNode.get(DOCUMENT_FILENAME).asText())
                        .documentToRemoveId(docId)
                        .build())
                    .build());
        }

        caseData.setDocumentToRemoveCollection(documentsCollection);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).build();
    }

}
