package uk.gov.hmcts.reform.finrem.caseorchestration.handler.documentremoval;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
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
    static final String DOCUMENT_BINARY_URL = "document_binary_url";

    private final DocumentRemovalService documentRemovalService;


    public DocumentRemovalAboutToStartHandler(FinremCaseDetailsMapper mapper,
                                              DocumentRemovalService documentRemovalService) {
        super(mapper);
        this.documentRemovalService = documentRemovalService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && (CaseType.CONTESTED.equals(caseType)
                || CaseType.CONSENTED.equals(caseType))
            && (EventType.REMOVE_CASE_DOCUMENT.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest, String userAuthorisation) {
        List<DocumentToKeepCollection> documentsCollection = new ArrayList<>();

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();

        log.info("Invoking event document removal about to start callback for Case ID: {}",
            caseDetails.getId());

        // TODO: Sort by document upload_timestamp if provided with document node.
        List<JsonNode> documentNodes = documentRemovalService.getDocumentNodes(caseData)
            .stream().distinct().toList();

        for (JsonNode documentNode : documentNodes) {
            String docUrl = documentNode.get(DOCUMENT_URL).asText();
            String[] documentUrlAsArray = docUrl.split("/");
            String docId = documentUrlAsArray[documentUrlAsArray.length - 1];

            documentsCollection.add(
                DocumentToKeepCollection.builder()
                    .value(DocumentToKeep.builder()
                        .documentId(docId)
                        .caseDocument(CaseDocument.builder()
                            .documentFilename(documentNode.get(DOCUMENT_FILENAME).asText())
                            .documentUrl(documentNode.get(DOCUMENT_URL).asText())
                            .documentBinaryUrl(documentNode.get(DOCUMENT_BINARY_URL).asText())
                            .build())
                        .build())
                    .build());
        }

        log.info("Retrieved {} case documents to remove from Case ID {}", documentNodes.size(), caseDetails.getId());

        caseData.setDocumentToKeepCollection(documentsCollection);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).build();
    }

}