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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InterimHearingService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterimHearingContestedSubmittedHandler implements CallbackHandler {

    private final CaseDataService caseDataService;
    private final NotificationService notificationService;
    private final InterimHearingService interimHearingService;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.INTERIM_HEARING.equals(eventType);
    }

    @Override
    public AboutToStartOrSubmitCallbackResponse handle(CallbackRequest callbackRequest,
                                                       String userAuthorisation) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();
        List<InterimHearingData> caseDataList = interimHearingService.filterInterimHearingToProcess(caseData);
        List<Map<String, Object>> interimCaseData = interimHearingService.convertInterimHearingCollectionDataToMap(caseDataList);

        if (!caseDataService.isPaperApplication(caseData)) {
            interimCaseData.forEach(interimHearingData -> sendNotification(caseDetails, interimHearingData));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(callbackRequest.getCaseDetails().getData()).build();
    }

    private void sendNotification(CaseDetails caseDetails, Map<String, Object> interimHearingData) {
        if (caseDataService.isApplicantSolicitorAgreeToReceiveEmails(caseDetails)) {
            log.info("Sending email notification to Applicant Solicitor about interim hearing");
            notificationService.sendInterimHearingNotificationEmailToApplicantSolicitor(caseDetails, interimHearingData);
        }
        if (notificationService.shouldEmailRespondentSolicitor(caseDetails.getData())) {
            log.info("Sending email notification to Respondent Solicitor about interim hearing");
            notificationService.sendInterimHearingNotificationEmailToRespondentSolicitor(caseDetails, interimHearingData);
        }
    }

}
