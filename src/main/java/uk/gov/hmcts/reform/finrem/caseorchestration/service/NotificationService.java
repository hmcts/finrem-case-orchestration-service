package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.NotificationServiceConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;

import java.net.URI;

import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private static final String NOTIFICATION_SERVICE_URL = "notification service url ";
    private static final String MESSAGE = "Failed to send notification email for case id : ";
    private static final String MSG_SOLICITOR_EMAIL = " for solicitor email";
    private static final String EXCEPTION = "exception :";
    private final NotificationServiceConfiguration notificationServiceConfiguration;
    private final RestTemplate restTemplate;

    public void sendHWFSuccessfulConfirmationEmail(CCDRequest ccdRequest, String authToken) {
        NotificationRequest notificationRequest = buildNotificationRequest(ccdRequest);
        HttpEntity<NotificationRequest> request = new HttpEntity<>(notificationRequest, buildHeaders(authToken));
        URI uri = buildUri(notificationServiceConfiguration.getHwfSuccessful());
        log.info(NOTIFICATION_SERVICE_URL, uri.toString());
        try {
            restTemplate.exchange(uri, HttpMethod.POST, request, String.class);
        } catch (Exception ex) {
            log.error(MESSAGE,
                    notificationRequest.getCaseReferenceNumber(), MSG_SOLICITOR_EMAIL,
                    notificationRequest.getNotificationEmail(),
                    EXCEPTION, ex.getMessage());
        }
    }

    public void sendAssignToJudgeConfirmationEmail(CCDRequest ccdRequest, String authToken) {
        URI uri = buildUri(notificationServiceConfiguration.getAssignToJudge());
        sendNotificationEmail(ccdRequest, authToken, uri);
    }

    public void sendConsentOrderMadeConfirmationEmail(CCDRequest ccdRequest, String authToken) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentOrderMade());
        sendNotificationEmail(ccdRequest, authToken, uri);
    }

    public void sendConsentOrderNotApprovedEmail(CCDRequest ccdRequest, String authToken) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentOrderNotApproved());
        sendNotificationEmail(ccdRequest, authToken, uri);
    }

    public void sendConsentOrderAvailableEmail(CCDRequest ccdRequest, String authToken) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentOrderAvailable());
        sendNotificationEmail(ccdRequest, authToken, uri);
    }

    private void sendNotificationEmail(CCDRequest ccdRequest, String authToken, URI uri) {
        NotificationRequest notificationRequest = buildNotificationRequest(ccdRequest);
        HttpEntity<NotificationRequest> request = new HttpEntity<>(notificationRequest, buildHeaders(authToken));
        log.info(NOTIFICATION_SERVICE_URL, uri.toString());
        try {
            restTemplate.exchange(uri, HttpMethod.POST, request, String.class);
        } catch (Exception ex) {
            log.error(MESSAGE,
                    notificationRequest.getCaseReferenceNumber(), MSG_SOLICITOR_EMAIL,
                    notificationRequest.getNotificationEmail(),
                    EXCEPTION, ex.getMessage());
        }
    }

    private NotificationRequest buildNotificationRequest(CCDRequest ccdRequest) {
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setCaseReferenceNumber(ccdRequest.getCaseDetails().getCaseId());
        notificationRequest.setSolicitorReferenceNumber(
                ccdRequest.getCaseDetails().getCaseData().getSolicitorReference());
        notificationRequest.setName(ccdRequest.getCaseDetails().getCaseData().getSolicitorName());
        notificationRequest.setNotificationEmail(ccdRequest.getCaseDetails().getCaseData().getSolicitorEmail());
        return notificationRequest;
    }

    private URI buildUri(String endPoint) {
        return fromHttpUrl(notificationServiceConfiguration.getUrl()
                + notificationServiceConfiguration.getApi()
                + endPoint)
                .build()
                .toUri();
    }

    private HttpHeaders buildHeaders(String authToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", authToken);
        headers.add("Content-Type", "application/json");
        return headers;
    }
}
