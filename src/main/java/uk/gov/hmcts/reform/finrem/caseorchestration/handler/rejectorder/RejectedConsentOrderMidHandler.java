package uk.gov.hmcts.reform.finrem.caseorchestration.handler.rejectorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.RefusalOrderDocumentService;

@Slf4j
@Service
public class RejectedConsentOrderMidHandler extends FinremCallbackHandler {

    private final RefusalOrderDocumentService refusalOrderDocumentService;

    public RejectedConsentOrderMidHandler(FinremCaseDetailsMapper mapper,
                                          RefusalOrderDocumentService refusalOrderDocumentService) {
        super(mapper);
        this.refusalOrderDocumentService = refusalOrderDocumentService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.REJECT_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.midEvent(callbackRequest));
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();

        FinremCaseData caseData = refusalOrderDocumentService.previewConsentOrderNotApproved(userAuthorisation, caseDetails);
        clearContestedFields(caseData);

        return response(caseData);
    }

    private void clearContestedFields(FinremCaseData caseData) {
        if (caseData != null) {
            caseData.setIntervenerOne(null);
            caseData.setIntervenerTwo(null);
            caseData.setIntervenerThree(null);
            caseData.setIntervenerFour(null);
        }
    }
}
