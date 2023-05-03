package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.NotificationServiceConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremNotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.NotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckSolicitorIsDigitalService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

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
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_ASSIGNED_TO_JUDGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_BARRISTER_ACCESS_ADDED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_BARRISTER_ACCESS_REMOVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENTED_GENERAL_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENTED_LIST_FOR_HEARING;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENTED_NOC_CASEWORKER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENTED_NOTICE_OF_CHANGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENT_GENERAL_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENT_ORDER_AVAILABLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENT_ORDER_AVAILABLE_CTSC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENT_ORDER_MADE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENT_ORDER_NOT_APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENT_ORDER_NOT_APPROVED_SENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_APPLICATION_ISSUED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_CONSENT_ORDER_APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_CONSENT_ORDER_NOT_APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_DRAFT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_GENERAL_APPLICATION_OUTCOME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_GENERAL_APPLICATION_REFER_TO_JUDGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_GENERAL_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_GENERAL_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_GENERAL_ORDER_CONSENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_HWF_SUCCESSFUL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_INTERIM_HEARING;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_NOC_CASEWORKER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_NOTICE_OF_CHANGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_PREPARE_FOR_HEARING;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_PREPARE_FOR_HEARING_ORDER_SENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_UPDATE_FRC_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_UPDATE_FRC_SOL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTEST_ORDER_APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTEST_ORDER_NOT_APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_HWF_SUCCESSFUL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_REJECT_GENERAL_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_TRANSFER_TO_LOCAL_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.getCourtDetailsString;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final EmailService emailService;
    private static final String DEFAULT_EMAIL = "fr_applicant_solicitor1@mailinator.com";
    private final NotificationServiceConfiguration notificationServiceConfiguration;
    private final FeatureToggleService featureToggleService;
    private final ObjectMapper objectMapper;
    private final NotificationRequestMapper notificationRequestMapper;
    private final FinremNotificationRequestMapper finremNotificationRequestMapper;
    private final CaseDataService caseDataService;
    private final CheckSolicitorIsDigitalService checkSolicitorIsDigitalService;

    @Deprecated
    public void sendConsentedHWFSuccessfulConfirmationEmail(CaseDetails caseDetails) {
        NotificationRequest notificationRequest =
            notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails);
        log.info("Received request for notification email for HWFSuccessful. Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        sendNotificationEmail(notificationRequest, FR_HWF_SUCCESSFUL);
    }

    public void sendConsentedHWFSuccessfulConfirmationEmail(FinremCaseDetails caseDetails) {
        NotificationRequest notificationRequest =
            finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails);
        log.info("Received request for notification email for HWFSuccessful. Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        sendNotificationEmail(notificationRequest, FR_HWF_SUCCESSFUL);

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
        log.info("Received request for notification email for Case assigned to Judge Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_ASSIGNED_TO_JUDGE);
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
        log.info("Received request for notification email for consent order made. Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONSENT_ORDER_MADE);
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
        log.info("Received request for notification email for consent order not approved, Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONSENT_ORDER_NOT_APPROVED);

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
        log.info("Received request for notification email for consent order available Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONSENT_ORDER_AVAILABLE);
    }

    @Deprecated
    public void sendConsentOrderAvailableCtscEmail(CaseDetails caseDetails) {
        NotificationRequest ctscNotificationRequest = notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails);
        ctscNotificationRequest.setNotificationEmail(notificationServiceConfiguration.getCtscEmail());
        log.info("Received request for notification email for CTSC consent order available Case ID : {}",
            ctscNotificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(ctscNotificationRequest, FR_CONSENT_ORDER_AVAILABLE_CTSC);
    }

    public void sendConsentOrderAvailableCtscEmail(FinremCaseDetails caseDetails) {
        NotificationRequest ctscNotificationRequest = finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails);
        ctscNotificationRequest.setNotificationEmail(notificationServiceConfiguration.getCtscEmail());
        log.info("Received request for notification email for CTSC consent order available Case ID : {}",
            ctscNotificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(ctscNotificationRequest, FR_CONSENT_ORDER_AVAILABLE_CTSC);
    }

    @Deprecated
    public void sendContestedHwfSuccessfulConfirmationEmail(CaseDetails caseDetails) {
        NotificationRequest notificationRequest =
            notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails);
        log.info("Received request for notification email for HWFSuccessful. Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_HWF_SUCCESSFUL);
    }

    public void sendContestedHwfSuccessfulConfirmationEmail(FinremCaseDetails caseDetails) {
        NotificationRequest notificationRequest =
            finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails);
        log.info("Received request for notification email for HWFSuccessful. Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_HWF_SUCCESSFUL);
    }

    @Deprecated
    public void sendContestedApplicationIssuedEmailToApplicantSolicitor(CaseDetails caseDetails) {
        sendContestedApplicationIssuedEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendContestedApplicationIssuedEmailToApplicantSolicitor(FinremCaseDetails caseDetails) {
        sendContestedApplicationIssuedEmail(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    private void sendContestedApplicationIssuedEmail(NotificationRequest notificationRequest) {
        log.info("Received request for notification email for Contested 'Application Issued'. Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_APPLICATION_ISSUED);
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
        log.info("Received request for notification email for 'Contest Order Approved'. Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTEST_ORDER_APPROVED);
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
        log.info("Received request for notification email for 'Prepare for hearing'. Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_PREPARE_FOR_HEARING);
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
        log.info("Received request for notification email for 'Prepare for hearing order sent'. Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_PREPARE_FOR_HEARING_ORDER_SENT);
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
        log.info("Received request for notification email for Contested 'Draft Order'. Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_DRAFT_ORDER);
    }

    @Deprecated
    public void sendConsentGeneralEmail(CaseDetails caseDetails) {
        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails);
        notificationRequest.setNotificationEmail(Objects.toString(caseDetails.getData().get(GENERAL_EMAIL_RECIPIENT)));
        log.info("Received request for notification email for consented general email Notification request : {}",
            notificationRequest);
        emailService.sendConfirmationEmail(notificationRequest, FR_CONSENT_GENERAL_EMAIL);
    }

    public void sendConsentGeneralEmail(FinremCaseDetails caseDetails) {
        NotificationRequest notificationRequest = finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails);
        notificationRequest.setNotificationEmail(caseDetails.getData().getGeneralEmailRecipient());
        log.info("Received request for notification email for consented general email Notification request : {}",
            notificationRequest);
        emailService.sendConfirmationEmail(notificationRequest, FR_CONSENT_GENERAL_EMAIL);
    }

    @Deprecated
    public void sendContestedGeneralEmail(CaseDetails caseDetails) {
        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails);
        notificationRequest.setNotificationEmail(Objects.toString(caseDetails.getData().get(GENERAL_EMAIL_RECIPIENT)));
        log.info("Received request for notification email for contested general email Notification request : {}",
            notificationRequest);
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_GENERAL_EMAIL);
    }


    public void sendContestedGeneralEmail(FinremCaseDetails caseDetails) {
        NotificationRequest notificationRequest = finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails);
        notificationRequest.setNotificationEmail(Objects.toString(caseDetails.getData().getGeneralEmailRecipient()));
        log.info("Received request for notification email for contested general email Notification request : {}",
            notificationRequest);
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_GENERAL_EMAIL);
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
        log.info("Received request for notification email for contest order not approved, Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTEST_ORDER_NOT_APPROVED);
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
        log.info("Received request for notification email for consent order approved, Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_CONSENT_ORDER_APPROVED);
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
        log.info("Received request for notification email for Consent Order Not Approved, Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_CONSENT_ORDER_NOT_APPROVED);
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
        log.info("Received request for notification email for Contested general order (consent), Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_GENERAL_ORDER_CONSENT);
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
        log.info("Received request for notification email for Consented general order, Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONSENTED_GENERAL_ORDER);
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
        log.info("Received request for notification email for Contested general order, Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_GENERAL_ORDER);
    }

    @Deprecated
    public void sendContestedGeneralApplicationReferToJudgeEmail(CaseDetails caseDetails) {
        NotificationRequest judgeNotificationRequest = notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails);
        judgeNotificationRequest.setNotificationEmail(Objects.toString(caseDetails.getData().get(GENERAL_APPLICATION_REFER_TO_JUDGE_EMAIL)));
        if (caseDetails.getData().get(GENERAL_APPLICATION_REFERRED_DETAIL) != null) {
            judgeNotificationRequest.setGeneralEmailBody(Objects.toString(caseDetails.getData().get(GENERAL_APPLICATION_REFERRED_DETAIL)));
        }
        log.info("Received request for notification email for Contested general application refer to judge, Case ID : {}",
            judgeNotificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(judgeNotificationRequest, FR_CONTESTED_GENERAL_APPLICATION_REFER_TO_JUDGE);
    }

    public void sendContestedGeneralApplicationReferToJudgeEmail(FinremCaseDetails caseDetails) {
        NotificationRequest judgeNotificationRequest = finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails);
        judgeNotificationRequest.setNotificationEmail(caseDetails.getData().getGeneralApplicationWrapper().getGeneralApplicationReferToJudgeEmail());
        if (caseDetails.getData().getGeneralApplicationWrapper().getGeneralApplicationReferDetail() != null) {
            judgeNotificationRequest.setGeneralEmailBody(
                caseDetails.getData().getGeneralApplicationWrapper().getGeneralApplicationReferDetail());
        }
        log.info("Received request for notification email for Contested general application refer to judge, Case ID : {}",
            judgeNotificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(judgeNotificationRequest, FR_CONTESTED_GENERAL_APPLICATION_REFER_TO_JUDGE);
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
        log.info("Received request for notification email for Contested General Application Outcome, Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_GENERAL_APPLICATION_OUTCOME);
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
        log.info("Received request for notification email for Contested General Application Outcome, Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_GENERAL_APPLICATION_OUTCOME);
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

    @Deprecated
    public void sendConsentHearingNotificationEmailToApplicantSolicitor(CaseDetails caseDetails,
                                                                        Map<String, Object> hearingData) {
        sendConsentedHearingNotificationEmail(notificationRequestMapper.getNotificationRequestForConsentApplicantSolicitor(caseDetails,
            hearingData));
    }

    public void sendConsentHearingNotificationEmailToApplicantSolicitor(FinremCaseDetails caseDetails,
                                                                        Map<String, Object> hearingData) {
        sendConsentedHearingNotificationEmail(notificationRequestMapper.getNotificationRequestForConsentApplicantSolicitor(caseDetails,
            hearingData));
    }

    private void sendConsentedHearingNotificationEmail(NotificationRequest notificationRequest) {
        log.info("Received request for notification email for 'hearing'. Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONSENTED_LIST_FOR_HEARING);
    }

    @Deprecated
    public void sendConsentHearingNotificationEmailToRespondentSolicitor(CaseDetails caseDetails,
                                                                         Map<String, Object> hearingData) {
        sendConsentedHearingNotificationEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails,
            hearingData));
    }

    public void sendConsentHearingNotificationEmailToRespondentSolicitor(FinremCaseDetails caseDetails,
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
        log.info("Received request for notification email for 'Prepare for interim hearing sent'. Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_INTERIM_HEARING);
    }

    private void sendConsentOrderNotApprovedSentEmail(NotificationRequest notificationRequest) {
        log.info("Received request for notification email for consent order not approved sent, Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONSENT_ORDER_NOT_APPROVED_SENT);
    }

    @Deprecated
    public void sendTransferToLocalCourtEmail(CaseDetails caseDetails) {
        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails);
        //Overwrite the email, set to the court provided, and use general body to include the Events "Free Text" field
        notificationRequest.setNotificationEmail(Objects.toString(caseDetails.getData().get(TRANSFER_COURTS_EMAIL)));
        notificationRequest.setGeneralEmailBody("The Judge has also ordered that:\n"
            + Objects.toString(caseDetails.getData().get(TRANSFER_COURTS_INSTRUCTIONS)));

        log.info("Received request for notification email for consented transfer to local court email Notification request : {}",
            notificationRequest);
        emailService.sendConfirmationEmail(notificationRequest, FR_TRANSFER_TO_LOCAL_COURT);
    }

    public void sendTransferToLocalCourtEmail(FinremCaseDetails caseDetails) {
        NotificationRequest notificationRequest = finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails);
        //Overwrite the email, set to the court provided, and use general body to include the Events "Free Text" field
        notificationRequest.setNotificationEmail(caseDetails.getData().getTransferLocalCourtEmail());
        notificationRequest.setGeneralEmailBody("The Judge has also ordered that:\n"
            + caseDetails.getData().getTransferLocalCourtInstructions());
        log.info("Received request for notification email for consented transfer to local court email Notification request : {}",
            notificationRequest);
        emailService.sendConfirmationEmail(notificationRequest, FR_TRANSFER_TO_LOCAL_COURT);
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
        log.info("Received request for notification email for 'Update FRC Information event'");
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_UPDATE_FRC_SOL);
    }

    @Deprecated
    public void sendUpdateFrcInformationEmailToCourt(CaseDetails caseDetails) throws JsonProcessingException {
        String recipientEmail = getRecipientEmail(caseDetails);

        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails);
        notificationRequest.setNotificationEmail(recipientEmail);
        log.info("Received request for notification email to court for 'Update FRC Information event'");
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_UPDATE_FRC_COURT);
    }

    public void sendUpdateFrcInformationEmailToCourt(FinremCaseDetails caseDetails) throws JsonProcessingException {
        String recipientEmail = getRecipientEmail(caseDetails);

        NotificationRequest notificationRequest = finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails);
        notificationRequest.setNotificationEmail(recipientEmail);
        log.info("Received request for notification email to court for 'Update FRC Information event'");
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_UPDATE_FRC_COURT);
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
        log.info("Received request for notification email for General Application Rejected event. Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_REJECT_GENERAL_APPLICATION);
    }

    @Deprecated
    public void sendBarristerAddedEmail(CaseDetails caseDetails, Barrister barrister) {
        NotificationRequest notificationRequest = notificationRequestMapper.buildInterimHearingNotificationRequest(caseDetails, barrister);
        log.info("Received request for notification email for Barrister Access Added event. Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_BARRISTER_ACCESS_ADDED);
    }

    public void sendBarristerAddedEmail(FinremCaseDetails caseDetails, Barrister barrister) {
        NotificationRequest notificationRequest = finremNotificationRequestMapper.buildNotificationRequest(caseDetails, barrister);
        log.info("Received request for notification email for Barrister Access Added event. Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_BARRISTER_ACCESS_ADDED);
    }

    @Deprecated
    public void sendBarristerRemovedEmail(CaseDetails caseDetails, Barrister barrister) {
        NotificationRequest notificationRequest = notificationRequestMapper.buildInterimHearingNotificationRequest(caseDetails, barrister);
        log.info("Received request for notification email for Barrister Access Added event. Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_BARRISTER_ACCESS_REMOVED);
    }

    public void sendBarristerRemovedEmail(FinremCaseDetails caseDetails, Barrister barrister) {
        NotificationRequest notificationRequest = finremNotificationRequestMapper.buildNotificationRequest(caseDetails, barrister);
        log.info("Received request for notification email for Barrister Access Added event. Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_BARRISTER_ACCESS_REMOVED);
    }

    private void sendNotificationEmail(NotificationRequest notificationRequest, EmailTemplateNames emailTemplateName) {
        emailService.sendConfirmationEmail(notificationRequest, emailTemplateName);
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

    public boolean isApplicantSolicitorDigitalAndEmailPopulated(CaseDetails caseDetails) {
        return caseDataService.isApplicantSolicitorEmailPopulated(caseDetails)
            && checkSolicitorIsDigitalService.isApplicantSolicitorDigital(caseDetails.getId().toString());
    }

    public boolean isApplicantSolicitorDigitalAndEmailPopulated(FinremCaseDetails caseDetails) {
        return caseDetails.getData().isApplicantSolicitorPopulated()
            && checkSolicitorIsDigitalService.isApplicantSolicitorDigital(caseDetails.getId().toString());
    }

    public boolean isRespondentSolicitorDigitalAndEmailPopulated(CaseDetails caseDetails) {
        return caseDataService.isNotEmpty(RESP_SOLICITOR_EMAIL, caseDetails.getData())
            && checkSolicitorIsDigitalService.isRespondentSolicitorDigital(caseDetails.getId().toString());
    }

    public boolean isRespondentSolicitorDigitalAndEmailPopulated(FinremCaseDetails caseDetails) {
        return caseDetails.getData().isRespondentSolicitorPopulated()
            && checkSolicitorIsDigitalService.isRespondentSolicitorDigital(caseDetails.getId().toString());
    }

    public boolean isIntervenerOneSolicitorDigitalAndEmailPopulated(FinremCaseDetails caseDetails) {
        return caseDetails.getData().isIntervenerSolOnePopulated()
            && checkSolicitorIsDigitalService.isIntervenerSolicitorDigital(caseDetails.getId().toString(),
            CaseRole.INTVR_SOLICITOR_1.getValue());
    }

    public boolean isIntervenerTwoSolicitorDigitalAndEmailPopulated(FinremCaseDetails caseDetails) {
        return caseDetails.getData().isIntervenerSolTwoPopulated()
            && checkSolicitorIsDigitalService.isIntervenerSolicitorDigital(caseDetails.getId().toString(),
            CaseRole.INTVR_SOLICITOR_2.getValue());
    }

    public boolean isIntervenerThreeSolicitorDigitalAndEmailPopulated(FinremCaseDetails caseDetails) {
        return caseDetails.getData().isIntervenerSolThreePopulated()
            && checkSolicitorIsDigitalService.isIntervenerSolicitorDigital(caseDetails.getId().toString(),
            CaseRole.INTVR_SOLICITOR_3.getValue());
    }

    public boolean isIntervenerFourSolicitorDigitalAndEmailPopulated(FinremCaseDetails caseDetails) {
        return caseDetails.getData().isIntervenerSolFourPopulated()
            && checkSolicitorIsDigitalService.isIntervenerSolicitorDigital(caseDetails.getId().toString(),
            CaseRole.INTVR_SOLICITOR_4.getValue());
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

    @Deprecated
    public void sendNoticeOfChangeEmail(CaseDetails caseDetails) {
        EmailTemplateNames template = getNoticeOfChangeTemplate(caseDetails);
        NotificationRequest notificationRequest = notificationRequestMapper
            .getNotificationRequestForNoticeOfChange(caseDetails);
        sendEmailIfSolicitorIsDigital(caseDetails, notificationRequest, template);
    }


    public void sendNoticeOfChangeEmail(FinremCaseDetails caseDetails) {
        EmailTemplateNames template = getNoticeOfChangeTemplate(caseDetails);
        NotificationRequest notificationRequest = finremNotificationRequestMapper
            .getNotificationRequestForNoticeOfChange(caseDetails);
        sendEmailIfSolicitorIsDigital(caseDetails, notificationRequest, template);
    }

    public void sendNoticeOfChangeEmailCaseworker(CaseDetails caseDetails) {
        EmailTemplateNames template = getNoticeOfChangeTemplateCaseworker(caseDetails);
        NotificationRequest notificationRequest = notificationRequestMapper
            .getNotificationRequestForNoticeOfChange(caseDetails);
        sendEmailIfSolicitorIsDigital(caseDetails, notificationRequest, template);
    }

    public void sendNoticeOfChangeEmailCaseworker(FinremCaseDetails caseDetails) {
        EmailTemplateNames template = getNoticeOfChangeTemplateCaseworker(caseDetails);
        NotificationRequest notificationRequest = finremNotificationRequestMapper
            .getNotificationRequestForNoticeOfChange(caseDetails);
        sendEmailIfSolicitorIsDigital(caseDetails, notificationRequest, template);
    }

    @Deprecated
    private void sendEmailIfSolicitorIsDigital(
        CaseDetails caseDetails,
        NotificationRequest notificationRequest,
        EmailTemplateNames template) {

        if (isApplicantNoticeOfChangeRequest(notificationRequest, caseDetails)) {
            if (checkSolicitorIsDigitalService.isApplicantSolicitorDigital(caseDetails.getId().toString())) {
                sendNotificationEmail(notificationRequest, template);
            }
            return;
        }

        if (checkSolicitorIsDigitalService.isRespondentSolicitorDigital(caseDetails.getId().toString())) {
            sendNotificationEmail(notificationRequest, template);
        }
    }

    private void sendEmailIfSolicitorIsDigital(
        FinremCaseDetails caseDetails,
        NotificationRequest notificationRequest,
        EmailTemplateNames template) {

        if (isApplicantNoticeOfChangeRequest(notificationRequest, caseDetails)) {
            if (checkSolicitorIsDigitalService.isApplicantSolicitorDigital(caseDetails.getId().toString())) {
                sendNotificationEmail(notificationRequest, template);
            }
            return;
        }

        if (checkSolicitorIsDigitalService.isRespondentSolicitorDigital(caseDetails.getId().toString())) {
            sendNotificationEmail(notificationRequest, template);
        }
    }

    @Deprecated
    private EmailTemplateNames getNoticeOfChangeTemplate(CaseDetails caseDetails) {
        return caseDataService.isConsentedApplication(caseDetails)
            ? FR_CONSENTED_NOTICE_OF_CHANGE
            : FR_CONTESTED_NOTICE_OF_CHANGE;
    }

    private EmailTemplateNames getNoticeOfChangeTemplate(FinremCaseDetails caseDetails) {
        return caseDetails.getData().isConsentedApplication()
            ? FR_CONSENTED_NOTICE_OF_CHANGE
            : FR_CONTESTED_NOTICE_OF_CHANGE;
    }


    @Deprecated
    private EmailTemplateNames getNoticeOfChangeTemplateCaseworker(CaseDetails caseDetails) {
        return caseDataService.isConsentedApplication(caseDetails)
            ? FR_CONSENTED_NOC_CASEWORKER
            : FR_CONTESTED_NOC_CASEWORKER;

    }

    private EmailTemplateNames getNoticeOfChangeTemplateCaseworker(FinremCaseDetails caseDetails) {
        return caseDetails.getData().isConsentedApplication()
            ? FR_CONSENTED_NOC_CASEWORKER
            : FR_CONTESTED_NOC_CASEWORKER;

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
