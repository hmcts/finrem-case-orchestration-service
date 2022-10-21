package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovedConsentOrderSubmittedHandler implements CallbackHandler<Map<String, Object>> {

    private final CaseDataService caseDataService;
    private final NotificationService notificationService;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.APPROVE_APPLICATION.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle(CallbackRequest callbackRequest,
                                                                                   String userAuthorisation) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        if (caseDataService.isApplicantSolicitorAgreeToReceiveEmails(caseDetails)
            && caseDataService.isConsentedApplication(caseDetails)) {
            log.info("Sending email notification to Applicant Solicitor for 'Consent Order Made'");
            notificationService.sendConsentOrderMadeConfirmationEmailToApplicantSolicitor(caseDetails);
        }

        if (notificationService.isRespondentSolicitorEmailCommunicationEnabled(caseData)) {
            log.info("Sending email notification to Respondent Solicitor for 'Consent Order Made'");
            notificationService.sendConsentOrderMadeConfirmationEmailToRespondentSolicitor(caseDetails);
        }

        return GenericAboutToStartOrSubmitCallbackResponse
            .<Map<String, Object>>builder()
            .data(callbackRequest.getCaseDetails().getData())
            .build();
    }
}
