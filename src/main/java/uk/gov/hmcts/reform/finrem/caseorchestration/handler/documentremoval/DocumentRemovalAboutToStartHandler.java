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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentToKeep;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentToKeepCollection;
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

    private final ObjectMapper objectMapper;
    private final DocumentRemovalService documentRemovalService;


    public DocumentRemovalAboutToStartHandler(FinremCaseDetailsMapper mapper,
                                              DocumentRemovalService documentRemovalService,
                                              ObjectMapper objectMapper) {
        super(mapper);
        this.documentRemovalService = documentRemovalService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && (EventType.REMOVE_CASE_DOCUMENT.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest, String userAuthorisation) {
        List<JsonNode> documentNodes = new ArrayList<>();
        List<DocumentToKeepCollection> documentsCollection = new ArrayList<>();

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();

        log.info("Invoking event document removal about to start callback for Case ID: {}",
            caseDetails.getId());

        JsonNode root = objectMapper.valueToTree(caseData);
        documentRemovalService.retrieveDocumentNodes(root, documentNodes);

        // TODO: Sort by document upload_timestamp if provided with document node.
        documentNodes = documentNodes.stream().distinct().toList();

        for (JsonNode documentNode : documentNodes) {
            String docUrl = documentNode.get(DOCUMENT_URL).asText();
            String[] documentUrlAsArray = docUrl.split("/");
            String docId = documentUrlAsArray[documentUrlAsArray.length - 1];

            documentsCollection.add(
                DocumentToKeepCollection.builder()
                    .value(DocumentToKeep.builder()
                        .documentUrl(docUrl)
                        .documentFilename(documentNode.get(DOCUMENT_FILENAME).asText())
                        .documentId(docId)
                        .build())
                    .build());
        }

        log.info("Retrieved {} case documents to remove from Case ID {}", documentNodes.size(), caseDetails.getId());

        caseData.setDocumentToKeepCollection(documentsCollection);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).build();
    }

}
