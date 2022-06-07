package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.SendOrderPostStateOption;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.SendOrderPostStateOption.getSendOrderPostStateOption;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FINAL_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SEND_ORDER_POST_STATE_OPTION_FIELD;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendOrderContestedSubmittedHandler implements CallbackHandler {

    private final CaseDataService caseDataService;
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
            .data(callbackRequest.getCaseDetails().getData())
            .build();
    }

    private void updateCaseWithPostStateOption(CallbackRequest callbackRequest, String userAuthorisation) {

        SendOrderPostStateOption sendOrderPostStateOption =
            getSendOrderPostStateOption(
                (String) callbackRequest.getCaseDetails().getData().get(SEND_ORDER_POST_STATE_OPTION_FIELD));

        if (isOptionThatRequireUpdate(sendOrderPostStateOption)) {
            callbackRequest.getCaseDetails().getData().put(SEND_ORDER_POST_STATE_OPTION_FIELD, null);
            ccdService.executeCcdEventOnCase(
                userAuthorisation,
                callbackRequest.getCaseDetails(),
                sendOrderPostStateOption.getEventToTrigger().getCcdType());
        }
    }

    private boolean isOptionThatRequireUpdate(SendOrderPostStateOption sendOrderPostStateOption) {
        return SendOrderPostStateOption.PREPARE_FOR_HEARING.equals(sendOrderPostStateOption)
            || SendOrderPostStateOption.CLOSE.equals(sendOrderPostStateOption);
    }

    private void sendNotifications(CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();
        if (!caseDataService.isPaperApplication(caseData) && Objects.nonNull(caseData.get(FINAL_ORDER_COLLECTION))) {
            log.info("Received request to send email for 'Contest Order Approved' for Case ID: {}", callbackRequest.getCaseDetails().getId());
            if (caseDataService.isApplicantSolicitorAgreeToReceiveEmails(caseDetails)) {
                log.info("Sending 'Contest Order Approved' email notification to Applicant Solicitor");
                notificationService.sendContestOrderApprovedEmailApplicant(caseDetails);
            }

            if (featureToggleService.isRespondentJourneyEnabled()
                && notificationService.shouldEmailRespondentSolicitor(caseData)) {
                log.info("Sending 'Contest Order Approved' email notification to Respondent Solicitor");
                notificationService.sendContestOrderApprovedEmailRespondent(caseDetails);
            }
        }
    }
}
