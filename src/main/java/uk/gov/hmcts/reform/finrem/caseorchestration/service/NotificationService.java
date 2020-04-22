package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.NotificationServiceConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.D81_QUESTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private static final String MESSAGE = "Failed to send notification email for case id : ";
    private static final String MSG_SOLICITOR_EMAIL = " for solicitor email";
    private static final String EXCEPTION = "exception :";
    private static final String ALLOCATED_COURT_LIST = "allocatedCourtList";
    private static final String KENT = "kent";
    private static final String REGION = "region";
    private static final String NEWPORT = "newport";
    private static final String SWANSEA = "swansea";
    private static final String CLEAVELAND = "cleaveland";
    private static final String NWYORKSHIRE = "nwyorkshire";
    private static final String HSYORKSHIRE = "hsyorkshire";
    private static final String NOTTINGHAM = "nottingham";
    private static final String BIRMINGHAM = "birmingham";
    private static final String CONSENTED = "consented";
    private static final String CONTESTED = "contested";
    private final NotificationServiceConfiguration notificationServiceConfiguration;
    private final RestTemplate restTemplate;
    private NotificationRequest notificationRequest;

    public void sendHWFSuccessfulConfirmationEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getHwfSuccessful());
        sendNotificationEmail(callbackRequest, uri);
    }

    public void sendAssignToJudgeConfirmationEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getAssignToJudge());
        sendNotificationEmail(callbackRequest, uri);
    }

    public void sendConsentOrderMadeConfirmationEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentOrderMade());
        sendNotificationEmail(callbackRequest, uri);
    }

    public void sendConsentOrderNotApprovedEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentOrderNotApproved());
        sendNotificationEmail(callbackRequest, uri);
    }

    public void sendConsentOrderAvailableEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentOrderAvailable());
        sendNotificationEmail(callbackRequest, uri);
    }

    public void sendContestedHwfSuccessfulConfirmationEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentedHwfSuccessful());
        sendNotificationEmail(callbackRequest, uri);
    }

    public void sendPrepareForHearingEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getPrepareForHearing());
        sendNotificationEmail(callbackRequest, uri);
    }

    private void sendNotificationEmail(CallbackRequest callbackRequest, URI uri) {
        try {
            if (isConsentedApplication(callbackRequest.getCaseDetails().getData())) {
                notificationRequest = buildNotificationRequest(callbackRequest, SOLICITOR_REFERENCE,
                        SOLICITOR_NAME, SOLICITOR_EMAIL, CONSENTED);
            } else {
                notificationRequest = buildNotificationRequest(callbackRequest, SOLICITOR_REFERENCE,
                        CONTESTED_SOLICITOR_NAME, CONTESTED_SOLICITOR_EMAIL, CONTESTED);
            }
        } catch (IOException ex) {
            log.error("Unable to create notification request for Case Id : {}, error message : {}",
                    callbackRequest.getCaseDetails().getId(), ex.getMessage());
        }

        HttpEntity<NotificationRequest> request = new HttpEntity<>(notificationRequest, buildHeaders());
        try {
            restTemplate.exchange(uri, HttpMethod.POST, request, String.class);
        } catch (Exception ex) {
            log.error(MESSAGE,
                    notificationRequest.getCaseReferenceNumber(), MSG_SOLICITOR_EMAIL,
                    notificationRequest.getNotificationEmail(),
                    EXCEPTION, ex.getMessage());
        }
    }

    private NotificationRequest buildNotificationRequest(CallbackRequest callbackRequest,
                                                         String solicitorReference,
                                                         String solicitorName,
                                                         String solicitorEmail, String caseType) throws IOException {
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setCaseReferenceNumber(Objects.toString(callbackRequest.getCaseDetails().getId()));
        Map<String, Object> mapOfCaseData = callbackRequest.getCaseDetails().getData();
        notificationRequest.setSolicitorReferenceNumber(Objects.toString(mapOfCaseData.get(solicitorReference)));
        notificationRequest.setName(Objects.toString(mapOfCaseData.get(solicitorName)));
        notificationRequest.setNotificationEmail(Objects.toString(mapOfCaseData.get(solicitorEmail)));
        Object allocatedCourtList = mapOfCaseData.get(ALLOCATED_COURT_LIST);
        if (CONTESTED.equalsIgnoreCase(caseType)) {
            String selectedCourt = getSelectedCourt(allocatedCourtList);
            log.info("selectedCourt is  {} for contested case Id : {}", selectedCourt,
                    notificationRequest.getCaseReferenceNumber());
            notificationRequest.setSelectedCourt(selectedCourt);
        }
        return notificationRequest;
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


    private boolean isConsentedApplication(Map<String, Object> caseData) {
        return isNotEmpty((String) caseData.get(D81_QUESTION));
    }

    private String getSelectedCourt(Object allocatedCourtList) throws IOException {
        HashMap<String, Object> allocatedCourtMap = (HashMap<String, Object>) allocatedCourtList;
        String region = (String) allocatedCourtMap.get(REGION);
        if ("midlands".equalsIgnoreCase(region)) {
            return getMidlandFRC(allocatedCourtMap);
        }
        if ("london".equalsIgnoreCase(region)) {
            return getLondonFRC(allocatedCourtMap);
        }
        if ("northwest".equalsIgnoreCase(region)) {
            return getNorthWestFRC(allocatedCourtMap);
        }

        if ("northeast".equalsIgnoreCase(region)) {
            return getNorthEastFRC(allocatedCourtMap);
        }

        if ("southeast".equalsIgnoreCase(region)) {
            return getSouthEastFRC(allocatedCourtMap);
        } else if ("wales".equalsIgnoreCase(region)) {
            return getWalesFRC(allocatedCourtMap);
        }
        return EMPTY;
    }

    private String getWalesFRC(Map allocatedCourtMap) {
        String walesList = (String) allocatedCourtMap.get("walesList");
        if (NEWPORT.equalsIgnoreCase(walesList)) {
            return NEWPORT;
        } else if (SWANSEA.equalsIgnoreCase(walesList)) {
            return SWANSEA;
        }
        return EMPTY;
    }

    private String getSouthEastFRC(Map allocatedCourtMap) {
        String southEastList = (String) allocatedCourtMap.get("southEastList");
        if (KENT.equalsIgnoreCase(southEastList)) {
            return KENT;
        }
        return EMPTY;
    }

    private String getNorthEastFRC(Map allocatedCourtMap) {
        String northEastList = (String) allocatedCourtMap.get("northEastList");
        if (CLEAVELAND.equalsIgnoreCase(northEastList)) {
            return CLEAVELAND;
        } else if (NWYORKSHIRE.equalsIgnoreCase(northEastList)) {
            return NWYORKSHIRE;
        } else if (HSYORKSHIRE.equalsIgnoreCase(northEastList)) {
            return HSYORKSHIRE;
        }
        return EMPTY;
    }

    private String getNorthWestFRC(Map allocatedCourtMap) {
        String northWestList = (String) allocatedCourtMap.get("northWestList");
        if ("liverpool".equalsIgnoreCase(northWestList)) {
            return "liverpool";
        } else if ("manchester".equalsIgnoreCase(northWestList)) {
            return "manchester";
        }
        return EMPTY;
    }


    private String getLondonFRC(Map allocatedCourtMap) {
        String londonList = (String) allocatedCourtMap.get("londonList");
        if ("cfc".equalsIgnoreCase(londonList)) {
            return "cfc";
        }
        return EMPTY;
    }


    private String getMidlandFRC(Map allocatedCourtMap) {
        String midlandsList = (String) allocatedCourtMap.get("midlandsList");
        if (NOTTINGHAM.equalsIgnoreCase(midlandsList)) {
            return NOTTINGHAM;
        } else if (BIRMINGHAM.equalsIgnoreCase(midlandsList)) {
            return BIRMINGHAM;
        }
        return EMPTY;
    }

}
