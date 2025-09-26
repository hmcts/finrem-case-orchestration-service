package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hwf.HwfCorrespondenceService;

@Slf4j
@Service
public class HwfSuccessfulSubmittedHandler extends FinremCallbackHandler {

    private final HwfCorrespondenceService hwfNotificationsService;

    public HwfSuccessfulSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                         HwfCorrespondenceService hwfNotificationsService) {

        super(finremCaseDetailsMapper);
        this.hwfNotificationsService = hwfNotificationsService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && (CaseType.CONTESTED.equals(caseType) || CaseType.CONSENTED.equals(caseType))
            && (EventType.FR_HWF_DECISION_MADE.equals(eventType)
            || EventType.FR_HWF_DECISION_MADE_FROM_AWAITING_PAYMENT.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.submitted(callbackRequest));
        validateCaseData(callbackRequest);
        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        hwfNotificationsService.sendCorrespondence(finremCaseDetails, userAuthorisation);
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(callbackRequest.getCaseDetails().getData())
            .build();
    }
}
