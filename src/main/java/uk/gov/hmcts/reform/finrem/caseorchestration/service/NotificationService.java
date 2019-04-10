package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ObjectUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.NotificationServiceConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;

import java.net.URI;
import java.util.Map;

import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;

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

    public void sendHWFSuccessfulConfirmationEmail(CallbackRequest callbackRequest , String authToken) {
        NotificationRequest notificationRequest = buildNotificationRequest(callbackRequest);
        HttpEntity<NotificationRequest> request = new HttpEntity<>(notificationRequest, buildHeaders(authToken));
        URI uri = buildUri(notificationServiceConfiguration.getHwfSuccessful());
        sendNotificationEmail(callbackRequest, authToken, uri);
    }

    public void sendAssignToJudgeConfirmationEmail(CallbackRequest callbackRequest, String authToken) {
        URI uri = buildUri(notificationServiceConfiguration.getAssignToJudge());
        sendNotificationEmail(callbackRequest, authToken, uri);
    }

    public void sendConsentOrderMadeConfirmationEmail(CallbackRequest callbackRequest, String authToken) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentOrderMade());
        sendNotificationEmail(callbackRequest, authToken, uri);
    }

    public void sendConsentOrderNotApprovedEmail(CallbackRequest callbackRequest, String authToken) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentOrderNotApproved());
        sendNotificationEmail(callbackRequest, authToken, uri);
    }

    public void sendConsentOrderAvailableEmail(CallbackRequest callbackRequest, String authToken) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentOrderAvailable());
        sendNotificationEmail(callbackRequest, authToken, uri);
    }

    private void sendNotificationEmail(CallbackRequest callbackRequest, String authToken, URI uri) {
        NotificationRequest notificationRequest = buildNotificationRequest(callbackRequest);
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

    private NotificationRequest buildNotificationRequest(CallbackRequest callbackRequest) {
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setCaseReferenceNumber(ObjectUtils.toString(callbackRequest.getCaseDetails().getId()));
        Map<String,Object> mapOfCaseData = callbackRequest.getCaseDetails().getData();
        notificationRequest.setSolicitorReferenceNumber(ObjectUtils.toString(mapOfCaseData.get(SOLICITOR_REFERENCE)));
        notificationRequest.setName(ObjectUtils.toString(mapOfCaseData.get(SOLICITOR_NAME)));
        notificationRequest.setNotificationEmail(ObjectUtils.toString(mapOfCaseData.get(SOLICITOR_EMAIL)));
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
