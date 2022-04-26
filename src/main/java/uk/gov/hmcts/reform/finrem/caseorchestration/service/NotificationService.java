package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.NotificationServiceConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.NotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_EMAIL_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_REFER_TO_JUDGE_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_EMAIL_RECIPIENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TRANSFER_COURTS_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TRANSFER_COURTS_INSTRUCTIONS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.getCourtDetailsString;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationServiceConfiguration notificationServiceConfiguration;
    private final RestTemplate restTemplate;
    private final FeatureToggleService featureToggleService;
    private final ObjectMapper objectMapper;
    private final NotificationRequestMapper notificationRequestMapper;
    private final CaseDataService caseDataService;

    private String recipientEmail = "fr_applicant_sol@sharklasers.com";

    public void sendConsentedHWFSuccessfulConfirmationEmail(CaseDetails caseDetails) {
        URI uri = buildUri(notificationServiceConfiguration.getHwfSuccessful());
        sendNotificationEmail(notificationRequestMapper.createNotificationRequestForAppSolicitor(caseDetails), uri);
    }

    public void sendAssignToJudgeConfirmationEmailToApplicantSolicitor(CaseDetails caseDetails) {
        sendAssignToJudgeConfirmationEmail(notificationRequestMapper.createNotificationRequestForAppSolicitor(caseDetails));
    }

    public void sendAssignToJudgeConfirmationEmailToRespondentSolicitor(CaseDetails caseDetails) {
        sendAssignToJudgeConfirmationEmail(notificationRequestMapper.createNotificationRequestForRespSolicitor(caseDetails));
    }

    private void sendAssignToJudgeConfirmationEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getAssignToJudge());
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendConsentOrderMadeConfirmationEmailToApplicantSolicitor(CaseDetails caseDetails) {
        sendConsentOrderMadeConfirmationEmail(notificationRequestMapper.createNotificationRequestForAppSolicitor(caseDetails));
    }

    public void sendConsentOrderMadeConfirmationEmailToRespondentSolicitor(CaseDetails caseDetails) {
        sendConsentOrderMadeConfirmationEmail(notificationRequestMapper.createNotificationRequestForRespSolicitor(caseDetails));
    }

    public void sendConsentOrderMadeConfirmationEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentOrderMade());
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendConsentOrderNotApprovedEmailToApplicantSolicitor(CaseDetails caseDetails) {
        sendConsentOrderNotApprovedEmail(notificationRequestMapper.createNotificationRequestForAppSolicitor(caseDetails));
    }

    public void sendConsentOrderNotApprovedEmailToRespondentSolicitor(CaseDetails caseDetails) {
        sendConsentOrderNotApprovedEmail(notificationRequestMapper.createNotificationRequestForRespSolicitor(caseDetails));
    }

    private void sendConsentOrderNotApprovedEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentOrderNotApproved());
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendConsentOrderAvailableEmailToApplicantSolicitor(CaseDetails caseDetails) {
        sendConsentOrderAvailableEmail(notificationRequestMapper.createNotificationRequestForAppSolicitor(caseDetails));
    }

    public void sendConsentOrderAvailableEmailToRespondentSolicitor(CaseDetails caseDetails) {
        sendConsentOrderAvailableEmail(notificationRequestMapper.createNotificationRequestForRespSolicitor(caseDetails));
    }

    private void sendConsentOrderAvailableEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentOrderAvailable());
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendConsentOrderAvailableCtscEmail(CaseDetails caseDetails) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentOrderAvailableCtsc());
        NotificationRequest ctscNotificationRequest = notificationRequestMapper.createNotificationRequestForAppSolicitor(caseDetails);
        ctscNotificationRequest.setNotificationEmail(notificationServiceConfiguration.getCtscEmail());
        sendNotificationEmail(ctscNotificationRequest, uri);
    }

    public void sendContestedHwfSuccessfulConfirmationEmail(CaseDetails caseDetails) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedHwfSuccessful());
        sendNotificationEmail(notificationRequestMapper.createNotificationRequestForAppSolicitor(caseDetails), uri);
    }

    public void sendContestedApplicationIssuedEmailToApplicantSolicitor(CaseDetails caseDetails) {
        sendContestedApplicationIssuedEmail(notificationRequestMapper.createNotificationRequestForAppSolicitor(caseDetails));
    }

    public void sendContestedApplicationIssuedEmailToRespondentSolicitor(CaseDetails caseDetails) {
        sendContestedApplicationIssuedEmail(notificationRequestMapper.createNotificationRequestForRespSolicitor(caseDetails));
    }

    private void sendContestedApplicationIssuedEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedApplicationIssued());
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendContestOrderApprovedEmailApplicant(CaseDetails caseDetails) {
        sendContestOrderApprovedEmail(notificationRequestMapper.createNotificationRequestForAppSolicitor(caseDetails));
    }

    public void sendContestOrderApprovedEmailRespondent(CaseDetails caseDetails) {
        sendContestOrderApprovedEmail(notificationRequestMapper.createNotificationRequestForRespSolicitor(caseDetails));
    }

    public void sendContestOrderApprovedEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestOrderApproved());
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendPrepareForHearingEmailApplicant(CaseDetails caseDetails) {
        sendPrepareForHearingEmail(notificationRequestMapper.createNotificationRequestForAppSolicitor(caseDetails));
    }

    public void sendPrepareForHearingEmailRespondent(CaseDetails caseDetails) {
        sendPrepareForHearingEmail(notificationRequestMapper.createNotificationRequestForRespSolicitor(caseDetails));
    }

    private void sendPrepareForHearingEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getPrepareForHearing());
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendPrepareForHearingOrderSentEmailApplicant(CaseDetails caseDetails) {
        sendPrepareForHearingOrderSentEmail(notificationRequestMapper.createNotificationRequestForAppSolicitor(caseDetails));
    }

    public void sendPrepareForHearingOrderSentEmailRespondent(CaseDetails caseDetails) {
        sendPrepareForHearingOrderSentEmail(notificationRequestMapper.createNotificationRequestForRespSolicitor(caseDetails));
    }

    private void sendPrepareForHearingOrderSentEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getPrepareForHearingOrderSent());
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendSolicitorToDraftOrderEmailApplicant(CaseDetails caseDetails) {
        sendSolicitorToDraftOrderEmail(notificationRequestMapper.createNotificationRequestForAppSolicitor(caseDetails));
    }

    public void sendSolicitorToDraftOrderEmailRespondent(CaseDetails caseDetails) {
        sendSolicitorToDraftOrderEmail(notificationRequestMapper.createNotificationRequestForRespSolicitor(caseDetails));
    }

    private void sendSolicitorToDraftOrderEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedDraftOrder());
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendConsentGeneralEmail(CaseDetails caseDetails) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentGeneralEmail());
        NotificationRequest notificationRequest = notificationRequestMapper.createNotificationRequestForAppSolicitor(caseDetails);
        notificationRequest.setNotificationEmail(Objects.toString(caseDetails.getData().get(GENERAL_EMAIL_RECIPIENT)));
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendContestedGeneralEmail(CaseDetails caseDetails) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedGeneralEmail());
        NotificationRequest notificationRequest = notificationRequestMapper.createNotificationRequestForAppSolicitor(caseDetails);
        notificationRequest.setNotificationEmail(Objects.toString(caseDetails.getData().get(GENERAL_EMAIL_RECIPIENT)));
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendContestOrderNotApprovedEmailApplicant(CaseDetails caseDetails) {
        sendContestOrderNotApprovedEmail(notificationRequestMapper.createNotificationRequestForAppSolicitor(caseDetails));
    }

    public void sendContestOrderNotApprovedEmailRespondent(CaseDetails caseDetails) {
        sendContestOrderNotApprovedEmail(notificationRequestMapper.createNotificationRequestForRespSolicitor(caseDetails));
    }

    private void sendContestOrderNotApprovedEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedOrderNotApproved());
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendContestedConsentOrderApprovedEmailToApplicantSolicitor(CaseDetails caseDetails) {
        sendContestedConsentOrderApprovedEmailToSolicitor(notificationRequestMapper.createNotificationRequestForAppSolicitor(caseDetails));
    }

    public void sendContestedConsentOrderApprovedEmailToRespondentSolicitor(CaseDetails caseDetails) {
        sendContestedConsentOrderApprovedEmailToSolicitor(notificationRequestMapper.createNotificationRequestForRespSolicitor(caseDetails));
    }

    private void sendContestedConsentOrderApprovedEmailToSolicitor(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedConsentOrderApproved());
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendContestedConsentOrderNotApprovedEmailApplicantSolicitor(CaseDetails caseDetails) {
        sendContestedConsentOrderNotApprovedEmail(notificationRequestMapper.createNotificationRequestForAppSolicitor(caseDetails));
    }

    public void sendContestedConsentOrderNotApprovedEmailRespondentSolicitor(CaseDetails caseDetails) {
        sendContestedConsentOrderNotApprovedEmail(notificationRequestMapper.createNotificationRequestForRespSolicitor(caseDetails));
    }

    private void sendContestedConsentOrderNotApprovedEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedConsentOrderNotApproved());
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendContestedConsentGeneralOrderEmailApplicantSolicitor(CaseDetails caseDetails) {
        sendContestedConsentGeneralOrderEmail(notificationRequestMapper.createNotificationRequestForAppSolicitor(caseDetails));
    }

    public void sendContestedConsentGeneralOrderEmailRespondentSolicitor(CaseDetails caseDetails) {
        sendContestedConsentGeneralOrderEmail(notificationRequestMapper.createNotificationRequestForRespSolicitor(caseDetails));
    }

    private void sendContestedConsentGeneralOrderEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedConsentGeneralOrder());
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendConsentedGeneralOrderEmailToApplicantSolicitor(CaseDetails caseDetails) {
        sendConsentedGeneralOrderEmail(notificationRequestMapper.createNotificationRequestForAppSolicitor(caseDetails));
    }

    public void sendConsentedGeneralOrderEmailToRespondentSolicitor(CaseDetails caseDetails) {
        sendConsentedGeneralOrderEmail(notificationRequestMapper.createNotificationRequestForRespSolicitor(caseDetails));
    }

    private void sendConsentedGeneralOrderEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentedGeneralOrder());
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendContestedGeneralOrderEmailApplicant(CaseDetails caseDetails) {
        sendContestedGeneralOrderEmail(notificationRequestMapper.createNotificationRequestForAppSolicitor(caseDetails));
    }

    public void sendContestedGeneralOrderEmailRespondent(CaseDetails caseDetails) {
        sendContestedGeneralOrderEmail(notificationRequestMapper.createNotificationRequestForRespSolicitor(caseDetails));
    }

    private void sendContestedGeneralOrderEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedGeneralOrder());
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendContestedGeneralApplicationReferToJudgeEmail(CaseDetails caseDetails) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedGeneralApplicationReferToJudge());
        NotificationRequest judgeNotificationRequest = notificationRequestMapper.createNotificationRequestForAppSolicitor(caseDetails);
        judgeNotificationRequest.setNotificationEmail(Objects.toString(caseDetails.getData().get(GENERAL_APPLICATION_REFER_TO_JUDGE_EMAIL)));
        sendNotificationEmail(judgeNotificationRequest, uri);
    }

    public void sendContestedGeneralApplicationOutcomeEmail(CaseDetails caseDetails) throws IOException {
        if (featureToggleService.isSendToFRCEnabled()) {
            Map<String, Object> data = caseDetails.getData();
            Map<String, Object> courtDetailsMap = objectMapper.readValue(getCourtDetailsString(), HashMap.class);
            Map<String, Object> courtDetails = (Map<String, Object>) courtDetailsMap.get(data.get(CaseHearingFunctions.getSelectedCourt(data)));

            recipientEmail = (String) courtDetails.get(COURT_DETAILS_EMAIL_KEY);
        }

        NotificationRequest notificationRequest = notificationRequestMapper.createNotificationRequestForAppSolicitor(caseDetails);
        notificationRequest.setNotificationEmail(recipientEmail);
        URI uri = buildUri(notificationServiceConfiguration.getContestedGeneralApplicationOutcome());
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendConsentOrderNotApprovedSentEmailToApplicantSolicitor(CaseDetails caseDetails) {
        sendConsentOrderNotApprovedSentEmail(notificationRequestMapper.createNotificationRequestForAppSolicitor(caseDetails));
    }

    public void sendConsentOrderNotApprovedSentEmailToRespondentSolicitor(CaseDetails caseDetails) {
        sendConsentOrderNotApprovedSentEmail(notificationRequestMapper.createNotificationRequestForRespSolicitor(caseDetails));
    }

    public void sendInterimNotificationEmailToApplicantSolicitor(CaseDetails caseDetails) {
        sendInterimNotificationEmail(notificationRequestMapper.createNotificationRequestForAppSolicitor(caseDetails));
    }

    public void sendInterimNotificationEmailToRespondentSolicitor(CaseDetails caseDetails) {
        sendInterimNotificationEmail(notificationRequestMapper.createNotificationRequestForRespSolicitor(caseDetails));
    }

    private void sendInterimNotificationEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getPrepareForInterimHearing());
        sendNotificationEmail(notificationRequest, uri);
    }

    private void sendConsentOrderNotApprovedSentEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentOrderNotApprovedSent());
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendTransferToLocalCourtEmail(CaseDetails caseDetails) {
        URI uri = buildUri(notificationServiceConfiguration.getTransferToLocalCourt());
        NotificationRequest notificationRequest = notificationRequestMapper.createNotificationRequestForAppSolicitor(caseDetails);
        //Overwrite the email, set to the court provided, and use general body to include the Events "Free Text" field
        notificationRequest.setNotificationEmail(Objects.toString(caseDetails.getData().get(TRANSFER_COURTS_EMAIL)));
        notificationRequest.setGeneralEmailBody("The Judge has also ordered that:\n"
            + Objects.toString(caseDetails.getData().get(TRANSFER_COURTS_INSTRUCTIONS)));

        sendNotificationEmail(notificationRequest, uri);
    }

    private void sendNotificationEmail(NotificationRequest notificationRequest, URI uri) {
        HttpEntity<NotificationRequest> request = new HttpEntity<>(notificationRequest, buildHeaders());
        try {
            restTemplate.exchange(uri, HttpMethod.POST, request, String.class);
        } catch (Exception ex) {
            log.error(String.format("Failed to send email for case ID: %s for email: %s due to exception: %s",
                notificationRequest.getCaseReferenceNumber(),
                notificationRequest.getNotificationEmail(),
                ex.getMessage()));
        }
    }

    public boolean shouldEmailRespondentSolicitor(Map<String, Object> caseData) {
        return !caseDataService.isPaperApplication(caseData)
            && caseDataService.isRespondentRepresentedByASolicitor(caseData)
            && caseDataService.isNotEmpty(RESP_SOLICITOR_EMAIL, caseData)
            && !NO_VALUE.equalsIgnoreCase(nullToEmpty(caseData.get(RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT)));
    }

    public boolean shouldEmailApplicantSolicitor(CaseDetails caseDetails) {
        return caseDataService.isApplicantSolicitorAgreeToReceiveEmails(caseDetails);
    }

    private URI buildUri(String endPoint) {
        return fromHttpUrl(notificationServiceConfiguration.getUrl()
            + notificationServiceConfiguration.getApi()
            + endPoint)
            .build()
            .toUri();
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        return headers;
    }

    public void sendConsentNoticeOfChangeEmail(CaseDetails caseDetails) {
    }

    public void sendContestedNoticeOfChangeEmail(CaseDetails caseDetails) {
    }
}
