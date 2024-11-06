package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetailsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.NotificationServiceConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremNotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.NotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDownloadService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckSolicitorIsDigitalService;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_CTSC_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.TestData.getConsentedFinremCaseDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.TestData.getContestedFinremCaseDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.TestData.getDefaultConsentedFinremCaseData;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.TestData.getDefaultContestedFinremCaseData;
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
    @Mock
    private EvidenceManagementDownloadService evidenceManagementDownloadService;
    @Mock
    private CourtDetailsConfiguration courtDetailsConfiguration;

    private final FinremCaseDetails consentedFinremCaseDetails = getConsentedFinremCaseDetails();
    private final FinremCaseDetails contestedFinremCaseDetails = getContestedFinremCaseDetails();
    private SolicitorCaseDataKeysWrapper dataKeysWrapper;

    @BeforeEach
    void setUp() {
        dataKeysWrapper = SolicitorCaseDataKeysWrapper.builder().build();

        NotificationRequest notificationRequest = new NotificationRequest();
        lenient().when(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(any(FinremCaseDetails.class),
            any())).thenReturn(notificationRequest);
        lenient().when(notificationRequestMapper.getNotificationRequestForConsentApplicantSolicitor(any(FinremCaseDetails.class), any()))
            .thenReturn(notificationRequest);
        lenient().when(notificationServiceConfiguration.getCtscEmail()).thenReturn(TEST_CTSC_EMAIL);

        lenient().when(finremNotificationRequestMapper
            .buildNotificationRequest(any(FinremCaseDetails.class), any())).thenReturn(notificationRequest);
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
        lenient().when(finremNotificationRequestMapper.buildNotificationRequest(any(FinremCaseDetails.class),
            any(Barrister.class))).thenReturn(notificationRequest);
        lenient().when(finremNotificationRequestMapper.buildNotificationRequest(any(FinremCaseDetails.class), any(IntervenerOne.class),
            anyString(), anyString(), anyString())).thenReturn(notificationRequest);

        Map<String, CourtDetails> courtDetailsMap = Map.of(
            "FR_s_NottinghamList_1", CourtDetails.builder().email("FRCLondon@justice.gov.uk").build(),
            "someCourt", CourtDetails.builder().email("court@example.com").build()
        );
        lenient().when(courtDetailsConfiguration.getCourts()).thenReturn(courtDetailsMap);
    }

    @Test
    void sendGeneralApplicationRejectionEmailToResSolicitor() {
        notificationService.sendGeneralApplicationRejectionEmailToResSolicitor(consentedFinremCaseDetails);
        verify(finremNotificationRequestMapper).getNotificationRequestForRespondentSolicitor(consentedFinremCaseDetails);

        verify(emailService).sendConfirmationEmail(any(), eq(FR_REJECT_GENERAL_APPLICATION));
    }

    @Test
    void sendConsentHearingNotificationEmailToRespondentSolicitor() {
        notificationService.sendConsentHearingNotificationEmailToRespondentSolicitor(consentedFinremCaseDetails, Map.of());
        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(consentedFinremCaseDetails, Map.of());

        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENTED_LIST_FOR_HEARING));
    }

    @Test
    void sendBarristerAddedEmail() {
        Barrister barrister = Barrister.builder().build();
        notificationService.sendBarristerAddedEmail(contestedFinremCaseDetails, barrister);

        verify(finremNotificationRequestMapper).buildNotificationRequest(contestedFinremCaseDetails, barrister);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_BARRISTER_ACCESS_ADDED));
    }

    @Test
    void sendConsentOrderNotApprovedSentEmailToIntervenerSolicitor() {
        notificationService.sendConsentOrderNotApprovedSentEmailToIntervenerSolicitor(consentedFinremCaseDetails, dataKeysWrapper);

        verify(finremNotificationRequestMapper).getNotificationRequestForIntervenerSolicitor(consentedFinremCaseDetails, dataKeysWrapper);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENT_ORDER_NOT_APPROVED_SENT));
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
        lenient().when(evidenceManagementDownloadService.downloadInResponseEntity(anyString(), anyString()))
            .thenReturn(ResponseEntity.ok().build());
        notificationService.sendConsentGeneralEmail(consentedFinremCaseDetails, anyString());

        verify(finremNotificationRequestMapper).getNotificationRequestForApplicantSolicitor(consentedFinremCaseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENT_GENERAL_EMAIL));
    }

    @Test
    void sendGeneralEmailConsentedWithAttachment() {
        FinremCaseData defaultFinremCaseData = getDefaultConsentedFinremCaseData();
        defaultFinremCaseData.getGeneralEmailWrapper()
            .setGeneralEmailUploadedDocument(CaseDocument.builder().documentBinaryUrl("binaryUrl").build());
        FinremCaseDetails caseDetails = getConsentedFinremCaseDetails(defaultFinremCaseData);
        lenient().when(evidenceManagementDownloadService.downloadInResponseEntity(anyString(), anyString()))
            .thenReturn(ResponseEntity.ok().build());
        notificationService.sendConsentGeneralEmail(caseDetails, anyString());

        verify(finremNotificationRequestMapper).getNotificationRequestForApplicantSolicitor(caseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENT_GENERAL_EMAIL_ATTACHMENT));
    }

    @Test
    void shouldThrowExceptionIfDownloadGeneralEmailUploadedDocumentsFailWhenSendGeneralEmailConsented() {
        FinremCaseData defaultFinremCaseData = getDefaultConsentedFinremCaseData();
        defaultFinremCaseData.getGeneralEmailWrapper()
            .setGeneralEmailUploadedDocument(CaseDocument.builder().documentBinaryUrl("binaryUrl").build());
        FinremCaseDetails caseDetails = getConsentedFinremCaseDetails(defaultFinremCaseData);
        lenient().when(evidenceManagementDownloadService.downloadInResponseEntity(anyString(), anyString()))
            .thenReturn(ResponseEntity.badRequest().build());

        assertThatThrownBy(() -> notificationService.sendConsentGeneralEmail(caseDetails, AUTH_TOKEN))
            .isInstanceOf(HttpClientErrorException.class);

        verify(finremNotificationRequestMapper).getNotificationRequestForApplicantSolicitor(caseDetails);
        verify(emailService, never()).sendConfirmationEmail(any(), eq(FR_CONSENT_GENERAL_EMAIL));
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
    void sendContestedGeneralApplicationReferToJudgeNotificationEmail_populateGeneralEmailBodyIfGeneralApplicationReferDetailExists() {
        FinremCaseData caseData = getDefaultContestedFinremCaseData();
        caseData.getGeneralApplicationWrapper().setGeneralApplicationReferDetail("TestGeneralApplicationReferDetail");
        FinremCaseDetails caseDetails = getContestedFinremCaseDetails(caseData);

        notificationService.sendContestedGeneralApplicationReferToJudgeEmail(caseDetails);

        verify(finremNotificationRequestMapper).getNotificationRequestForApplicantSolicitor(caseDetails);
        verify(emailService).sendConfirmationEmail(argThat(email -> "TestGeneralApplicationReferDetail".equals(email.getGeneralEmailBody())),
            eq(FR_CONTESTED_GENERAL_APPLICATION_REFER_TO_JUDGE));
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

    @ParameterizedTest
    @CsvSource({
        "false, true, true, , true",  // Paper app = false, represented by solicitor = true, has email, no email consent set
        "false, true, true, NO, false",  // Paper app = false, represented by solicitor = true, has email, email consent NO
        "true, true, true, , false",  // Paper app = true (invalid)
        "false, false, true, , false",  // Not represented by solicitor
        "false, true, false, , false",  // No solicitor email provided
        "false, true, true, YES, true"  // Explicit consent given
    })
    void testIsRespondentSolicitorEmailCommunicationEnabled(
        boolean isPaperApplication,
        boolean isRespondentRepresentedBySolicitor,
        boolean hasSolicitorEmail,
        String emailConsentValue,
        boolean expectedResult) {

        // Prepare case data
        Map<String, Object> caseData = new HashMap<>();
        if (hasSolicitorEmail) {
            caseData.put(RESP_SOLICITOR_EMAIL, "solicitor@example.com");
        }
        if (emailConsentValue != null) {
            caseData.put(RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT, emailConsentValue);
        }

        // Mock the service methods
        lenient().when(caseDataService.isPaperApplication(caseData)).thenReturn(isPaperApplication);
        lenient().when(caseDataService.isRespondentRepresentedByASolicitor(caseData)).thenReturn(isRespondentRepresentedBySolicitor);
        lenient().when(caseDataService.isNotEmpty(RESP_SOLICITOR_EMAIL, caseData)).thenReturn(hasSolicitorEmail);

        // Perform the test
        boolean result = notificationService.isRespondentSolicitorEmailCommunicationEnabled(caseData);

        // Assert the result
        assertEquals(expectedResult, result);
    }

    @ParameterizedTest
    @MethodSource("provideTestCases")
    void testIsContestedApplicantSolicitorEmailCommunicationEnabled(Map<String, Object> caseData,
                                                                    boolean isPaperApplication, boolean isRepresented, boolean isEmailNotEmpty,
                                                                    boolean expectedResult) {

        // Mocking caseDataService methods
        lenient().when(caseDataService.isPaperApplication(caseData)).thenReturn(isPaperApplication);
        lenient().when(caseDataService.isApplicantRepresentedByASolicitor(caseData)).thenReturn(isRepresented);
        lenient().when(caseDataService.isNotEmpty(CONTESTED_SOLICITOR_EMAIL, caseData)).thenReturn(isEmailNotEmpty);

        // Perform the test
        boolean result = notificationService.isContestedApplicantSolicitorEmailCommunicationEnabled(caseData);

        // Assert the result
        assertEquals(expectedResult, result);
    }

    // MethodSource for parameterized test cases
    static Stream<Arguments> provideTestCases() {
        Map<String, Object> caseDataWithAgreement = new HashMap<>();
        caseDataWithAgreement.put(APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED, YES_VALUE);

        Map<String, Object> caseDataWithoutAgreement = new HashMap<>();
        caseDataWithoutAgreement.put(APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED, "No");

        return Stream.of(
            // Scenario 1: All conditions are true, expect true
            Arguments.of(caseDataWithAgreement, false, true, true, true),

            // Scenario 2: Paper application, expect false
            Arguments.of(caseDataWithAgreement, true, true, true, false),

            // Scenario 3: Not represented by a solicitor, expect false
            Arguments.of(caseDataWithAgreement, false, false, true, false),

            // Scenario 4: Email is empty, expect false
            Arguments.of(caseDataWithAgreement, false, true, false, false),

            // Scenario 5: Agreement to receive emails is "No", expect false
            Arguments.of(caseDataWithoutAgreement, false, true, true, false)
        );
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
    void sendUpdateFrcInformationEmailToCourt() {
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
    void sendGeneralApplicationRejectionEmailToAppSolicitor() {
        notificationService.sendGeneralApplicationRejectionEmailToAppSolicitor(consentedFinremCaseDetails);

        verify(finremNotificationRequestMapper).getNotificationRequestForApplicantSolicitor(consentedFinremCaseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_REJECT_GENERAL_APPLICATION));
    }

    @Test
    void sendBarristerRemovedEmail() {
        notificationService.sendBarristerRemovedEmail(consentedFinremCaseDetails, Barrister.builder().build());

        verify(finremNotificationRequestMapper).buildNotificationRequest(eq(consentedFinremCaseDetails), any());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_BARRISTER_ACCESS_REMOVED));
    }

    @Test
    void sendIntervenerAddedEmail() {
        IntervenerDetails i1 = IntervenerOne.builder().build();
        notificationService.sendIntervenerAddedEmail(consentedFinremCaseDetails, i1, TEST_SOLICITOR_NAME,
            TEST_RESP_SOLICITOR_EMAIL, TEST_SOLICITOR_REFERENCE);

        verify(finremNotificationRequestMapper).buildNotificationRequest(consentedFinremCaseDetails, i1, TEST_SOLICITOR_NAME,
            TEST_RESP_SOLICITOR_EMAIL, TEST_SOLICITOR_REFERENCE);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_INTERVENER_ADDED_EMAIL));
    }

    @Test
    void sendIntervenerSolicitorAddedEmail() {
        IntervenerDetails i1 = IntervenerOne.builder().build();
        notificationService.sendIntervenerSolicitorAddedEmail(consentedFinremCaseDetails, i1, TEST_SOLICITOR_NAME,
            TEST_RESP_SOLICITOR_EMAIL, TEST_SOLICITOR_REFERENCE);

        verify(finremNotificationRequestMapper).buildNotificationRequest(consentedFinremCaseDetails, i1, TEST_SOLICITOR_NAME,
            TEST_RESP_SOLICITOR_EMAIL, TEST_SOLICITOR_REFERENCE);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_INTERVENER_SOLICITOR_ADDED_EMAIL));
    }

    @Test
    void sendIntervenerRemovedEmail() {
        IntervenerDetails i1 = IntervenerOne.builder().build();
        notificationService.sendIntervenerRemovedEmail(consentedFinremCaseDetails, i1, TEST_SOLICITOR_NAME,
            TEST_RESP_SOLICITOR_EMAIL, TEST_SOLICITOR_REFERENCE);

        verify(finremNotificationRequestMapper).buildNotificationRequest(consentedFinremCaseDetails, i1, TEST_SOLICITOR_NAME,
            TEST_RESP_SOLICITOR_EMAIL, TEST_SOLICITOR_REFERENCE);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_INTERVENER_REMOVED_EMAIL));
    }

    @Test
    void sendIntervenerSolicitorRemovedEmail() {
        IntervenerDetails i1 = IntervenerOne.builder().build();
        notificationService.sendIntervenerSolicitorRemovedEmail(consentedFinremCaseDetails, i1, TEST_SOLICITOR_NAME,
            TEST_RESP_SOLICITOR_EMAIL, TEST_SOLICITOR_REFERENCE);

        verify(finremNotificationRequestMapper).buildNotificationRequest(consentedFinremCaseDetails, i1, TEST_SOLICITOR_NAME,
            TEST_RESP_SOLICITOR_EMAIL, TEST_SOLICITOR_REFERENCE);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_INTERVENER_SOLICITOR_REMOVED_EMAIL));
    }

    @ParameterizedTest
    @CsvSource({
        "true, " + YES_VALUE + ", , true", // Contested, Solicitor agrees, Consented field null
        "false, , " + YES_VALUE + ", true", // Consented, Solicitor agrees, Contested field null
        "true, NO, , false", // Contested, Solicitor does not agree
        "false, , NO, false", // Consented, Solicitor does not agree
        "true, , , false", // Contested, No agreement provided
        "false, , , false" // Consented, No agreement provided
    })
    void testIsApplicantSolicitorAgreeToReceiveEmails(
        boolean isContestedApplication,
        String contestedValue,
        String consentedValue,
        boolean expectedResult) {

        // Prepare case data
        Map<String, Object> caseData = new HashMap<>();
        if (contestedValue != null) {
            caseData.put(APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED, contestedValue);
        }
        if (consentedValue != null) {
            caseData.put(APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED, consentedValue);
        }

        // Build the case details
        CaseDetails caseDetails = CaseDetails.builder()
            .data(caseData)
            .build();

        // Mock case type
        when(caseDataService.isContestedApplication(caseDetails)).thenReturn(isContestedApplication);

        // Perform test
        boolean result = notificationService.isApplicantSolicitorAgreeToReceiveEmails(caseDetails);

        // Assert the result
        assertEquals(expectedResult, result);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testIsApplicantSolicitorResponsibleToDraftOrder(boolean isResponsible) {
        // Prepare mock data
        Map<String, Object> caseData = new HashMap<>();

        // Mock the service method
        when(caseDataService.isApplicantSolicitorResponsibleToDraftOrder(caseData)).thenReturn(isResponsible);

        // Perform the test
        boolean result = notificationService.isApplicantSolicitorResponsibleToDraftOrder(caseData);

        // Assert the result
        assertEquals(isResponsible, result);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testIsRespondentSolicitorResponsibleToDraftOrder(boolean isResponsible) {
        // Prepare mock data
        Map<String, Object> caseData = new HashMap<>();

        // Mock the service method
        when(caseDataService.isRespondentSolicitorResponsibleToDraftOrder(caseData)).thenReturn(isResponsible);

        // Perform the test
        boolean result = notificationService.isRespondentSolicitorResponsibleToDraftOrder(caseData);

        // Assert the result
        assertEquals(isResponsible, result);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testIsContestedApplication(boolean isContested) {
        // Prepare mock FinremCaseDetails
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().build();

        // Mock the service method
        when(caseDataService.isContestedApplication(caseDetails)).thenReturn(isContested);

        // Perform the test
        boolean result = notificationService.isContestedApplication(caseDetails);

        // Assert the result
        assertEquals(isContested, result);
    }

    @ParameterizedTest
    @MethodSource("provideCourtDetailsTestCases")
    void testGetRecipientEmailFromSelectedCourt(String selectedAllocatedCourt, String expectedEmail) {
        String result = notificationService.getRecipientEmailFromSelectedCourt(selectedAllocatedCourt);
        assertEquals(expectedEmail, result);
    }

    // MethodSource for the parameterized test
    static Stream<Arguments> provideCourtDetailsTestCases() {
        return Stream.of(
            // Scenario: valid court email is found, no error log expected
            Arguments.of("someCourt", "court@example.com"),

            // Scenario: court details not found, log the error
            Arguments.of("nonExistentCourt", "fr_applicant_solicitor1@mailinator.com")
        );
    }

    @ParameterizedTest
    @MethodSource("provideTestCases_testShouldEmailRespondentSolicitor")
    void testShouldEmailRespondentSolicitor(Map<String, Object> caseData, boolean isRepresented, boolean isEmailNotEmpty, boolean expectedResult) {

        // Mocking caseDataService methods
        lenient().when(caseDataService.isRespondentRepresentedByASolicitor(caseData)).thenReturn(isRepresented);
        lenient().when(caseDataService.isNotEmpty(RESP_SOLICITOR_EMAIL, caseData)).thenReturn(isEmailNotEmpty);

        // Perform the test
        boolean result = notificationService.shouldEmailRespondentSolicitor(caseData);

        // Assert the result
        assertEquals(expectedResult, result);
    }

    // MethodSource for parameterized test cases
    static Stream<Arguments> provideTestCases_testShouldEmailRespondentSolicitor() {
        Map<String, Object> caseDataWithEmail = new HashMap<>();
        caseDataWithEmail.put(RESP_SOLICITOR_EMAIL, "respSolicitor@example.com");

        Map<String, Object> caseDataWithoutEmail = new HashMap<>(); // No email scenario

        return Stream.of(
            // Scenario 1: Respondent is represented by a solicitor and email is present, expect true
            Arguments.of(caseDataWithEmail, true, true, true),

            // Scenario 2: Respondent is represented by a solicitor but email is empty, expect false
            Arguments.of(caseDataWithEmail, true, false, false),

            // Scenario 3: Respondent is not represented by a solicitor, expect false
            Arguments.of(caseDataWithEmail, false, true, false),

            // Scenario 4: Respondent is not represented and email is empty, expect false
            Arguments.of(caseDataWithoutEmail, false, false, false)
        );
    }

    @Mock
    private FinremCaseDetails mockCaseDetails;
    @Mock
    private FinremCaseData mockCaseData;

    @ParameterizedTest
    @CsvSource({
        "true, true",   // Scenario 1: Solicitor data is populated, expect true
        "false, false"  // Scenario 2: Solicitor data is not populated, expect false
    })
    void testIsApplicantSolicitorEmailPopulated(boolean isSolicitorPopulated, boolean expectedResult) {
        // Mock the behavior of caseData
        when(mockCaseDetails.getData()).thenReturn(mockCaseData);
        when(mockCaseData.isApplicantSolicitorPopulated()).thenReturn(isSolicitorPopulated);

        // Perform the test
        boolean result = notificationService.isApplicantSolicitorEmailPopulated(mockCaseDetails);

        // Assert the result
        assertEquals(expectedResult, result);
    }
}
