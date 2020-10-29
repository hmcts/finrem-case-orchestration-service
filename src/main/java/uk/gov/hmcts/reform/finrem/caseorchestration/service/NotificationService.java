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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BIRMINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CFC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLEAVELAND;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_EMAIL_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_REFER_TO_JUDGE_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_EMAIL_BODY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_EMAIL_RECIPIENT;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SWANSEA;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.getCourtDetailsString;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isConsentedApplication;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isContestedApplication;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private static final String CONSENTED = "consented";
    private static final String CONTESTED = "contested";

    private final NotificationServiceConfiguration notificationServiceConfiguration;
    private final RestTemplate restTemplate;
    private final FeatureToggleService featureToggleService;
    private final ObjectMapper objectMapper;

    private NotificationRequest applicantNotificationRequest;

    private String recipientEmail = "fr_applicant_sol@sharklasers.com";

    public void sendConsentedHWFSuccessfulConfirmationEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getHwfSuccessful());
        applicantNotificationRequest = createNotificationRequestForAppSolicitor(callbackRequest);
        sendNotificationEmail(applicantNotificationRequest, uri);
    }

    public void sendAssignToJudgeConfirmationEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getAssignToJudge());
        applicantNotificationRequest = createNotificationRequestForAppSolicitor(callbackRequest);
        sendNotificationEmail(applicantNotificationRequest, uri);
    }

    public void sendConsentOrderMadeConfirmationEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentOrderMade());
        applicantNotificationRequest = createNotificationRequestForAppSolicitor(callbackRequest);
        sendNotificationEmail(applicantNotificationRequest, uri);
    }

    public void sendConsentOrderNotApprovedEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentOrderNotApproved());
        applicantNotificationRequest = createNotificationRequestForAppSolicitor(callbackRequest);
        sendNotificationEmail(applicantNotificationRequest, uri);
    }

    public void sendConsentOrderAvailableEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentOrderAvailable());
        applicantNotificationRequest = createNotificationRequestForAppSolicitor(callbackRequest);
        sendNotificationEmail(applicantNotificationRequest, uri);
    }

    public void sendConsentOrderAvailableCtscEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentOrderAvailableCtsc());
        NotificationRequest ctscNotificationRequest = createNotificationRequestForAppSolicitor(callbackRequest);
        ctscNotificationRequest.setNotificationEmail(notificationServiceConfiguration.getCtscEmail());
        sendNotificationEmail(ctscNotificationRequest, uri);
    }

    public void sendContestedHwfSuccessfulConfirmationEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedHwfSuccessful());
        applicantNotificationRequest = createNotificationRequestForAppSolicitor(callbackRequest);
        sendNotificationEmail(applicantNotificationRequest, uri);
    }

    public void sendContestedApplicationIssuedEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedApplicationIssued());
        applicantNotificationRequest = createNotificationRequestForAppSolicitor(callbackRequest);
        sendNotificationEmail(applicantNotificationRequest, uri);
    }

    public void sendContestOrderApprovedEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestOrderApproved());
        applicantNotificationRequest = createNotificationRequestForAppSolicitor(callbackRequest);
        sendNotificationEmail(applicantNotificationRequest, uri);
    }

    public void sendPrepareForHearingEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getPrepareForHearing());
        applicantNotificationRequest = createNotificationRequestForAppSolicitor(callbackRequest);
        sendNotificationEmail(applicantNotificationRequest, uri);
    }

    public void sendPrepareForHearingOrderSentEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getPrepareForHearingOrderSent());
        applicantNotificationRequest = createNotificationRequestForAppSolicitor(callbackRequest);
        sendNotificationEmail(applicantNotificationRequest, uri);
    }

    public void sendSolicitorToDraftOrderEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedDraftOrder());
        applicantNotificationRequest = createNotificationRequestForAppSolicitor(callbackRequest);
        sendNotificationEmail(applicantNotificationRequest, uri);
    }

    public void sendConsentGeneralEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentGeneralEmail());
        applicantNotificationRequest = createNotificationRequestForAppSolicitor(callbackRequest);
        applicantNotificationRequest.setNotificationEmail(Objects.toString(callbackRequest.getCaseDetails().getData().get(GENERAL_EMAIL_RECIPIENT)));
        sendNotificationEmail(applicantNotificationRequest, uri);
    }

    public void sendContestedGeneralEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedGeneralEmail());
        applicantNotificationRequest = createNotificationRequestForAppSolicitor(callbackRequest);
        applicantNotificationRequest.setNotificationEmail(Objects.toString(callbackRequest.getCaseDetails().getData().get(GENERAL_EMAIL_RECIPIENT)));
        sendNotificationEmail(applicantNotificationRequest, uri);
    }

    public void sendContestOrderNotApprovedEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedOrderNotApproved());
        applicantNotificationRequest = createNotificationRequestForAppSolicitor(callbackRequest);
        sendNotificationEmail(applicantNotificationRequest, uri);
    }

    public void sendContestedConsentOrderApprovedEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedConsentOrderApproved());
        applicantNotificationRequest = createNotificationRequestForAppSolicitor(callbackRequest);
        sendNotificationEmail(applicantNotificationRequest, uri);
    }

    public void sendContestedConsentOrderNotApprovedEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedConsentOrderNotApproved());
        applicantNotificationRequest = createNotificationRequestForAppSolicitor(callbackRequest);
        sendNotificationEmail(applicantNotificationRequest, uri);
    }

    public void sendContestedConsentGeneralOrderEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedConsentGeneralOrder());
        applicantNotificationRequest = createNotificationRequestForAppSolicitor(callbackRequest);
        sendNotificationEmail(applicantNotificationRequest, uri);
    }

    public void sendConsentedGeneralOrderEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentedGeneralOrder());
        applicantNotificationRequest = createNotificationRequestForAppSolicitor(callbackRequest);
        sendNotificationEmail(applicantNotificationRequest, uri);
    }

    public void sendContestedGeneralOrderEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedGeneralOrder());
        applicantNotificationRequest = createNotificationRequestForAppSolicitor(callbackRequest);
        sendNotificationEmail(applicantNotificationRequest, uri);
    }

    public void sendContestedGeneralApplicationReferToJudgeEmail(CallbackRequest callbackRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedGeneralApplicationReferToJudge());
        NotificationRequest judgeNotificationRequest = createNotificationRequestForAppSolicitor(callbackRequest);
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

        applicantNotificationRequest = createNotificationRequestForAppSolicitor(callbackRequest);
        applicantNotificationRequest.setNotificationEmail(recipientEmail);

        URI uri = buildUri(notificationServiceConfiguration.getContestedGeneralApplicationOutcome());
        sendNotificationEmail(applicantNotificationRequest, uri);
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

    public NotificationRequest createNotificationRequestForAppSolicitor(CallbackRequest callbackRequest) {

        applicantNotificationRequest = isConsentedApplication(callbackRequest.getCaseDetails())
            ? buildNotificationRequest(callbackRequest, SOLICITOR_REFERENCE,
            CONSENTED_SOLICITOR_NAME, SOLICITOR_EMAIL, CONSENTED, GENERAL_EMAIL_BODY, DIVORCE_CASE_NUMBER)
            : buildNotificationRequest(callbackRequest, SOLICITOR_REFERENCE,
            CONTESTED_SOLICITOR_NAME, CONTESTED_SOLICITOR_EMAIL, CONTESTED, GENERAL_EMAIL_BODY, DIVORCE_CASE_NUMBER);

        return applicantNotificationRequest;
    }

    public NotificationRequest createNotificationRequestForRespSolicitor(CallbackRequest callbackRequest) {

        applicantNotificationRequest = isConsentedApplication(callbackRequest.getCaseDetails())
            ? buildNotificationRequest(callbackRequest, RESP_SOLICITOR_REFERENCE,
            RESP_SOLICITOR_NAME, RESP_SOLICITOR_EMAIL, CONSENTED, GENERAL_EMAIL_BODY, DIVORCE_CASE_NUMBER)
            : buildNotificationRequest(callbackRequest, RESP_SOLICITOR_REFERENCE,
            RESP_SOLICITOR_NAME, RESP_SOLICITOR_EMAIL, CONTESTED, GENERAL_EMAIL_BODY, DIVORCE_CASE_NUMBER);

        return applicantNotificationRequest;
    }

    private NotificationRequest buildNotificationRequest(CallbackRequest callbackRequest,
                                                         String solicitorReference,
                                                         String solicitorName,
                                                         String solicitorEmail,
                                                         String caseType,
                                                         String generalEmailBody,
                                                         String divorceCaseNumber) {
        NotificationRequest notificationRequest = new NotificationRequest();
        Map<String, Object> mapOfCaseData = callbackRequest.getCaseDetails().getData();

        notificationRequest.setCaseReferenceNumber(Objects.toString(callbackRequest.getCaseDetails().getId()));
        notificationRequest.setSolicitorReferenceNumber(Objects.toString(mapOfCaseData.get(solicitorReference)));
        notificationRequest.setDivorceCaseNumber(Objects.toString(mapOfCaseData.get(divorceCaseNumber)));
        notificationRequest.setName(Objects.toString(mapOfCaseData.get(solicitorName)));
        notificationRequest.setNotificationEmail(Objects.toString(mapOfCaseData.get(solicitorEmail)));
        notificationRequest.setGeneralEmailBody(Objects.toString(mapOfCaseData.get(generalEmailBody)));
        notificationRequest.setCaseType(caseType);

        // TODO replcae this check with isContestedApplication()
        if (isContestedApplication(callbackRequest.getCaseDetails())) {
            String selectedCourt = getSelectedCourt(mapOfCaseData);
            notificationRequest.setSelectedCourt(selectedCourt);

            log.info("selectedCourt is {} for case ID: {}", selectedCourt,
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
}
