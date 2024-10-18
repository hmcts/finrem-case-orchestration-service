package uk.gov.hmcts.reform.finrem.caseorchestration.handler.documentremoval;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentToKeepCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentremoval.DocumentRemovalService;

import java.util.List;

@Slf4j
@Service
public class DocumentRemovalAboutToStartHandler extends FinremCallbackHandler {

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

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();

        log.info("Invoking event document removal about to start callback for Case ID: {}",
            caseDetails.getId());

        List<DocumentToKeepCollection> documentsCollection =
            documentRemovalService.getCaseDocumentsList(caseData);

        log.info("Retrieved {} case documents to remove from Case ID {}", documentsCollection.size(), caseDetails.getId());

        caseData.setDocumentToKeepCollection(documentsCollection);
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).build();
    }

}
