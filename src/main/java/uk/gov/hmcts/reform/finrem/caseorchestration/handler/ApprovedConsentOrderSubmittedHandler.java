package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
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
public class ApprovedConsentOrderSubmittedHandler implements CallbackHandler {

    private final CaseDataService caseDataService;
    private final NotificationService notificationService;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.APPROVE_APPLICATION.equals(eventType);
    }

    @Override
    public AboutToStartOrSubmitCallbackResponse handle(CallbackRequest callbackRequest,
                                                       String userAuthorisation) {

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getCaseData();

        if (notificationService.isApplicantSolicitorRegisteredAndEmailCommunicationEnabled(caseDetails)
            && caseData.isConsentedApplication()) {
            log.info("Sending email notification to Applicant Solicitor for 'Consent Order Made'");
            notificationService.sendConsentOrderMadeConfirmationEmailToApplicantSolicitor(caseDetails);
        }

        if (notificationService.isRespondentSolicitorEmailCommunicationEnabled(caseData)) {
            log.info("Sending email notification to Respondent Solicitor for 'Consent Order Made'");
            notificationService.sendConsentOrderMadeConfirmationEmailToRespondentSolicitor(caseDetails);
        }

        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(callbackRequest.getCaseDetails().getCaseData())
            .build();
    }
}
