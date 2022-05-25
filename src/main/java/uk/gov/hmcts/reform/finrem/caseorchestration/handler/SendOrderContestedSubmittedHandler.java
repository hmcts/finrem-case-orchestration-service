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
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.SendOrderPostStateOption.CLOSE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.SendOrderPostStateOption.PREPARE_FOR_HEARING;
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

        processPostEventStateOptions(callbackRequest);

        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(callbackRequest.getCaseDetails().getData())
            .build();
    }

    private void processPostEventStateOptions(CallbackRequest callbackRequest) {
        Optional
            .ofNullable((String) callbackRequest.getCaseDetails().getData().get(SEND_ORDER_POST_STATE_OPTION_FIELD))
            .ifPresent(postStateOptionField -> updateCaseWithPostStateOption(postStateOptionField, callbackRequest));
    }

    private void updateCaseWithPostStateOption(String sendOrderPostStateOptionCcdField,
                                               CallbackRequest callbackRequest) {

        SendOrderPostStateOption sendOrderPostStateOption =
            getSendOrderPostStateOption(sendOrderPostStateOptionCcdField);

        CaseDetails updatedCaseDetails = callbackRequest.getCaseDetails();
        if (PREPARE_FOR_HEARING.equals(sendOrderPostStateOption)) {
            updatedCaseDetails =
                ccdService.executeCcdEventOnCase(callbackRequest, EventType.PREPARE_FOR_HEARING.getCcdType());
        } else if (CLOSE.equals(sendOrderPostStateOption)) {
            updatedCaseDetails =
                ccdService.executeCcdEventOnCase(callbackRequest, EventType.CLOSE.getCcdType());
        }

        callbackRequest.setCaseDetails(updatedCaseDetails);
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
