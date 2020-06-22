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
import java.util.Map;
import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BIRMINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CFC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLEAVELAND;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HSYORKSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENTFRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LIVERPOOL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MANCHESTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NEWPORT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHEAST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWEST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NWYORKSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SWANSEA;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isConsentedApplication;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private static final String MESSAGE = "Failed to send notification email for case id : ";
    private static final String MSG_SOLICITOR_EMAIL = " for solicitor email";
    private static final String EXCEPTION = "exception :";
    private static final String CONSENTED = "consented";
    private static final String CONTESTED = "contested";

    private final NotificationServiceConfiguration notificationServiceConfiguration;
    private final RestTemplate restTemplate;

    private NotificationRequest notificationRequest;

    public void sendConsentedHWFSuccessfulConfirmationEmail(CallbackRequest callbackRequest) {
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
        URI uri = buildUri(notificationServiceConfiguration.getContestedHwfSuccessful());
        sendNotificationEmail(callbackRequest, uri);
    }

    public void sendContestedApplicationIssuedEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedApplicationIssued());
        sendNotificationEmail(callbackRequest, uri);
    }

    public void sendContestOrderApprovedEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestOrderApproved());
        sendNotificationEmail(callbackRequest, uri);
    }

    public void sendPrepareForHearingEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getPrepareForHearing());
        sendNotificationEmail(callbackRequest, uri);
    }

    public void sendPrepareForHearingOrderSentEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getPrepareForHearingOrderSent());
        sendNotificationEmail(callbackRequest, uri);
    }

    public void sendSolicitorToDraftOrderEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedDraftOrder());
        sendNotificationEmail(callbackRequest, uri);
    }

    private void sendNotificationEmail(CallbackRequest callbackRequest, URI uri) {
        try {
            if (isConsentedApplication(callbackRequest.getCaseDetails())) {
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
                                                         String solicitorEmail,
                                                         String caseType) throws IOException {
        NotificationRequest notificationRequest = new NotificationRequest();
        Map<String, Object> mapOfCaseData = callbackRequest.getCaseDetails().getData();

        notificationRequest.setCaseReferenceNumber(Objects.toString(callbackRequest.getCaseDetails().getId()));
        notificationRequest.setSolicitorReferenceNumber(Objects.toString(mapOfCaseData.get(solicitorReference)));
        notificationRequest.setName(Objects.toString(mapOfCaseData.get(solicitorName)));
        notificationRequest.setNotificationEmail(Objects.toString(mapOfCaseData.get(solicitorEmail)));
        notificationRequest.setCaseType(caseType);

        if (CONTESTED.equalsIgnoreCase(caseType)) {
            String selectedCourt = getSelectedCourt(mapOfCaseData);
            notificationRequest.setSelectedCourt(selectedCourt);

            log.info("selectedCourt is  {} for case Id : {}", selectedCourt,
                notificationRequest.getCaseReferenceNumber());
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

    private String getSelectedCourt(Map<String, Object> mapOfCaseData) {
        String region = (String) mapOfCaseData.get(REGION);
        if (MIDLANDS.equalsIgnoreCase(region)) {
            return getMidlandFRC(mapOfCaseData);
        }
        if (LONDON.equalsIgnoreCase(region)) {
            return getLondonFRC(mapOfCaseData);
        }
        if (NORTHWEST.equalsIgnoreCase(region)) {
            return getNorthWestFRC(mapOfCaseData);
        }
        if (NORTHEAST.equalsIgnoreCase(region)) {
            return getNorthEastFRC(mapOfCaseData);
        }
        if (SOUTHEAST.equalsIgnoreCase(region)) {
            return getSouthEastFRC(mapOfCaseData);
        } else if (WALES.equalsIgnoreCase(region)) {
            return getWalesFRC(mapOfCaseData);
        }
        return EMPTY;
    }

    private String getWalesFRC(Map mapOfCaseData) {
        String walesList = (String) mapOfCaseData.get(WALES_FRC_LIST);
        if (NEWPORT.equalsIgnoreCase(walesList)) {
            return NEWPORT;
        } else if (SWANSEA.equalsIgnoreCase(walesList)) {
            return SWANSEA;
        }
        return EMPTY;
    }

    private String getSouthEastFRC(Map mapOfCaseData) {
        String southEastList = (String) mapOfCaseData.get(SOUTHEAST_FRC_LIST);
        if (KENTFRC.equalsIgnoreCase(southEastList)) {
            return KENTFRC;
        }
        return EMPTY;
    }

    private String getNorthEastFRC(Map mapOfCaseData) {
        String northEastList = (String) mapOfCaseData.get(NORTHEAST_FRC_LIST);
        if (CLEAVELAND.equalsIgnoreCase(northEastList)) {
            return CLEAVELAND;
        } else if (NWYORKSHIRE.equalsIgnoreCase(northEastList)) {
            return NWYORKSHIRE;
        } else if (HSYORKSHIRE.equalsIgnoreCase(northEastList)) {
            return HSYORKSHIRE;
        }
        return EMPTY;
    }

    private String getNorthWestFRC(Map mapOfCaseData) {
        String northWestList = (String) mapOfCaseData.get(NORTHWEST_FRC_LIST);
        if (LIVERPOOL.equalsIgnoreCase(northWestList)) {
            return LIVERPOOL;
        } else if (MANCHESTER.equalsIgnoreCase(northWestList)) {
            return MANCHESTER;
        }
        return EMPTY;
    }

    private String getLondonFRC(Map mapOfCaseData) {
        String londonList = (String) mapOfCaseData.get(LONDON_FRC_LIST);
        if (CFC.equalsIgnoreCase(londonList)) {
            return CFC;
        }
        return EMPTY;
    }

    private String getMidlandFRC(Map mapOfCaseData) {
        String midlandsList = (String) mapOfCaseData.get(MIDLANDS_FRC_LIST);
        if (NOTTINGHAM.equalsIgnoreCase(midlandsList)) {
            return NOTTINGHAM;
        } else if (BIRMINGHAM.equalsIgnoreCase(midlandsList)) {
            return BIRMINGHAM;
        }
        return EMPTY;
    }
}
