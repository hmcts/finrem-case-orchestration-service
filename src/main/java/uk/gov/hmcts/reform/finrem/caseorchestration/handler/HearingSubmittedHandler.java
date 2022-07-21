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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AdditionalHearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

@Slf4j
@Service
@RequiredArgsConstructor
public class HearingSubmittedHandler implements CallbackHandler {

    private final CaseDataService caseDataService;
    private final NotificationService notificationService;
    private final HearingDocumentService hearingDocumentService;
    private final AdditionalHearingDocumentService additionalHearingDocumentService;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.LIST_FOR_HEARING.equals(eventType);
    }

    @Override
    public AboutToStartOrSubmitCallbackResponse handle(CallbackRequest callbackRequest,
                                                       String userAuthorisation) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Received request to send email for 'Prepare for Hearing' for Case ID: {}", callbackRequest.getCaseDetails().getId());

        if (caseDataService.isApplicantSolicitorAgreeToReceiveEmails(caseDetails)) {
            log.info("Sending email notification to Applicant Solicitor for 'Prepare for Hearing'");
            notificationService.sendPrepareForHearingEmailApplicant(caseDetails);
        }

        if (notificationService.shouldEmailRespondentSolicitor(caseDetails.getData())) {
            log.info("Sending email notification to Respondent Solicitor for 'Prepare for Hearing'");
            notificationService.sendPrepareForHearingEmailRespondent(caseDetails);
        }

        if (caseDataService.isContestedPaperApplication(caseDetails)) {
            if (hearingDocumentService.alreadyHadFirstHearing(callbackRequest.getCaseDetailsBefore())) {
                log.info("Sending Additional Hearing Document to bulk print for Contested Paper Case ID: {}", caseDetails.getId());
                additionalHearingDocumentService.sendAdditionalHearingDocuments(userAuthorisation, caseDetails);
            } else {
                log.info("Sending Forms A, C, G & Out of family court resolution to bulk print for"
                    + " Contested Paper Case ID: {}", caseDetails.getId());
                hearingDocumentService.sendFormCAndGForBulkPrint(caseDetails, userAuthorisation);
            }
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(callbackRequest.getCaseDetails().getData()).build();
    }
}
