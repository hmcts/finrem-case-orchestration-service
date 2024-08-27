package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.NotificationServiceConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremNotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.NotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDownloadService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckSolicitorIsDigitalService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENT_GENERAL_EMAIL_ATTACHMENT;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_GENERAL_EMAIL_ATTACHMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_GENERAL_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_GENERAL_ORDER_CONSENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_HWF_SUCCESSFUL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_INTERIM_HEARING;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_NOC_CASEWORKER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_NOTICE_OF_CHANGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_PREPARE_FOR_HEARING;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_PREPARE_FOR_HEARING_INTERVENER_SOL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_PREPARE_FOR_HEARING_ORDER_SENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_UPDATE_FRC_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_UPDATE_FRC_SOL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTEST_ORDER_APPROVED_APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTEST_ORDER_APPROVED_INTERVENER1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTEST_ORDER_APPROVED_INTERVENER2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTEST_ORDER_APPROVED_INTERVENER3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTEST_ORDER_APPROVED_INTERVENER4;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTEST_ORDER_APPROVED_RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTEST_ORDER_NOT_APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_HWF_SUCCESSFUL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_INTERVENER_ADDED_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_INTERVENER_REMOVED_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_INTERVENER_SOLICITOR_ADDED_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_INTERVENER_SOLICITOR_REMOVED_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_REJECT_GENERAL_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_TRANSFER_TO_LOCAL_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.getCourtDetailsString;

@Service
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("java:S1133")
public class NotificationService {

    private final EmailService emailService;
    private static final String DEFAULT_EMAIL = "fr_applicant_solicitor1@mailinator.com";
    private static final String HWF_LOG = "Received request for notification email for HWFSuccessful. Case ID : {}";
    private static final String BARRISTER_ACCESS_LOG = "Received request for notification email for Barrister Access Added event. Case ID : {}";
    private final NotificationServiceConfiguration notificationServiceConfiguration;
    private final FeatureToggleService featureToggleService;
    private final ObjectMapper objectMapper;
    private final NotificationRequestMapper notificationRequestMapper;
    private final FinremNotificationRequestMapper finremNotificationRequestMapper;
    private final CaseDataService caseDataService;
    private final CheckSolicitorIsDigitalService checkSolicitorIsDigitalService;
    private final EvidenceManagementDownloadService evidenceManagementDownloadService;

    /**
     * No Return.
     * <p>Please use @{@link #sendConsentedHWFSuccessfulConfirmationEmail(FinremCaseDetails)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @deprecated Use {@link CaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
    public void sendConsentedHWFSuccessfulConfirmationEmail(CaseDetails caseDetails) {
        NotificationRequest notificationRequest =
            notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails);
        log.info(HWF_LOG, notificationRequest.getCaseReferenceNumber());
        sendNotificationEmail(notificationRequest, FR_HWF_SUCCESSFUL);
    }

    public void sendConsentedHWFSuccessfulConfirmationEmail(FinremCaseDetails caseDetails) {
        NotificationRequest notificationRequest =
            finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails);
        log.info(HWF_LOG, notificationRequest.getCaseReferenceNumber());
        sendNotificationEmail(notificationRequest, FR_HWF_SUCCESSFUL);

    }

    /**
     * No Return.
     * <p>Please use @{@link #sendAssignToJudgeConfirmationEmailToApplicantSolicitor(FinremCaseDetails)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @deprecated Use {@link CaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
    public void sendAssignToJudgeConfirmationEmailToApplicantSolicitor(CaseDetails caseDetails) {
        sendAssignToJudgeConfirmationEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendAssignToJudgeConfirmationEmailToApplicantSolicitor(FinremCaseDetails caseDetails) {
        sendAssignToJudgeConfirmationEmail(finremNotificationRequestMapper
            .getNotificationRequestForApplicantSolicitor(caseDetails, !isApplicantSolicitorDigital(caseDetails)));
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendAssignToJudgeConfirmationEmailToRespondentSolicitor(FinremCaseDetails)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @deprecated Use {@link CaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
    public void sendAssignToJudgeConfirmationEmailToRespondentSolicitor(CaseDetails caseDetails) {
        sendAssignToJudgeConfirmationEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    public void sendAssignToJudgeConfirmationEmailToRespondentSolicitor(FinremCaseDetails finremCaseDetails) {
        NotificationRequest notificationRequestForRespondentSolicitor =
            finremNotificationRequestMapper
                .getNotificationRequestForRespondentSolicitor(finremCaseDetails, !isRespondentSolicitorDigital(finremCaseDetails));
        sendAssignToJudgeConfirmationEmail(notificationRequestForRespondentSolicitor);
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendAssignToJudgeConfirmationEmailToIntervenerSolicitor(FinremCaseDetails, SolicitorCaseDataKeysWrapper)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @param dataKeysWrapper instance of SolicitorCaseDataKeysWrapper
     * @deprecated Use {@link CaseDetails caseDetails, SolicitorCaseDataKeysWrapper dataKeysWrapper}
     */
    @Deprecated(since = "15-june-2023")
    public void sendAssignToJudgeConfirmationEmailToIntervenerSolicitor(CaseDetails caseDetails,
                                                                        SolicitorCaseDataKeysWrapper dataKeysWrapper) {
        sendAssignToJudgeConfirmationEmail(notificationRequestMapper.getNotificationRequestForIntervenerSolicitor(caseDetails, dataKeysWrapper));
    }

    public void sendAssignToJudgeConfirmationEmailToIntervenerSolicitor(FinremCaseDetails finremCaseDetails,
                                                                        SolicitorCaseDataKeysWrapper dataKeysWrapper) {
        NotificationRequest notificationRequestForRespondentSolicitor =
            finremNotificationRequestMapper.getNotificationRequestForIntervenerSolicitor(finremCaseDetails, dataKeysWrapper);
        sendAssignToJudgeConfirmationEmail(notificationRequestForRespondentSolicitor);
    }

    private void sendAssignToJudgeConfirmationEmail(NotificationRequest notificationRequest) {
        log.info("Received request for notification email for Case assigned to Judge Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_ASSIGNED_TO_JUDGE);
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendConsentOrderMadeConfirmationEmailToApplicantSolicitor(FinremCaseDetails)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @deprecated Use {@link CaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
    public void sendConsentOrderMadeConfirmationEmailToApplicantSolicitor(CaseDetails caseDetails) {
        sendConsentOrderMadeConfirmationEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendConsentOrderMadeConfirmationEmailToApplicantSolicitor(FinremCaseDetails caseDetails) {
        sendConsentOrderMadeConfirmationEmail(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendConsentOrderMadeConfirmationEmailToRespondentSolicitor(FinremCaseDetails)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @deprecated Use {@link CaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
    public void sendConsentOrderMadeConfirmationEmailToRespondentSolicitor(CaseDetails caseDetails) {
        sendConsentOrderMadeConfirmationEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    public void sendConsentOrderMadeConfirmationEmailToRespondentSolicitor(FinremCaseDetails caseDetails) {
        NotificationRequest notificationRequestForRespondentSolicitor = finremNotificationRequestMapper
            .getNotificationRequestForRespondentSolicitor(caseDetails);
        sendConsentOrderMadeConfirmationEmail(notificationRequestForRespondentSolicitor);
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendConsentOrderMadeConfirmationEmailToIntervenerSolicitor(FinremCaseDetails, SolicitorCaseDataKeysWrapper)}</p>
     *
     * @param caseDetails     instance of CaseDetails
     * @param dataKeysWrapper instance of SolicitorCaseDataKeysWrapper
     * @deprecated Use {@link CaseDetails caseDetails, SolicitorCaseDataKeysWrapper dataKeysWrapper}
     */
    @Deprecated(since = "15-june-2023")
    public void sendConsentOrderMadeConfirmationEmailToIntervenerSolicitor(CaseDetails caseDetails,
                                                                           SolicitorCaseDataKeysWrapper dataKeysWrapper) {
        sendConsentOrderMadeConfirmationEmail(notificationRequestMapper.getNotificationRequestForIntervenerSolicitor(caseDetails,
            dataKeysWrapper));
    }

    public void sendConsentOrderMadeConfirmationEmailToIntervenerSolicitor(FinremCaseDetails caseDetails,
                                                                           SolicitorCaseDataKeysWrapper caseDataKeysWrapper) {
        NotificationRequest notificationRequestForRespondentSolicitor = finremNotificationRequestMapper
            .getNotificationRequestForIntervenerSolicitor(caseDetails, caseDataKeysWrapper);
        sendConsentOrderMadeConfirmationEmail(notificationRequestForRespondentSolicitor);
    }

    public void sendConsentOrderMadeConfirmationEmail(NotificationRequest notificationRequest) {
        log.info("Received request for notification email for consent order made. Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONSENT_ORDER_MADE);
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendConsentOrderNotApprovedEmailToApplicantSolicitor(FinremCaseDetails)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @deprecated Use {@link CaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
    public void sendConsentOrderNotApprovedEmailToApplicantSolicitor(CaseDetails caseDetails) {
        sendConsentOrderNotApprovedEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendConsentOrderNotApprovedEmailToApplicantSolicitor(FinremCaseDetails caseDetails) {
        sendConsentOrderNotApprovedEmail(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendConsentOrderNotApprovedEmailToRespondentSolicitor(FinremCaseDetails)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @deprecated Use {@link CaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
    public void sendConsentOrderNotApprovedEmailToRespondentSolicitor(CaseDetails caseDetails) {
        sendConsentOrderNotApprovedEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    public void sendConsentOrderNotApprovedEmailToRespondentSolicitor(FinremCaseDetails caseDetails) {
        NotificationRequest notificationRequestForRespondentSolicitor = finremNotificationRequestMapper
            .getNotificationRequestForRespondentSolicitor(caseDetails);
        sendConsentOrderNotApprovedEmail(notificationRequestForRespondentSolicitor);
    }

    private void sendConsentOrderNotApprovedEmail(NotificationRequest notificationRequest) {
        log.info("Received request for notification email for consent order not approved, Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONSENT_ORDER_NOT_APPROVED);

    }

    /**
     * No Return.
     * <p>Please use @{@link #sendConsentOrderAvailableEmailToApplicantSolicitor(FinremCaseDetails)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @deprecated Use {@link CaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
    public void sendConsentOrderAvailableEmailToApplicantSolicitor(CaseDetails caseDetails) {
        sendConsentOrderAvailableEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendConsentOrderAvailableEmailToApplicantSolicitor(FinremCaseDetails caseDetails) {
        sendConsentOrderAvailableEmail(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendConsentOrderAvailableEmailToRespondentSolicitor(FinremCaseDetails)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @deprecated Use {@link CaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
    public void sendConsentOrderAvailableEmailToRespondentSolicitor(CaseDetails caseDetails) {
        sendConsentOrderAvailableEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    public void sendConsentOrderAvailableEmailToRespondentSolicitor(FinremCaseDetails caseDetails) {
        NotificationRequest notificationRequestForRespondentSolicitor = finremNotificationRequestMapper
            .getNotificationRequestForRespondentSolicitor(caseDetails);
        sendConsentOrderAvailableEmail(notificationRequestForRespondentSolicitor);
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendConsentOrderAvailableEmailToIntervenerSolicitor(FinremCaseDetails, SolicitorCaseDataKeysWrapper)}</p>
     *
     * @param caseDetails     instance of CaseDetails
     * @param dataKeysWrapper instance of SolicitorCaseDataKeysWrapper
     * @deprecated Use {@link CaseDetails caseDetails, SolicitorCaseDataKeysWrapper dataKeysWrapper}
     */
    @Deprecated(since = "15-june-2023")
    public void sendConsentOrderAvailableEmailToIntervenerSolicitor(CaseDetails caseDetails,
                                                                    SolicitorCaseDataKeysWrapper dataKeysWrapper) {
        sendConsentOrderAvailableEmail(notificationRequestMapper.getNotificationRequestForIntervenerSolicitor(caseDetails,
            dataKeysWrapper));
    }

    public void sendConsentOrderAvailableEmailToIntervenerSolicitor(FinremCaseDetails caseDetails,
                                                                    SolicitorCaseDataKeysWrapper caseDataKeysWrapper) {
        NotificationRequest notificationRequestForRespondentSolicitor = finremNotificationRequestMapper
            .getNotificationRequestForIntervenerSolicitor(caseDetails, caseDataKeysWrapper);
        sendConsentOrderAvailableEmail(notificationRequestForRespondentSolicitor);
    }

    private void sendConsentOrderAvailableEmail(NotificationRequest notificationRequest) {
        log.info("Received request for notification email for consent order available Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONSENT_ORDER_AVAILABLE);
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendConsentOrderAvailableCtscEmail(FinremCaseDetails)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @deprecated Use {@link CaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
    public void sendConsentOrderAvailableCtscEmail(CaseDetails caseDetails) {
        NotificationRequest ctscNotificationRequest = notificationRequestMapper
            .getNotificationRequestForApplicantSolicitor(caseDetails);
        ctscNotificationRequest.setNotificationEmail(notificationServiceConfiguration.getCtscEmail());
        log.info("Received request for notification email for CTSC consent order available Case ID : {}",
            ctscNotificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(ctscNotificationRequest, FR_CONSENT_ORDER_AVAILABLE_CTSC);
    }

    public void sendConsentOrderAvailableCtscEmail(FinremCaseDetails caseDetails) {
        NotificationRequest ctscNotificationRequest = finremNotificationRequestMapper
            .getNotificationRequestForApplicantSolicitor(caseDetails);
        ctscNotificationRequest.setNotificationEmail(notificationServiceConfiguration.getCtscEmail());
        log.info("Received request for notification email for CTSC consent order available Case ID : {}",
            ctscNotificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(ctscNotificationRequest, FR_CONSENT_ORDER_AVAILABLE_CTSC);
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendContestedHwfSuccessfulConfirmationEmail(FinremCaseDetails)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @deprecated Use {@link CaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
    public void sendContestedHwfSuccessfulConfirmationEmail(CaseDetails caseDetails) {
        NotificationRequest notificationRequest =
            notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails);
        log.info(HWF_LOG, notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_HWF_SUCCESSFUL);
    }

    public void sendContestedHwfSuccessfulConfirmationEmail(FinremCaseDetails caseDetails) {
        NotificationRequest notificationRequest =
            finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails);
        log.info(HWF_LOG, notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_HWF_SUCCESSFUL);
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendContestedApplicationIssuedEmailToApplicantSolicitor(FinremCaseDetails)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @deprecated Use {@link CaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
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
        log.info("Sending notification email to Applicant for 'Contest Order Approved'. Case ID : {}", caseDetails.getId());
        sendContestOrderApprovedEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails),
            FR_CONTEST_ORDER_APPROVED_APPLICANT);
    }

    public void sendContestOrderApprovedEmailApplicant(FinremCaseDetails caseDetails) {
        log.info("Sending notification email to Applicant for 'Contest Order Approved'. Case ID : {}", caseDetails.getId());
        emailService.sendConfirmationEmail(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails),
            FR_CONTEST_ORDER_APPROVED_APPLICANT);
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendContestOrderApprovedEmailRespondent(FinremCaseDetails)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @deprecated Use {@link CaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
    public void sendContestOrderApprovedEmailRespondent(CaseDetails caseDetails) {
        log.info("Sending notification email to Respondent for 'Contest Order Approved'. Case ID : {}",
            caseDetails.getId());
        sendContestOrderApprovedEmail(
            notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails),
            FR_CONTEST_ORDER_APPROVED_RESPONDENT);
    }

    public void sendContestOrderApprovedEmailRespondent(FinremCaseDetails caseDetails) {
        log.info("Sending notification email to Respondent for 'Contest Order Approved'. Case ID : {}",
            caseDetails.getId());
        sendContestOrderApprovedEmail(
            finremNotificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails),
            FR_CONTEST_ORDER_APPROVED_RESPONDENT);
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendContestOrderApprovedEmailIntervener(FinremCaseDetails, SolicitorCaseDataKeysWrapper, IntervenerType)}</p>
     *
     * @param caseDetails         instance of CaseDetails
     * @param caseDataKeysWrapper instance of SolicitorCaseDataKeysWrapper
     * @deprecated Use {@link CaseDetails caseDetails, SolicitorCaseDataKeysWrapper dataKeysWrapper}
     */
    @Deprecated(since = "15-june-2023")
    public void sendContestOrderApprovedEmailIntervener(CaseDetails caseDetails,
                                                        SolicitorCaseDataKeysWrapper caseDataKeysWrapper,
                                                        IntervenerType intervener) {
        log.info("Sending notification email to {} for 'Contest Order Approved'. Case ID : {}",
            intervener, caseDetails.getId());
        NotificationRequest request = notificationRequestMapper.getNotificationRequestForIntervenerSolicitor(
            caseDetails, caseDataKeysWrapper);
        EmailTemplateNames template = getIntervenerSendOrderContestedTemplate(intervener);
        sendContestOrderApprovedEmail(request, template);
    }

    public void sendContestOrderApprovedEmailIntervener(FinremCaseDetails caseDetails,
                                                        SolicitorCaseDataKeysWrapper caseDataKeysWrapper,
                                                        IntervenerType intervener) {
        log.info("Sending notification email to {} for 'Contest Order Approved'. Case ID : {}",
            intervener, caseDetails.getId());
        NotificationRequest request = finremNotificationRequestMapper.getNotificationRequestForIntervenerSolicitor(
            caseDetails, caseDataKeysWrapper);
        EmailTemplateNames template = getIntervenerSendOrderContestedTemplate(intervener);
        sendContestOrderApprovedEmail(request, template);
    }

    public void sendContestOrderApprovedEmail(NotificationRequest notificationRequest, EmailTemplateNames template) {
        log.info("Received request for notification email for 'Contest Order Approved'. Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, template);
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendPrepareForHearingEmailApplicant(FinremCaseDetails)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @deprecated Use {@link CaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
    public void sendPrepareForHearingEmailApplicant(CaseDetails caseDetails) {
        sendPrepareForHearingEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendPrepareForHearingEmailApplicant(FinremCaseDetails caseDetails) {
        sendPrepareForHearingEmail(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendPrepareForHearingEmailRespondent(FinremCaseDetails)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @deprecated Use {@link CaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
    public void sendPrepareForHearingEmailRespondent(CaseDetails caseDetails) {
        sendPrepareForHearingEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    public void sendPrepareForHearingEmailRespondent(FinremCaseDetails caseDetails) {
        sendPrepareForHearingEmail(finremNotificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendPrepareForHearingEmailIntervener(FinremCaseDetails, SolicitorCaseDataKeysWrapper)}</p>
     *
     * @param caseDetails     instance of CaseDetails
     * @param dataKeysWrapper instance of SolicitorCaseDataKeysWrapper
     * @deprecated Use {@link CaseDetails caseDetails, SolicitorCaseDataKeysWrapper dataKeysWrapper}
     */
    @Deprecated(since = "15-june-2023")
    public void sendPrepareForHearingEmailIntervener(CaseDetails caseDetails,
                                                     SolicitorCaseDataKeysWrapper dataKeysWrapper) {

        NotificationRequest notificationRequest =
            notificationRequestMapper.getNotificationRequestForIntervenerSolicitor(caseDetails,
            dataKeysWrapper);
        log.info("Received request to send notification email to intervener for 'List for hearing'. Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_PREPARE_FOR_HEARING_INTERVENER_SOL);
    }

    public void sendPrepareForHearingEmailIntervener(FinremCaseDetails caseDetails,
                                                     SolicitorCaseDataKeysWrapper dataKeysWrapper) {
        NotificationRequest notificationRequestForIntervenerSolicitor = finremNotificationRequestMapper
            .getNotificationRequestForIntervenerSolicitor(caseDetails, dataKeysWrapper);
        log.info("Received request to send notification email to intervener for 'List for hearing'. Case ID : {}",
            notificationRequestForIntervenerSolicitor.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequestForIntervenerSolicitor,
            FR_CONTESTED_PREPARE_FOR_HEARING_INTERVENER_SOL);
    }

    private void sendPrepareForHearingEmail(NotificationRequest notificationRequest) {
        log.info("Received request for notification email for 'Prepare for hearing'. Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_PREPARE_FOR_HEARING);
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendPrepareForHearingOrderSentEmailApplicant(FinremCaseDetails)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @deprecated Use {@link CaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
    public void sendPrepareForHearingOrderSentEmailApplicant(CaseDetails caseDetails) {
        sendPrepareForHearingOrderSentEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendPrepareForHearingOrderSentEmailApplicant(FinremCaseDetails caseDetails) {
        sendPrepareForHearingOrderSentEmail(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendPrepareForHearingOrderSentEmailRespondent(FinremCaseDetails)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @deprecated Use {@link CaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
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

    /**
     * No Return.
     * <p>Please use @{@link #sendSolicitorToDraftOrderEmailApplicant(FinremCaseDetails)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @deprecated Use {@link FinremCaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
    public void sendSolicitorToDraftOrderEmailApplicant(CaseDetails caseDetails) {
        sendSolicitorToDraftOrderEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendSolicitorToDraftOrderEmailApplicant(FinremCaseDetails caseDetails) {
        sendSolicitorToDraftOrderEmail(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendSolicitorToDraftOrderEmailRespondent(FinremCaseDetails)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @deprecated Use {@link FinremCaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
    public void sendSolicitorToDraftOrderEmailRespondent(CaseDetails caseDetails) {
        sendSolicitorToDraftOrderEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    public void sendSolicitorToDraftOrderEmailRespondent(FinremCaseDetails caseDetails) {
        sendSolicitorToDraftOrderEmail(finremNotificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendSolicitorToDraftOrderEmailIntervener(FinremCaseDetails, SolicitorCaseDataKeysWrapper)}</p>
     *
     * @param caseDetails     instance of CaseDetails
     * @param dataKeysWrapper of SolicitorCaseDataKeysWrapper
     * @deprecated Use {@link CaseDetails caseDetails, SolicitorCaseDataKeysWrapper dataKeysWrapper}
     */
    @Deprecated(since = "15-june-2023")
    public void sendSolicitorToDraftOrderEmailIntervener(CaseDetails caseDetails,
                                                         SolicitorCaseDataKeysWrapper dataKeysWrapper) {
        sendSolicitorToDraftOrderEmail(notificationRequestMapper.getNotificationRequestForIntervenerSolicitor(caseDetails, dataKeysWrapper));
    }

    public void sendSolicitorToDraftOrderEmailIntervener(FinremCaseDetails caseDetails,
                                                         SolicitorCaseDataKeysWrapper dataKeysWrapper) {
        sendSolicitorToDraftOrderEmail(finremNotificationRequestMapper.getNotificationRequestForIntervenerSolicitor(caseDetails, dataKeysWrapper));
    }

    private void sendSolicitorToDraftOrderEmail(NotificationRequest notificationRequest) {
        log.info("Received request for notification email for Contested 'Draft Order'. Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_DRAFT_ORDER);
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendConsentGeneralEmail(FinremCaseDetails, String)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @deprecated Use {@link CaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
    public void sendConsentGeneralEmail(CaseDetails caseDetails) {
        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails);
        notificationRequest.setNotificationEmail(Objects.toString(caseDetails.getData().get(GENERAL_EMAIL_RECIPIENT)));
        log.info("Received request for notification email for consented general email Notification request : {}",
            notificationRequest);
        emailService.sendConfirmationEmail(notificationRequest, FR_CONSENT_GENERAL_EMAIL);
    }

    public void sendConsentGeneralEmail(FinremCaseDetails caseDetails, String auth) {
        NotificationRequest notificationRequest = finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails);
        notificationRequest.setNotificationEmail(caseDetails.getData().getGeneralEmailWrapper().getGeneralEmailRecipient());
        final boolean hasAttachment = downloadGeneralEmailUploadedDocument(caseDetails, notificationRequest, auth);
        log.info("Received request for notification email for consented general email Notification request : {}",
            notificationRequest);
        final EmailTemplateNames templateName = (hasAttachment) ? FR_CONSENT_GENERAL_EMAIL_ATTACHMENT : FR_CONSENT_GENERAL_EMAIL;
        emailService.sendConfirmationEmail(notificationRequest, templateName);

    }

    private boolean downloadGeneralEmailUploadedDocument(FinremCaseDetails caseDetails,
                                                         NotificationRequest notificationRequest,
                                                         String auth) {
        CaseDocument caseDocument = caseDetails.getData().getGeneralEmailWrapper().getGeneralEmailUploadedDocument();
        if (caseDocument != null) {
            ResponseEntity<Resource> response = evidenceManagementDownloadService.downloadInResponseEntity(caseDocument.getDocumentBinaryUrl(),
                auth);
            if (response.getStatusCode() != HttpStatus.OK) {
                log.error("Download failed for url {}, filename {} and Case ID: {}", caseDocument.getDocumentBinaryUrl(),
                    caseDocument.getDocumentFilename(), caseDetails.getId());
                throw new HttpClientErrorException(response.getStatusCode());
            }
            ByteArrayResource resource = (ByteArrayResource) response.getBody();
            notificationRequest.setDocumentContents((resource != null) ? resource.getByteArray() : new byte[0]);
            return true;
        }
        return false;
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendContestedGeneralEmail(FinremCaseDetails, String)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @deprecated Use {@link CaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
    public void sendContestedGeneralEmail(CaseDetails caseDetails) {
        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails);
        notificationRequest.setNotificationEmail(Objects.toString(caseDetails.getData().get(GENERAL_EMAIL_RECIPIENT)));
        log.info("Received request for notification email for contested general email Notification request : {}",
            notificationRequest);
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_GENERAL_EMAIL);
    }

    public void sendContestedGeneralEmail(FinremCaseDetails caseDetails, String auth) {
        NotificationRequest notificationRequest = finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails);
        notificationRequest.setNotificationEmail(caseDetails.getData().getGeneralEmailWrapper().getGeneralEmailRecipient());
        final boolean hasAttachment = downloadGeneralEmailUploadedDocument(caseDetails, notificationRequest, auth);
        log.info("Received request for notification email for contested general email Notification request : {}",
            notificationRequest);
        final EmailTemplateNames templateName = (hasAttachment) ? FR_CONTESTED_GENERAL_EMAIL_ATTACHMENT : FR_CONTESTED_GENERAL_EMAIL;
        emailService.sendConfirmationEmail(notificationRequest, templateName);
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendContestOrderNotApprovedEmailRespondent(FinremCaseDetails)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @deprecated Use {@link CaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
    public void sendContestOrderNotApprovedEmailApplicant(CaseDetails caseDetails) {
        sendContestOrderNotApprovedEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendContestOrderNotApprovedEmailApplicant(FinremCaseDetails caseDetails) {
        sendContestOrderNotApprovedEmail(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendContestOrderNotApprovedEmailRespondent(FinremCaseDetails)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @deprecated Use {@link FinremCaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
    public void sendContestOrderNotApprovedEmailRespondent(CaseDetails caseDetails) {
        sendContestOrderNotApprovedEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    public void sendContestOrderNotApprovedEmailRespondent(FinremCaseDetails caseDetails) {
        sendContestOrderNotApprovedEmail(finremNotificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendContestOrderNotApprovedEmailIntervener(FinremCaseDetails, SolicitorCaseDataKeysWrapper)}</p>
     *
     * @param caseDetails         instance of CaseDetails
     * @param caseDataKeysWrapper instance of SolicitorCaseDataKeysWrapper
     * @deprecated Use {@link CaseDetails caseDetails, SolicitorCaseDataKeysWrapper caseDataKeysWrapper}
     */
    @Deprecated(since = "15-june-2023")
    public void sendContestOrderNotApprovedEmailIntervener(CaseDetails caseDetails,
                                                           SolicitorCaseDataKeysWrapper caseDataKeysWrapper) {
        sendContestOrderNotApprovedEmail(notificationRequestMapper.getNotificationRequestForIntervenerSolicitor(caseDetails,
            caseDataKeysWrapper));
    }

    public void sendContestOrderNotApprovedEmailIntervener(FinremCaseDetails caseDetails,
                                                           SolicitorCaseDataKeysWrapper caseDataKeysWrapper) {
        sendContestOrderNotApprovedEmail(finremNotificationRequestMapper.getNotificationRequestForIntervenerSolicitor(caseDetails,
            caseDataKeysWrapper));
    }

    private void sendContestOrderNotApprovedEmail(NotificationRequest notificationRequest) {
        log.info("Received request for notification email for contest order not approved, Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTEST_ORDER_NOT_APPROVED);
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendContestedConsentOrderApprovedEmailToApplicantSolicitor(FinremCaseDetails)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @deprecated Use {@link CaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
    public void sendContestedConsentOrderApprovedEmailToApplicantSolicitor(CaseDetails caseDetails) {
        sendContestedConsentOrderApprovedEmailToSolicitor(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendContestedConsentOrderApprovedEmailToApplicantSolicitor(FinremCaseDetails caseDetails) {
        sendContestedConsentOrderApprovedEmailToSolicitor(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendContestedConsentOrderApprovedEmailToRespondentSolicitor(FinremCaseDetails)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @deprecated Use {@link CaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
    public void sendContestedConsentOrderApprovedEmailToRespondentSolicitor(CaseDetails caseDetails) {
        sendContestedConsentOrderApprovedEmailToSolicitor(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    public void sendContestedConsentOrderApprovedEmailToRespondentSolicitor(FinremCaseDetails caseDetails) {
        sendContestedConsentOrderApprovedEmailToSolicitor(finremNotificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendContestedConsentOrderApprovedEmailToIntervenerSolicitor(FinremCaseDetails, SolicitorCaseDataKeysWrapper)}</p>
     *
     * @param caseDetails         instance of CaseDetails
     * @param caseDataKeysWrapper instance of SolicitorCaseDataKeysWrapper
     * @deprecated Use {@link CaseDetails caseDetails, SolicitorCaseDataKeysWrapper caseDataKeysWrapper}
     */
    @Deprecated(since = "15-june-2023")
    public void sendContestedConsentOrderApprovedEmailToIntervenerSolicitor(CaseDetails caseDetails,
                                                                            SolicitorCaseDataKeysWrapper caseDataKeysWrapper) {
        sendContestedConsentOrderApprovedEmailToSolicitor(notificationRequestMapper.getNotificationRequestForIntervenerSolicitor(caseDetails,
            caseDataKeysWrapper));
    }

    public void sendContestedConsentOrderApprovedEmailToIntervenerSolicitor(FinremCaseDetails caseDetails,
                                                                            SolicitorCaseDataKeysWrapper caseDataKeysWrapper) {
        sendContestedConsentOrderApprovedEmailToSolicitor(finremNotificationRequestMapper.getNotificationRequestForIntervenerSolicitor(caseDetails,
            caseDataKeysWrapper));
    }

    private void sendContestedConsentOrderApprovedEmailToSolicitor(NotificationRequest notificationRequest) {
        log.info("Received request for notification email for consent order approved, Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_CONSENT_ORDER_APPROVED);
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendContestedConsentOrderNotApprovedEmailRespondentSolicitor(FinremCaseDetails)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @deprecated Use {@link CaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
    public void sendContestedConsentOrderNotApprovedEmailApplicantSolicitor(CaseDetails caseDetails) {
        sendContestedConsentOrderNotApprovedEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendContestedConsentOrderNotApprovedEmailApplicantSolicitor(FinremCaseDetails caseDetails) {
        sendContestedConsentOrderNotApprovedEmail(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendContestedConsentOrderNotApprovedEmailRespondentSolicitor(FinremCaseDetails)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @deprecated Use {@link CaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
    public void sendContestedConsentOrderNotApprovedEmailRespondentSolicitor(CaseDetails caseDetails) {
        sendContestedConsentOrderNotApprovedEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    public void sendContestedConsentOrderNotApprovedEmailRespondentSolicitor(FinremCaseDetails caseDetails) {
        sendContestedConsentOrderNotApprovedEmail(finremNotificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendContestedConsentOrderNotApprovedEmailIntervenerSolicitor(FinremCaseDetails, SolicitorCaseDataKeysWrapper)}</p>
     *
     * @param caseDetails         instance of CaseDetails
     * @param caseDataKeysWrapper instance of SolicitorCaseDataKeysWrapper
     * @deprecated Use {@link CaseDetails caseDetails, SolicitorCaseDataKeysWrapper caseDataKeysWrapper}
     */
    @Deprecated(since = "15-june-2023")
    public void sendContestedConsentOrderNotApprovedEmailIntervenerSolicitor(CaseDetails caseDetails,
                                                                             SolicitorCaseDataKeysWrapper caseDataKeysWrapper) {
        sendContestedConsentOrderNotApprovedEmail(notificationRequestMapper.getNotificationRequestForIntervenerSolicitor(caseDetails,
            caseDataKeysWrapper));
    }

    public void sendContestedConsentOrderNotApprovedEmailIntervenerSolicitor(FinremCaseDetails caseDetails,
                                                                             SolicitorCaseDataKeysWrapper caseDataKeysWrapper) {
        sendContestedConsentOrderNotApprovedEmail(finremNotificationRequestMapper.getNotificationRequestForIntervenerSolicitor(caseDetails,
            caseDataKeysWrapper));
    }

    private void sendContestedConsentOrderNotApprovedEmail(NotificationRequest notificationRequest) {
        log.info("Received request for notification email for Consent Order Not Approved, Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_CONSENT_ORDER_NOT_APPROVED);
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendContestedConsentGeneralOrderEmailApplicantSolicitor(FinremCaseDetails)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @deprecated Use {@link CaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
    public void sendContestedConsentGeneralOrderEmailApplicantSolicitor(CaseDetails caseDetails) {
        sendContestedConsentGeneralOrderEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendContestedConsentGeneralOrderEmailApplicantSolicitor(FinremCaseDetails caseDetails) {
        sendContestedConsentGeneralOrderEmail(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendContestedConsentGeneralOrderEmailRespondentSolicitor(FinremCaseDetails)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @deprecated Use {@link CaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
    public void sendContestedConsentGeneralOrderEmailRespondentSolicitor(CaseDetails caseDetails) {
        sendContestedConsentGeneralOrderEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    public void sendContestedConsentGeneralOrderEmailRespondentSolicitor(FinremCaseDetails caseDetails) {
        sendContestedConsentGeneralOrderEmail(finremNotificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendContestedConsentGeneralOrderEmailIntervenerSolicitor(FinremCaseDetails, SolicitorCaseDataKeysWrapper)}</p>
     *
     * @param caseDetails         instance of CaseDetails
     * @param caseDataKeysWrapper instance of SolicitorCaseDataKeysWrapper
     * @deprecated Use {@link CaseDetails caseDetails, SolicitorCaseDataKeysWrapper caseDataKeysWrapper}
     */
    @Deprecated(since = "15-june-2023")
    public void sendContestedConsentGeneralOrderEmailIntervenerSolicitor(CaseDetails caseDetails,
                                                                         SolicitorCaseDataKeysWrapper caseDataKeysWrapper) {
        sendContestedConsentGeneralOrderEmail(notificationRequestMapper.getNotificationRequestForIntervenerSolicitor(caseDetails,
            caseDataKeysWrapper));
    }

    public void sendContestedConsentGeneralOrderEmailIntervenerSolicitor(FinremCaseDetails caseDetails,
                                                                         SolicitorCaseDataKeysWrapper caseDataKeysWrapper) {
        sendContestedConsentGeneralOrderEmail(finremNotificationRequestMapper.getNotificationRequestForIntervenerSolicitor(caseDetails,
            caseDataKeysWrapper));
    }

    private void sendContestedConsentGeneralOrderEmail(NotificationRequest notificationRequest) {
        log.info("Received request for notification email for Contested general order (consent), Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_GENERAL_ORDER_CONSENT);
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendConsentedGeneralOrderEmailToApplicantSolicitor(FinremCaseDetails)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @deprecated Use {@link CaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
    public void sendConsentedGeneralOrderEmailToApplicantSolicitor(CaseDetails caseDetails) {
        sendConsentedGeneralOrderEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendConsentedGeneralOrderEmailToApplicantSolicitor(FinremCaseDetails caseDetails) {
        sendConsentedGeneralOrderEmail(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendConsentedGeneralOrderEmailToRespondentSolicitor(FinremCaseDetails)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @deprecated Use {@link CaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
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

    /**
     * No Return.
     * <p>Please use @{@link #sendContestedGeneralOrderEmailApplicant(FinremCaseDetails)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @deprecated Use {@link CaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
    public void sendContestedGeneralOrderEmailApplicant(CaseDetails caseDetails) {
        sendContestedGeneralOrderEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendContestedGeneralOrderEmailApplicant(FinremCaseDetails caseDetails) {
        sendContestedGeneralOrderEmail(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendContestedGeneralOrderEmailRespondent(FinremCaseDetails)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @deprecated Use {@link FinremCaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
    public void sendContestedGeneralOrderEmailRespondent(CaseDetails caseDetails) {
        sendContestedGeneralOrderEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    public void sendContestedGeneralOrderEmailRespondent(FinremCaseDetails caseDetails) {
        sendContestedGeneralOrderEmail(finremNotificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendContestedGeneralOrderEmailIntervener(FinremCaseDetails, SolicitorCaseDataKeysWrapper)}</p>
     *
     * @param caseDetails         instance of CaseDetails
     * @param caseDataKeysWrapper instance of SolicitorCaseDataKeysWrapper
     * @deprecated Use {@link CaseDetails caseDetails, SolicitorCaseDataKeysWrapper caseDataKeysWrapper}
     */
    @Deprecated(since = "15-june-2023")
    public void sendContestedGeneralOrderEmailIntervener(CaseDetails caseDetails,
                                                         SolicitorCaseDataKeysWrapper caseDataKeysWrapper) {
        sendContestedGeneralOrderEmail(notificationRequestMapper.getNotificationRequestForIntervenerSolicitor(caseDetails,
            caseDataKeysWrapper));
    }

    public void sendContestedGeneralOrderEmailIntervener(FinremCaseDetails caseDetails,
                                                         SolicitorCaseDataKeysWrapper caseDataKeysWrapper) {
        sendContestedGeneralOrderEmail(finremNotificationRequestMapper.getNotificationRequestForIntervenerSolicitor(caseDetails,
            caseDataKeysWrapper));
    }

    private void sendContestedGeneralOrderEmail(NotificationRequest notificationRequest) {
        log.info("Received request for notification email for Contested general order, Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_GENERAL_ORDER);
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendContestedGeneralApplicationReferToJudgeEmail(FinremCaseDetails)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @deprecated Use {@link CaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
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

    /**
     * No Return.
     * <p>Please use @{@link #sendContestedGeneralApplicationOutcomeEmail(FinremCaseDetails)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @deprecated Use {@link CaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
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
            Map<String, Object> courtDetails = (Map<String, Object>) courtDetailsMap.get(caseDetails.getData().getSelectedAllocatedCourt());
            recipientEmail = (String) courtDetails.get(COURT_DETAILS_EMAIL_KEY);
        }

        NotificationRequest notificationRequest = finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails);
        notificationRequest.setNotificationEmail(recipientEmail);
        log.info("Received request for notification email for Contested General Application Outcome, Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_GENERAL_APPLICATION_OUTCOME);
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendConsentOrderNotApprovedSentEmailToApplicantSolicitor(FinremCaseDetails)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @deprecated Use {@link CaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
    public void sendConsentOrderNotApprovedSentEmailToApplicantSolicitor(CaseDetails caseDetails) {
        sendConsentOrderNotApprovedSentEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendConsentOrderNotApprovedSentEmailToApplicantSolicitor(FinremCaseDetails caseDetails) {
        sendConsentOrderNotApprovedSentEmail(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendConsentOrderNotApprovedSentEmailToRespondentSolicitor(FinremCaseDetails)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @deprecated Use {@link CaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
    public void sendConsentOrderNotApprovedSentEmailToRespondentSolicitor(CaseDetails caseDetails) {
        sendConsentOrderNotApprovedSentEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    public void sendConsentOrderNotApprovedSentEmailToRespondentSolicitor(FinremCaseDetails caseDetails) {
        sendConsentOrderNotApprovedSentEmail(finremNotificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendConsentOrderNotApprovedSentEmailToIntervenerSolicitor(FinremCaseDetails, SolicitorCaseDataKeysWrapper)}</p>
     *
     * @param caseDetails         instance of CaseDetails
     * @param caseDataKeysWrapper instance of SolicitorCaseDataKeysWrapper
     * @deprecated Use {@link CaseDetails caseDetails, SolicitorCaseDataKeysWrapper caseDataKeysWrapper}
     */
    @Deprecated(since = "15-june-2023")
    public void sendConsentOrderNotApprovedSentEmailToIntervenerSolicitor(CaseDetails caseDetails,
                                                                          SolicitorCaseDataKeysWrapper caseDataKeysWrapper) {
        sendConsentOrderNotApprovedSentEmail(notificationRequestMapper.getNotificationRequestForIntervenerSolicitor(caseDetails,
            caseDataKeysWrapper));
    }

    public void sendConsentOrderNotApprovedSentEmailToIntervenerSolicitor(FinremCaseDetails caseDetails,
                                                                          SolicitorCaseDataKeysWrapper caseDataKeysWrapper) {
        sendConsentOrderNotApprovedSentEmail(finremNotificationRequestMapper.getNotificationRequestForIntervenerSolicitor(caseDetails,
            caseDataKeysWrapper));
    }

    public void sendInterimHearingNotificationEmailToApplicantSolicitor(FinremCaseDetails caseDetails,
                                                                        Map<String, Object> interimHearingData) {
        sendInterimNotificationEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails,
            interimHearingData));
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendConsentHearingNotificationEmailToApplicantSolicitor(FinremCaseDetails, Map)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @param hearingData instance of Map
     * @deprecated Use {@link CaseDetails caseDetails, Map hearingData}
     */
    @Deprecated(since = "15-june-2023")
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

    /**
     * No Return.
     * <p>Please use @{@link #sendConsentHearingNotificationEmailToRespondentSolicitor(FinremCaseDetails, Map)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @param hearingData instance of Map
     * @deprecated Use {@link CaseDetails caseDetails, Map hearingData}
     */
    @Deprecated(since = "15-june-2023")
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

    /**
     * No Return.
     * <p>Please use @{@link #sendInterimNotificationEmailToApplicantSolicitor(FinremCaseDetails)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @deprecated Use {@link CaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
    public void sendInterimNotificationEmailToApplicantSolicitor(CaseDetails caseDetails) {
        sendInterimNotificationEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendInterimNotificationEmailToApplicantSolicitor(FinremCaseDetails caseDetails) {
        sendInterimNotificationEmail(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    @SuppressWarnings("squid:CallToDeprecatedMethod")
    public void sendInterimHearingNotificationEmailToRespondentSolicitor(FinremCaseDetails caseDetails,
                                                                         Map<String, Object> interimHearingData) {
        sendInterimNotificationEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails,
            interimHearingData));
    }

    public void sendInterimHearingNotificationEmailToIntervenerSolicitor(FinremCaseDetails caseDetails,
                                                                         Map<String, Object> interimHearingData,
                                                                         SolicitorCaseDataKeysWrapper dataKeysWrapper) {
        sendInterimNotificationEmail(notificationRequestMapper.getNotificationRequestForIntervenerSolicitor(caseDetails,
            interimHearingData, dataKeysWrapper));
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendInterimNotificationEmailToRespondentSolicitor(FinremCaseDetails)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @deprecated Use {@link CaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
    public void sendInterimNotificationEmailToRespondentSolicitor(CaseDetails caseDetails) {
        sendInterimNotificationEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    public void sendInterimNotificationEmailToRespondentSolicitor(FinremCaseDetails caseDetails) {
        sendInterimNotificationEmail(finremNotificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendInterimNotificationEmailToIntervenerSolicitor(FinremCaseDetails, SolicitorCaseDataKeysWrapper)}</p>
     *
     * @param caseDetails     instance of CaseDetails
     * @param dataKeysWrapper instance of SolicitorCaseDataKeysWrapper
     * @deprecated Use {@link CaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
    public void sendInterimNotificationEmailToIntervenerSolicitor(CaseDetails caseDetails,
                                                                  SolicitorCaseDataKeysWrapper dataKeysWrapper) {
        sendInterimNotificationEmail(notificationRequestMapper.getNotificationRequestForIntervenerSolicitor(caseDetails, dataKeysWrapper));
    }

    public void sendInterimNotificationEmailToIntervenerSolicitor(FinremCaseDetails caseDetails,
                                                                  SolicitorCaseDataKeysWrapper dataKeysWrapper) {
        sendInterimNotificationEmail(finremNotificationRequestMapper.getNotificationRequestForIntervenerSolicitor(caseDetails, dataKeysWrapper));
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

    /**
     * No Return.
     * <p>Please use @{@link #sendTransferToLocalCourtEmail(FinremCaseDetails)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @deprecated Use {@link CaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
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

    /**
     * No Return.
     * <p>Please use @{@link #sendUpdateFrcInformationEmailToAppSolicitor(FinremCaseDetails)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @deprecated Use {@link CaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
    public void sendUpdateFrcInformationEmailToAppSolicitor(CaseDetails caseDetails) {
        sendUpdateFrcInformationEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendUpdateFrcInformationEmailToAppSolicitor(FinremCaseDetails caseDetails) {
        sendUpdateFrcInformationEmail(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendUpdateFrcInformationEmailToRespondentSolicitor(FinremCaseDetails)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @deprecated Use {@link CaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
    public void sendUpdateFrcInformationEmailToRespondentSolicitor(CaseDetails caseDetails) {
        sendUpdateFrcInformationEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    public void sendUpdateFrcInformationEmailToRespondentSolicitor(FinremCaseDetails caseDetails) {
        sendUpdateFrcInformationEmail(finremNotificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendUpdateFrcInformationEmailToIntervenerSolicitor(FinremCaseDetails, SolicitorCaseDataKeysWrapper)}</p>
     *
     * @param caseDetails     instance of CaseDetails
     * @param dataKeysWrapper instance of SolicitorCaseDataKeysWrapper
     * @deprecated Use {@link CaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
    public void sendUpdateFrcInformationEmailToIntervenerSolicitor(CaseDetails caseDetails,
                                                                   SolicitorCaseDataKeysWrapper dataKeysWrapper) {
        sendUpdateFrcInformationEmail(notificationRequestMapper.getNotificationRequestForIntervenerSolicitor(caseDetails, dataKeysWrapper));
    }

    public void sendUpdateFrcInformationEmailToIntervenerSolicitor(FinremCaseDetails caseDetails,
                                                                   SolicitorCaseDataKeysWrapper dataKeysWrapper) {
        sendUpdateFrcInformationEmail(finremNotificationRequestMapper.getNotificationRequestForIntervenerSolicitor(caseDetails, dataKeysWrapper));
    }

    public void sendUpdateFrcInformationEmail(NotificationRequest notificationRequest) {
        log.info("Received request for notification email for 'Update FRC Information event'");
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_UPDATE_FRC_SOL);
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendUpdateFrcInformationEmailToCourt(FinremCaseDetails)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @deprecated Use {@link CaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
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

    /**
     * No Return.
     * <p>Please use @{@link #sendGeneralApplicationRejectionEmailToAppSolicitor(FinremCaseDetails)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @deprecated Use {@link CaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
    public void sendGeneralApplicationRejectionEmailToAppSolicitor(CaseDetails caseDetails) {
        sendGeneralApplicationRejectionEmail(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    public void sendGeneralApplicationRejectionEmailToAppSolicitor(FinremCaseDetails caseDetails) {
        sendGeneralApplicationRejectionEmail(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails));
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendGeneralApplicationRejectionEmailToResSolicitor(FinremCaseDetails)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @deprecated Use {@link CaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
    public void sendGeneralApplicationRejectionEmailToResSolicitor(CaseDetails caseDetails) {
        sendGeneralApplicationRejectionEmail(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    public void sendGeneralApplicationRejectionEmailToResSolicitor(FinremCaseDetails caseDetails) {
        sendGeneralApplicationRejectionEmail(finremNotificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails));
    }

    public void sendGeneralApplicationRejectionEmailToIntervenerSolicitor(FinremCaseDetails caseDetails, IntervenerWrapper intervenerWrapper) {
        sendGeneralApplicationRejectionEmail(finremNotificationRequestMapper.getNotificationRequestForIntervenerSolicitor(caseDetails,
            getCaseDataKeysForIntervenerSolicitor(intervenerWrapper)));
    }

    public void sendGeneralApplicationRejectionEmail(NotificationRequest notificationRequest) {
        log.info("Received request for notification email for General Application Rejected event. Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_REJECT_GENERAL_APPLICATION);
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendBarristerRemovedEmail(FinremCaseDetails, Barrister)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @param barrister   instance of Barrister
     * @deprecated Use {@link CaseDetails caseDetails, Barrister barrister}
     */
    @Deprecated(since = "15-june-2023")
    public void sendBarristerAddedEmail(CaseDetails caseDetails, Barrister barrister) {
        NotificationRequest notificationRequest = notificationRequestMapper.buildInterimHearingNotificationRequest(caseDetails, barrister);
        log.info(BARRISTER_ACCESS_LOG, notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_BARRISTER_ACCESS_ADDED);
    }

    public void sendBarristerAddedEmail(FinremCaseDetails caseDetails, Barrister barrister) {
        NotificationRequest notificationRequest = finremNotificationRequestMapper.buildNotificationRequest(caseDetails, barrister);
        log.info(BARRISTER_ACCESS_LOG, notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_BARRISTER_ACCESS_ADDED);
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendBarristerRemovedEmail(FinremCaseDetails, Barrister)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @param barrister   instance of Barrister
     * @deprecated Use {@link CaseDetails caseDetails, Barrister barrister}
     */
    @Deprecated(since = "15-june-2023")
    public void sendBarristerRemovedEmail(CaseDetails caseDetails, Barrister barrister) {
        NotificationRequest notificationRequest = notificationRequestMapper.buildInterimHearingNotificationRequest(caseDetails, barrister);
        log.info(BARRISTER_ACCESS_LOG, notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_BARRISTER_ACCESS_REMOVED);
    }

    public void sendBarristerRemovedEmail(FinremCaseDetails caseDetails, Barrister barrister) {
        NotificationRequest notificationRequest = finremNotificationRequestMapper.buildNotificationRequest(caseDetails, barrister);
        log.info(BARRISTER_ACCESS_LOG, notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_BARRISTER_ACCESS_REMOVED);
    }

    public void sendIntervenerAddedEmail(FinremCaseDetails caseDetails, IntervenerDetails intervenerDetails,
                                         String recipientName, String recipientEmail, String referenceNumber) {
        NotificationRequest notificationRequest = finremNotificationRequestMapper.buildNotificationRequest(
            caseDetails, intervenerDetails, recipientName, recipientEmail, referenceNumber);
        log.info("Received request for notification email for Intervener Added event. Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_INTERVENER_ADDED_EMAIL);
    }

    public void sendIntervenerSolicitorAddedEmail(FinremCaseDetails caseDetails, IntervenerDetails intervenerDetails,
                                                  String recipientName, String recipientEmail, String referenceNumber) {
        NotificationRequest notificationRequest = finremNotificationRequestMapper.buildNotificationRequest(
            caseDetails, intervenerDetails, recipientName, recipientEmail, referenceNumber);
        log.info("Received request for notification email for Intervener Solicitor Added event. Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_INTERVENER_SOLICITOR_ADDED_EMAIL);
    }

    public void sendIntervenerRemovedEmail(FinremCaseDetails caseDetails, IntervenerDetails intervenerDetails,
                                           String recipientName, String recipientEmail, String referenceNumber) {
        NotificationRequest notificationRequest = finremNotificationRequestMapper.buildNotificationRequest(
            caseDetails, intervenerDetails, recipientName, recipientEmail, referenceNumber);
        log.info("Received request for notification email for Intervener Removed event. Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_INTERVENER_REMOVED_EMAIL);
    }

    public void sendIntervenerSolicitorRemovedEmail(FinremCaseDetails caseDetails, IntervenerDetails intervenerDetails,
                                                    String recipientName, String recipientEmail, String referenceNumber) {
        NotificationRequest notificationRequest = finremNotificationRequestMapper.buildNotificationRequest(
            caseDetails, intervenerDetails, recipientName, recipientEmail, referenceNumber);
        log.info("Received request for notification email for Intervener Solicitor removed event. Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_INTERVENER_SOLICITOR_REMOVED_EMAIL);
    }

    private void sendNotificationEmail(NotificationRequest notificationRequest, EmailTemplateNames emailTemplateName) {
        emailService.sendConfirmationEmail(notificationRequest, emailTemplateName);
    }

    public boolean isApplicantSolicitorAgreeToReceiveEmails(CaseDetails caseDetails) {
        boolean isContestedApplication = caseDataService.isContestedApplication(caseDetails);
        Map<String, Object> caseData = caseDetails.getData();
        return (isContestedApplication && YES_VALUE.equalsIgnoreCase(nullToEmpty(caseData.get(APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED))))
            || (!isContestedApplication && YES_VALUE.equalsIgnoreCase(nullToEmpty(caseData.get(APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED))));
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

    public boolean isApplicantSolicitorEmailPopulated(FinremCaseDetails caseDetails) {
        return caseDetails.getData().isApplicantSolicitorPopulated();
    }

    public boolean isApplicantSolicitorDigitalAndEmailPopulated(CaseDetails caseDetails) {
        return caseDataService.isApplicantSolicitorEmailPopulated(caseDetails)
            && checkSolicitorIsDigitalService.isApplicantSolicitorDigital(caseDetails.getId().toString());
    }

    public boolean isApplicantSolicitorDigitalAndEmailPopulated(FinremCaseDetails caseDetails) {
        return caseDetails.getData().isApplicantSolicitorPopulated()
            && checkSolicitorIsDigitalService.isApplicantSolicitorDigital(caseDetails.getId().toString());
    }

    public boolean isApplicantSolicitorDigital(FinremCaseDetails caseDetails) {
        return checkSolicitorIsDigitalService.isApplicantSolicitorDigital(caseDetails.getId().toString());
    }

    public boolean isRespondentSolicitorDigitalAndEmailPopulated(CaseDetails caseDetails) {
        return caseDataService.isNotEmpty(RESP_SOLICITOR_EMAIL, caseDetails.getData())
            && checkSolicitorIsDigitalService.isRespondentSolicitorDigital(caseDetails.getId().toString());
    }

    public boolean isRespondentSolicitorDigitalAndEmailPopulated(FinremCaseDetails caseDetails) {
        return caseDetails.getData().isRespondentSolicitorPopulated()
            && checkSolicitorIsDigitalService.isRespondentSolicitorDigital(caseDetails.getId().toString());
    }

    public boolean isRespondentSolicitorDigital(FinremCaseDetails caseDetails) {
        return checkSolicitorIsDigitalService.isRespondentSolicitorDigital(caseDetails.getId().toString());
    }

    public boolean isRespondentSolicitorEmailPopulated(FinremCaseDetails caseDetails) {
        return caseDetails.getData().isRespondentSolicitorPopulated();
    }

    public boolean isIntervenerSolicitorEmailPopulated(IntervenerWrapper intervenerWrapper) {
        return intervenerWrapper.isIntervenerSolicitorPopulated();
    }

    public boolean wasIntervenerSolicitorEmailPopulated(IntervenerDetails intervenerDetails) {
        return intervenerDetails.getIntervenerSolEmail() != null;
    }

    public boolean isIntervenerSolicitorDigitalAndEmailPopulated(IntervenerWrapper intervenerWrapper, FinremCaseDetails caseDetails) {
        return intervenerWrapper.isIntervenerSolicitorPopulated()
            && checkSolicitorIsDigitalService.isIntervenerSolicitorDigital(caseDetails.getId().toString(),
            intervenerWrapper.getIntervenerSolicitorCaseRole().getCcdCode());
    }

    public boolean isIntervenerSolicitorDigitalAndEmailPopulated(IntervenerWrapper intervenerWrapper, CaseDetails caseDetails) {
        return intervenerWrapper.isIntervenerSolicitorPopulated()
            && checkSolicitorIsDigitalService.isIntervenerSolicitorDigital(caseDetails.getId().toString(),
            intervenerWrapper.getIntervenerSolicitorCaseRole().getCcdCode());
    }

    public SolicitorCaseDataKeysWrapper getCaseDataKeysForIntervenerSolicitor(IntervenerWrapper intervenerWrapper) {
        return SolicitorCaseDataKeysWrapper.builder()
            .solicitorEmailKey(intervenerWrapper.getIntervenerSolEmail())
            .solicitorNameKey(nullToEmpty(Objects.toString(intervenerWrapper.getIntervenerSolName(), intervenerWrapper.getIntervenerSolicitorFirm())))
            .solicitorReferenceKey(nullToEmpty(intervenerWrapper.getIntervenerSolicitorReference()))
            .build();
    }

    public boolean isRespondentSolicitorRegisteredAndEmailCommunicationEnabled(CaseDetails caseDetails) {
        return shouldEmailRespondentSolicitor(caseDetails.getData())
            && checkSolicitorIsDigitalService.isRespondentSolicitorDigital(caseDetails.getId().toString());
    }

    public boolean isContestedApplicationAndApplicantOrRespondentSolicitorsIsNotRegisteredOrAcceptingEmails(CaseDetails caseDetails) {
        return caseDataService.isContestedPaperApplication(caseDetails)
            && (!isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)
            || !isRespondentSolicitorRegisteredAndEmailCommunicationEnabled(caseDetails));
    }

    /**
     * Do not expect any return.
     * <p>Please use @{@link #sendNoticeOfChangeEmail(FinremCaseDetails)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @deprecated Use {@link CaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
    public void sendNoticeOfChangeEmail(CaseDetails caseDetails) {
        EmailTemplateNames template = getNoticeOfChangeTemplate(caseDetails);
        NotificationRequest notificationRequest = notificationRequestMapper
            .getNotificationRequestForNoticeOfChange(caseDetails);
        sendNocEmail(notificationRequest, template);
    }

    public void sendNoticeOfChangeEmail(FinremCaseDetails caseDetails) {
        EmailTemplateNames template = getNoticeOfChangeTemplate(caseDetails);
        NotificationRequest notificationRequest = finremNotificationRequestMapper
            .getNotificationRequestForNoticeOfChange(caseDetails);
        sendNocEmail(notificationRequest, template);
    }

    @SuppressWarnings("squid:CallToDeprecatedMethod")
    public void sendNoticeOfChangeEmailCaseworker(CaseDetails caseDetails) {
        EmailTemplateNames template = getNoticeOfChangeTemplateCaseworker(caseDetails);
        NotificationRequest notificationRequest = notificationRequestMapper
            .getNotificationRequestForNoticeOfChange(caseDetails);
        sendNocEmailIfSolicitorIsDigital(caseDetails, notificationRequest, template);
    }

    public void sendNoticeOfChangeEmailCaseworker(FinremCaseDetails caseDetails) {
        EmailTemplateNames template = getNoticeOfChangeTemplateCaseworker(caseDetails);
        NotificationRequest notificationRequest = finremNotificationRequestMapper
            .getNotificationRequestForNoticeOfChange(caseDetails);
        sendNocEmailIfSolicitorIsDigital(caseDetails, notificationRequest, template);
    }

    public boolean isApplicantSolicitorResponsibleToDraftOrder(Map<String, Object> caseData) {
        return caseDataService.isApplicantSolicitorResponsibleToDraftOrder(caseData);
    }

    public boolean isRespondentSolicitorResponsibleToDraftOrder(Map<String, Object> caseData) {
        return caseDataService.isRespondentSolicitorResponsibleToDraftOrder(caseData);
    }

    public boolean isContestedApplication(CaseDetails caseDetails) {
        return caseDataService.isContestedApplication(caseDetails);
    }

    public boolean isContestedApplication(FinremCaseDetails caseDetails) {
        return caseDataService.isContestedApplication(caseDetails);
    }

    private void sendNocEmail(
        NotificationRequest notificationRequest,
        EmailTemplateNames template) {
        if (StringUtils.hasText(notificationRequest.getNotificationEmail())) {
            sendNotificationEmail(notificationRequest, template);
        }
    }

    /**
     * Return String Object for given Case with the given indentation used.
     * <p>Please use @{@link #sendNocEmailIfSolicitorIsDigital(FinremCaseDetails, NotificationRequest, EmailTemplateNames)}</p>
     *
     * @param caseDetails         instance of CaseDetails
     * @param notificationRequest instance of NotificationRequest
     * @param template            instance of EmailTemplateNames
     * @deprecated Use {@link CaseDetails caseDetails, NotificationRequest notificationRequest, EmailTemplateNames template}
     */
    @Deprecated(since = "15-june-2023")
    private void sendNocEmailIfSolicitorIsDigital(
        CaseDetails caseDetails,
        NotificationRequest notificationRequest,
        EmailTemplateNames template) {

        sendNocEmailIfSolicitorIsDigitalInternal(
            caseDetails.getId().toString(),
            notificationRequest,
            template,
            () -> checkSolicitorIsDigitalService.isApplicantSolicitorDigital(caseDetails.getId().toString()),
            () -> checkSolicitorIsDigitalService.isRespondentSolicitorDigital(caseDetails.getId().toString()),
            () -> (String) caseDetails.getData().get(getSolicitorNameKey(caseDetails))
        );
    }

    private void sendNocEmailIfSolicitorIsDigital(
        FinremCaseDetails caseDetails,
        NotificationRequest notificationRequest,
        EmailTemplateNames template) {

        sendNocEmailIfSolicitorIsDigitalInternal(
            caseDetails.getId().toString(),
            notificationRequest,
            template,
            () -> checkSolicitorIsDigitalService.isApplicantSolicitorDigital(caseDetails.getId().toString()),
            () -> checkSolicitorIsDigitalService.isRespondentSolicitorDigital(caseDetails.getId().toString()),
            () -> caseDetails.getData().isConsentedApplication() ? caseDetails.getData().getContactDetailsWrapper().getSolicitorName()
                : caseDetails.getData().getContactDetailsWrapper().getApplicantSolicitorName()
        );
    }

    private void sendNocEmailIfSolicitorIsDigitalInternal(
        String caseId,
        NotificationRequest notificationRequest,
        EmailTemplateNames template,
        BooleanSupplier isApplicantSolicitorDigitalSupplier,
        BooleanSupplier isRespondentSolicitorDigitalSupplier,
        Supplier<String> nameSupplier) {

        if (isApplicantNoticeOfChangeRequest(notificationRequest, nameSupplier)) {
            log.info("{} - isApplicantNoticeOfChangeRequest = true", caseId);
            boolean isApplicantSolicitorDigital = isApplicantSolicitorDigitalSupplier.getAsBoolean();
            log.info("{} - isApplicantSolicitorDigital = {}}", caseId, isApplicantSolicitorDigital);
            if (isApplicantSolicitorDigital) {
                sendNotificationEmail(notificationRequest, template);
            }
            return;
        }
        boolean isRespondentSolicitorDigital = isRespondentSolicitorDigitalSupplier.getAsBoolean();
        log.info("{} - isRespondentSolicitorDigital = {}}", caseId, isRespondentSolicitorDigital);
        if (isRespondentSolicitorDigital) {
            sendNotificationEmail(notificationRequest, template);
        }
    }

    /**
     * Return EmailTemplateNames Object for given Case with the given indentation used.
     * <p>Please use @{@link #getNoticeOfChangeTemplate(FinremCaseDetails)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @return EmailTemplateNames Object
     * @deprecated Use {@link CaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
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

    /**
     * Return String Object for given Case with the given indentation used.
     *
     * @param caseDetails instance of CaseDetails
     * @return EmailTemplateNames Object
     * @deprecated Use {@link FinremCaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
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

    private boolean isApplicantNoticeOfChangeRequest(NotificationRequest notificationRequest,
                                                     Supplier<String> nameSupplier) {
        return notificationRequest.getName().equalsIgnoreCase(
            nullToEmpty(nameSupplier.get()));
    }

    private String getSolicitorNameKey(CaseDetails caseDetails) {
        return caseDataService.isConsentedApplication(caseDetails)
            ? CONSENTED_SOLICITOR_NAME
            : CONTESTED_SOLICITOR_NAME;
    }

    /**
     * Return Recipient Email for given Case .
     * <p>Please use @{@link #getRecipientEmail(FinremCaseDetails)}</p>
     *
     * @param caseDetails instance of CaseDetails
     * @return List Object
     * @deprecated Use {@link CaseDetails caseDetails}
     */
    @Deprecated(since = "15-june-2023")
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
            Map<String, Object> courtDetails = (Map<String, Object>) courtDetailsMap.get(caseDetails.getData().getSelectedAllocatedCourt());

            return (String) courtDetails.get(COURT_DETAILS_EMAIL_KEY);
        }
        return DEFAULT_EMAIL;
    }

    private EmailTemplateNames getIntervenerSendOrderContestedTemplate(IntervenerType intervener) {
        switch (intervener) {
            case INTERVENER_ONE -> {
                return FR_CONTEST_ORDER_APPROVED_INTERVENER1;
            }
            case INTERVENER_TWO -> {
                return FR_CONTEST_ORDER_APPROVED_INTERVENER2;
            }
            case INTERVENER_THREE -> {
                return FR_CONTEST_ORDER_APPROVED_INTERVENER3;
            }
            case INTERVENER_FOUR -> {
                return FR_CONTEST_ORDER_APPROVED_INTERVENER4;
            }
            default -> {
                return FR_CONTEST_ORDER_APPROVED_INTERVENER1;
            }
        }
    }
}
