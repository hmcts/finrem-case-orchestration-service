package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.ccd.callback.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseType;
import uk.gov.hmcts.reform.finrem.ccd.domain.EventType;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

@Slf4j
@Service
@RequiredArgsConstructor
public class RejectedConsentOrderSubmittedHandler implements CallbackHandler {

    private final NotificationService notificationService;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.ORDER_REFUSAL.equals(eventType);
    }

    @Override
    public AboutToStartOrSubmitCallbackResponse handle(CallbackRequest callbackRequest,
                                                       String userAuthorisation) {

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();

        if (notificationService.isApplicantSolicitorRegisteredAndEmailCommunicationEnabled(caseDetails)) {
            if (caseDetails.getCaseData().isConsentedApplication()) {
                log.info("Sending email notification to Applicant Solicitor for 'Consent Order Not Approved'");
                notificationService.sendConsentOrderNotApprovedEmailToApplicantSolicitor(caseDetails);
            } else {
                log.info("Sending email notification to Applicant Solicitor for 'Contest Order Not Approved'");
                notificationService.sendContestOrderNotApprovedEmailApplicant(caseDetails);
            }
        }

        FinremCaseData caseData = caseDetails.getCaseData();
        if (notificationService.isRespondentSolicitorEmailCommunicationEnabled(caseData)) {
            if (caseData.isConsentedApplication()) {
                log.info("Sending email notification to Respondent Solicitor for 'Consent Order Not Approved'");
                notificationService.sendConsentOrderNotApprovedEmailToRespondentSolicitor(caseDetails);
            } else {
                log.info("Sending email notification to Respondent Solicitor for 'Contest Order Not Approved'");
                notificationService.sendContestOrderNotApprovedEmailRespondent(caseDetails);
            }
        }
        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(callbackRequest.getCaseDetails().getCaseData())
            .build();
    }
}
