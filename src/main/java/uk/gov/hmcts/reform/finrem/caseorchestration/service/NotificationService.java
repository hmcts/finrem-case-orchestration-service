package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.NotificationServiceConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.NotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_EMAIL_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_REFER_TO_JUDGE_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_EMAIL_RECIPIENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.getCourtDetailsString;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isNotEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isPaperApplication;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isRespondentRepresentedByASolicitor;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationServiceConfiguration notificationServiceConfiguration;
    private final RestTemplate restTemplate;
    private final FeatureToggleService featureToggleService;
    private final ObjectMapper objectMapper;
    private final NotificationRequestMapper notificationRequestMapper;
    private NotificationRequest notificationRequest;

    private String recipientEmail = "fr_applicant_sol@sharklasers.com";

    public void sendConsentedHWFSuccessfulConfirmationEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getHwfSuccessful());
        sendNotificationEmail(notificationRequestMapper.createNotificationRequestForAppSolicitor(callbackRequest), uri);
    }

    public void sendAssignToJudgeConfirmationEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getAssignToJudge());
        sendNotificationEmail(notificationRequestMapper.createNotificationRequestForAppSolicitor(callbackRequest), uri);
    }

    public void sendConsentOrderMadeConfirmationEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentOrderMade());
        sendNotificationEmail(notificationRequestMapper.createNotificationRequestForAppSolicitor(callbackRequest), uri);
    }

    public void sendConsentOrderNotApprovedEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentOrderNotApproved());
        sendNotificationEmail(notificationRequestMapper.createNotificationRequestForAppSolicitor(callbackRequest), uri);
    }

    public void sendConsentOrderAvailableEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentOrderAvailable());
        sendNotificationEmail(notificationRequestMapper.createNotificationRequestForAppSolicitor(callbackRequest), uri);
    }

    public void sendConsentOrderAvailableCtscEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentOrderAvailableCtsc());
        NotificationRequest ctscNotificationRequest = notificationRequestMapper.createNotificationRequestForAppSolicitor(callbackRequest);
        ctscNotificationRequest.setNotificationEmail(notificationServiceConfiguration.getCtscEmail());
        sendNotificationEmail(ctscNotificationRequest, uri);
    }

    public void sendContestedHwfSuccessfulConfirmationEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedHwfSuccessful());
        sendNotificationEmail(notificationRequestMapper.createNotificationRequestForAppSolicitor(callbackRequest), uri);
    }

    public void sendContestedApplicationIssuedEmailToApplicantSolicitor(CallbackRequest callbackRequest) {
        notificationRequest = notificationRequestMapper.createNotificationRequestForAppSolicitor(callbackRequest);
        sendContestedApplicationIssuedEmail(notificationRequest);
    }

    public void sendContestedApplicationIssuedEmailToRespondentSolicitor(CallbackRequest callbackRequest) {
        notificationRequest = notificationRequestMapper.createNotificationRequestForRespSolicitor(callbackRequest);
        sendContestedApplicationIssuedEmail(notificationRequest);
    }

    private void sendContestedApplicationIssuedEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedApplicationIssued());
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendContestOrderApprovedEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestOrderApproved());
        sendNotificationEmail(notificationRequestMapper.createNotificationRequestForAppSolicitor(callbackRequest), uri);
    }

    public void sendPrepareForHearingEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getPrepareForHearing());
        sendNotificationEmail(notificationRequestMapper.createNotificationRequestForAppSolicitor(callbackRequest), uri);
    }

    public void sendPrepareForHearingOrderSentEmailApplicant(CallbackRequest callbackRequest) {
        sendPrepareForHearingOrderSentEmail(notificationRequestMapper.createNotificationRequestForAppSolicitor(callbackRequest));
    }

    public void sendPrepareForHearingOrderSentEmailRespondent(CallbackRequest callbackRequest) {
        sendPrepareForHearingOrderSentEmail(notificationRequestMapper.createNotificationRequestForRespSolicitor(callbackRequest));

    }

    private void sendPrepareForHearingOrderSentEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getPrepareForHearingOrderSent());
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendSolicitorToDraftOrderEmailApplicant(CallbackRequest callbackRequest) {
        sendSolicitorToDraftOrderEmail(notificationRequestMapper.createNotificationRequestForAppSolicitor(callbackRequest));
    }

    public void sendSolicitorToDraftOrderEmailRespondent(CallbackRequest callbackRequest) {
        sendSolicitorToDraftOrderEmail(notificationRequestMapper.createNotificationRequestForRespSolicitor(callbackRequest));
    }

    private void sendSolicitorToDraftOrderEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedDraftOrder());
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendConsentGeneralEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentGeneralEmail());
        NotificationRequest notificationRequest = notificationRequestMapper.createNotificationRequestForAppSolicitor(callbackRequest);
        notificationRequest.setNotificationEmail(Objects.toString(callbackRequest.getCaseDetails().getData().get(GENERAL_EMAIL_RECIPIENT)));
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendContestedGeneralEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedGeneralEmail());
        NotificationRequest notificationRequest = notificationRequestMapper.createNotificationRequestForAppSolicitor(callbackRequest);
        notificationRequest.setNotificationEmail(Objects.toString(callbackRequest.getCaseDetails().getData().get(GENERAL_EMAIL_RECIPIENT)));
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendContestOrderNotApprovedEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedOrderNotApproved());
        sendNotificationEmail(notificationRequestMapper.createNotificationRequestForAppSolicitor(callbackRequest), uri);
    }

    public void sendContestedConsentOrderApprovedEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedConsentOrderApproved());
        sendNotificationEmail(notificationRequestMapper.createNotificationRequestForAppSolicitor(callbackRequest), uri);
    }

    public void sendContestedConsentOrderNotApprovedEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedConsentOrderNotApproved());
        sendNotificationEmail(notificationRequestMapper.createNotificationRequestForAppSolicitor(callbackRequest), uri);
    }

    public void sendContestedConsentGeneralOrderEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedConsentGeneralOrder());
        sendNotificationEmail(notificationRequestMapper.createNotificationRequestForAppSolicitor(callbackRequest), uri);
    }

    public void sendConsentedGeneralOrderEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentedGeneralOrder());
        sendNotificationEmail(notificationRequestMapper.createNotificationRequestForAppSolicitor(callbackRequest), uri);
    }

    public void sendContestedGeneralOrderEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedGeneralOrder());
        sendNotificationEmail(notificationRequestMapper.createNotificationRequestForAppSolicitor(callbackRequest), uri);
    }

    public void sendContestedGeneralApplicationReferToJudgeEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedGeneralApplicationReferToJudge());
        NotificationRequest judgeNotificationRequest = notificationRequestMapper.createNotificationRequestForAppSolicitor(callbackRequest);
        judgeNotificationRequest.setNotificationEmail(Objects.toString(
            callbackRequest.getCaseDetails().getData().get(GENERAL_APPLICATION_REFER_TO_JUDGE_EMAIL)));
        sendNotificationEmail(judgeNotificationRequest, uri);
    }

    public void sendContestedGeneralApplicationOutcomeEmail(CallbackRequest callbackRequest) throws IOException {
        if (featureToggleService.isSendToFRCEnabled()) {
            Map<String, Object> data = callbackRequest.getCaseDetails().getData();
            Map<String, Object> courtDetailsMap = objectMapper.readValue(getCourtDetailsString(), HashMap.class);
            Map<String, Object> courtDetails = (Map<String, Object>) courtDetailsMap.get(data.get(CaseHearingFunctions.getSelectedCourt(data)));

            recipientEmail = (String) courtDetails.get(COURT_DETAILS_EMAIL_KEY);
        }

        NotificationRequest notificationRequest = notificationRequestMapper.createNotificationRequestForAppSolicitor(callbackRequest);
        notificationRequest.setNotificationEmail(recipientEmail);

        URI uri = buildUri(notificationServiceConfiguration.getContestedGeneralApplicationOutcome());
        sendNotificationEmail(notificationRequest, uri);
    }

    private void sendNotificationEmail(NotificationRequest notificationRequest, URI uri) {
        HttpEntity<NotificationRequest> request = new HttpEntity<>(notificationRequest, buildHeaders());
        try {
            restTemplate.exchange(uri, HttpMethod.POST, request, String.class);
        } catch (Exception ex) {
            log.error(String.format("Failed to send email for case ID: %s for solicitor email: %s due to exception: %s",
                notificationRequest.getCaseReferenceNumber(),
                notificationRequest.getNotificationEmail(),
                ex.getMessage()));
        }
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

    public boolean shouldEmailRespondentSolicitor(Map<String, Object> caseData) {
        return !isPaperApplication(caseData)
            && isRespondentRepresentedByASolicitor(caseData)
            && isNotEmpty(RESP_SOLICITOR_EMAIL, caseData);
    }
}
