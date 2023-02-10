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
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremNotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.NotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckSolicitorIsDigitalService;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_EMAIL_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_REFERRED_DETAIL;
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

    private static final String DEFAULT_EMAIL = "fr_applicant_solicitor1@mailinator.com";
    private final NotificationServiceConfiguration notificationServiceConfiguration;
    private final RestTemplate restTemplate;
    private final FeatureToggleService featureToggleService;
    private final ObjectMapper objectMapper;
    private final NotificationRequestMapper notificationRequestMapper;
    private final FinremNotificationRequestMapper finremNotificationRequestMapper;
    private final CaseDataService caseDataService;
    private final CheckSolicitorIsDigitalService checkSolicitorIsDigitalService;

    @Deprecated
    public void sendConsentedHWFSuccessfulConfirmationEmail(CaseDetails caseDetails) {
        URI uri = buildUri(notificationServiceConfiguration.getHwfSuccessful());
        sendNotificationEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails), uri);
    }

    public void sendConsentedHWFSuccessfulConfirmationEmail(FinremCaseDetails caseDetails) {
        URI uri = buildUri(notificationServiceConfiguration.getHwfSuccessful());
        sendNotificationEmail(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails), uri);
    }

    @Deprecated
    public void sendAssignToJudgeConfirmationEmailToApplicantSolicitor(CaseDetails caseDetails) {
        sendAssignToJudgeConfirmationEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendAssignToJudgeConfirmationEmailToApplicantSolicitor(FinremCaseDetails caseDetails) {
        sendAssignToJudgeConfirmationEmail(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    @Deprecated
    public void sendAssignToJudgeConfirmationEmailToRespondentSolicitor(CaseDetails caseDetails) {
        sendAssignToJudgeConfirmationEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    public void sendAssignToJudgeConfirmationEmailToRespondentSolicitor(FinremCaseDetails caseDetails) {
        sendAssignToJudgeConfirmationEmail(finremNotificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    private void sendAssignToJudgeConfirmationEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getAssignToJudge());
        sendNotificationEmail(notificationRequest, uri);
    }

    @Deprecated
    public void sendConsentOrderMadeConfirmationEmailToApplicantSolicitor(CaseDetails caseDetails) {
        sendConsentOrderMadeConfirmationEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendConsentOrderMadeConfirmationEmailToApplicantSolicitor(FinremCaseDetails caseDetails) {
        sendConsentOrderMadeConfirmationEmail(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    @Deprecated
    public void sendConsentOrderMadeConfirmationEmailToRespondentSolicitor(CaseDetails caseDetails) {
        sendConsentOrderMadeConfirmationEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    public void sendConsentOrderMadeConfirmationEmailToRespondentSolicitor(FinremCaseDetails caseDetails) {
        sendConsentOrderMadeConfirmationEmail(finremNotificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    public void sendConsentOrderMadeConfirmationEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentOrderMade());
        sendNotificationEmail(notificationRequest, uri);
    }

    @Deprecated
    public void sendConsentOrderNotApprovedEmailToApplicantSolicitor(CaseDetails caseDetails) {
        sendConsentOrderNotApprovedEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendConsentOrderNotApprovedEmailToApplicantSolicitor(FinremCaseDetails caseDetails) {
        sendConsentOrderNotApprovedEmail(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    @Deprecated
    public void sendConsentOrderNotApprovedEmailToRespondentSolicitor(CaseDetails caseDetails) {
        sendConsentOrderNotApprovedEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    public void sendConsentOrderNotApprovedEmailToRespondentSolicitor(FinremCaseDetails caseDetails) {
        sendConsentOrderNotApprovedEmail(finremNotificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    private void sendConsentOrderNotApprovedEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentOrderNotApproved());
        sendNotificationEmail(notificationRequest, uri);
    }

    @Deprecated
    public void sendConsentOrderAvailableEmailToApplicantSolicitor(CaseDetails caseDetails) {
        sendConsentOrderAvailableEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendConsentOrderAvailableEmailToApplicantSolicitor(FinremCaseDetails caseDetails) {
        sendConsentOrderAvailableEmail(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    @Deprecated
    public void sendConsentOrderAvailableEmailToRespondentSolicitor(CaseDetails caseDetails) {
        sendConsentOrderAvailableEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    public void sendConsentOrderAvailableEmailToRespondentSolicitor(FinremCaseDetails caseDetails) {
        sendConsentOrderAvailableEmail(finremNotificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    private void sendConsentOrderAvailableEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentOrderAvailable());
        sendNotificationEmail(notificationRequest, uri);
    }

    @Deprecated
    public void sendConsentOrderAvailableCtscEmail(CaseDetails caseDetails) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentOrderAvailableCtsc());
        NotificationRequest ctscNotificationRequest = notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails);
        ctscNotificationRequest.setNotificationEmail(notificationServiceConfiguration.getCtscEmail());
        sendNotificationEmail(ctscNotificationRequest, uri);
    }

    public void sendConsentOrderAvailableCtscEmail(FinremCaseDetails caseDetails) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentOrderAvailableCtsc());
        NotificationRequest ctscNotificationRequest = finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails);
        ctscNotificationRequest.setNotificationEmail(notificationServiceConfiguration.getCtscEmail());
        sendNotificationEmail(ctscNotificationRequest, uri);
    }

    @Deprecated
    public void sendContestedHwfSuccessfulConfirmationEmail(CaseDetails caseDetails) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedHwfSuccessful());
        sendNotificationEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails), uri);
    }

    public void sendContestedHwfSuccessfulConfirmationEmail(FinremCaseDetails caseDetails) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedHwfSuccessful());
        sendNotificationEmail(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails), uri);
    }

    @Deprecated
    public void sendContestedApplicationIssuedEmailToApplicantSolicitor(CaseDetails caseDetails) {
        sendContestedApplicationIssuedEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendContestedApplicationIssuedEmailToApplicantSolicitor(FinremCaseDetails caseDetails) {
        sendContestedApplicationIssuedEmail(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    private void sendContestedApplicationIssuedEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedApplicationIssued());
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendContestOrderApprovedEmailApplicant(CaseDetails caseDetails) {
        CompletableFuture.runAsync(() ->
            sendContestOrderApprovedEmail(
                notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails)));
    }

    public void sendContestOrderApprovedEmailApplicant(FinremCaseDetails caseDetails) {
        CompletableFuture.runAsync(() ->
            sendContestOrderApprovedEmail(
                finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails)));
    }

    @Deprecated
    public void sendContestOrderApprovedEmailRespondent(CaseDetails caseDetails) {
        CompletableFuture.runAsync(() ->
            sendContestOrderApprovedEmail(
                notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails)));
    }

    public void sendContestOrderApprovedEmailRespondent(FinremCaseDetails caseDetails) {
        CompletableFuture.runAsync(() ->
            sendContestOrderApprovedEmail(
                finremNotificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails)));
    }

    public void sendContestOrderApprovedEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestOrderApproved());
        sendNotificationEmail(notificationRequest, uri);
    }

    @Deprecated
    public void sendPrepareForHearingEmailApplicant(CaseDetails caseDetails) {
        sendPrepareForHearingEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendPrepareForHearingEmailApplicant(FinremCaseDetails caseDetails) {
        sendPrepareForHearingEmail(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    @Deprecated
    public void sendPrepareForHearingEmailRespondent(CaseDetails caseDetails) {
        sendPrepareForHearingEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    public void sendPrepareForHearingEmailRespondent(FinremCaseDetails caseDetails) {
        sendPrepareForHearingEmail(finremNotificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    private void sendPrepareForHearingEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getPrepareForHearing());
        sendNotificationEmail(notificationRequest, uri);
    }

    @Deprecated
    public void sendPrepareForHearingOrderSentEmailApplicant(CaseDetails caseDetails) {
        sendPrepareForHearingOrderSentEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendPrepareForHearingOrderSentEmailApplicant(FinremCaseDetails caseDetails) {
        sendPrepareForHearingOrderSentEmail(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    @Deprecated
    public void sendPrepareForHearingOrderSentEmailRespondent(CaseDetails caseDetails) {
        sendPrepareForHearingOrderSentEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    public void sendPrepareForHearingOrderSentEmailRespondent(FinremCaseDetails caseDetails) {
        sendPrepareForHearingOrderSentEmail(finremNotificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    private void sendPrepareForHearingOrderSentEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getPrepareForHearingOrderSent());
        sendNotificationEmail(notificationRequest, uri);
    }

    @Deprecated
    public void sendSolicitorToDraftOrderEmailApplicant(CaseDetails caseDetails) {
        sendSolicitorToDraftOrderEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }


    public void sendSolicitorToDraftOrderEmailApplicant(FinremCaseDetails caseDetails) {
        sendSolicitorToDraftOrderEmail(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    @Deprecated
    public void sendSolicitorToDraftOrderEmailRespondent(CaseDetails caseDetails) {
        sendSolicitorToDraftOrderEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    public void sendSolicitorToDraftOrderEmailRespondent(FinremCaseDetails caseDetails) {
        sendSolicitorToDraftOrderEmail(finremNotificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    private void sendSolicitorToDraftOrderEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedDraftOrder());
        sendNotificationEmail(notificationRequest, uri);
    }

    @Deprecated
    public void sendConsentGeneralEmail(CaseDetails caseDetails) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentGeneralEmail());
        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails);
        notificationRequest.setNotificationEmail(Objects.toString(caseDetails.getData().get(GENERAL_EMAIL_RECIPIENT)));
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendConsentGeneralEmail(FinremCaseDetails caseDetails) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentGeneralEmail());
        NotificationRequest notificationRequest = finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails);
        notificationRequest.setNotificationEmail(caseDetails.getData().getGeneralEmailRecipient());
        sendNotificationEmail(notificationRequest, uri);
    }

    @Deprecated
    public void sendContestedGeneralEmail(CaseDetails caseDetails) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedGeneralEmail());
        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails);
        notificationRequest.setNotificationEmail(Objects.toString(caseDetails.getData().get(GENERAL_EMAIL_RECIPIENT)));
        sendNotificationEmail(notificationRequest, uri);
    }


    public void sendContestedGeneralEmail(FinremCaseDetails caseDetails) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedGeneralEmail());
        NotificationRequest notificationRequest = finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails);
        notificationRequest.setNotificationEmail(Objects.toString(caseDetails.getData().getGeneralEmailRecipient()));
        sendNotificationEmail(notificationRequest, uri);
    }

    @Deprecated
    public void sendContestOrderNotApprovedEmailApplicant(CaseDetails caseDetails) {
        sendContestOrderNotApprovedEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendContestOrderNotApprovedEmailApplicant(FinremCaseDetails caseDetails) {
        sendContestOrderNotApprovedEmail(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    @Deprecated
    public void sendContestOrderNotApprovedEmailRespondent(CaseDetails caseDetails) {
        sendContestOrderNotApprovedEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    public void sendContestOrderNotApprovedEmailRespondent(FinremCaseDetails caseDetails) {
        sendContestOrderNotApprovedEmail(finremNotificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    private void sendContestOrderNotApprovedEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedOrderNotApproved());
        sendNotificationEmail(notificationRequest, uri);
    }

    @Deprecated
    public void sendContestedConsentOrderApprovedEmailToApplicantSolicitor(CaseDetails caseDetails) {
        sendContestedConsentOrderApprovedEmailToSolicitor(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendContestedConsentOrderApprovedEmailToApplicantSolicitor(FinremCaseDetails caseDetails) {
        sendContestedConsentOrderApprovedEmailToSolicitor(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    @Deprecated
    public void sendContestedConsentOrderApprovedEmailToRespondentSolicitor(CaseDetails caseDetails) {
        sendContestedConsentOrderApprovedEmailToSolicitor(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    public void sendContestedConsentOrderApprovedEmailToRespondentSolicitor(FinremCaseDetails caseDetails) {
        sendContestedConsentOrderApprovedEmailToSolicitor(finremNotificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    private void sendContestedConsentOrderApprovedEmailToSolicitor(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedConsentOrderApproved());
        sendNotificationEmail(notificationRequest, uri);
    }

    @Deprecated
    public void sendContestedConsentOrderNotApprovedEmailApplicantSolicitor(CaseDetails caseDetails) {
        sendContestedConsentOrderNotApprovedEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendContestedConsentOrderNotApprovedEmailApplicantSolicitor(FinremCaseDetails caseDetails) {
        sendContestedConsentOrderNotApprovedEmail(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    @Deprecated
    public void sendContestedConsentOrderNotApprovedEmailRespondentSolicitor(CaseDetails caseDetails) {
        sendContestedConsentOrderNotApprovedEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    public void sendContestedConsentOrderNotApprovedEmailRespondentSolicitor(FinremCaseDetails caseDetails) {
        sendContestedConsentOrderNotApprovedEmail(finremNotificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    private void sendContestedConsentOrderNotApprovedEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedConsentOrderNotApproved());
        sendNotificationEmail(notificationRequest, uri);
    }

    @Deprecated
    public void sendContestedConsentGeneralOrderEmailApplicantSolicitor(CaseDetails caseDetails) {
        sendContestedConsentGeneralOrderEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendContestedConsentGeneralOrderEmailApplicantSolicitor(FinremCaseDetails caseDetails) {
        sendContestedConsentGeneralOrderEmail(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    @Deprecated
    public void sendContestedConsentGeneralOrderEmailRespondentSolicitor(CaseDetails caseDetails) {
        sendContestedConsentGeneralOrderEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    public void sendContestedConsentGeneralOrderEmailRespondentSolicitor(FinremCaseDetails caseDetails) {
        sendContestedConsentGeneralOrderEmail(finremNotificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    private void sendContestedConsentGeneralOrderEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedConsentGeneralOrder());
        sendNotificationEmail(notificationRequest, uri);
    }

    @Deprecated
    public void sendConsentedGeneralOrderEmailToApplicantSolicitor(CaseDetails caseDetails) {
        sendConsentedGeneralOrderEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendConsentedGeneralOrderEmailToApplicantSolicitor(FinremCaseDetails caseDetails) {
        sendConsentedGeneralOrderEmail(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    @Deprecated
    public void sendConsentedGeneralOrderEmailToRespondentSolicitor(CaseDetails caseDetails) {
        sendConsentedGeneralOrderEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    public void sendConsentedGeneralOrderEmailToRespondentSolicitor(FinremCaseDetails caseDetails) {
        sendConsentedGeneralOrderEmail(finremNotificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    private void sendConsentedGeneralOrderEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentedGeneralOrder());
        sendNotificationEmail(notificationRequest, uri);
    }

    @Deprecated
    public void sendContestedGeneralOrderEmailApplicant(CaseDetails caseDetails) {
        sendContestedGeneralOrderEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendContestedGeneralOrderEmailApplicant(FinremCaseDetails caseDetails) {
        sendContestedGeneralOrderEmail(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    @Deprecated
    public void sendContestedGeneralOrderEmailRespondent(CaseDetails caseDetails) {
        sendContestedGeneralOrderEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    public void sendContestedGeneralOrderEmailRespondent(FinremCaseDetails caseDetails) {
        sendContestedGeneralOrderEmail(finremNotificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    private void sendContestedGeneralOrderEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedGeneralOrder());
        sendNotificationEmail(notificationRequest, uri);
    }

    @Deprecated
    public void sendContestedGeneralApplicationReferToJudgeEmail(CaseDetails caseDetails) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedGeneralApplicationReferToJudge());
        NotificationRequest judgeNotificationRequest = notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails);
        judgeNotificationRequest.setNotificationEmail(Objects.toString(caseDetails.getData().get(GENERAL_APPLICATION_REFER_TO_JUDGE_EMAIL)));
        if (caseDetails.getData().get(GENERAL_APPLICATION_REFERRED_DETAIL) != null) {
            judgeNotificationRequest.setGeneralEmailBody(Objects.toString(caseDetails.getData().get(GENERAL_APPLICATION_REFERRED_DETAIL)));
        }
        sendNotificationEmail(judgeNotificationRequest, uri);
    }

    public void sendContestedGeneralApplicationReferToJudgeEmail(FinremCaseDetails caseDetails) {
        URI uri = buildUri(notificationServiceConfiguration.getContestedGeneralApplicationReferToJudge());
        NotificationRequest judgeNotificationRequest = finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails);
        judgeNotificationRequest.setNotificationEmail(caseDetails.getData().getGeneralApplicationWrapper().getGeneralApplicationReferToJudgeEmail());
        if (caseDetails.getData().getGeneralApplicationWrapper().getGeneralApplicationReferDetail() != null) {
            judgeNotificationRequest.setGeneralEmailBody(
                caseDetails.getData().getGeneralApplicationWrapper().getGeneralApplicationReferDetail());
        }
        sendNotificationEmail(judgeNotificationRequest, uri);
    }

    @Deprecated
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


    public void sendContestedGeneralApplicationOutcomeEmail(FinremCaseDetails caseDetails) throws IOException {
        String recipientEmail = DEFAULT_EMAIL;
        if (featureToggleService.isSendToFRCEnabled()) {
            Map<String, Object> courtDetailsMap = objectMapper.readValue(getCourtDetailsString(), HashMap.class);
            Map<String, Object> courtDetails = (Map<String, Object>) courtDetailsMap.get(caseDetails.getData().getSelectedCourt());
            recipientEmail = (String) courtDetails.get(COURT_DETAILS_EMAIL_KEY);
        }

        NotificationRequest notificationRequest = finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails);
        notificationRequest.setNotificationEmail(recipientEmail);
        URI uri = buildUri(notificationServiceConfiguration.getContestedGeneralApplicationOutcome());
        sendNotificationEmail(notificationRequest, uri);
    }

    @Deprecated
    public void sendConsentOrderNotApprovedSentEmailToApplicantSolicitor(CaseDetails caseDetails) {
        sendConsentOrderNotApprovedSentEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendConsentOrderNotApprovedSentEmailToApplicantSolicitor(FinremCaseDetails caseDetails) {
        sendConsentOrderNotApprovedSentEmail(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    @Deprecated
    public void sendConsentOrderNotApprovedSentEmailToRespondentSolicitor(CaseDetails caseDetails) {
        sendConsentOrderNotApprovedSentEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    public void sendConsentOrderNotApprovedSentEmailToRespondentSolicitor(FinremCaseDetails caseDetails) {
        sendConsentOrderNotApprovedSentEmail(finremNotificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    public void sendInterimHearingNotificationEmailToApplicantSolicitor(CaseDetails caseDetails,
                                                                        Map<String, Object> interimHearingData) {
        sendInterimNotificationEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails,
            interimHearingData));
    }

    public void sendConsentHearingNotificationEmailToApplicantSolicitor(CaseDetails caseDetails,
                                                                        Map<String, Object> hearingData) {
        sendConsentedHearingNotificationEmail(notificationRequestMapper.getNotificationRequestForConsentApplicantSolicitor(caseDetails,
            hearingData));
    }

    private void sendConsentedHearingNotificationEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentedHearing());
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendConsentHearingNotificationEmailToRespondentSolicitor(CaseDetails caseDetails,
                                                                         Map<String, Object> hearingData) {
        sendConsentedHearingNotificationEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails,
            hearingData));
    }

    @Deprecated
    public void sendInterimNotificationEmailToApplicantSolicitor(CaseDetails caseDetails) {
        sendInterimNotificationEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendInterimNotificationEmailToApplicantSolicitor(FinremCaseDetails caseDetails) {
        sendInterimNotificationEmail(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendInterimHearingNotificationEmailToRespondentSolicitor(CaseDetails caseDetails,
                                                                         Map<String, Object> interimHearingData) {
        sendInterimNotificationEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails,
            interimHearingData));
    }

    @Deprecated
    public void sendInterimNotificationEmailToRespondentSolicitor(CaseDetails caseDetails) {
        sendInterimNotificationEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    public void sendInterimNotificationEmailToRespondentSolicitor(FinremCaseDetails caseDetails) {
        sendInterimNotificationEmail(finremNotificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    private void sendInterimNotificationEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getPrepareForInterimHearing());
        sendNotificationEmail(notificationRequest, uri);
    }

    private void sendConsentOrderNotApprovedSentEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getConsentOrderNotApprovedSent());
        sendNotificationEmail(notificationRequest, uri);
    }

    @Deprecated
    public void sendTransferToLocalCourtEmail(CaseDetails caseDetails) {
        URI uri = buildUri(notificationServiceConfiguration.getTransferToLocalCourt());
        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails);
        //Overwrite the email, set to the court provided, and use general body to include the Events "Free Text" field
        notificationRequest.setNotificationEmail(Objects.toString(caseDetails.getData().get(TRANSFER_COURTS_EMAIL)));
        notificationRequest.setGeneralEmailBody("The Judge has also ordered that:\n"
            + Objects.toString(caseDetails.getData().get(TRANSFER_COURTS_INSTRUCTIONS)));

        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendTransferToLocalCourtEmail(FinremCaseDetails caseDetails) {
        URI uri = buildUri(notificationServiceConfiguration.getTransferToLocalCourt());
        NotificationRequest notificationRequest = finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails);
        //Overwrite the email, set to the court provided, and use general body to include the Events "Free Text" field
        notificationRequest.setNotificationEmail(caseDetails.getData().getTransferLocalCourtEmail());
        notificationRequest.setGeneralEmailBody("The Judge has also ordered that:\n"
            + caseDetails.getData().getTransferLocalCourtInstructions());

        sendNotificationEmail(notificationRequest, uri);
    }

    @Deprecated
    public void sendUpdateFrcInformationEmailToAppSolicitor(CaseDetails caseDetails) {
        sendUpdateFrcInformationEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendUpdateFrcInformationEmailToAppSolicitor(FinremCaseDetails caseDetails) {
        sendUpdateFrcInformationEmail(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    @Deprecated
    public void sendUpdateFrcInformationEmailToRespondentSolicitor(CaseDetails caseDetails) {
        sendUpdateFrcInformationEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    public void sendUpdateFrcInformationEmailToRespondentSolicitor(FinremCaseDetails caseDetails) {
        sendUpdateFrcInformationEmail(finremNotificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    public void sendUpdateFrcInformationEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getUpdateFRCInformation());
        sendNotificationEmail(notificationRequest, uri);
    }

    @Deprecated
    public void sendUpdateFrcInformationEmailToCourt(CaseDetails caseDetails) throws JsonProcessingException {
        String recipientEmail = getRecipientEmail(caseDetails);

        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails);
        notificationRequest.setNotificationEmail(recipientEmail);
        URI uri = buildUri(notificationServiceConfiguration.getUpdateFRCInformationCourt());
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendUpdateFrcInformationEmailToCourt(FinremCaseDetails caseDetails) throws JsonProcessingException {
        String recipientEmail = getRecipientEmail(caseDetails);

        NotificationRequest notificationRequest = finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails);
        notificationRequest.setNotificationEmail(recipientEmail);
        URI uri = buildUri(notificationServiceConfiguration.getUpdateFRCInformationCourt());
        sendNotificationEmail(notificationRequest, uri);
    }

    @Deprecated
    public void sendGeneralApplicationRejectionEmailToAppSolicitor(CaseDetails caseDetails) {
        sendGeneralApplicationRejectionEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendGeneralApplicationRejectionEmailToAppSolicitor(FinremCaseDetails caseDetails) {
        sendGeneralApplicationRejectionEmail(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    @Deprecated
    public void sendGeneralApplicationRejectionEmailToResSolicitor(CaseDetails caseDetails) {
        sendGeneralApplicationRejectionEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    public void sendGeneralApplicationRejectionEmailToResSolicitor(FinremCaseDetails caseDetails) {
        sendGeneralApplicationRejectionEmail(finremNotificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    public void sendGeneralApplicationRejectionEmail(NotificationRequest notificationRequest) {
        URI uri = buildUri(notificationServiceConfiguration.getGeneralApplicationRejection());
        sendNotificationEmail(notificationRequest, uri);
    }

    @Deprecated
    public void sendBarristerAddedEmail(CaseDetails caseDetails, Barrister barrister) {
        URI uri = buildUri(notificationServiceConfiguration.getAddedBarrister());
        NotificationRequest notificationRequest = notificationRequestMapper.buildNotificationRequest(caseDetails, barrister);
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendBarristerAddedEmail(FinremCaseDetails caseDetails, Barrister barrister) {
        URI uri = buildUri(notificationServiceConfiguration.getAddedBarrister());
        NotificationRequest notificationRequest = finremNotificationRequestMapper.buildNotificationRequest(caseDetails, barrister);
        sendNotificationEmail(notificationRequest, uri);
    }

    @Deprecated
    public void sendBarristerRemovedEmail(CaseDetails caseDetails, Barrister barrister) {
        URI uri = buildUri(notificationServiceConfiguration.getRemovedBarrister());
        NotificationRequest notificationRequest = notificationRequestMapper.buildNotificationRequest(caseDetails, barrister);
        sendNotificationEmail(notificationRequest, uri);
    }

    public void sendBarristerRemovedEmail(FinremCaseDetails caseDetails, Barrister barrister) {
        URI uri = buildUri(notificationServiceConfiguration.getRemovedBarrister());
        NotificationRequest notificationRequest = finremNotificationRequestMapper.buildNotificationRequest(caseDetails, barrister);
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

    public boolean isRespondentSolicitorEmailCommunicationEnabled(Map<String, Object> caseData) {
        return !caseDataService.isPaperApplication(caseData)
            && caseDataService.isRespondentRepresentedByASolicitor(caseData)
            && caseDataService.isNotEmpty(RESP_SOLICITOR_EMAIL, caseData)
            && !NO_VALUE.equalsIgnoreCase(nullToEmpty(caseData.get(RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT)));
    }

    public boolean shouldEmailRespondentSolicitor(Map<String, Object> caseData) {
        return caseDataService.isRespondentRepresentedByASolicitor(caseData)
            && caseDataService.isNotEmpty(RESP_SOLICITOR_EMAIL, caseData);
    }

    public boolean isContestedApplicantSolicitorEmailCommunicationEnabled(Map<String, Object> caseData) {
        return !caseDataService.isPaperApplication(caseData)
            && caseDataService.isApplicantRepresentedByASolicitor(caseData)
            && caseDataService.isNotEmpty(CONTESTED_SOLICITOR_EMAIL, caseData)
            && YES_VALUE.equalsIgnoreCase(nullToEmpty(caseData.get(APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED)));
    }

    @Deprecated
    public boolean isApplicantSolicitorDigitalAndEmailPopulated(CaseDetails caseDetails) {
        return caseDataService.isApplicantSolicitorEmailPopulated(caseDetails)
            && checkSolicitorIsDigitalService.isApplicantSolicitorDigital(caseDetails.getId().toString());
    }

    public boolean isApplicantSolicitorDigitalAndEmailPopulated(FinremCaseDetails caseDetails) {
        return caseDetails.getData().isApplicantSolicitorPopulated()
            && checkSolicitorIsDigitalService.isApplicantSolicitorDigital(caseDetails.getId().toString());
    }

    @Deprecated
    public boolean isRespondentSolicitorDigitalAndEmailPopulated(CaseDetails caseDetails) {
        return caseDataService.isNotEmpty(RESP_SOLICITOR_EMAIL, caseDetails.getData())
            && checkSolicitorIsDigitalService.isRespondentSolicitorDigital(caseDetails.getId().toString());
    }

    public boolean isRespondentSolicitorDigitalAndEmailPopulated(FinremCaseDetails caseDetails) {
        return caseDetails.getData().isRespondentSolicitorPopulated()
            && checkSolicitorIsDigitalService.isRespondentSolicitorDigital(caseDetails.getId().toString());
    }

    @Deprecated
    public boolean isRespondentSolicitorRegisteredAndEmailCommunicationEnabled(CaseDetails caseDetails) {
        return shouldEmailRespondentSolicitor(caseDetails.getData())
            && checkSolicitorIsDigitalService.isRespondentSolicitorDigital(caseDetails.getId().toString());
    }

    public boolean isContestedApplicationAndApplicantOrRespondentSolicitorsIsNotRegisteredOrAcceptingEmails(CaseDetails caseDetails) {
        return caseDataService.isContestedPaperApplication(caseDetails)
            && (!isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)
            || !isRespondentSolicitorRegisteredAndEmailCommunicationEnabled(caseDetails));
    }


    public boolean shouldPrintForApplicantSolicitor(CaseDetails caseDetails) {
        return caseDataService.isApplicantRepresentedByASolicitor(caseDetails.getData())
            && !caseDataService.isApplicantSolicitorAgreeToReceiveEmails(caseDetails);
    }

    public boolean shouldPrintForApplicant(CaseDetails caseDetails) {
        return !caseDataService.isApplicantRepresentedByASolicitor(caseDetails.getData());
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

    @Deprecated
    public void sendNoticeOfChangeEmail(CaseDetails caseDetails) {
        URI uri = getNoticeOfChangeUri(caseDetails);
        NotificationRequest notificationRequest = notificationRequestMapper
            .getNotificationRequestForNoticeOfChange(caseDetails);
        sendEmailIfSolicitorIsDigital(caseDetails, notificationRequest, uri);
    }


    public void sendNoticeOfChangeEmail(FinremCaseDetails caseDetails) {
        URI uri = getNoticeOfChangeUri(caseDetails);
        NotificationRequest notificationRequest = finremNotificationRequestMapper
            .getNotificationRequestForNoticeOfChange(caseDetails);
        sendEmailIfSolicitorIsDigital(caseDetails, notificationRequest, uri);
    }

    public void sendNoticeOfChangeEmailCaseworker(CaseDetails caseDetails) {
        URI uri = getNoticeOfChangeUriCaseworker(caseDetails);
        NotificationRequest notificationRequest = notificationRequestMapper
            .getNotificationRequestForNoticeOfChange(caseDetails);
        sendEmailIfSolicitorIsDigital(caseDetails, notificationRequest, uri);
    }

    public void sendNoticeOfChangeEmailCaseworker(FinremCaseDetails caseDetails) {
        URI uri = getNoticeOfChangeUriCaseworker(caseDetails);
        NotificationRequest notificationRequest = finremNotificationRequestMapper
            .getNotificationRequestForNoticeOfChange(caseDetails);
        sendEmailIfSolicitorIsDigital(caseDetails, notificationRequest, uri);
    }

    @Deprecated
    private void sendEmailIfSolicitorIsDigital(
        CaseDetails caseDetails,
        NotificationRequest notificationRequest,
        URI uri) {

        if (isApplicantNoticeOfChangeRequest(notificationRequest, caseDetails)) {
            if (checkSolicitorIsDigitalService.isApplicantSolicitorDigital(caseDetails.getId().toString())) {
                sendNotificationEmail(notificationRequest, uri);
            }
            return;
        }

        if (checkSolicitorIsDigitalService.isRespondentSolicitorDigital(caseDetails.getId().toString())) {
            sendNotificationEmail(notificationRequest, uri);
        }
    }

    private void sendEmailIfSolicitorIsDigital(
        FinremCaseDetails caseDetails,
        NotificationRequest notificationRequest,
        URI uri) {

        if (isApplicantNoticeOfChangeRequest(notificationRequest, caseDetails)) {
            if (checkSolicitorIsDigitalService.isApplicantSolicitorDigital(caseDetails.getId().toString())) {
                sendNotificationEmail(notificationRequest, uri);
            }
            return;
        }

        if (checkSolicitorIsDigitalService.isRespondentSolicitorDigital(caseDetails.getId().toString())) {
            sendNotificationEmail(notificationRequest, uri);
        }
    }

    @Deprecated
    private URI getNoticeOfChangeUri(CaseDetails caseDetails) {
        return buildUri(caseDataService.isConsentedApplication(caseDetails)
            ? notificationServiceConfiguration.getConsentedNoticeOfChange()
            : notificationServiceConfiguration.getContestedNoticeOfChange());
    }

    private URI getNoticeOfChangeUri(FinremCaseDetails caseDetails) {
        return buildUri(caseDetails.getData().isConsentedApplication()
            ? notificationServiceConfiguration.getConsentedNoticeOfChange()
            : notificationServiceConfiguration.getContestedNoticeOfChange());
    }


    @Deprecated
    private URI getNoticeOfChangeUriCaseworker(CaseDetails caseDetails) {
        return buildUri(caseDataService.isConsentedApplication(caseDetails)
            ? notificationServiceConfiguration.getConsentedNoCCaseworker()
            : notificationServiceConfiguration.getContestedNoCCaseworker());

    }

    private URI getNoticeOfChangeUriCaseworker(FinremCaseDetails caseDetails) {
        return buildUri(caseDetails.getData().isConsentedApplication()
            ? notificationServiceConfiguration.getConsentedNoCCaseworker()
            : notificationServiceConfiguration.getContestedNoCCaseworker());

    }

    @Deprecated
    private boolean isApplicantNoticeOfChangeRequest(NotificationRequest notificationRequest,
                                                     CaseDetails caseDetails) {
        return notificationRequest.getName().equalsIgnoreCase(
            nullToEmpty(caseDetails.getData().get(getSolicitorNameKey(caseDetails))));
    }

    private boolean isApplicantNoticeOfChangeRequest(NotificationRequest notificationRequest,
                                                     FinremCaseDetails caseDetails) {
        return notificationRequest.getName().equalsIgnoreCase(
            nullToEmpty(caseDetails.getData().isConsentedApplication() ? caseDetails.getData().getContactDetailsWrapper().getSolicitorName()
                : caseDetails.getData().getContactDetailsWrapper().getApplicantSolicitorName()));
    }

    @Deprecated
    private String getSolicitorNameKey(CaseDetails caseDetails) {
        return caseDataService.isConsentedApplication(caseDetails)
            ? CONSENTED_SOLICITOR_NAME
            : CONTESTED_SOLICITOR_NAME;
    }

    @Deprecated
    private String getRecipientEmail(CaseDetails caseDetails) throws JsonProcessingException {
        if (featureToggleService.isSendToFRCEnabled()) {
            Map<String, Object> data = caseDetails.getData();
            Map<String, Object> courtDetailsMap = objectMapper.readValue(getCourtDetailsString(), HashMap.class);
            Map<String, Object> courtDetails = (Map<String, Object>) courtDetailsMap.get(data.get(CaseHearingFunctions.getSelectedCourt(data)));

            return (String) courtDetails.get(COURT_DETAILS_EMAIL_KEY);
        }
        return DEFAULT_EMAIL;
    }


    private String getRecipientEmail(FinremCaseDetails caseDetails) throws JsonProcessingException {
        if (featureToggleService.isSendToFRCEnabled()) {
            Map<String, Object> courtDetailsMap = objectMapper.readValue(getCourtDetailsString(), HashMap.class);
            Map<String, Object> courtDetails = (Map<String, Object>) courtDetailsMap.get(caseDetails.getData().getSelectedCourt());

            return (String) courtDetails.get(COURT_DETAILS_EMAIL_KEY);
        }
        return DEFAULT_EMAIL;
    }
}
