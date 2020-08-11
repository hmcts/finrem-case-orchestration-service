package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.NotificationServiceConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ALLOCATED_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BIRMINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CFC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLEAVELAND;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_EMAIL_BODY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HSYORKSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENT;
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
    private final FeatureToggleService featureToggleService;

    private NotificationRequest notificationRequest;

    public void sendConsentedHWFSuccessfulConfirmationEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getHwfSuccessful());
        notificationRequest = createNotificationRequest(callbackRequest);
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendAssignToJudgeConfirmationEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getAssignToJudge());
        notificationRequest = createNotificationRequest(callbackRequest);
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendConsentOrderMadeConfirmationEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentOrderMade());
        notificationRequest = createNotificationRequest(callbackRequest);
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendConsentOrderNotApprovedEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentOrderNotApproved());
        notificationRequest = createNotificationRequest(callbackRequest);
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendConsentOrderAvailableEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentOrderAvailable());
        notificationRequest = createNotificationRequest(callbackRequest);
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendContestedHwfSuccessfulConfirmationEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedHwfSuccessful());
        notificationRequest = createNotificationRequest(callbackRequest);
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendContestedApplicationIssuedEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedApplicationIssued());
        notificationRequest = createNotificationRequest(callbackRequest);
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendContestOrderApprovedEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestOrderApproved());
        notificationRequest = createNotificationRequest(callbackRequest);
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendPrepareForHearingEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getPrepareForHearing());
        notificationRequest = createNotificationRequest(callbackRequest);
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendPrepareForHearingOrderSentEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getPrepareForHearingOrderSent());
        notificationRequest = createNotificationRequest(callbackRequest);
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendSolicitorToDraftOrderEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedDraftOrder());
        notificationRequest = createNotificationRequest(callbackRequest);
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendConsentGeneralEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentGeneralEmail());
        notificationRequest = createNotificationRequest(callbackRequest);
        notificationRequest.setNotificationEmail(Objects.toString(callbackRequest.getCaseDetails().getData().get("generalEmailRecipient")));
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendContestedGeneralEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedGeneralEmail());
        notificationRequest = createNotificationRequest(callbackRequest);
        notificationRequest.setNotificationEmail(Objects.toString(callbackRequest.getCaseDetails().getData().get("generalEmailRecipient")));
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendContestOrderNotApprovedEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedOrderNotApproved());
        notificationRequest = createNotificationRequest(callbackRequest);
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendContestedConsentOrderApprovedEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedConsentOrderApproved());
        notificationRequest = createNotificationRequest(callbackRequest);
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendCTSCNotificationOfAutomatedSendOrder(CaseDetails caseDetails) {
        URI uri = buildUri(notificationServiceConfiguration.getCTSCSendOrderNotification());
        notificationRequest = createNotificationRequest(callbackRequest);
        sendNotificationEmail(notificationRequest, uri);
    }

    private void sendNotificationEmail(NotificationRequest notificationRequest, URI uri) {
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

    private NotificationRequest createNotificationRequest(CallbackRequest callbackRequest) {
        if (isConsentedApplication(callbackRequest.getCaseDetails())) {
            notificationRequest = buildNotificationRequest(callbackRequest, SOLICITOR_REFERENCE,
                CONSENTED_SOLICITOR_NAME, SOLICITOR_EMAIL, CONSENTED, GENERAL_EMAIL_BODY);
        } else {
            notificationRequest = buildNotificationRequest(callbackRequest, SOLICITOR_REFERENCE,
                CONTESTED_SOLICITOR_NAME, CONTESTED_SOLICITOR_EMAIL, CONTESTED, GENERAL_EMAIL_BODY);
        }

        return notificationRequest;
    }

    private NotificationRequest buildNotificationRequest(CallbackRequest callbackRequest,
                                                         String solicitorReference,
                                                         String solicitorName,
                                                         String solicitorEmail,
                                                         String caseType,
                                                         String generalEmailBody) {
        NotificationRequest notificationRequest = new NotificationRequest();
        Map<String, Object> mapOfCaseData = callbackRequest.getCaseDetails().getData();

        notificationRequest.setCaseReferenceNumber(Objects.toString(callbackRequest.getCaseDetails().getId()));
        notificationRequest.setSolicitorReferenceNumber(Objects.toString(mapOfCaseData.get(solicitorReference)));
        notificationRequest.setName(Objects.toString(mapOfCaseData.get(solicitorName)));
        notificationRequest.setNotificationEmail(Objects.toString(mapOfCaseData.get(solicitorEmail)));
        notificationRequest.setGeneralEmailBody(Objects.toString(mapOfCaseData.get(generalEmailBody)));
        notificationRequest.setCaseType(caseType);

        if (CONTESTED.equalsIgnoreCase(caseType)) {
            String selectedCourt = featureToggleService.isContestedCourtDetailsMigrationEnabled()
                ? getSelectedCourt(mapOfCaseData) : getSelectedCourtAllocatedCourt(mapOfCaseData.get(ALLOCATED_COURT_LIST));
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
        if (KENT.equalsIgnoreCase(southEastList)) {
            return KENT;
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

    private String getSelectedCourtAllocatedCourt(Object allocatedCourtList) {
        HashMap<String, Object> allocatedCourtMap = (HashMap<String, Object>) allocatedCourtList;
        String region = (String) allocatedCourtMap.get("region");
        if (MIDLANDS.equalsIgnoreCase(region)) {
            return getMidlandsFrcAllocatedCourt(allocatedCourtMap);
        }
        if (LONDON.equalsIgnoreCase(region)) {
            return getLondonFrcAllocatedCourt(allocatedCourtMap);
        }
        if (NORTHWEST.equalsIgnoreCase(region)) {
            return getNorthWestFrcAllocatedCourt(allocatedCourtMap);
        }
        if (NORTHEAST.equalsIgnoreCase(region)) {
            return getNorthEastFrcAllocatedCourt(allocatedCourtMap);
        }
        if (SOUTHEAST.equalsIgnoreCase(region)) {
            return getSouthEastFrcAllocatedCourt(allocatedCourtMap);
        } else if (WALES.equalsIgnoreCase(region)) {
            return getWalesFrcAllocatedCourt(allocatedCourtMap);
        }
        return EMPTY;
    }

    private String getWalesFrcAllocatedCourt(Map allocatedCourtMap) {
        String walesList = (String) allocatedCourtMap.get("walesList");
        if (NEWPORT.equalsIgnoreCase(walesList)) {
            return NEWPORT;
        } else if (SWANSEA.equalsIgnoreCase(walesList)) {
            return SWANSEA;
        }
        return EMPTY;
    }

    private String getSouthEastFrcAllocatedCourt(Map allocatedCourtMap) {
        String southEastList = (String) allocatedCourtMap.get("southEastList");
        if (KENT.equalsIgnoreCase(southEastList)) {
            return KENT;
        }
        return EMPTY;
    }

    private String getNorthEastFrcAllocatedCourt(Map allocatedCourtMap) {
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

    private String getNorthWestFrcAllocatedCourt(Map allocatedCourtMap) {
        String northWestList = (String) allocatedCourtMap.get("northWestList");
        if (LIVERPOOL.equalsIgnoreCase(northWestList)) {
            return LIVERPOOL;
        } else if (MANCHESTER.equalsIgnoreCase(northWestList)) {
            return MANCHESTER;
        }
        return EMPTY;
    }

    private String getLondonFrcAllocatedCourt(Map allocatedCourtMap) {
        String londonList = (String) allocatedCourtMap.get("londonList");
        if (CFC.equalsIgnoreCase(londonList)) {
            return CFC;
        }
        return EMPTY;
    }

    private String getMidlandsFrcAllocatedCourt(Map allocatedCourtMap) {
        String midlandsList = (String) allocatedCourtMap.get("midlandsList");
        if (NOTTINGHAM.equalsIgnoreCase(midlandsList)) {
            return NOTTINGHAM;
        } else if (BIRMINGHAM.equalsIgnoreCase(midlandsList)) {
            return BIRMINGHAM;
        }
        return EMPTY;
    }
}
