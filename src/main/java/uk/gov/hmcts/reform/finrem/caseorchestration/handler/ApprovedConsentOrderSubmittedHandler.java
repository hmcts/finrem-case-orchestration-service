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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovedConsentOrderSubmittedHandler implements CallbackHandler {

    private final CaseDataService caseDataService;
    private final NotificationService notificationService;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.APPROVE_ORDER.equals(eventType);
    }

    @Override
    public AboutToStartOrSubmitCallbackResponse handle(CallbackRequest callbackRequest,
                                                       String userAuthorisation) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        sendConsentOrderAvailableEmailNotifications(caseDetails, caseData);
        sendConsentOrderMadeEmailNotifications(caseDetails, caseData);

        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(callbackRequest.getCaseDetails().getData())
            .build();
    }


    private void sendConsentOrderAvailableEmailNotifications(CaseDetails caseDetails, Map<String, Object> caseData) {
        notificationService.sendConsentOrderAvailableCtscEmail(caseDetails);

        if (caseDataService.isApplicantSolicitorAgreeToReceiveEmails(caseDetails)) {
            log.info("case - {}: Sending email notification for to Applicant Solicitor for 'Consent Order Available'", caseDetails.getId());
            notificationService.sendConsentOrderAvailableEmailToApplicantSolicitor(caseDetails);
        }
        if (notificationService.isRespondentSolicitorEmailCommunicationEnabled(caseData)) {
            log.info("case - {}: Sending email notification to Respondent Solicitor for 'Consent Order Available'", caseDetails.getId());
            notificationService.sendConsentOrderAvailableEmailToRespondentSolicitor(caseDetails);
        }
    }

    private void sendConsentOrderMadeEmailNotifications(CaseDetails caseDetails, Map<String, Object> caseData) {
        if (caseDataService.isApplicantSolicitorAgreeToReceiveEmails(caseDetails)
            && caseDataService.isConsentedApplication(caseDetails)) {
            log.info("Sending email notification to Applicant Solicitor for 'Consent Order Made'");
            notificationService.sendConsentOrderMadeConfirmationEmailToApplicantSolicitor(caseDetails);
        }

        if (notificationService.isRespondentSolicitorEmailCommunicationEnabled(caseData)) {
            log.info("Sending email notification to Respondent Solicitor for 'Consent Order Made'");
            notificationService.sendConsentOrderMadeConfirmationEmailToRespondentSolicitor(caseDetails);
        }
    }
}
