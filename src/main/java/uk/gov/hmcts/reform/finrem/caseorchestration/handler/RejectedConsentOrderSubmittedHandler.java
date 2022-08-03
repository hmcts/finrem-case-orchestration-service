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
public class RejectedConsentOrderSubmittedHandler implements CallbackHandler {

    private final CaseDataService caseDataService;
    private final NotificationService notificationService;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.REJECT_ORDER.equals(eventType);
    }

    @Override
    public AboutToStartOrSubmitCallbackResponse handle(CallbackRequest callbackRequest,
                                                       String userAuthorisation) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        if (caseDataService.isApplicantSolicitorAgreeToReceiveEmails(caseDetails)) {
            if (caseDataService.isConsentedApplication(caseDetails)) {
                log.info("Sending email notification to Applicant Solicitor for 'Consent Order Not Approved'");
                notificationService.sendConsentOrderNotApprovedEmailToApplicantSolicitor(caseDetails);
            } else {
                log.info("Sending email notification to Applicant Solicitor for 'Contest Order Not Approved'");
                notificationService.sendContestOrderNotApprovedEmailApplicant(caseDetails);
            }
        }

        Map<String, Object> caseData = caseDetails.getData();
        if (notificationService.isRespondentSolicitorEmailCommunicationEnabled(caseData)) {
            if (caseDataService.isConsentedApplication(caseDetails)) {
                log.info("Sending email notification to Respondent Solicitor for 'Consent Order Not Approved'");
                notificationService.sendConsentOrderNotApprovedEmailToRespondentSolicitor(caseDetails);
            } else {
                log.info("Sending email notification to Respondent Solicitor for 'Contest Order Not Approved'");
                notificationService.sendContestOrderNotApprovedEmailRespondent(caseDetails);
            }
        }
        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(callbackRequest.getCaseDetails().getData())
            .build();
    }
}
