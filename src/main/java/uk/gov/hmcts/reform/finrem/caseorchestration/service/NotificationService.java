package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckApplicantSolicitorIsDigitalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckRespondentSolicitorIsDigitalService;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

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
    private final CheckApplicantSolicitorIsDigitalService checkApplicantSolicitorIsDigitalService;
    private final CheckRespondentSolicitorIsDigitalService checkRespondentSolicitorIsDigitalService;

    private static final String DEFAULT_EMAIL = "fr_applicant_solicitor1@mailinator.com";

    public void sendConsentedHWFSuccessfulConfirmationEmail(CaseDetails caseDetails) {
        URI uri = buildUri(notificationServiceConfiguration.getHwfSuccessful());
        sendNotificationEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails), uri);
    }

    public void sendConsentedHWFSuccessfulConfirmationEmail(FinremCaseDetails caseDetails) {
        URI uri = buildUri(notificationServiceConfiguration.getHwfSuccessful());
        sendNotificationEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails), uri);
    }

    public void sendAssignToJudgeConfirmationEmailToApplicantSolicitor(FinremCaseDetails caseDetails) {
        sendAssignToJudgeConfirmationEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendAssignToJudgeConfirmationEmailToRespondentSolicitor(FinremCaseDetails caseDetails) {
        sendAssignToJudgeConfirmationEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    private void sendAssignToJudgeConfirmationEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getAssignToJudge());
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendConsentOrderMadeConfirmationEmailToApplicantSolicitor(CaseDetails caseDetails) {
        sendConsentOrderMadeConfirmationEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendConsentOrderMadeConfirmationEmailToRespondentSolicitor(CaseDetails caseDetails) {
        sendConsentOrderMadeConfirmationEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    public void sendConsentOrderMadeConfirmationEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentOrderMade());
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendConsentOrderNotApprovedEmailToApplicantSolicitor(CaseDetails caseDetails) {
        sendConsentOrderNotApprovedEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendConsentOrderNotApprovedEmailToRespondentSolicitor(CaseDetails caseDetails) {
        sendConsentOrderNotApprovedEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    private void sendConsentOrderNotApprovedEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentOrderNotApproved());
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendConsentOrderAvailableEmailToApplicantSolicitor(FinremCaseDetails caseDetails) {
        sendConsentOrderAvailableEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendConsentOrderAvailableEmailToRespondentSolicitor(FinremCaseDetails caseDetails) {
        sendConsentOrderAvailableEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    private void sendConsentOrderAvailableEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentOrderAvailable());
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendConsentOrderAvailableCtscEmail(FinremCaseDetails caseDetails) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentOrderAvailableCtsc());
        NotificationRequest ctscNotificationRequest =
            notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails);
        ctscNotificationRequest.setNotificationEmail(notificationServiceConfiguration.getCtscEmail());
        sendNotificationEmail(ctscNotificationRequest, uri);
    }

    public void sendContestedHwfSuccessfulConfirmationEmail(FinremCaseDetails caseDetails) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedHwfSuccessful());
        sendNotificationEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails), uri);
    }

    public void sendContestedApplicationIssuedEmailToApplicantSolicitor(CaseDetails caseDetails) {
        sendContestedApplicationIssuedEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendContestedApplicationIssuedEmailToRespondentSolicitor(CaseDetails caseDetails) {
        sendContestedApplicationIssuedEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    private void sendContestedApplicationIssuedEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedApplicationIssued());
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendContestOrderApprovedEmailApplicant(FinremCaseDetails caseDetails) {
        CompletableFuture.runAsync(() ->
            sendContestOrderApprovedEmail(
                notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails)));
    }


    public void sendContestOrderApprovedEmailRespondent(FinremCaseDetails caseDetails) {
        CompletableFuture.runAsync(() ->
            sendContestOrderApprovedEmail(
                notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails)));
    }

    public void sendContestOrderApprovedEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestOrderApproved());
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendPrepareForHearingEmailApplicant(FinremCaseDetails caseDetails) {
        sendPrepareForHearingEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendPrepareForHearingEmailRespondent(FinremCaseDetails caseDetails) {
        sendPrepareForHearingEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    private void sendPrepareForHearingEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getPrepareForHearing());
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendPrepareForHearingOrderSentEmailApplicant(CaseDetails caseDetails) {
        sendPrepareForHearingOrderSentEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendPrepareForHearingOrderSentEmailRespondent(CaseDetails caseDetails) {
        sendPrepareForHearingOrderSentEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    private void sendPrepareForHearingOrderSentEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getPrepareForHearingOrderSent());
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendSolicitorToDraftOrderEmailApplicant(CaseDetails caseDetails) {
        sendSolicitorToDraftOrderEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendSolicitorToDraftOrderEmailRespondent(CaseDetails caseDetails) {
        sendSolicitorToDraftOrderEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    private void sendSolicitorToDraftOrderEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedDraftOrder());
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendConsentGeneralEmail(CaseDetails caseDetails) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentGeneralEmail());
        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails);
        notificationRequest.setNotificationEmail(Objects.toString(caseDetails.getData().get(GENERAL_EMAIL_RECIPIENT)));
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendContestedGeneralEmail(CaseDetails caseDetails) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedGeneralEmail());
        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails);
        notificationRequest.setNotificationEmail(Objects.toString(caseDetails.getData().get(GENERAL_EMAIL_RECIPIENT)));
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendContestOrderNotApprovedEmailApplicant(CaseDetails caseDetails) {
        sendContestOrderNotApprovedEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendContestOrderNotApprovedEmailRespondent(CaseDetails caseDetails) {
        sendContestOrderNotApprovedEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    private void sendContestOrderNotApprovedEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedOrderNotApproved());
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendContestedConsentOrderApprovedEmailToApplicantSolicitor(CaseDetails caseDetails) {
        sendContestedConsentOrderApprovedEmailToSolicitor(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendContestedConsentOrderApprovedEmailToRespondentSolicitor(CaseDetails caseDetails) {
        sendContestedConsentOrderApprovedEmailToSolicitor(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    private void sendContestedConsentOrderApprovedEmailToSolicitor(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedConsentOrderApproved());
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendContestedConsentOrderNotApprovedEmailApplicantSolicitor(CaseDetails caseDetails) {
        sendContestedConsentOrderNotApprovedEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendContestedConsentOrderNotApprovedEmailRespondentSolicitor(CaseDetails caseDetails) {
        sendContestedConsentOrderNotApprovedEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    private void sendContestedConsentOrderNotApprovedEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedConsentOrderNotApproved());
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendContestedConsentGeneralOrderEmailApplicantSolicitor(CaseDetails caseDetails) {
        sendContestedConsentGeneralOrderEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendContestedConsentGeneralOrderEmailRespondentSolicitor(CaseDetails caseDetails) {
        sendContestedConsentGeneralOrderEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    private void sendContestedConsentGeneralOrderEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedConsentGeneralOrder());
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendConsentedGeneralOrderEmailToApplicantSolicitor(CaseDetails caseDetails) {
        sendConsentedGeneralOrderEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendConsentedGeneralOrderEmailToRespondentSolicitor(CaseDetails caseDetails) {
        sendConsentedGeneralOrderEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    private void sendConsentedGeneralOrderEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentedGeneralOrder());
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendContestedGeneralOrderEmailApplicant(CaseDetails caseDetails) {
        sendContestedGeneralOrderEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendContestedGeneralOrderEmailRespondent(CaseDetails caseDetails) {
        sendContestedGeneralOrderEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    private void sendContestedGeneralOrderEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedGeneralOrder());
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendContestedGeneralApplicationReferToJudgeEmail(CaseDetails caseDetails) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedGeneralApplicationReferToJudge());
        NotificationRequest judgeNotificationRequest = notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails);
        judgeNotificationRequest.setNotificationEmail(Objects.toString(caseDetails.getData().get(GENERAL_APPLICATION_REFER_TO_JUDGE_EMAIL)));
        sendNotificationEmail(judgeNotificationRequest, uri);
    }

    public void sendContestedGeneralApplicationOutcomeEmail(CaseDetails caseDetails) throws IOException {
        String recipientEmail = DEFAULT_EMAIL;
        if (featureToggleService.isSendToFRCEnabled()) {
            Map<String, Object> data = caseDetails.getData();
            Map<String, Object> courtDetailsMap = objectMapper.readValue(getCourtDetailsString(), HashMap.class);
            Map<String, Object> courtDetails = (Map<String, Object>) courtDetailsMap.get(data.get(CaseHearingFunctions.getSelectedCourt(data)));

            recipientEmail = (String) courtDetails.get(COURT_DETAILS_EMAIL_KEY);
        }

        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails);
        notificationRequest.setNotificationEmail(recipientEmail);
        URI uri = buildUri(notificationServiceConfiguration.getContestedGeneralApplicationOutcome());
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendConsentOrderNotApprovedSentEmailToApplicantSolicitor(CaseDetails caseDetails) {
        sendConsentOrderNotApprovedSentEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendConsentOrderNotApprovedSentEmailToRespondentSolicitor(CaseDetails caseDetails) {
        sendConsentOrderNotApprovedSentEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    public void sendInterimHearingNotificationEmailToApplicantSolicitor(CaseDetails caseDetails,
                                                                        Map<String, Object> interimHearingData) {
        sendInterimNotificationEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails,
            interimHearingData));
    }

    public void sendInterimNotificationEmailToApplicantSolicitor(CaseDetails caseDetails) {
        sendInterimNotificationEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendInterimHearingNotificationEmailToRespondentSolicitor(CaseDetails caseDetails,
                                                                         Map<String, Object> interimHearingData) {
        sendInterimNotificationEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails,
            interimHearingData));
    }

    public void sendInterimNotificationEmailToRespondentSolicitor(CaseDetails caseDetails) {
        sendInterimNotificationEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
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
        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails);
        //Overwrite the email, set to the court provided, and use general body to include the Events "Free Text" field
        notificationRequest.setNotificationEmail(Objects.toString(caseDetails.getData().get(TRANSFER_COURTS_EMAIL)));
        notificationRequest.setGeneralEmailBody("The Judge has also ordered that:\n"
            + Objects.toString(caseDetails.getData().get(TRANSFER_COURTS_INSTRUCTIONS)));

        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendUpdateFrcInformationEmailToAppSolicitor(FinremCaseDetails caseDetails) {
        sendUpdateFrcInformationEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendUpdateFrcInformationEmailToRespondentSolicitor(FinremCaseDetails caseDetails) {
        sendUpdateFrcInformationEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    public void sendUpdateFrcInformationEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getUpdateFRCInformation());
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendUpdateFrcInformationEmailToCourt(FinremCaseDetails caseDetails) throws JsonProcessingException {
        String recipientEmail = getRecipientEmail(caseDetails);

        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails);
        notificationRequest.setNotificationEmail(recipientEmail);
        URI uri = buildUri(notificationServiceConfiguration.getUpdateFRCInformationCourt());
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

    @Deprecated
    public boolean shouldEmailRespondentSolicitor(Map<String, Object> caseData) {
        return !caseDataService.isPaperApplication(caseData)
            && caseDataService.isRespondentRepresentedByASolicitor(caseData)
            && caseDataService.isNotEmpty(RESP_SOLICITOR_EMAIL, caseData)
            && !NO_VALUE.equalsIgnoreCase(nullToEmpty(caseData.get(RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT)));
    }

    public boolean shouldEmailRespondentSolicitor(FinremCaseData caseData) {
        return !caseData.isPaperCase()
            && caseData.isRespondentRepresentedByASolicitor()
            && !nullToEmpty(caseData.getContactDetailsWrapper().getRespondentSolicitorEmail()).isEmpty()
            && caseData.isRespondentSolicitorAgreeToReceiveEmails();
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

    public void sendNoticeOfChangeEmail(FinremCaseDetails caseDetails) {
        URI uri = getNoticeOfChangeUri(caseDetails);
        NotificationRequest notificationRequest = notificationRequestMapper
            .getNotificationRequestForNoticeOfChange(caseDetails);
        sendEmailIfSolicitorIsDigital(caseDetails, notificationRequest, uri);
    }

    public void sendNoticeOfChangeEmailCaseworker(FinremCaseDetails caseDetails) {
        URI uri = getNoticeOfChangeUriCaseworker(caseDetails);
        NotificationRequest notificationRequest = notificationRequestMapper
            .getNotificationRequestForNoticeOfChange(caseDetails);
        sendEmailIfSolicitorIsDigital(caseDetails, notificationRequest, uri);
    }

    private void sendEmailIfSolicitorIsDigital(FinremCaseDetails caseDetails,
                                               NotificationRequest notificationRequest,
                                               URI uri) {

        if (isApplicantNoticeOfChangeRequest(notificationRequest, caseDetails)) {
            if (checkApplicantSolicitorIsDigitalService.isSolicitorDigital(caseDetails)) {
                sendNotificationEmail(notificationRequest, uri);
            }
            return;
        }

        if (checkRespondentSolicitorIsDigitalService.isSolicitorDigital(caseDetails)) {
            sendNotificationEmail(notificationRequest, uri);
        }

    }

    private URI getNoticeOfChangeUri(FinremCaseDetails caseDetails) {
        return buildUri(caseDetails.getCaseData().isConsentedApplication()
            ? notificationServiceConfiguration.getConsentedNoticeOfChange()
            : notificationServiceConfiguration.getContestedNoticeOfChange());
    }

    private URI getNoticeOfChangeUriCaseworker(FinremCaseDetails caseDetails) {
        return buildUri(caseDetails.getCaseData().isConsentedApplication()
            ? notificationServiceConfiguration.getConsentedNoCCaseworker()
            : notificationServiceConfiguration.getContestedNoCCaseworker());

    }

    private boolean isApplicantNoticeOfChangeRequest(NotificationRequest notificationRequest,
                                                     FinremCaseDetails caseDetails) {
        return notificationRequest.getName().equalsIgnoreCase(
            nullToEmpty(caseDetails.getCaseData().getApplicantSolicitorName()));
    }

    private String getRecipientEmail(FinremCaseDetails caseDetails) throws JsonProcessingException {
        if (featureToggleService.isSendToFRCEnabled()) {
            FinremCaseData caseData = caseDetails.getCaseData();
            Map<String, Object> courtDetailsMap = objectMapper.readValue(getCourtDetailsString(), HashMap.class);
            Map<String, Object> courtDetails = (Map<String, Object>) courtDetailsMap.get(caseData.getSelectedCourt());

            return (String) courtDetails.get(COURT_DETAILS_EMAIL_KEY);
        }

        return DEFAULT_EMAIL;
    }
}
