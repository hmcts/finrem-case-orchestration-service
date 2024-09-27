package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.NotificationServiceConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremNotificationRequestMapper;
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
    private FinremNotificationRequestMapper notificationRequestMapper;
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

    private FinremCallbackRequest callbackRequest;
    private FinremCallbackRequest newCallbackRequest;
    private SolicitorCaseDataKeysWrapper dataKeysWrapper;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        callbackRequest = getConsentedNewCallbackRequest();
        newCallbackRequest = getConsentedNewCallbackRequest();
        dataKeysWrapper = SolicitorCaseDataKeysWrapper.builder().build();

        NotificationRequest notificationRequest = new NotificationRequest();
        lenient().when(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(any(FinremCaseDetails.class)))
            .thenReturn(notificationRequest);
        lenient().when(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(any(FinremCaseDetails.class), anyBoolean()))
            .thenReturn(notificationRequest);
        lenient().when(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(any(FinremCaseDetails.class)))
            .thenReturn(notificationRequest);
        lenient().when(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(any(FinremCaseDetails.class), anyBoolean()))
            .thenReturn(notificationRequest);
        lenient().when(notificationRequestMapper.getNotificationRequestForIntervenerSolicitor(any(FinremCaseDetails.class),
            any(SolicitorCaseDataKeysWrapper.class))).thenReturn(notificationRequest);
        lenient().when(notificationServiceConfiguration.getCtscEmail()).thenReturn(TEST_CTSC_EMAIL);
        lenient().when(objectMapper.readValue(getCourtDetailsString(), HashMap.class))
            .thenReturn(new HashMap(Map.of("email", "FRCLondon@justice.gov.uk")));
    }

    @Test
    void sendHwfSuccessfulNotificationEmail() {
        notificationService.sendConsentedHWFSuccessfulConfirmationEmail(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_HWF_SUCCESSFUL));
    }

    @Test
    void sendAssignToJudgeNotificationEmailToApplicantSolicitor() {
        notificationService.sendAssignToJudgeConfirmationEmailToApplicantSolicitor(newCallbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(newCallbackRequest.getCaseDetails(), true);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_ASSIGNED_TO_JUDGE));
    }

    @Test
    void sendAssignToJudgeNotificationEmailToRespondentSolicitor() {
        notificationService.sendAssignToJudgeConfirmationEmailToRespondentSolicitor(newCallbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(newCallbackRequest.getCaseDetails(), true);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_ASSIGNED_TO_JUDGE));
    }

    @Test
    void sendConsentOrderMadeConfirmationEmailToApplicantSolicitor() {
        notificationService.sendConsentOrderMadeConfirmationEmailToApplicantSolicitor(newCallbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(newCallbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENT_ORDER_MADE));
    }

    @Test
    void sendConsentOrderMadeConfirmationEmailToRespondentSolicitor() {
        notificationService.sendConsentOrderMadeConfirmationEmailToRespondentSolicitor(newCallbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(newCallbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENT_ORDER_MADE));
    }

    @Test
    void sendPrepareForHearingNotificationEmailToApplicantSolicitor() {
        callbackRequest = getContestedNewCallbackRequest();

        notificationService.sendPrepareForHearingEmailApplicant(newCallbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(newCallbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_PREPARE_FOR_HEARING));
    }

    @Test
    void sendPrepareForHearingNotificationEmailToRespondentSolicitor() {
        callbackRequest = getContestedNewCallbackRequest();

        notificationService.sendPrepareForHearingEmailRespondent(newCallbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(newCallbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_PREPARE_FOR_HEARING));
    }

    @Test
    void sendConsentOrderNotApprovedNotificationEmailToApplicantSolicitor() {
        notificationService.sendConsentOrderNotApprovedEmailToApplicantSolicitor(newCallbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(newCallbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENT_ORDER_NOT_APPROVED));
    }

    @Test
    void sendConsentOrderNotApprovedNotificationEmailToRespondentSolicitor() {
        notificationService.sendConsentOrderNotApprovedEmailToRespondentSolicitor(newCallbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(newCallbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENT_ORDER_NOT_APPROVED));
    }

    @Test
    void sendConsentOrderAvailableNotificationEmail() {
        notificationService.sendConsentOrderAvailableEmailToApplicantSolicitor(newCallbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(newCallbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENT_ORDER_AVAILABLE));
    }

    @Test
    void sendConsentOrderAvailableNotificationCtscEmail() {
        notificationService.sendConsentOrderAvailableCtscEmail(newCallbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(newCallbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENT_ORDER_AVAILABLE_CTSC));
    }

    @Test
    void sendContestedApplicationIssuedEmail() {
        notificationService.sendContestedApplicationIssuedEmailToApplicantSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_APPLICATION_ISSUED));
    }

    @Test
    void sendContestOrderApprovedEmailApplicantSolicitor() {
        notificationService.sendContestOrderApprovedEmailApplicant(newCallbackRequest.getCaseDetails());

        verify(notificationRequestMapper)
            .getNotificationRequestForApplicantSolicitor(newCallbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTEST_ORDER_APPROVED_APPLICANT));
    }

    @Test
    void sendContestOrderApprovedEmailRespondentSolicitor() {
        notificationService.sendContestOrderApprovedEmailRespondent(newCallbackRequest.getCaseDetails());

        verify(notificationRequestMapper)
            .getNotificationRequestForRespondentSolicitor(newCallbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTEST_ORDER_APPROVED_RESPONDENT));
    }

    @Test
    void sendFinremContestOrderApprovedEmailIntervener1Solicitor() {
        notificationService.sendContestOrderApprovedEmailIntervener(newCallbackRequest.getCaseDetails(),
            dataKeysWrapper, IntervenerType.INTERVENER_ONE);

        verify(notificationRequestMapper)
            .getNotificationRequestForIntervenerSolicitor(newCallbackRequest.getCaseDetails(), dataKeysWrapper);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTEST_ORDER_APPROVED_INTERVENER1));
    }

    @Test
    void sendFinremContestOrderApprovedEmailIntervener2Solicitor() {
        notificationService.sendContestOrderApprovedEmailIntervener(newCallbackRequest.getCaseDetails(),
            dataKeysWrapper, IntervenerType.INTERVENER_TWO);

        verify(notificationRequestMapper)
            .getNotificationRequestForIntervenerSolicitor(newCallbackRequest.getCaseDetails(), dataKeysWrapper);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTEST_ORDER_APPROVED_INTERVENER2));
    }

    @Test
    void sendFinremContestOrderApprovedEmailIntervener3Solicitor() {
        notificationService.sendContestOrderApprovedEmailIntervener(newCallbackRequest.getCaseDetails(),
            dataKeysWrapper, IntervenerType.INTERVENER_THREE);

        verify(notificationRequestMapper)
            .getNotificationRequestForIntervenerSolicitor(newCallbackRequest.getCaseDetails(), dataKeysWrapper);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTEST_ORDER_APPROVED_INTERVENER3));
    }

    @Test
    void sendFinremContestOrderApprovedEmailIntervener4Solicitor() {
        notificationService.sendContestOrderApprovedEmailIntervener(newCallbackRequest.getCaseDetails(),
            dataKeysWrapper, IntervenerType.INTERVENER_FOUR);

        verify(notificationRequestMapper)
            .getNotificationRequestForIntervenerSolicitor(newCallbackRequest.getCaseDetails(), dataKeysWrapper);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTEST_ORDER_APPROVED_INTERVENER4));
    }

    @Test
    void sendSolicitorToDraftOrderEmailRespondent() {
        notificationService.sendSolicitorToDraftOrderEmailRespondent(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_DRAFT_ORDER));
    }

    @Test
    void sendSolicitorToDraftOrderEmailApplicant() {
        notificationService.sendSolicitorToDraftOrderEmailApplicant(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_DRAFT_ORDER));
    }

    @Test
    void sendContestedHwfSuccessfulNotificationEmail() {
        callbackRequest = getContestedNewCallbackRequest();
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(newCallbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(newCallbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_HWF_SUCCESSFUL));
    }

    @Test
    void sendGeneralEmailConsented() {
        notificationService.sendConsentGeneralEmail(callbackRequest.getCaseDetails(), anyString());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENT_GENERAL_EMAIL));
    }

    @Test
    void sendGeneralEmailContested() {
        notificationService.sendContestedGeneralEmail(callbackRequest.getCaseDetails(), anyString());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_GENERAL_EMAIL));
    }

    @Test
    void sendContestOrderNotApprovedNotificationEmailApplicant() {
        notificationService.sendContestOrderNotApprovedEmailApplicant(newCallbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(newCallbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTEST_ORDER_NOT_APPROVED));
    }

    @Test
    void sendContestOrderNotApprovedNotificationEmailRespondent() {
        notificationService.sendContestOrderNotApprovedEmailRespondent(newCallbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(newCallbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTEST_ORDER_NOT_APPROVED));
    }

    @Test
    void sendContestedConsentOrderApprovedNotificationEmailToApplicantSolicitor() {
        notificationService.sendContestedConsentOrderApprovedEmailToApplicantSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_CONSENT_ORDER_APPROVED));
    }

    @Test
    void sendContestedConsentOrderApprovedNotificationEmailToRespondentSolicitor() {
        notificationService.sendContestedConsentOrderApprovedEmailToRespondentSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_CONSENT_ORDER_APPROVED));
    }

    @Test
    void sendContestedGeneralApplicationReferToJudgeNotificationEmail() {
        callbackRequest = getContestedNewCallbackRequest();

        notificationService.sendContestedGeneralApplicationReferToJudgeEmail(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_GENERAL_APPLICATION_REFER_TO_JUDGE));
    }

    @Test
    void sendContestedGeneralApplicationOutcomeNotificationEmailWhenSendToFRCToggleTrue() {
        when(featureToggleService.isSendToFRCEnabled()).thenReturn(true);

        callbackRequest = getContestedNewCallbackRequest();

        notificationService.sendContestedGeneralApplicationOutcomeEmail(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_GENERAL_APPLICATION_OUTCOME));
    }

    @Test
    void sendContestedGeneralApplicationOutcomeNotificationEmailToTestAccountWhenSendToFRCToggleFalse() {
        when(featureToggleService.isSendToFRCEnabled()).thenReturn(false);

        callbackRequest = getContestedNewCallbackRequest();

        notificationService.sendContestedGeneralApplicationOutcomeEmail(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_GENERAL_APPLICATION_OUTCOME));
    }

    @Test
    void sendContestedConsentGeneralOrderNotificationEmailApplicantSolicitor() {
        notificationService.sendContestedConsentGeneralOrderEmailApplicantSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_GENERAL_ORDER_CONSENT));
    }

    @Test
    void sendContestedConsentGeneralOrderNotificationEmailRespondentSolicitor() {
        notificationService.sendContestedConsentGeneralOrderEmailRespondentSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_GENERAL_ORDER_CONSENT));
    }

    @Test
    void sendContestedGeneralOrderNotificationEmailApplicant() {
        notificationService.sendContestedGeneralOrderEmailApplicant(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_GENERAL_ORDER));
    }

    @Test
    void sendContestedGeneralOrderNotificationEmailRespondent() {
        notificationService.sendContestedGeneralOrderEmailRespondent(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_GENERAL_ORDER));
    }

    @Test
    void sendConsentedGeneralOrderNotificationEmailApplicant() {
        notificationService.sendConsentedGeneralOrderEmailToApplicantSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENTED_GENERAL_ORDER));
    }

    @Test
    void sendConsentedGeneralOrderNotificationEmailRespondent() {
        notificationService.sendConsentedGeneralOrderEmailToRespondentSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENTED_GENERAL_ORDER));
    }

    @Test
    void sendContestedConsentOrderNotApprovedNotificationEmail() {
        notificationService.sendContestedConsentOrderNotApprovedEmailApplicantSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_CONSENT_ORDER_NOT_APPROVED));
    }

    @Test
    void sendContestedConsentOrderNotApprovedNotificationEmailToRespondentSolicitor() {
        notificationService.sendContestedConsentOrderNotApprovedEmailRespondentSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_CONSENT_ORDER_NOT_APPROVED));
    }

    @Test
    void sendConsentOrderNotApprovedSentEmailToApplicantSolicitor() {
        notificationService.sendConsentOrderNotApprovedSentEmailToApplicantSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENT_ORDER_NOT_APPROVED_SENT));
    }

    @Test
    void sendConsentOrderNotApprovedSentEmailToRespondentSolicitor() {
        notificationService.sendConsentOrderNotApprovedSentEmailToRespondentSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
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
        notificationService.sendTransferToLocalCourtEmail(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_TRANSFER_TO_LOCAL_COURT));
    }

    @Test
    void sendInterimNotificationEmailToApplicantSolicitor() {
        callbackRequest = getContestedNewCallbackRequest();

        notificationService.sendInterimNotificationEmailToApplicantSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_INTERIM_HEARING));
    }

    @Test
    void sendInterimNotificationEmailToRespondentSolicitor() {
        callbackRequest = getContestedNewCallbackRequest();

        notificationService.sendInterimNotificationEmailToRespondentSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_INTERIM_HEARING));
    }

    @Test
    void givenContestedCaseWhenSendNoticeOfChangeEmailThenSendNoticeOfChangeContestedEmail() {
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setName(TEST_SOLICITOR_NAME);
        notificationRequest.setNotificationEmail("test@test.com");
        when(notificationRequestMapper.getNotificationRequestForNoticeOfChange(any())).thenReturn(notificationRequest);
        FinremCaseDetails caseDetails = getContestedNewCallbackRequest().getCaseDetails();

        notificationService.sendNoticeOfChangeEmail(caseDetails);

        verify(notificationRequestMapper).getNotificationRequestForNoticeOfChange(caseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_NOTICE_OF_CHANGE));
    }

    @Test
    void givenConsentedCaseWhenSendNoticeOfChangeEmailThenSendNoticeOfChangeContestedEmail() {
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setName(TEST_SOLICITOR_NAME);
        notificationRequest.setNotificationEmail("test@test.com");
        when(notificationRequestMapper.getNotificationRequestForNoticeOfChange(any())).thenReturn(notificationRequest);
        FinremCaseDetails caseDetails = getConsentedNewCallbackRequest().getCaseDetails();

        notificationService.sendNoticeOfChangeEmail(caseDetails);

        verify(notificationRequestMapper).getNotificationRequestForNoticeOfChange(caseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENTED_NOTICE_OF_CHANGE));
    }

    @Test
    void givenContestedCase_whenSendNoCCaseworkerEmail_thenSendContestedEmail() {
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setName(TEST_SOLICITOR_NAME);
        when(notificationRequestMapper.getNotificationRequestForNoticeOfChange(any())).thenReturn(notificationRequest);
        when(checkSolicitorIsDigitalService.isApplicantSolicitorDigital(anyString())).thenReturn(true);
        FinremCaseDetails caseDetails = getContestedNewCallbackRequest().getCaseDetails();

        notificationService.sendNoticeOfChangeEmailCaseworker(caseDetails);

        verify(notificationRequestMapper).getNotificationRequestForNoticeOfChange(caseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_NOC_CASEWORKER));
    }

    @Test
    void givenConsentedCase_whenSendNoCCaseworkerEmail_thenSendConsentedEmail() {
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setName(TEST_SOLICITOR_NAME);
        when(notificationRequestMapper.getNotificationRequestForNoticeOfChange(any())).thenReturn(notificationRequest);
        when(checkSolicitorIsDigitalService.isApplicantSolicitorDigital(anyString())).thenReturn(true);
        FinremCaseDetails caseDetails = getConsentedNewCallbackRequest().getCaseDetails();

        notificationService.sendNoticeOfChangeEmailCaseworker(caseDetails);

        verify(notificationRequestMapper).getNotificationRequestForNoticeOfChange(caseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENTED_NOC_CASEWORKER));
    }

    @Test
    void givenContestedCaseAndNonDigitalSol_whenSendNocEmail_thenNotSendContestedEmailCaseworker() {
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setName(TEST_SOLICITOR_NAME);
        when(notificationRequestMapper.getNotificationRequestForNoticeOfChange(any())).thenReturn(notificationRequest);
        when(checkSolicitorIsDigitalService.isApplicantSolicitorDigital(anyString())).thenReturn(false);
        FinremCaseDetails caseDetails = getContestedNewCallbackRequest().getCaseDetails();

        notificationService.sendNoticeOfChangeEmailCaseworker(caseDetails);

        verify(notificationRequestMapper).getNotificationRequestForNoticeOfChange(caseDetails);
        verifyNoMoreInteractions(emailService);
    }

    @Test
    void sendUpdateFrcInformationEmailToAppSolicitor() {
        newCallbackRequest = getContestedNewCallbackRequest();

        notificationService.sendUpdateFrcInformationEmailToAppSolicitor(newCallbackRequest.getCaseDetails());
        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(newCallbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_UPDATE_FRC_SOL));
    }

    @Test
    void sendUpdateFrcInformationEmailToRespSolicitor() {
        newCallbackRequest = getContestedNewCallbackRequest();

        notificationService.sendUpdateFrcInformationEmailToRespondentSolicitor(newCallbackRequest.getCaseDetails());
        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(newCallbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_UPDATE_FRC_SOL));
    }

    @Test
    void sendUpdateFrcInformationEmailToCourt() throws JsonProcessingException {
        when(featureToggleService.isSendToFRCEnabled()).thenReturn(true);

        newCallbackRequest = getContestedNewCallbackRequest();

        notificationService.sendUpdateFrcInformationEmailToCourt(newCallbackRequest.getCaseDetails());
        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(newCallbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_UPDATE_FRC_COURT));
    }

    @Test
    void sendConsentOrderAvailableEmailToIntervenerSolicitor() {
        notificationService.sendConsentOrderAvailableEmailToIntervenerSolicitor(callbackRequest.getCaseDetails(),
            dataKeysWrapper);

        verify(notificationRequestMapper).getNotificationRequestForIntervenerSolicitor(callbackRequest.getCaseDetails(), dataKeysWrapper);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENT_ORDER_AVAILABLE));
    }

    @Test
    void sendPrepareForHearingAfterSentNotificationEmailRespondent() {
        notificationService.sendPrepareForHearingOrderSentEmailRespondent(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_PREPARE_FOR_HEARING_ORDER_SENT));
    }

    protected FinremCallbackRequest getContestedNewCallbackRequest() {
        FinremCaseData caseData = getFinremCaseData();
        caseData.getContactDetailsWrapper().setRespondentFmName("David");
        caseData.getContactDetailsWrapper().setRespondentLname("Goodman");
        caseData.setCcdCaseType(CaseType.CONTESTED);
        return FinremCallbackRequest.builder()
            .caseDetails(FinremCaseDetails.builder()
                .caseType(CaseType.CONTESTED)
                .id(12345L)
                .data(caseData)
                .build())
            .build();
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

    protected FinremCallbackRequest getConsentedNewCallbackRequest() {
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
        return FinremCallbackRequest.builder()
            .caseDetails(FinremCaseDetails.builder()
                .caseType(CaseType.CONSENTED)
                .id(12345L)
                .data(caseData)
                .build())
            .build();
    }
}
