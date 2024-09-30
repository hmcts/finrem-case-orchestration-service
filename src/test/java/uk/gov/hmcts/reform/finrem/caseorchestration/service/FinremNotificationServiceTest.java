package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCaseDetailsBuilderFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.NotificationServiceConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremNotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.NotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NottinghamCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionMidlandsFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckSolicitorIsDigitalService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_CTSC_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_JUDGE_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_ASSIGNED_TO_JUDGE;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_OS_ORDERS_NEED_REVIEW_CASEWORKER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_PREPARE_FOR_HEARING;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_TRANSFER_TO_LOCAL_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.getCourtDetailsString;

@ExtendWith(MockitoExtension.class)
class FinremNotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private FinremNotificationRequestMapper finremNotificationRequestMapper;
    @Mock
    private NotificationRequestMapper notificationRequestMapper;
    @Mock
    private CheckSolicitorIsDigitalService checkSolicitorIsDigitalService;
    @Mock
    private CaseDataService caseDataService;
    @Mock
    private EmailService emailService;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private NotificationServiceConfiguration notificationServiceConfiguration;

    private final FinremCaseDetails consentedFinremCaseDetails = getConsentedFinremCaseDetails();
    private final FinremCaseDetails contestedFinremCaseDetails = getContestedFinremCaseDetails();
    private SolicitorCaseDataKeysWrapper dataKeysWrapper;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        dataKeysWrapper = SolicitorCaseDataKeysWrapper.builder().build();

        NotificationRequest notificationRequest = new NotificationRequest();
        lenient().when(notificationRequestMapper.getNotificationRequestForConsentApplicantSolicitor(any(FinremCaseDetails.class), any()))
            .thenReturn(notificationRequest);
        lenient().when(finremNotificationRequestMapper.getNotificationRequestForCaseworker(any(FinremCaseDetails.class)))
            .thenReturn(notificationRequest);
        lenient().when(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(any(FinremCaseDetails.class)))
            .thenReturn(notificationRequest);
        lenient().when(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(any(FinremCaseDetails.class), anyBoolean()))
            .thenReturn(notificationRequest);
        lenient().when(finremNotificationRequestMapper.getNotificationRequestForRespondentSolicitor(any(FinremCaseDetails.class)))
            .thenReturn(notificationRequest);
        lenient().when(finremNotificationRequestMapper.getNotificationRequestForRespondentSolicitor(any(FinremCaseDetails.class), anyBoolean()))
            .thenReturn(notificationRequest);
        lenient().when(finremNotificationRequestMapper.getNotificationRequestForIntervenerSolicitor(any(FinremCaseDetails.class),
            any(SolicitorCaseDataKeysWrapper.class))).thenReturn(notificationRequest);
        lenient().when(notificationServiceConfiguration.getCtscEmail()).thenReturn(TEST_CTSC_EMAIL);
        lenient().when(objectMapper.readValue(getCourtDetailsString(), HashMap.class))
            .thenReturn(new HashMap(Map.of("email", "FRCLondon@justice.gov.uk")));
    }

    @Test
    void sendPrepareForHearingOrderSentEmailApplicant() {
        notificationService.sendPrepareForHearingOrderSentEmailApplicant(contestedFinremCaseDetails);

        verify(finremNotificationRequestMapper).getNotificationRequestForApplicantSolicitor(contestedFinremCaseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_PREPARE_FOR_HEARING_ORDER_SENT));
    }

    @Test
    void sendConsentHearingNotificationEmailToApplicantSolicitor() {
        notificationService.sendConsentHearingNotificationEmailToApplicantSolicitor(consentedFinremCaseDetails, Map.of());

        verify(notificationRequestMapper).getNotificationRequestForConsentApplicantSolicitor(consentedFinremCaseDetails, Map.of());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENTED_LIST_FOR_HEARING));
    }

    @Test
    void sendConsentOrderAvailableEmailToRespondentSolicitor() {
        notificationService.sendConsentOrderAvailableEmailToRespondentSolicitor(consentedFinremCaseDetails);

        verify(finremNotificationRequestMapper).getNotificationRequestForRespondentSolicitor(consentedFinremCaseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENT_ORDER_AVAILABLE));
    }

    @Test
    void sendHwfSuccessfulNotificationEmail() {
        notificationService.sendConsentedHWFSuccessfulConfirmationEmail(consentedFinremCaseDetails);

        verify(finremNotificationRequestMapper).getNotificationRequestForApplicantSolicitor(consentedFinremCaseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_HWF_SUCCESSFUL));
    }

    @Test
    void sendAssignToJudgeNotificationEmailToApplicantSolicitor() {
        notificationService.sendAssignToJudgeConfirmationEmailToApplicantSolicitor(consentedFinremCaseDetails);

        verify(finremNotificationRequestMapper).getNotificationRequestForApplicantSolicitor(consentedFinremCaseDetails, true);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_ASSIGNED_TO_JUDGE));
    }

    @Test
    void sendAssignToJudgeNotificationEmailToRespondentSolicitor() {
        notificationService.sendAssignToJudgeConfirmationEmailToRespondentSolicitor(consentedFinremCaseDetails);

        verify(finremNotificationRequestMapper).getNotificationRequestForRespondentSolicitor(consentedFinremCaseDetails, true);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_ASSIGNED_TO_JUDGE));
    }

    @Test
    void sendConsentOrderMadeConfirmationEmailToApplicantSolicitor() {
        notificationService.sendConsentOrderMadeConfirmationEmailToApplicantSolicitor(consentedFinremCaseDetails);

        verify(finremNotificationRequestMapper).getNotificationRequestForApplicantSolicitor(consentedFinremCaseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENT_ORDER_MADE));
    }

    @Test
    void sendConsentOrderMadeConfirmationEmailToRespondentSolicitor() {
        notificationService.sendConsentOrderMadeConfirmationEmailToRespondentSolicitor(consentedFinremCaseDetails);

        verify(finremNotificationRequestMapper).getNotificationRequestForRespondentSolicitor(consentedFinremCaseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENT_ORDER_MADE));
    }

    @Test
    void sendPrepareForHearingNotificationEmailToApplicantSolicitor() {
        notificationService.sendPrepareForHearingEmailApplicant(contestedFinremCaseDetails);

        verify(finremNotificationRequestMapper).getNotificationRequestForApplicantSolicitor(contestedFinremCaseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_PREPARE_FOR_HEARING));
    }

    @Test
    void sendPrepareForHearingNotificationEmailToRespondentSolicitor() {
        notificationService.sendPrepareForHearingEmailRespondent(contestedFinremCaseDetails);

        verify(finremNotificationRequestMapper).getNotificationRequestForRespondentSolicitor(contestedFinremCaseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_PREPARE_FOR_HEARING));
    }

    @Test
    void sendConsentOrderNotApprovedNotificationEmailToApplicantSolicitor() {
        notificationService.sendConsentOrderNotApprovedEmailToApplicantSolicitor(consentedFinremCaseDetails);

        verify(finremNotificationRequestMapper).getNotificationRequestForApplicantSolicitor(consentedFinremCaseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENT_ORDER_NOT_APPROVED));
    }

    @Test
    void sendConsentOrderNotApprovedNotificationEmailToRespondentSolicitor() {
        notificationService.sendConsentOrderNotApprovedEmailToRespondentSolicitor(consentedFinremCaseDetails);

        verify(finremNotificationRequestMapper).getNotificationRequestForRespondentSolicitor(consentedFinremCaseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENT_ORDER_NOT_APPROVED));
    }

    @Test
    void sendConsentOrderAvailableNotificationEmail() {
        notificationService.sendConsentOrderAvailableEmailToApplicantSolicitor(consentedFinremCaseDetails);

        verify(finremNotificationRequestMapper).getNotificationRequestForApplicantSolicitor(consentedFinremCaseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENT_ORDER_AVAILABLE));
    }

    @Test
    void sendConsentOrderAvailableNotificationCtscEmail() {
        notificationService.sendConsentOrderAvailableCtscEmail(consentedFinremCaseDetails);

        verify(finremNotificationRequestMapper).getNotificationRequestForApplicantSolicitor(consentedFinremCaseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENT_ORDER_AVAILABLE_CTSC));
    }

    @Test
    void sendContestedApplicationIssuedEmail() {
        notificationService.sendContestedApplicationIssuedEmailToApplicantSolicitor(consentedFinremCaseDetails);

        verify(finremNotificationRequestMapper).getNotificationRequestForApplicantSolicitor(consentedFinremCaseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_APPLICATION_ISSUED));
    }

    @Test
    void sendContestOrderApprovedEmailApplicantSolicitor() {
        notificationService.sendContestOrderApprovedEmailApplicant(consentedFinremCaseDetails);

        verify(finremNotificationRequestMapper)
            .getNotificationRequestForApplicantSolicitor(consentedFinremCaseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTEST_ORDER_APPROVED_APPLICANT));
    }

    @Test
    void sendContestOrderApprovedEmailRespondentSolicitor() {
        notificationService.sendContestOrderApprovedEmailRespondent(consentedFinremCaseDetails);

        verify(finremNotificationRequestMapper)
            .getNotificationRequestForRespondentSolicitor(consentedFinremCaseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTEST_ORDER_APPROVED_RESPONDENT));
    }

    @Test
    void sendFinremContestOrderApprovedEmailIntervener1Solicitor() {
        notificationService.sendContestOrderApprovedEmailIntervener(consentedFinremCaseDetails,
            dataKeysWrapper, IntervenerType.INTERVENER_ONE);

        verify(finremNotificationRequestMapper)
            .getNotificationRequestForIntervenerSolicitor(consentedFinremCaseDetails, dataKeysWrapper);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTEST_ORDER_APPROVED_INTERVENER1));
    }

    @Test
    void sendFinremContestOrderApprovedEmailIntervener2Solicitor() {
        notificationService.sendContestOrderApprovedEmailIntervener(consentedFinremCaseDetails,
            dataKeysWrapper, IntervenerType.INTERVENER_TWO);

        verify(finremNotificationRequestMapper)
            .getNotificationRequestForIntervenerSolicitor(consentedFinremCaseDetails, dataKeysWrapper);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTEST_ORDER_APPROVED_INTERVENER2));
    }

    @Test
    void sendFinremContestOrderApprovedEmailIntervener3Solicitor() {
        notificationService.sendContestOrderApprovedEmailIntervener(consentedFinremCaseDetails,
            dataKeysWrapper, IntervenerType.INTERVENER_THREE);

        verify(finremNotificationRequestMapper)
            .getNotificationRequestForIntervenerSolicitor(consentedFinremCaseDetails, dataKeysWrapper);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTEST_ORDER_APPROVED_INTERVENER3));
    }

    @Test
    void sendFinremContestOrderApprovedEmailIntervener4Solicitor() {
        notificationService.sendContestOrderApprovedEmailIntervener(consentedFinremCaseDetails,
            dataKeysWrapper, IntervenerType.INTERVENER_FOUR);

        verify(finremNotificationRequestMapper)
            .getNotificationRequestForIntervenerSolicitor(consentedFinremCaseDetails, dataKeysWrapper);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTEST_ORDER_APPROVED_INTERVENER4));
    }

    @Test
    void sendSolicitorToDraftOrderEmailRespondent() {
        notificationService.sendSolicitorToDraftOrderEmailRespondent(consentedFinremCaseDetails);

        verify(finremNotificationRequestMapper).getNotificationRequestForRespondentSolicitor(consentedFinremCaseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_DRAFT_ORDER));
    }

    @Test
    void sendSolicitorToDraftOrderEmailApplicant() {
        notificationService.sendSolicitorToDraftOrderEmailApplicant(consentedFinremCaseDetails);

        verify(finremNotificationRequestMapper).getNotificationRequestForApplicantSolicitor(consentedFinremCaseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_DRAFT_ORDER));
    }

    @Test
    void sendContestedHwfSuccessfulNotificationEmail() {
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(contestedFinremCaseDetails);

        verify(finremNotificationRequestMapper).getNotificationRequestForApplicantSolicitor(contestedFinremCaseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_HWF_SUCCESSFUL));
    }

    @Test
    void sendGeneralEmailConsented() {
        notificationService.sendConsentGeneralEmail(consentedFinremCaseDetails, anyString());

        verify(finremNotificationRequestMapper).getNotificationRequestForApplicantSolicitor(consentedFinremCaseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENT_GENERAL_EMAIL));
    }

    @Test
    void sendGeneralEmailContested() {
        notificationService.sendContestedGeneralEmail(contestedFinremCaseDetails, anyString());

        verify(finremNotificationRequestMapper).getNotificationRequestForApplicantSolicitor(contestedFinremCaseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_GENERAL_EMAIL));
    }

    @Test
    void sendContestOrderNotApprovedNotificationEmailApplicant() {
        notificationService.sendContestOrderNotApprovedEmailApplicant(contestedFinremCaseDetails);

        verify(finremNotificationRequestMapper).getNotificationRequestForApplicantSolicitor(contestedFinremCaseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTEST_ORDER_NOT_APPROVED));
    }

    @Test
    void sendContestOrderNotApprovedNotificationEmailRespondent() {
        notificationService.sendContestOrderNotApprovedEmailRespondent(contestedFinremCaseDetails);

        verify(finremNotificationRequestMapper).getNotificationRequestForRespondentSolicitor(contestedFinremCaseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTEST_ORDER_NOT_APPROVED));
    }

    @Test
    void sendContestedConsentOrderApprovedNotificationEmailToApplicantSolicitor() {
        notificationService.sendContestedConsentOrderApprovedEmailToApplicantSolicitor(contestedFinremCaseDetails);

        verify(finremNotificationRequestMapper).getNotificationRequestForApplicantSolicitor(contestedFinremCaseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_CONSENT_ORDER_APPROVED));
    }

    @Test
    void sendContestedConsentOrderApprovedNotificationEmailToRespondentSolicitor() {
        notificationService.sendContestedConsentOrderApprovedEmailToRespondentSolicitor(contestedFinremCaseDetails);

        verify(finremNotificationRequestMapper).getNotificationRequestForRespondentSolicitor(contestedFinremCaseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_CONSENT_ORDER_APPROVED));
    }

    @Test
    void sendContestedGeneralApplicationReferToJudgeNotificationEmail() {
        notificationService.sendContestedGeneralApplicationReferToJudgeEmail(contestedFinremCaseDetails);

        verify(finremNotificationRequestMapper).getNotificationRequestForApplicantSolicitor(contestedFinremCaseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_GENERAL_APPLICATION_REFER_TO_JUDGE));
    }

    @Test
    void sendContestedGeneralApplicationOutcomeNotificationEmailWhenSendToFRCToggleTrue() {
        when(featureToggleService.isSendToFRCEnabled()).thenReturn(true);

        notificationService.sendContestedGeneralApplicationOutcomeEmail(contestedFinremCaseDetails);

        verify(finremNotificationRequestMapper).getNotificationRequestForApplicantSolicitor(contestedFinremCaseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_GENERAL_APPLICATION_OUTCOME));
    }

    @Test
    void sendContestedGeneralApplicationOutcomeNotificationEmailToTestAccountWhenSendToFRCToggleFalse() {
        when(featureToggleService.isSendToFRCEnabled()).thenReturn(false);

        notificationService.sendContestedGeneralApplicationOutcomeEmail(contestedFinremCaseDetails);

        verify(finremNotificationRequestMapper).getNotificationRequestForApplicantSolicitor(contestedFinremCaseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_GENERAL_APPLICATION_OUTCOME));
    }

    @Test
    void sendContestedConsentGeneralOrderNotificationEmailApplicantSolicitor() {
        notificationService.sendContestedConsentGeneralOrderEmailApplicantSolicitor(contestedFinremCaseDetails);

        verify(finremNotificationRequestMapper).getNotificationRequestForApplicantSolicitor(contestedFinremCaseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_GENERAL_ORDER_CONSENT));
    }

    @Test
    void sendContestedConsentGeneralOrderNotificationEmailRespondentSolicitor() {
        notificationService.sendContestedConsentGeneralOrderEmailRespondentSolicitor(contestedFinremCaseDetails);

        verify(finremNotificationRequestMapper).getNotificationRequestForRespondentSolicitor(contestedFinremCaseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_GENERAL_ORDER_CONSENT));
    }

    @Test
    void sendContestedGeneralOrderNotificationEmailApplicant() {
        notificationService.sendContestedGeneralOrderEmailApplicant(contestedFinremCaseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_GENERAL_ORDER));
    }

    @Test
    void sendContestedGeneralOrderNotificationEmailRespondent() {
        notificationService.sendContestedGeneralOrderEmailRespondent(contestedFinremCaseDetails);

        verify(finremNotificationRequestMapper).getNotificationRequestForRespondentSolicitor(contestedFinremCaseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_GENERAL_ORDER));
    }

    @Test
    void sendConsentedGeneralOrderNotificationEmailApplicant() {
        notificationService.sendConsentedGeneralOrderEmailToApplicantSolicitor(consentedFinremCaseDetails);

        verify(finremNotificationRequestMapper).getNotificationRequestForApplicantSolicitor(consentedFinremCaseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENTED_GENERAL_ORDER));
    }

    @Test
    void sendConsentedGeneralOrderNotificationEmailRespondent() {
        notificationService.sendConsentedGeneralOrderEmailToRespondentSolicitor(consentedFinremCaseDetails);

        verify(finremNotificationRequestMapper).getNotificationRequestForRespondentSolicitor(consentedFinremCaseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENTED_GENERAL_ORDER));
    }

    @Test
    void sendContestedConsentOrderNotApprovedNotificationEmail() {
        notificationService.sendContestedConsentOrderNotApprovedEmailApplicantSolicitor(contestedFinremCaseDetails);

        verify(finremNotificationRequestMapper).getNotificationRequestForApplicantSolicitor(contestedFinremCaseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_CONSENT_ORDER_NOT_APPROVED));
    }

    @Test
    void sendContestedConsentOrderNotApprovedNotificationEmailToRespondentSolicitor() {
        notificationService.sendContestedConsentOrderNotApprovedEmailRespondentSolicitor(contestedFinremCaseDetails);

        verify(finremNotificationRequestMapper).getNotificationRequestForRespondentSolicitor(contestedFinremCaseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_CONSENT_ORDER_NOT_APPROVED));
    }

    @Test
    void sendConsentOrderNotApprovedSentEmailToApplicantSolicitor() {
        notificationService.sendConsentOrderNotApprovedSentEmailToApplicantSolicitor(consentedFinremCaseDetails);

        verify(finremNotificationRequestMapper).getNotificationRequestForApplicantSolicitor(consentedFinremCaseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENT_ORDER_NOT_APPROVED_SENT));
    }

    @Test
    void sendConsentOrderNotApprovedSentEmailToRespondentSolicitor() {
        notificationService.sendConsentOrderNotApprovedSentEmailToRespondentSolicitor(consentedFinremCaseDetails);

        verify(finremNotificationRequestMapper).getNotificationRequestForRespondentSolicitor(consentedFinremCaseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENT_ORDER_NOT_APPROVED_SENT));
    }

    @Test
    void shouldEmailRespondentSolicitor() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(RESP_SOLICITOR_EMAIL, TEST_RESP_SOLICITOR_EMAIL);
        caseData.put(RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT, YES_VALUE);

        when(caseDataService.isPaperApplication(anyMap())).thenReturn(false);
        when(caseDataService.isRespondentRepresentedByASolicitor(any())).thenReturn(true);
        when(caseDataService.isNotEmpty(RESP_SOLICITOR_EMAIL, caseData)).thenReturn(true);

        assertTrue(notificationService.isRespondentSolicitorEmailCommunicationEnabled(caseData));
    }

    @Test
    void shouldNotEmailRespondentSolicitor() {
        lenient().when(caseDataService.isPaperApplication(any(Map.class))).thenReturn(true);
        lenient().when(caseDataService.isRespondentRepresentedByASolicitor(any())).thenReturn(false);

        assertFalse(notificationService.isRespondentSolicitorEmailCommunicationEnabled(new HashMap<>()));
    }

    @Test
    void shouldEmailRespondentSolicitorWhenNullEmailConsent() {
        lenient().when(caseDataService.isPaperApplication(any(FinremCaseData.class))).thenReturn(false);
        lenient().when(caseDataService.isRespondentRepresentedByASolicitor(any())).thenReturn(true);
        lenient().when(caseDataService.isNotEmpty(any(), any())).thenReturn(true);

        Map<String, Object> caseData = new HashMap<>();
        caseData.put(RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT, null);

        boolean isRespondentCommunicationEnabled = notificationService.isRespondentSolicitorEmailCommunicationEnabled(caseData);

        assertTrue(isRespondentCommunicationEnabled);
    }

    @Test
    void shouldEmailContestedAppSolicitor() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CONTESTED_SOLICITOR_EMAIL, TEST_SOLICITOR_EMAIL);
        caseData.put(APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED, YES_VALUE);

        lenient().when(caseDataService.isPaperApplication(any(FinremCaseData.class))).thenReturn(false);
        lenient().when(caseDataService.isApplicantRepresentedByASolicitor(any())).thenReturn(true);
        lenient().when(caseDataService.isNotEmpty(CONTESTED_SOLICITOR_EMAIL, caseData)).thenReturn(true);

        assertTrue(notificationService.isContestedApplicantSolicitorEmailCommunicationEnabled(caseData));
    }

    @Test
    void shouldNotEmailContestedAppSolicitor() {
        lenient().when(caseDataService.isPaperApplication(any(FinremCaseData.class))).thenReturn(true);
        lenient().when(caseDataService.isApplicantRepresentedByASolicitor(any())).thenReturn(false);

        assertFalse(notificationService.isContestedApplicantSolicitorEmailCommunicationEnabled(new HashMap<>()));
    }

    @Test
    void sendTransferToCourtEmailConsented() {
        notificationService.sendTransferToLocalCourtEmail(consentedFinremCaseDetails);

        verify(finremNotificationRequestMapper).getNotificationRequestForApplicantSolicitor(consentedFinremCaseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_TRANSFER_TO_LOCAL_COURT));
    }

    @Test
    void sendInterimNotificationEmailToApplicantSolicitor() {
        notificationService.sendInterimNotificationEmailToApplicantSolicitor(contestedFinremCaseDetails);

        verify(finremNotificationRequestMapper).getNotificationRequestForApplicantSolicitor(contestedFinremCaseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_INTERIM_HEARING));
    }

    @Test
    void sendInterimNotificationEmailToRespondentSolicitor() {
        notificationService.sendInterimNotificationEmailToRespondentSolicitor(contestedFinremCaseDetails);

        verify(finremNotificationRequestMapper).getNotificationRequestForRespondentSolicitor(contestedFinremCaseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_INTERIM_HEARING));
    }

    @Test
    void givenContestedCaseWhenSendNoticeOfChangeEmailThenSendNoticeOfChangeContestedEmail() {
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setName(TEST_SOLICITOR_NAME);
        notificationRequest.setNotificationEmail("test@test.com");
        when(finremNotificationRequestMapper.getNotificationRequestForNoticeOfChange(any())).thenReturn(notificationRequest);
        FinremCaseDetails caseDetails = getContestedFinremCaseDetails();

        notificationService.sendNoticeOfChangeEmail(caseDetails);

        verify(finremNotificationRequestMapper).getNotificationRequestForNoticeOfChange(caseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_NOTICE_OF_CHANGE));
    }

    @Test
    void givenConsentedCaseWhenSendNoticeOfChangeEmailThenSendNoticeOfChangeContestedEmail() {
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setName(TEST_SOLICITOR_NAME);
        notificationRequest.setNotificationEmail("test@test.com");
        when(finremNotificationRequestMapper.getNotificationRequestForNoticeOfChange(any())).thenReturn(notificationRequest);
        FinremCaseDetails caseDetails = getConsentedFinremCaseDetails();

        notificationService.sendNoticeOfChangeEmail(caseDetails);

        verify(finremNotificationRequestMapper).getNotificationRequestForNoticeOfChange(caseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENTED_NOTICE_OF_CHANGE));
    }

    @Test
    void givenContestedCase_whenSendNoCCaseworkerEmail_thenSendContestedEmail() {
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setName(TEST_SOLICITOR_NAME);
        when(finremNotificationRequestMapper.getNotificationRequestForNoticeOfChange(any())).thenReturn(notificationRequest);
        when(checkSolicitorIsDigitalService.isApplicantSolicitorDigital(anyString())).thenReturn(true);
        FinremCaseDetails caseDetails = getContestedFinremCaseDetails();

        notificationService.sendNoticeOfChangeEmailCaseworker(caseDetails);

        verify(finremNotificationRequestMapper).getNotificationRequestForNoticeOfChange(caseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_NOC_CASEWORKER));
    }

    @Test
    void givenConsentedCase_whenSendNoCCaseworkerEmail_thenSendConsentedEmail() {
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setName(TEST_SOLICITOR_NAME);
        when(finremNotificationRequestMapper.getNotificationRequestForNoticeOfChange(any())).thenReturn(notificationRequest);
        when(checkSolicitorIsDigitalService.isApplicantSolicitorDigital(anyString())).thenReturn(true);
        FinremCaseDetails caseDetails = getConsentedFinremCaseDetails();

        notificationService.sendNoticeOfChangeEmailCaseworker(caseDetails);

        verify(finremNotificationRequestMapper).getNotificationRequestForNoticeOfChange(caseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENTED_NOC_CASEWORKER));
    }

    @Test
    void givenContestedCaseAndNonDigitalSol_whenSendNocEmail_thenNotSendContestedEmailCaseworker() {
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setName(TEST_SOLICITOR_NAME);
        when(finremNotificationRequestMapper.getNotificationRequestForNoticeOfChange(any())).thenReturn(notificationRequest);
        when(checkSolicitorIsDigitalService.isApplicantSolicitorDigital(anyString())).thenReturn(false);
        FinremCaseDetails caseDetails = getContestedFinremCaseDetails();

        notificationService.sendNoticeOfChangeEmailCaseworker(caseDetails);

        verify(finremNotificationRequestMapper).getNotificationRequestForNoticeOfChange(caseDetails);
        verifyNoMoreInteractions(emailService);
    }

    @Test
    void sendUpdateFrcInformationEmailToAppSolicitor() {
        notificationService.sendUpdateFrcInformationEmailToAppSolicitor(contestedFinremCaseDetails);
        verify(finremNotificationRequestMapper).getNotificationRequestForApplicantSolicitor(contestedFinremCaseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_UPDATE_FRC_SOL));
    }

    @Test
    void sendUpdateFrcInformationEmailToRespSolicitor() {
        notificationService.sendUpdateFrcInformationEmailToRespondentSolicitor(contestedFinremCaseDetails);
        verify(finremNotificationRequestMapper).getNotificationRequestForRespondentSolicitor(contestedFinremCaseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_UPDATE_FRC_SOL));
    }

    @Test
    void sendUpdateFrcInformationEmailToCourt() throws JsonProcessingException {
        when(featureToggleService.isSendToFRCEnabled()).thenReturn(true);

        notificationService.sendUpdateFrcInformationEmailToCourt(contestedFinremCaseDetails);
        verify(finremNotificationRequestMapper).getNotificationRequestForApplicantSolicitor(contestedFinremCaseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_UPDATE_FRC_COURT));
    }

    @Test
    void sendConsentOrderAvailableEmailToIntervenerSolicitor() {
        notificationService.sendConsentOrderAvailableEmailToIntervenerSolicitor(consentedFinremCaseDetails,
            dataKeysWrapper);

        verify(finremNotificationRequestMapper).getNotificationRequestForIntervenerSolicitor(consentedFinremCaseDetails, dataKeysWrapper);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENT_ORDER_AVAILABLE));
    }

    @Test
    void sendPrepareForHearingAfterSentNotificationEmailRespondent() {
        notificationService.sendPrepareForHearingOrderSentEmailRespondent(consentedFinremCaseDetails);

        verify(finremNotificationRequestMapper).getNotificationRequestForRespondentSolicitor(consentedFinremCaseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_PREPARE_FOR_HEARING_ORDER_SENT));
    }

    @Test
    void sendContestedOutstandingOrdersNeedReviewEmailToCaseworker() {
        notificationService.sendContestedOutstandingOrdersNeedReviewEmailToCaseworker(contestedFinremCaseDetails);

        verify(finremNotificationRequestMapper).getNotificationRequestForCaseworker(contestedFinremCaseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_OS_ORDERS_NEED_REVIEW_CASEWORKER));
    }

    private FinremCaseData getFinremCaseData() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.getContactDetailsWrapper().setApplicantFmName("Victoria");
        caseData.getContactDetailsWrapper().setApplicantLname("Goodman");
        caseData.getContactDetailsWrapper().setApplicantSolicitorEmail(TEST_SOLICITOR_EMAIL);
        caseData.getContactDetailsWrapper().setApplicantSolicitorName(TEST_SOLICITOR_NAME);
        caseData.getContactDetailsWrapper().setRespondentSolicitorEmail(TEST_RESP_SOLICITOR_EMAIL);
        caseData.getContactDetailsWrapper().setRespondentSolicitorName(TEST_RESP_SOLICITOR_NAME);
        caseData.getContactDetailsWrapper().setRespondentSolicitorReference(TEST_RESP_SOLICITOR_REFERENCE);
        caseData.getContactDetailsWrapper().setSolicitorReference(TEST_SOLICITOR_REFERENCE);
        caseData.setDivorceCaseNumber(TEST_DIVORCE_CASE_NUMBER);
        caseData.getGeneralApplicationWrapper().setGeneralApplicationReferToJudgeEmail(TEST_JUDGE_EMAIL);
        caseData.getRegionWrapper().getAllocatedRegionWrapper().setRegionList(Region.MIDLANDS);
        caseData.getRegionWrapper().getAllocatedRegionWrapper().setMidlandsFrcList(RegionMidlandsFrc.NOTTINGHAM);
        caseData.getRegionWrapper().getAllocatedRegionWrapper().getDefaultCourtListWrapper()
            .setNottinghamCourtList(NottinghamCourt.NOTTINGHAM_COUNTY_COURT_AND_FAMILY_COURT);
        caseData.setBulkPrintLetterIdRes(NOTTINGHAM);
        return caseData;
    }

    private FinremCaseDetails getContestedFinremCaseDetails() {
        FinremCaseData caseData = getFinremCaseData();
        caseData.getContactDetailsWrapper().setRespondentFmName("David");
        caseData.getContactDetailsWrapper().setRespondentLname("Goodman");
        caseData.setCcdCaseType(CaseType.CONTESTED);
        return FinremCaseDetailsBuilderFactory.from(12345L, CaseType.CONTESTED, caseData).build();
    }

    private FinremCaseDetails getConsentedFinremCaseDetails() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.getContactDetailsWrapper().setAppRespondentFmName("David");
        caseData.getContactDetailsWrapper().setAppRespondentLName("Goodman");
        caseData.getContactDetailsWrapper().setApplicantFmName("Victoria");
        caseData.getContactDetailsWrapper().setApplicantLname("Goodman");
        caseData.getContactDetailsWrapper().setSolicitorEmail(TEST_SOLICITOR_EMAIL);
        caseData.getContactDetailsWrapper().setSolicitorName(TEST_SOLICITOR_NAME);
        caseData.getContactDetailsWrapper().setSolicitorReference(TEST_SOLICITOR_REFERENCE);
        caseData.getContactDetailsWrapper().setRespondentSolicitorEmail(TEST_RESP_SOLICITOR_EMAIL);
        caseData.getContactDetailsWrapper().setRespondentSolicitorName(TEST_RESP_SOLICITOR_NAME);
        caseData.getContactDetailsWrapper().setRespondentSolicitorReference(TEST_RESP_SOLICITOR_REFERENCE);
        caseData.setDivorceCaseNumber(TEST_DIVORCE_CASE_NUMBER);
        caseData.setCcdCaseType(CaseType.CONSENTED);
        caseData.getGeneralApplicationWrapper().setGeneralApplicationReferToJudgeEmail(TEST_JUDGE_EMAIL);
        caseData.getRegionWrapper().getAllocatedRegionWrapper().setRegionList(Region.MIDLANDS);
        caseData.getRegionWrapper().getAllocatedRegionWrapper().setMidlandsFrcList(RegionMidlandsFrc.NOTTINGHAM);
        caseData.getRegionWrapper().getAllocatedRegionWrapper().getDefaultCourtListWrapper()
            .setNottinghamCourtList(NottinghamCourt.NOTTINGHAM_COUNTY_COURT_AND_FAMILY_COURT);
        caseData.setBulkPrintLetterIdRes(NOTTINGHAM);
        return FinremCaseDetailsBuilderFactory.from(12345L, CaseType.CONSENTED, caseData).build();
    }
}
