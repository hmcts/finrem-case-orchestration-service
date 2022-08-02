package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.ccd.callback.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseType;
import uk.gov.hmcts.reform.finrem.ccd.domain.EventType;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.SendOrderEventPostStateOption;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendOrderContestedSubmittedHandler implements CallbackHandler {

    private final FeatureToggleService featureToggleService;
    private final NotificationService notificationService;
    private final CcdService ccdService;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.SEND_ORDER.equals(eventType);
    }

    @Override
    public AboutToStartOrSubmitCallbackResponse handle(CallbackRequest callbackRequest,
                                                       String userAuthorisation) {

        sendNotifications(callbackRequest);

        updateCaseWithPostStateOption(callbackRequest, userAuthorisation);

        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(callbackRequest.getCaseDetails().getCaseData())
            .build();
    }

    private void updateCaseWithPostStateOption(CallbackRequest callbackRequest, String userAuthorisation) {

        SendOrderEventPostStateOption postStateOption =
            callbackRequest.getCaseDetails().getCaseData().getSendOrderPostStateOption();

        if (isOptionThatRequireUpdate(postStateOption)) {
            callbackRequest.getCaseDetails().getCaseData().setSendOrderPostStateOption(null);
            ccdService.executeCcdEventOnCase(
                userAuthorisation,
                callbackRequest.getCaseDetails(),
                postStateOption.getEventToTrigger().getCcdType());
        }
    }

    private boolean isOptionThatRequireUpdate(SendOrderEventPostStateOption postStateOption) {
        return SendOrderEventPostStateOption.PREPARE_FOR_HEARING.equals(postStateOption)
            || SendOrderEventPostStateOption.CLOSE.equals(postStateOption);
    }

    private void sendNotifications(CallbackRequest callbackRequest) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getCaseData();
        if (!caseData.isPaperCase() && Objects.nonNull(caseData.getFinalOrderCollection())) {
            log.info("Received request to send email for 'Contest Order Approved' for Case ID: {}", callbackRequest.getCaseDetails().getId());
            if (caseData.isApplicantSolicitorAgreeToReceiveEmails()) {
                log.info("Sending 'Contest Order Approved' email notification to Applicant Solicitor");
                notificationService.sendContestOrderApprovedEmailApplicant(caseDetails);
            }

            if (notificationService.isRespondentSolicitorEmailCommunicationEnabled(caseData)) {
                log.info("Sending 'Contest Order Approved' email notification to Respondent Solicitor");
                notificationService.sendContestOrderApprovedEmailRespondent(caseDetails);
            }
        }
    }
}
