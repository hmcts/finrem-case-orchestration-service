package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetailsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.NotificationServiceConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedHearingHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremNotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.NotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentedHearingDataWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NottinghamCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionMidlandsFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralEmailWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFour;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThree;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerChangeDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDownloadService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckSolicitorIsDigitalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogs;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_JUDGE_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_LETTER_ID_RES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_REFER_TO_JUDGE_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INCLUDES_REPRESENTATIVE_UPDATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.UPDATE_CONTACT_DETAILS_EVENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_ASSIGNED_TO_JUDGE;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_DRAFT_ORDER_OR_PSA_REFUSED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_DRAFT_ORDER_READY_FOR_REVIEW_ADMIN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_DRAFT_ORDER_READY_FOR_REVIEW_JUDGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_DRAFT_ORDER_REVIEW_OVERDUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_GENERAL_APPLICATION_OUTCOME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_GENERAL_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_GENERAL_EMAIL_ATTACHMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_GENERAL_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_GENERAL_ORDER_CONSENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_HEARING_NOTIFICATION_SOLICITOR;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_REJECT_GENERAL_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_TRANSFER_TO_LOCAL_COURT;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    private static final String TEST_USER_EMAIL = "fr_applicant_sol@sharklasers.com";
    private static final String INTERVENER_SOL_EMAIL = "intervenerSol@email.com";

    private static final String INTERIM_HEARING_JSON = "/fixtures/contested/interim-hearing-two-old-two-new-collections.json";
    private static final String CONSENTED_HEARING_JSON = "/fixtures/consented.listOfHearing/list-for-hearing.json";

    @TestLogs
    private final TestLogger logs = new TestLogger(NotificationService.class);
    @InjectMocks
    private NotificationService notificationService;
    @Mock
    private EmailService emailService;
    private ConsentedHearingHelper helper;
    @Mock
    private CourtDetailsConfiguration courtDetailsConfiguration;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private NotificationRequestMapper notificationRequestMapper;
    @Mock
    private FinremNotificationRequestMapper finremNotificationRequestMapper;
    @Mock
    private CheckSolicitorIsDigitalService checkSolicitorIsDigitalService;
    @Mock
    private EvidenceManagementDownloadService evidenceManagementDownloadService;
    @Mock
    private CaseDataService caseDataService;
    @Mock
    private NotificationServiceConfiguration notificationServiceConfiguration;

    protected ObjectMapper mapper;
    private NotificationRequest notificationRequest;
    private SolicitorCaseDataKeysWrapper dataKeysWrapper;

    @BeforeEach
    void setUp() {
        notificationRequest = new NotificationRequest();
        dataKeysWrapper = SolicitorCaseDataKeysWrapper.builder().build();
        lenient().when(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(any(CaseDetails.class))).thenReturn(notificationRequest);
        lenient().when(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(any(CaseDetails.class))).thenReturn(notificationRequest);
        lenient().when(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(any(CaseDetails.class)))
            .thenReturn(notificationRequest);
        lenient().when(notificationRequestMapper.getNotificationRequestForIntervenerSolicitor(any(CaseDetails.class),
            any(SolicitorCaseDataKeysWrapper.class))).thenReturn(notificationRequest);
        lenient().when(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(any(FinremCaseDetails.class)))
            .thenReturn(notificationRequest);
        lenient().when(finremNotificationRequestMapper.getNotificationRequestForRespondentSolicitor(any(FinremCaseDetails.class)))
            .thenReturn(notificationRequest);
        lenient().when(finremNotificationRequestMapper.getNotificationRequestForIntervenerSolicitor(any(FinremCaseDetails.class),
            any(SolicitorCaseDataKeysWrapper.class))).thenReturn(notificationRequest);
        lenient().when(evidenceManagementDownloadService.downloadInResponseEntity(anyString(), anyString()))
            .thenReturn(ResponseEntity.status(HttpStatus.OK).body(new ByteArrayResource(new byte[2048])));
        mapper = JsonMapper
            .builder()
            .addModule(new JavaTimeModule())
            .addModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES))
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();
        helper = new ConsentedHearingHelper(mapper);
    }

    @Test
    void sendHwfSuccessfulNotificationEmail() {
        CallbackRequest callbackRequest = getConsentedCallbackRequest();
        notificationService.sendConsentedHWFSuccessfulConfirmationEmail(callbackRequest.getCaseDetails());
        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_HWF_SUCCESSFUL);
    }

    @Test
    void sendFinremAssignToJudgeNotificationEmailToIntervenerSolicitor() {
        FinremCallbackRequest finremCallbackRequest = getContestedNewCallbackRequest();
        notificationService.sendAssignToJudgeConfirmationEmailToIntervenerSolicitor(finremCallbackRequest.getCaseDetails(),
            dataKeysWrapper);
        verify(finremNotificationRequestMapper).getNotificationRequestForIntervenerSolicitor(finremCallbackRequest.getCaseDetails(),
            dataKeysWrapper);
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_ASSIGNED_TO_JUDGE);
    }

    @Test
    void sendConsentOrderMadeConfirmationEmailToApplicantSolicitor() {
        CallbackRequest callbackRequest = getConsentedCallbackRequest();
        notificationService.sendConsentOrderMadeConfirmationEmailToApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONSENT_ORDER_MADE);
    }

    @Test
    void sendConsentOrderMadeConfirmationEmailToRespondentSolicitor() {
        CallbackRequest callbackRequest = getConsentedCallbackRequest();
        notificationService.sendConsentOrderMadeConfirmationEmailToRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONSENT_ORDER_MADE);
    }

    @Test
    void sendConsentOrderMadeConfirmationEmailToIntervenerSolicitor() {
        CallbackRequest callbackRequest = getConsentedCallbackRequest();
        notificationService.sendConsentOrderMadeConfirmationEmailToIntervenerSolicitor(callbackRequest.getCaseDetails(),
            dataKeysWrapper);
        verify(notificationRequestMapper).getNotificationRequestForIntervenerSolicitor(callbackRequest.getCaseDetails(),
            dataKeysWrapper);
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONSENT_ORDER_MADE);
    }

    @Test
    void sendFinremConsentOrderMadeConfirmationEmailToIntervenerSolicitor() {
        FinremCallbackRequest finremCallbackRequest = getContestedNewCallbackRequest();
        notificationService.sendConsentOrderMadeConfirmationEmailToIntervenerSolicitor(finremCallbackRequest.getCaseDetails(),
            dataKeysWrapper);
        verify(finremNotificationRequestMapper).getNotificationRequestForIntervenerSolicitor(finremCallbackRequest.getCaseDetails(),
            dataKeysWrapper);
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONSENT_ORDER_MADE);
    }

    @Test
    void sendPrepareForHearingNotificationEmailToApplicantSolicitor() {
        CallbackRequest callbackRequest = getContestedCallbackRequest();
        notificationService.sendPrepareForHearingEmailApplicant(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_PREPARE_FOR_HEARING);
    }

    @Test
    void sendPrepareForHearingNotificationEmailToRespondentSolicitor() {
        CallbackRequest callbackRequest = getContestedCallbackRequest();
        notificationService.sendPrepareForHearingEmailRespondent(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_PREPARE_FOR_HEARING);
    }

    @Test
    void sendPrepareForHearingNotificationEmailToIntervenerSolicitor() {
        CallbackRequest callbackRequest = getContestedCallbackRequest();
        notificationService.sendPrepareForHearingEmailIntervener(callbackRequest.getCaseDetails(), dataKeysWrapper);

        verify(notificationRequestMapper).getNotificationRequestForIntervenerSolicitor(callbackRequest.getCaseDetails(),
            dataKeysWrapper);
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_PREPARE_FOR_HEARING_INTERVENER_SOL);
    }

    @Test
    void sendFinremPrepareForHearingNotificationEmailToIntervenerSolicitor() {
        FinremCallbackRequest finremCallbackRequest = getContestedNewCallbackRequest();
        notificationService.sendPrepareForHearingEmailIntervener(finremCallbackRequest.getCaseDetails(), dataKeysWrapper);

        verify(finremNotificationRequestMapper).getNotificationRequestForIntervenerSolicitor(finremCallbackRequest.getCaseDetails(),
            dataKeysWrapper);
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_PREPARE_FOR_HEARING_INTERVENER_SOL);
    }

    @Test
    void sendPrepareForHearingAfterSentNotificationEmailApplicant() {
        CallbackRequest callbackRequest = getContestedCallbackRequest();
        notificationService.sendPrepareForHearingOrderSentEmailApplicant(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_PREPARE_FOR_HEARING_ORDER_SENT);
    }

    @Test
    void sendPrepareForHearingAfterSentNotificationEmailRespondent() {
        CallbackRequest callbackRequest = getContestedCallbackRequest();
        notificationService.sendPrepareForHearingOrderSentEmailRespondent(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_PREPARE_FOR_HEARING_ORDER_SENT);
    }

    @Test
    void sendConsentOrderNotApprovedNotificationEmailToApplicantSolicitor() {
        CallbackRequest callbackRequest = getConsentedCallbackRequest();
        notificationService.sendConsentOrderNotApprovedEmailToApplicantSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONSENT_ORDER_NOT_APPROVED);
    }

    @Test
    void sendConsentOrderNotApprovedNotificationEmailToRespondentSolicitor() {
        CallbackRequest callbackRequest = getConsentedCallbackRequest();
        notificationService.sendConsentOrderNotApprovedEmailToRespondentSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONSENT_ORDER_NOT_APPROVED);
    }

    @Test
    void sendConsentOrderAvailableApplicantSolNotificationEmail() {
        CallbackRequest callbackRequest = getConsentedCallbackRequest();
        notificationService.sendConsentOrderAvailableEmailToApplicantSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONSENT_ORDER_AVAILABLE);
    }

    @Test
    void sendConsentOrderAvailableRespondentSolNotificationEmail() {
        CallbackRequest callbackRequest = getConsentedCallbackRequest();
        notificationService.sendConsentOrderAvailableEmailToRespondentSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONSENT_ORDER_AVAILABLE);
    }

    @Test
    void sendConsentOrderAvailableEmailToIntervenerSolicitor() {
        CallbackRequest callbackRequest = getConsentedCallbackRequest();
        notificationService.sendConsentOrderAvailableEmailToIntervenerSolicitor(callbackRequest.getCaseDetails(),
            dataKeysWrapper);

        verify(notificationRequestMapper).getNotificationRequestForIntervenerSolicitor(callbackRequest.getCaseDetails(), dataKeysWrapper);
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONSENT_ORDER_AVAILABLE);
    }

    @Test
    void sendConsentOrderAvailableNotificationCtscEmail() {
        CallbackRequest callbackRequest = getConsentedCallbackRequest();

        when(notificationServiceConfiguration.getCtscEmail()).thenReturn("ctsc@email.com");

        notificationService.sendConsentOrderAvailableCtscEmail(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONSENT_ORDER_AVAILABLE_CTSC);
    }

    @Test
    void sendContestedApplicationIssuedEmail() {
        CallbackRequest callbackRequest = getConsentedCallbackRequest();
        notificationService.sendContestedApplicationIssuedEmailToApplicantSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_APPLICATION_ISSUED);
    }

    @Test
    void sendContestOrderApprovedEmailApplicantSolicitor() {
        CallbackRequest callbackRequest = getConsentedCallbackRequest();
        notificationService.sendContestOrderApprovedEmailApplicant(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper)
            .getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTEST_ORDER_APPROVED_APPLICANT);
    }

    @Test
    void sendContestOrderApprovedEmailRespondentSolicitor() {
        CallbackRequest callbackRequest = getConsentedCallbackRequest();
        notificationService.sendContestOrderApprovedEmailRespondent(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper)
            .getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTEST_ORDER_APPROVED_RESPONDENT);
    }

    @Test
    void sendContestOrderApprovedEmailIntervener1Solicitor() {
        CallbackRequest callbackRequest = getConsentedCallbackRequest();
        notificationService.sendContestOrderApprovedEmailIntervener(callbackRequest.getCaseDetails(),
            dataKeysWrapper, IntervenerType.INTERVENER_ONE);

        verify(notificationRequestMapper, timeout(100).times(1))
            .getNotificationRequestForIntervenerSolicitor(callbackRequest.getCaseDetails(), dataKeysWrapper);
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTEST_ORDER_APPROVED_INTERVENER1);
    }

    @Test
    void sendContestOrderApprovedEmailIntervener2Solicitor() {
        CallbackRequest callbackRequest = getConsentedCallbackRequest();
        notificationService.sendContestOrderApprovedEmailIntervener(callbackRequest.getCaseDetails(),
            dataKeysWrapper, IntervenerType.INTERVENER_TWO);

        verify(notificationRequestMapper, timeout(100).times(1))
            .getNotificationRequestForIntervenerSolicitor(callbackRequest.getCaseDetails(), dataKeysWrapper);
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTEST_ORDER_APPROVED_INTERVENER2);
    }

    @Test
    void sendContestOrderApprovedEmailIntervener3Solicitor() {
        CallbackRequest callbackRequest = getConsentedCallbackRequest();
        notificationService.sendContestOrderApprovedEmailIntervener(callbackRequest.getCaseDetails(),
            dataKeysWrapper, IntervenerType.INTERVENER_THREE);

        verify(notificationRequestMapper, timeout(100).times(1))
            .getNotificationRequestForIntervenerSolicitor(callbackRequest.getCaseDetails(), dataKeysWrapper);
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTEST_ORDER_APPROVED_INTERVENER3);
    }

    @Test
    void sendContestOrderApprovedEmailIntervener4Solicitor() {
        CallbackRequest callbackRequest = getConsentedCallbackRequest();
        notificationService.sendContestOrderApprovedEmailIntervener(callbackRequest.getCaseDetails(),
            dataKeysWrapper, IntervenerType.INTERVENER_FOUR);

        verify(notificationRequestMapper, timeout(100).times(1))
            .getNotificationRequestForIntervenerSolicitor(callbackRequest.getCaseDetails(), dataKeysWrapper);
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTEST_ORDER_APPROVED_INTERVENER4);
    }

    @Test
    void sendSolicitorToDraftOrderEmailRespondent() {
        CallbackRequest callbackRequest = getConsentedCallbackRequest();
        notificationService.sendSolicitorToDraftOrderEmailRespondent(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_DRAFT_ORDER);
    }

    @Test
    void sendSolicitorToDraftOrderEmailApplicant() {
        CallbackRequest callbackRequest = getConsentedCallbackRequest();
        notificationService.sendSolicitorToDraftOrderEmailApplicant(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_DRAFT_ORDER);
    }

    @Test
    void sendSolicitorToDraftOrderEmailIntervener() {
        CallbackRequest callbackRequest = getConsentedCallbackRequest();
        notificationService.sendSolicitorToDraftOrderEmailIntervener(callbackRequest.getCaseDetails(), dataKeysWrapper);

        verify(notificationRequestMapper).getNotificationRequestForIntervenerSolicitor(callbackRequest.getCaseDetails(),
            dataKeysWrapper);
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_DRAFT_ORDER);
    }

    @Test
    void sendFinremSolicitorToDraftOrderEmailIntervener() {
        FinremCallbackRequest finremCallbackRequest = getContestedNewCallbackRequest();
        notificationService.sendSolicitorToDraftOrderEmailIntervener(finremCallbackRequest.getCaseDetails(), dataKeysWrapper);

        verify(finremNotificationRequestMapper).getNotificationRequestForIntervenerSolicitor(finremCallbackRequest.getCaseDetails(),
            dataKeysWrapper);
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_DRAFT_ORDER);
    }

    @Test
    void sendContestedHwfSuccessfulNotificationEmail() {
        CallbackRequest callbackRequest = getContestedCallbackRequest();
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_HWF_SUCCESSFUL);
    }

    @Test
    void sendGeneralEmailConsented() {
        CallbackRequest callbackRequest = getConsentedCallbackRequest();
        notificationService.sendConsentGeneralEmail(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONSENT_GENERAL_EMAIL);
    }

    @Test
    void sendGeneralEmailContested() {
        CallbackRequest callbackRequest = getConsentedCallbackRequest();
        notificationService.sendContestedGeneralEmail(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_GENERAL_EMAIL);
    }

    @Test
    void sendContestOrderNotApprovedNotificationEmailApplicant() {
        CallbackRequest callbackRequest = getConsentedCallbackRequest();
        notificationService.sendContestOrderNotApprovedEmailApplicant(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTEST_ORDER_NOT_APPROVED);
    }

    @Test
    void sendContestOrderNotApprovedNotificationEmailRespondent() {
        CallbackRequest callbackRequest = getConsentedCallbackRequest();
        notificationService.sendContestOrderNotApprovedEmailRespondent(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTEST_ORDER_NOT_APPROVED);
    }

    @Test
    void sendContestOrderNotApprovedNotificationEmailIntervener() {
        CallbackRequest callbackRequest = getConsentedCallbackRequest();
        notificationService.sendContestOrderNotApprovedEmailIntervener(callbackRequest.getCaseDetails(), dataKeysWrapper);

        verify(notificationRequestMapper).getNotificationRequestForIntervenerSolicitor(callbackRequest.getCaseDetails(),
            dataKeysWrapper);
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTEST_ORDER_NOT_APPROVED);
    }

    @Test
    void sendFinremContestOrderNotApprovedNotificationEmailIntervener() {
        FinremCallbackRequest finremCallbackRequest = getContestedNewCallbackRequest();
        notificationService.sendContestOrderNotApprovedEmailIntervener(finremCallbackRequest.getCaseDetails(), dataKeysWrapper);

        verify(finremNotificationRequestMapper).getNotificationRequestForIntervenerSolicitor(finremCallbackRequest.getCaseDetails(),
            dataKeysWrapper);
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTEST_ORDER_NOT_APPROVED);
    }

    @Test
    void sendContestedConsentOrderApprovedNotificationEmailToApplicantSolicitor() {
        CallbackRequest callbackRequest = getConsentedCallbackRequest();
        notificationService.sendContestedConsentOrderApprovedEmailToApplicantSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_CONSENT_ORDER_APPROVED);
    }

    @Test
    void sendContestedConsentOrderApprovedNotificationEmailToRespondentSolicitor() {
        CallbackRequest callbackRequest = getConsentedCallbackRequest();
        notificationService.sendContestedConsentOrderApprovedEmailToRespondentSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_CONSENT_ORDER_APPROVED);
    }

    @Test
    void sendContestedConsentOrderApprovedNotificationEmailToIntervenerSolicitor() {
        CallbackRequest callbackRequest = getConsentedCallbackRequest();
        notificationService.sendContestedConsentOrderApprovedEmailToIntervenerSolicitor(callbackRequest.getCaseDetails(),
            dataKeysWrapper);

        verify(notificationRequestMapper).getNotificationRequestForIntervenerSolicitor(callbackRequest.getCaseDetails(),
            dataKeysWrapper);
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_CONSENT_ORDER_APPROVED);
    }

    @Test
    void sendFinremContestedConsentOrderApprovedNotificationEmailToIntervenerSolicitor() {
        FinremCallbackRequest finremCallbackRequest = getContestedNewCallbackRequest();
        notificationService.sendContestedConsentOrderApprovedEmailToIntervenerSolicitor(finremCallbackRequest.getCaseDetails(),
            dataKeysWrapper);

        verify(finremNotificationRequestMapper).getNotificationRequestForIntervenerSolicitor(finremCallbackRequest.getCaseDetails(),
            dataKeysWrapper);
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_CONSENT_ORDER_APPROVED);
    }

    @Test
    void sendContestedGeneralApplicationOutcomeNotificationEmailWhenSendToFRCToggleTrue() {
        when(featureToggleService.isSendToFRCEnabled()).thenReturn(true);

        CallbackRequest callbackRequest = getContestedCallbackRequest();
        notificationService.sendContestedGeneralApplicationOutcomeEmail(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_GENERAL_APPLICATION_OUTCOME);
    }

    @Test
    void sendContestedConsentGeneralOrderNotificationEmailApplicantSolicitor() {
        CallbackRequest callbackRequest = getConsentedCallbackRequest();
        notificationService.sendContestedConsentGeneralOrderEmailApplicantSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_GENERAL_ORDER_CONSENT);
    }

    @Test
    void sendContestedConsentGeneralOrderNotificationEmailRespondentSolicitor() {
        CallbackRequest callbackRequest = getConsentedCallbackRequest();
        notificationService.sendContestedConsentGeneralOrderEmailRespondentSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_GENERAL_ORDER_CONSENT);
    }

    @Test
    void sendContestedConsentGeneralOrderNotificationEmailIntervenerSolicitor() {
        CallbackRequest callbackRequest = getConsentedCallbackRequest();
        notificationService.sendContestedConsentGeneralOrderEmailIntervenerSolicitor(callbackRequest.getCaseDetails(),
            dataKeysWrapper);

        verify(notificationRequestMapper).getNotificationRequestForIntervenerSolicitor(callbackRequest.getCaseDetails(),
            dataKeysWrapper);
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_GENERAL_ORDER_CONSENT);
    }

    @Test
    void sendFinremContestedConsentGeneralOrderNotificationEmailIntervenerSolicitor() {
        FinremCallbackRequest finremCallbackRequest = getContestedNewCallbackRequest();
        notificationService.sendContestedConsentGeneralOrderEmailIntervenerSolicitor(finremCallbackRequest.getCaseDetails(),
            dataKeysWrapper);

        verify(finremNotificationRequestMapper).getNotificationRequestForIntervenerSolicitor(finremCallbackRequest.getCaseDetails(),
            dataKeysWrapper);
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_GENERAL_ORDER_CONSENT);
    }

    @Test
    void sendContestedGeneralOrderNotificationEmailApplicant() {
        CallbackRequest callbackRequest = getConsentedCallbackRequest();
        notificationService.sendContestedGeneralOrderEmailApplicant(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_GENERAL_ORDER);
    }

    @Test
    void sendContestedGeneralOrderNotificationEmailRespondent() {
        CallbackRequest callbackRequest = getConsentedCallbackRequest();
        notificationService.sendContestedGeneralOrderEmailRespondent(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_GENERAL_ORDER);
    }

    @Test
    void sendContestedGeneralOrderNotificationEmailIntervener() {
        CallbackRequest callbackRequest = getConsentedCallbackRequest();
        notificationService.sendContestedGeneralOrderEmailIntervener(callbackRequest.getCaseDetails(), dataKeysWrapper);

        verify(notificationRequestMapper).getNotificationRequestForIntervenerSolicitor(callbackRequest.getCaseDetails(),
            dataKeysWrapper);
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_GENERAL_ORDER);
    }

    @Test
    void sendFinremContestedGeneralOrderNotificationEmailIntervener() {
        FinremCallbackRequest finremCallbackRequest = getContestedNewCallbackRequest();
        notificationService.sendContestedGeneralOrderEmailIntervener(finremCallbackRequest.getCaseDetails(), dataKeysWrapper);

        verify(finremNotificationRequestMapper).getNotificationRequestForIntervenerSolicitor(finremCallbackRequest.getCaseDetails(),
            dataKeysWrapper);
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_GENERAL_ORDER);
    }

    @Test
    void sendConsentedGeneralOrderNotificationEmailApplicant() {
        CallbackRequest callbackRequest = getConsentedCallbackRequest();
        notificationService.sendConsentedGeneralOrderEmailToApplicantSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONSENTED_GENERAL_ORDER);
    }

    @Test
    void sendConsentedGeneralOrderNotificationEmailRespondent() {
        CallbackRequest callbackRequest = getConsentedCallbackRequest();
        notificationService.sendConsentedGeneralOrderEmailToRespondentSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONSENTED_GENERAL_ORDER);
    }

    @Test
    void sendContestedConsentOrderNotApprovedNotificationEmail() {
        CallbackRequest callbackRequest = getConsentedCallbackRequest();
        notificationService.sendContestedConsentOrderNotApprovedEmailApplicantSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_CONSENT_ORDER_NOT_APPROVED);
    }

    @Test
    void sendContestedConsentOrderNotApprovedNotificationEmailToRespondentSolicitor() {
        CallbackRequest callbackRequest = getConsentedCallbackRequest();
        notificationService.sendContestedConsentOrderNotApprovedEmailRespondentSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_CONSENT_ORDER_NOT_APPROVED);
    }

    @Test
    void sendContestedConsentOrderNotApprovedNotificationEmailToIntervenerSolicitor() {
        CallbackRequest callbackRequest = getConsentedCallbackRequest();
        notificationService.sendContestedConsentOrderNotApprovedEmailIntervenerSolicitor(callbackRequest.getCaseDetails(),
            dataKeysWrapper);

        verify(notificationRequestMapper).getNotificationRequestForIntervenerSolicitor(callbackRequest.getCaseDetails(),
            dataKeysWrapper);
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_CONSENT_ORDER_NOT_APPROVED);
    }

    @Test
    void sendFinremContestedConsentOrderNotApprovedNotificationEmailToIntervenerSolicitor() {
        FinremCallbackRequest finremCallbackRequest = getContestedNewCallbackRequest();
        notificationService.sendContestedConsentOrderNotApprovedEmailIntervenerSolicitor(finremCallbackRequest.getCaseDetails(),
            dataKeysWrapper);

        verify(finremNotificationRequestMapper).getNotificationRequestForIntervenerSolicitor(finremCallbackRequest.getCaseDetails(),
            dataKeysWrapper);
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_CONSENT_ORDER_NOT_APPROVED);
    }

    @Test
    void sendConsentOrderNotApprovedSentEmailToApplicantSolicitor() {
        CallbackRequest callbackRequest = getConsentedCallbackRequest();
        notificationService.sendConsentOrderNotApprovedSentEmailToApplicantSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONSENT_ORDER_NOT_APPROVED_SENT);
    }

    @Test
    void sendConsentOrderNotApprovedSentEmailToRespondentSolicitor() {
        CallbackRequest callbackRequest = getConsentedCallbackRequest();
        notificationService.sendConsentOrderNotApprovedSentEmailToRespondentSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONSENT_ORDER_NOT_APPROVED_SENT);
    }

    @Test
    void shouldEmailRespondentSolicitor() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(RESP_SOLICITOR_EMAIL, TEST_USER_EMAIL);
        caseData.put(RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT, YES_VALUE);

        when(caseDataService.isPaperApplication(anyMap())).thenReturn(false);
        when(caseDataService.isRespondentRepresentedByASolicitor(anyMap())).thenReturn(true);
        when(caseDataService.isNotEmpty(RESP_SOLICITOR_EMAIL, caseData)).thenReturn(true);

        assertTrue(notificationService.isRespondentSolicitorEmailCommunicationEnabled(caseData));
    }

    @Test
    void shouldNotEmailRespondentSolicitor() {
        when(caseDataService.isPaperApplication(anyMap())).thenReturn(true);
        assertFalse(notificationService.isRespondentSolicitorEmailCommunicationEnabled(anyMap()));
    }

    @Test
    void shouldEmailRespondentSolicitorWhenNullEmailConsent() {
        when(caseDataService.isPaperApplication(anyMap())).thenReturn(false);
        when(caseDataService.isRespondentRepresentedByASolicitor(anyMap())).thenReturn(true);
        when(caseDataService.isNotEmpty(any(), any())).thenReturn(true);

        Map<String, Object> caseData = new HashMap<>();
        caseData.put(RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT, null);

        boolean isRespondentCommunicationEnabled = notificationService.isRespondentSolicitorEmailCommunicationEnabled(caseData);

        assertTrue(isRespondentCommunicationEnabled);
    }

    @Test
    void shouldEmailContestedAppSolicitor() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CONTESTED_SOLICITOR_EMAIL, TEST_USER_EMAIL);
        caseData.put(APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED, YES_VALUE);

        when(caseDataService.isPaperApplication(anyMap())).thenReturn(false);
        when(caseDataService.isApplicantRepresentedByASolicitor(anyMap())).thenReturn(true);
        when(caseDataService.isNotEmpty(CONTESTED_SOLICITOR_EMAIL, caseData)).thenReturn(true);

        assertTrue(notificationService.isContestedApplicantSolicitorEmailCommunicationEnabled(caseData));
    }

    @Test
    void shouldNotEmailContestedAppSolicitor() {
        when(caseDataService.isPaperApplication(anyMap())).thenReturn(true);
        lenient().when(caseDataService.isApplicantRepresentedByASolicitor(anyMap())).thenReturn(false);

        assertFalse(notificationService.isContestedApplicantSolicitorEmailCommunicationEnabled(anyMap()));
    }

    @Test
    void sendTransferToCourtEmailConsented() {
        CallbackRequest callbackRequest = getConsentedCallbackRequest();
        notificationService.sendTransferToLocalCourtEmail(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_TRANSFER_TO_LOCAL_COURT);
    }

    @Test
    void sendInterimNotificationEmailToApplicantSolicitor() {
        CallbackRequest callbackRequest = getContestedCallbackRequest();
        notificationService.sendInterimNotificationEmailToApplicantSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_INTERIM_HEARING);
    }

    @Test
    void sendInterimNotificationEmailToRespondentSolicitor() {
        CallbackRequest callbackRequest = getContestedCallbackRequest();
        notificationService.sendInterimNotificationEmailToRespondentSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_INTERIM_HEARING);
    }

    @Test
    void sendInterimNotificationEmailToIntervenerSolicitor() {
        CallbackRequest callbackRequest = getContestedCallbackRequest();
        notificationService.sendInterimNotificationEmailToIntervenerSolicitor(callbackRequest.getCaseDetails(),
            dataKeysWrapper);

        verify(notificationRequestMapper).getNotificationRequestForIntervenerSolicitor(callbackRequest.getCaseDetails(),
            dataKeysWrapper);
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_INTERIM_HEARING);
    }

    @Test
    void sendFinremInterimNotificationEmailToIntervenerSolicitor() {
        FinremCallbackRequest finremCallbackRequest = getContestedNewCallbackRequest();
        notificationService.sendInterimNotificationEmailToIntervenerSolicitor(finremCallbackRequest.getCaseDetails(),
            dataKeysWrapper);

        verify(finremNotificationRequestMapper).getNotificationRequestForIntervenerSolicitor(finremCallbackRequest.getCaseDetails(),
            dataKeysWrapper);
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_INTERIM_HEARING);
    }

    @Test
    void givenContestedCaseWhenSendNoticeOfChangeEmailThenSendNoticeOfChangeContestedEmail() {
        NotificationRequest myNotificationRequest = new NotificationRequest();
        myNotificationRequest.setNotificationEmail("test@test.com");
        myNotificationRequest.setName(TEST_SOLICITOR_NAME);


        CaseDetails caseDetails = getContestedCallbackRequest().getCaseDetails();

        when(notificationRequestMapper.getNotificationRequestForNoticeOfChange(caseDetails)).thenReturn(myNotificationRequest);

        notificationService.sendNoticeOfChangeEmail(caseDetails);

        verify(notificationRequestMapper).getNotificationRequestForNoticeOfChange(caseDetails);
        verify(emailService).sendConfirmationEmail(myNotificationRequest, FR_CONTESTED_NOTICE_OF_CHANGE);
    }

    @Test
    void givenConsentedCaseWhenSendNoticeOfChangeEmailThenSendNoticeOfChangeContestedEmail() {
        NotificationRequest myNotificationRequest = new NotificationRequest();
        myNotificationRequest.setName(TEST_SOLICITOR_NAME);
        myNotificationRequest.setNotificationEmail("test@test.com");

        CaseDetails caseDetails = getConsentedCallbackRequest().getCaseDetails();

        when(notificationRequestMapper.getNotificationRequestForNoticeOfChange(caseDetails)).thenReturn(myNotificationRequest);
        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(true);

        notificationService.sendNoticeOfChangeEmail(caseDetails);

        verify(notificationRequestMapper).getNotificationRequestForNoticeOfChange(caseDetails);
        verify(emailService).sendConfirmationEmail(myNotificationRequest, FR_CONSENTED_NOTICE_OF_CHANGE);
    }

    @Test
    void givenConsentedCaseWhenSendNoticeOfChangeEmail_whenMissingEmailAddress_thenNotSendingNoticeOfChangeContestedEmail() {
        notificationRequest = new NotificationRequest();
        notificationRequest.setName(TEST_SOLICITOR_NAME);

        CaseDetails caseDetails = getConsentedCallbackRequest().getCaseDetails();

        when(notificationRequestMapper.getNotificationRequestForNoticeOfChange(caseDetails)).thenReturn(notificationRequest);
        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(true);

        notificationService.sendNoticeOfChangeEmail(caseDetails);

        verify(notificationRequestMapper).getNotificationRequestForNoticeOfChange(caseDetails);
        verify(emailService, never()).sendConfirmationEmail(notificationRequest, FR_CONSENTED_NOTICE_OF_CHANGE);
    }

    @Test
    void givenContestedCaseAndNonDigitalSol_whenSendNocEmail_thenNotSendContestedEmail() {
        notificationRequest = new NotificationRequest();
        notificationRequest.setName(TEST_SOLICITOR_NAME);
        when(notificationRequestMapper.getNotificationRequestForNoticeOfChange(any())).thenReturn(notificationRequest);

        notificationService.sendNoticeOfChangeEmail(getContestedCallbackRequest().getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForNoticeOfChange(getContestedCallbackRequest().getCaseDetails());
        verifyNoMoreInteractions(emailService);
    }

    @Test
    void givenContestedCase_whenSendNoCCaseworkerEmail_thenSendContestedEmail() {
        notificationRequest = new NotificationRequest();
        notificationRequest.setName(TEST_SOLICITOR_NAME);
        when(notificationRequestMapper.getNotificationRequestForNoticeOfChange(any())).thenReturn(notificationRequest);
        when(checkSolicitorIsDigitalService.isApplicantSolicitorDigital(any())).thenReturn(true);

        notificationService.sendNoticeOfChangeEmailCaseworker(getContestedCallbackRequestUpdateDetails()
            .getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForNoticeOfChange(getContestedCallbackRequestUpdateDetails()
            .getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_NOC_CASEWORKER);
    }

    @Test
    void givenConsentedCase_whenSendNoCCaseworkerEmail_thenSendConsentedEmail() {
        notificationRequest = new NotificationRequest();
        notificationRequest.setName(TEST_SOLICITOR_NAME);
        when(notificationRequestMapper.getNotificationRequestForNoticeOfChange(any())).thenReturn(notificationRequest);
        when(checkSolicitorIsDigitalService.isApplicantSolicitorDigital(any())).thenReturn(true);
        when(caseDataService.isConsentedApplication(any(CaseDetails.class))).thenReturn(true);

        notificationService.sendNoticeOfChangeEmailCaseworker(getConsentedCallbackRequestUpdateDetails()
            .getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForNoticeOfChange(getConsentedCallbackRequestUpdateDetails()
            .getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONSENTED_NOC_CASEWORKER);
    }

    @Test
    void givenConsentedCaseAndRequestedByDigitalRespondentSolicitor_whenSendNoCCaseworkerEmail_thenSendConsentedEmail() {
        notificationRequest = new NotificationRequest();
        notificationRequest.setName(TEST_RESP_SOLICITOR_NAME);
        when(notificationRequestMapper.getNotificationRequestForNoticeOfChange(any())).thenReturn(notificationRequest);
        lenient().when(checkSolicitorIsDigitalService.isApplicantSolicitorDigital(any())).thenReturn(true);
        lenient().when(checkSolicitorIsDigitalService.isRespondentSolicitorDigital(any())).thenReturn(true);
        lenient().when(caseDataService.isConsentedApplication(any(CaseDetails.class))).thenReturn(true);

        notificationService.sendNoticeOfChangeEmailCaseworker(getConsentedCallbackRequestUpdateDetails()
            .getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForNoticeOfChange(getConsentedCallbackRequestUpdateDetails()
            .getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONSENTED_NOC_CASEWORKER);
    }

    @Test
    void givenConsentedCaseAndRequestedByNonDigitalRespondentSolicitor_whenSendNoCCaseworkerEmail_thenSendConsentedEmail() {
        notificationRequest = new NotificationRequest();
        notificationRequest.setName(TEST_RESP_SOLICITOR_NAME);
        lenient().when(notificationRequestMapper.getNotificationRequestForNoticeOfChange(any())).thenReturn(notificationRequest);
        lenient().when(checkSolicitorIsDigitalService.isApplicantSolicitorDigital(any())).thenReturn(true);
        lenient().when(checkSolicitorIsDigitalService.isRespondentSolicitorDigital(any())).thenReturn(false);
        lenient().when(caseDataService.isConsentedApplication(any(CaseDetails.class))).thenReturn(true);

        notificationService.sendNoticeOfChangeEmailCaseworker(getConsentedCallbackRequestUpdateDetails()
            .getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForNoticeOfChange(getConsentedCallbackRequestUpdateDetails()
            .getCaseDetails());
        verify(emailService, never()).sendConfirmationEmail(notificationRequest, FR_CONSENTED_NOC_CASEWORKER);
    }

    @Test
    void givenContestedCaseAndNonDigitalSol_whenSendNocEmail_thenNotSendContestedEmailCaseworker() {
        notificationRequest = new NotificationRequest();
        notificationRequest.setName(TEST_SOLICITOR_NAME);
        when(notificationRequestMapper.getNotificationRequestForNoticeOfChange(any())).thenReturn(notificationRequest);
        when(checkSolicitorIsDigitalService.isApplicantSolicitorDigital(any())).thenReturn(false);

        notificationService.sendNoticeOfChangeEmailCaseworker(getContestedCallbackRequest().getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForNoticeOfChange(getContestedCallbackRequest().getCaseDetails());
        verifyNoMoreInteractions(emailService);
    }

    @Test
    void sendUpdateFrcInformationEmailToRespSolicitor() {
        CallbackRequest callbackRequest = getContestedCallbackRequest();

        notificationService.sendUpdateFrcInformationEmailToRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_UPDATE_FRC_SOL);
    }

    @Test
    void sendUpdateFrcInformationEmailToIntervenerSolicitor() {
        CallbackRequest callbackRequest = getContestedCallbackRequest();

        notificationService.sendUpdateFrcInformationEmailToIntervenerSolicitor(callbackRequest.getCaseDetails(),
            dataKeysWrapper);
        verify(notificationRequestMapper).getNotificationRequestForIntervenerSolicitor(callbackRequest.getCaseDetails(),
            dataKeysWrapper);
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_UPDATE_FRC_SOL);
    }

    @Test
    void sendFinremUpdateFrcInformationEmailToIntervenerSolicitor() {
        FinremCallbackRequest finremCallbackRequest = getContestedNewCallbackRequest();

        notificationService.sendUpdateFrcInformationEmailToIntervenerSolicitor(finremCallbackRequest.getCaseDetails(),
            dataKeysWrapper);
        verify(finremNotificationRequestMapper).getNotificationRequestForIntervenerSolicitor(finremCallbackRequest.getCaseDetails(),
            dataKeysWrapper);
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_UPDATE_FRC_SOL);
    }

    @Test
    void sendUpdateFrcInformationEmailToCourt() {
        when(featureToggleService.isSendToFRCEnabled()).thenReturn(true);
        CallbackRequest callbackRequest = getContestedCallbackRequest();

        notificationService.sendUpdateFrcInformationEmailToCourt(callbackRequest.getCaseDetails());
        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_UPDATE_FRC_COURT);
    }

    @Test
    void sendGeneralApplicationRejectionEmailApplicantSolicitor() {
        CallbackRequest callbackRequest = getContestedCallbackRequest();

        notificationService.sendGeneralApplicationRejectionEmailToAppSolicitor(callbackRequest.getCaseDetails());
        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_REJECT_GENERAL_APPLICATION);
    }

    @Test
    void sendGeneralApplicationRejectionEmailRespondentSolicitor() {
        CallbackRequest callbackRequest = getContestedCallbackRequest();

        notificationService.sendGeneralApplicationRejectionEmailToResSolicitor(callbackRequest.getCaseDetails());
        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_REJECT_GENERAL_APPLICATION);
    }

    @Test
    void sendGeneralApplicationRejectionEmailIntervenerSolicitor() {
        FinremCallbackRequest finremCallbackRequest = getContestedNewCallbackRequest();
        IntervenerWrapper intervenerOneWrapper = IntervenerOne.builder().build();
        notificationService.sendGeneralApplicationRejectionEmailToIntervenerSolicitor(finremCallbackRequest.getCaseDetails(),
            intervenerOneWrapper);
        verify(finremNotificationRequestMapper).getNotificationRequestForIntervenerSolicitor(finremCallbackRequest.getCaseDetails(),
            SolicitorCaseDataKeysWrapper.builder().solicitorReferenceKey("").solicitorNameKey("").build());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_REJECT_GENERAL_APPLICATION);
    }

    @Test
    void shouldEmailApplicantSolicitorWhenApplicantSolicitorIsRegisteredAndAcceptingEmails() {
        when(checkSolicitorIsDigitalService.isApplicantSolicitorDigital(any())).thenReturn(true);
        when(caseDataService.isApplicantSolicitorEmailPopulated(any())).thenReturn(true);

        assertTrue(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(CaseDetails.builder()
            .id(123450L).build()));
    }

    @Test
    void shouldSendGeneralEmailWithAttachmentConsented() {
        FinremCaseDetails finremCaseDetails = getFinremCaseDetails(CaseType.CONSENTED);
        notificationService.sendConsentGeneralEmail(finremCaseDetails, anyString());

        verify(finremNotificationRequestMapper).getNotificationRequestForApplicantSolicitor(finremCaseDetails);
        verify(evidenceManagementDownloadService).getByteArray(any(CaseDocument.class), anyString());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONSENT_GENERAL_EMAIL_ATTACHMENT);
    }

    @Test
    void shouldSendGeneralEmailWithAttachmentContested() {
        FinremCaseDetails finremCaseDetails = getFinremCaseDetails(CaseType.CONTESTED);
        notificationService.sendContestedGeneralEmail(finremCaseDetails, anyString());

        verify(finremNotificationRequestMapper).getNotificationRequestForApplicantSolicitor(finremCaseDetails);
        verify(evidenceManagementDownloadService).getByteArray(any(CaseDocument.class), anyString());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_GENERAL_EMAIL_ATTACHMENT);
    }

    @Test
    void shouldEmailApplicantSolicitorWhenApplicantSolicitorIsDigitalAndEmailIsPopulated() {
        FinremCaseData caseData = FinremCaseData.builder().contactDetailsWrapper(
            ContactDetailsWrapper.builder().applicantSolicitorEmail(APPLICANT_EMAIL).build()).build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123456780L).data(caseData).build();

        when(checkSolicitorIsDigitalService.isApplicantSolicitorDigital(caseDetails.getId().toString())).thenReturn(true);

        assertTrue(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails));
    }

    @Test
    void shouldNotEmailApplicantSolicitorWhenApplicantSolicitorIsNotDigitalAndEmailIsPopulatedFinrem() {
        FinremCaseData caseData = FinremCaseData.builder().contactDetailsWrapper(
            ContactDetailsWrapper.builder().respondentSolicitorEmail(APPLICANT_EMAIL).build()).build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123456780L).data(caseData).build();

        lenient().when(checkSolicitorIsDigitalService.isApplicantSolicitorDigital(caseDetails.getId().toString())).thenReturn(false);

        assertFalse(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails));
    }

    @Test
    void shouldNotEmailApplicantSolicitorWhenApplicantSolicitorIsDigitalAndEmailIsNotPopulatedFinrem() {
        FinremCaseData caseData = FinremCaseData.builder().contactDetailsWrapper(
            ContactDetailsWrapper.builder().applicantSolicitorEmail(null).build()).build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123456780L).data(caseData).build();

        assertFalse(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails));
    }

    @Test
    void shouldNotEmailApplicantSolicitorWhenApplicantSolicitorIsNotRegisteredButIsAcceptingEmails() {
        when(checkSolicitorIsDigitalService.isApplicantSolicitorDigital(any())).thenReturn(false);
        when(caseDataService.isApplicantSolicitorEmailPopulated(any(CaseDetails.class))).thenReturn(true);
        assertFalse(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(CaseDetails.builder()
            .id(1234567890L).build()));
    }

    @Test
    void shouldNotEmailApplicantSolicitorWhenApplicantSolicitorIsNotDigital() {
        lenient().when(checkSolicitorIsDigitalService.isApplicantSolicitorDigital(any())).thenReturn(false);
        lenient().when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any(CaseDetails.class))).thenReturn(true);
        assertFalse(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(CaseDetails.builder()
            .id(1234567890L).build()));
    }

    @Test
    void shouldEmailRespondentSolicitorWhenRespondentSolicitorIsRegisteredAndAcceptingEmails() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT, YES_VALUE);

        when(caseDataService.isRespondentRepresentedByASolicitor(caseData)).thenReturn(true);
        when(caseDataService.isNotEmpty(RESP_SOLICITOR_EMAIL, caseData)).thenReturn(true);
        when(checkSolicitorIsDigitalService.isRespondentSolicitorDigital(any())).thenReturn(true);

        CaseDetails caseDetails = CaseDetails.builder().id(123456780L).data(caseData).build();
        assertTrue(notificationService.isRespondentSolicitorRegisteredAndEmailCommunicationEnabled(caseDetails));
    }

    @Test
    void shouldEmailRespondentSolicitorWhenRespondentSolicitorIsDigitalAndEmailPopulated() {
        FinremCaseData caseData = FinremCaseData.builder().contactDetailsWrapper(
            ContactDetailsWrapper.builder().respondentSolicitorEmail(RESP_SOLICITOR_EMAIL).build()).build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123456780L).data(caseData).build();

        when(checkSolicitorIsDigitalService.isRespondentSolicitorDigital(caseDetails.getId().toString())).thenReturn(true);

        assertTrue(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails));
    }

    @Test
    void shouldNotEmailRespondentSolicitorWhenRespondentSolicitorIsNotRegisteredAndAcceptingEmails() {
        when(checkSolicitorIsDigitalService.isRespondentSolicitorDigital(any())).thenReturn(false);

        Map<String, Object> caseData = new HashMap<>();

        lenient().when(caseDataService.isPaperApplication(caseData)).thenReturn(false);
        when(caseDataService.isRespondentRepresentedByASolicitor(caseData)).thenReturn(true);
        when(caseDataService.isNotEmpty(RESP_SOLICITOR_EMAIL, caseData)).thenReturn(true);

        caseData.put(RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT, YES_VALUE);
        CaseDetails caseDetails = CaseDetails.builder().id(123450L).data(caseData).build();

        assertFalse(notificationService.isRespondentSolicitorRegisteredAndEmailCommunicationEnabled(caseDetails));
    }

    @Test
    void shouldEmailRespondentSolicitorWhenRespondentSolicitorIsDigitalAndEmailIsPopulated() {
        when(checkSolicitorIsDigitalService.isRespondentSolicitorDigital(any())).thenReturn(true);
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(RESP_SOLICITOR_EMAIL, "someemailaddress@email.com");
        when(caseDataService.isNotEmpty(RESP_SOLICITOR_EMAIL, caseData)).thenReturn(true);
        CaseDetails caseDetails = CaseDetails.builder().id(123456780L).data(caseData).build();

        assertTrue(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails));
    }

    @Test
    void shouldNotEmailRespondentSolicitorWhenRespondentSolicitorIsNotDigitalAndEmailIsPopulated() {
        when(checkSolicitorIsDigitalService.isRespondentSolicitorDigital(any())).thenReturn(false);
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(RESP_SOLICITOR_EMAIL, "someemailaddress@email.com");
        when(caseDataService.isNotEmpty(RESP_SOLICITOR_EMAIL, caseData)).thenReturn(true);
        CaseDetails caseDetails = CaseDetails.builder().id(123456780L).data(caseData).build();

        assertFalse(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails));
    }

    @Test
    void shouldNotEmailRespondentSolicitorWhenRespondentSolicitorIsNotDigitalAndEmailIsPopulatedFinrem() {
        FinremCaseData caseData = FinremCaseData.builder().contactDetailsWrapper(
            ContactDetailsWrapper.builder().respondentSolicitorEmail(RESP_SOLICITOR_EMAIL).build()).build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123456780L).data(caseData).build();

        when(checkSolicitorIsDigitalService.isRespondentSolicitorDigital(caseDetails.getId().toString())).thenReturn(false);

        assertFalse(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails));
    }

    @Test
    void shouldEmailIfIntervenerOneSolicitorIsPopulated() {
        FinremCaseData caseData = FinremCaseData.builder().intervenerOne(
            IntervenerOne.builder().intervenerRepresented(YesOrNo.YES)
                .intervenerSolEmail(INTERVENER_SOL_EMAIL)
                .intervenerSolName("name").build()).build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123456780L).data(caseData).build();

        lenient().when(checkSolicitorIsDigitalService.isIntervenerSolicitorDigital(caseDetails.getId().toString(),
            CaseRole.INTVR_SOLICITOR_1.getCcdCode())).thenReturn(true);
        assertTrue(notificationService.isIntervenerSolicitorEmailPopulated(caseData.getIntervenerOne()));
    }

    @Test
    void shouldNotEmailIfIntervenerOneSolicitorIsNotPopulated() {
        FinremCaseData caseData = FinremCaseData.builder().intervenerOne(
            IntervenerOne.builder().intervenerRepresented(YesOrNo.NO).build()).build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123456780L).data(caseData).build();

        assertFalse(notificationService.isIntervenerSolicitorEmailPopulated(caseData.getIntervenerOne()));
    }

    @Test
    void shouldEmailIfIntervenerTwoSolicitorIsPopulated() {
        FinremCaseData caseData = FinremCaseData.builder().intervenerTwo(
            IntervenerTwo.builder().intervenerRepresented(YesOrNo.YES)
                .intervenerSolEmail(INTERVENER_SOL_EMAIL)
                .intervenerSolName("name").build()).build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123456780L).data(caseData).build();

        lenient().when(checkSolicitorIsDigitalService.isIntervenerSolicitorDigital(caseDetails.getId().toString(),
            CaseRole.INTVR_SOLICITOR_2.getCcdCode())).thenReturn(true);
        assertTrue(notificationService.isIntervenerSolicitorEmailPopulated(caseData.getIntervenerTwo()));
    }

    @Test
    void shouldNotEmailIfIntervenerTwoSolicitorIsNotPopulated() {
        FinremCaseData caseData = FinremCaseData.builder().intervenerTwo(
            IntervenerTwo.builder().intervenerRepresented(YesOrNo.NO).build()).build();
        assertFalse(notificationService.isIntervenerSolicitorEmailPopulated(caseData.getIntervenerTwo()));
    }

    @Test
    void shouldEmailIfIntervenerThreeSolicitorIsPopulated() {
        FinremCaseData caseData = FinremCaseData.builder().intervenerThree(
            IntervenerThree.builder().intervenerRepresented(YesOrNo.YES)
                .intervenerSolEmail(INTERVENER_SOL_EMAIL)
                .intervenerSolName("name").build()).build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123456780L).data(caseData).build();

        lenient().when(checkSolicitorIsDigitalService.isIntervenerSolicitorDigital(caseDetails.getId().toString(),
            CaseRole.INTVR_SOLICITOR_3.getCcdCode())).thenReturn(true);
        assertTrue(notificationService.isIntervenerSolicitorEmailPopulated(caseData.getIntervenerThree()));
    }

    @Test
    void shouldNotEmailIfIntervenerThreeSolicitorIsNotPopulated() {
        FinremCaseData caseData = FinremCaseData.builder().intervenerThree(
            IntervenerThree.builder().intervenerRepresented(YesOrNo.NO).build()).build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123456780L).data(caseData).build();

        lenient().when(checkSolicitorIsDigitalService.isIntervenerSolicitorDigital(caseDetails.getId().toString(),
            CaseRole.INTVR_SOLICITOR_3.getCcdCode())).thenReturn(false);
        assertFalse(notificationService.isIntervenerSolicitorEmailPopulated(caseData.getIntervenerThree()));
    }

    @Test
    void shouldEmailIfIntervenerFourSolicitorIsPopulated() {
        FinremCaseData caseData = FinremCaseData.builder().intervenerFour(
            IntervenerFour.builder().intervenerRepresented(YesOrNo.YES)
                .intervenerSolEmail(INTERVENER_SOL_EMAIL)
                .intervenerSolName("name").build()).build();
        assertTrue(notificationService.isIntervenerSolicitorEmailPopulated(caseData.getIntervenerFour()));
    }

    @Test
    void shouldEmailIntervenerSolicitorIfIntervenerSolicitorWasPopulated() {
        IntervenerWrapper wrapper = new IntervenerOne();
        FinremCaseData caseData = FinremCaseData.builder().build();
        IntervenerChangeDetails changeDetails = new IntervenerChangeDetails();
        wrapper.setIntervenerSolEmail("intvrsol@email.com");
        changeDetails.setIntervenerDetails(wrapper);
        caseData.setCurrentIntervenerChangeDetails(changeDetails);
        assertTrue(notificationService.wasIntervenerSolicitorEmailPopulated(caseData.getCurrentIntervenerChangeDetails().getIntervenerDetails()));
    }

    @Test
    void shouldNotEmailIntervenerSolicitorIfIntervenerSolicitorWasNotPopulated() {
        IntervenerWrapper wrapper = new IntervenerOne();
        FinremCaseData caseData = FinremCaseData.builder().build();
        IntervenerChangeDetails changeDetails = new IntervenerChangeDetails();
        changeDetails.setIntervenerDetails(wrapper);
        caseData.setCurrentIntervenerChangeDetails(changeDetails);
        assertFalse(notificationService.wasIntervenerSolicitorEmailPopulated(caseData.getCurrentIntervenerChangeDetails().getIntervenerDetails()));
    }

    @Test
    void shouldNotEmailRespondentSolicitorWhenRespondentSolicitorIsDigitalAndEmailIsNotPopulatedFinrem() {
        FinremCaseData caseData = FinremCaseData.builder().contactDetailsWrapper(
            ContactDetailsWrapper.builder().respondentSolicitorEmail(null).build()).build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123456780L).data(caseData).build();

        lenient().when(checkSolicitorIsDigitalService.isRespondentSolicitorDigital(caseDetails.getId().toString())).thenReturn(false);

        assertFalse(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails));
    }

    @Test
    void givenAppIsContestedAndApplicantSolicitorIsNotRegisteredOrAcceptingEmails_shouldSendLettersApplicantSolicitor() {
        lenient().when(caseDataService.isContestedPaperApplication(any())).thenReturn(true);
        lenient().when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(false);
        lenient().when(checkSolicitorIsDigitalService.isApplicantSolicitorDigital(any())).thenReturn(false);

        assertTrue(notificationService.isContestedApplicationAndApplicantOrRespondentSolicitorsIsNotRegisteredOrAcceptingEmails(any()));
    }

    @Test
    void givenAppIsNotContestedAndApplicantSolicitorIsRegisteredAndAcceptingEmails_shouldNotSendLetters() {
        lenient().when(caseDataService.isContestedApplication(any(FinremCaseDetails.class))).thenReturn(false);
        lenient().when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(true);
        lenient().when(checkSolicitorIsDigitalService.isApplicantSolicitorDigital(any())).thenReturn(true);

        Map<String, Object> caseData = new HashMap<>();
        caseData.put(RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT, YES_VALUE);

        lenient().when(checkSolicitorIsDigitalService.isRespondentSolicitorDigital(any())).thenReturn(true);
        lenient().when(caseDataService.isNotEmpty(RESP_SOLICITOR_EMAIL, caseData)).thenReturn(true);
        lenient().when(caseDataService.isPaperApplication(any(FinremCaseData.class))).thenReturn(false);
        lenient().when(caseDataService.isRespondentRepresentedByASolicitor(anyMap())).thenReturn(true);

        CaseDetails caseDetails = CaseDetails.builder().data(caseData).build();

        assertFalse(notificationService.isContestedApplicationAndApplicantOrRespondentSolicitorsIsNotRegisteredOrAcceptingEmails(caseDetails));
    }

    @Test
    void isContestedAndRespondentSolicitorIsNotRegisteredOrAcceptingEmails() {
        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().data(caseData).build();

        when(caseDataService.isContestedPaperApplication(caseDetails)).thenReturn(true);
        lenient().when(caseDataService.isNotEmpty(RESP_SOLICITOR_EMAIL, caseData)).thenReturn(false);
        lenient().when(checkSolicitorIsDigitalService.isRespondentSolicitorDigital(any())).thenReturn(true);
        lenient().when(caseDataService.isRespondentRepresentedByASolicitor(anyMap())).thenReturn(false);

        assertTrue(notificationService.isContestedApplicationAndApplicantOrRespondentSolicitorsIsNotRegisteredOrAcceptingEmails(caseDetails));
    }

    @Test
    void givenBarristerAdded_sendAddedEmail() {
        Barrister barrister = new Barrister().toBuilder().build();
        CaseDetails caseDetails = CaseDetails.builder().build();
        when(notificationRequestMapper.buildInterimHearingNotificationRequest(caseDetails, barrister)).thenReturn(notificationRequest);
        notificationService.sendBarristerAddedEmail(caseDetails, barrister);
        verify(notificationRequestMapper).buildInterimHearingNotificationRequest(caseDetails, barrister);
    }

    @Test
    void givenBarristerRemoved_sendRemovedEmail() {
        Barrister barrister = new Barrister().toBuilder().build();
        CaseDetails caseDetails = CaseDetails.builder().build();
        when(notificationRequestMapper.buildInterimHearingNotificationRequest(caseDetails, barrister)).thenReturn(notificationRequest);
        notificationService.sendBarristerRemovedEmail(caseDetails, barrister);
        verify(notificationRequestMapper).buildInterimHearingNotificationRequest(caseDetails, barrister);
    }

    @Test
    void sendInterimHearingNotificationEmailToApplicantSolicitor() {
        FinremCallbackRequest callbackRequest = buildHearingFinremCallbackRequest(INTERIM_HEARING_JSON);

        List<InterimHearingCollection> interimHearingList =
            callbackRequest.getCaseDetails().getData().getInterimWrapper().getInterimHearingsScreenField();

        List<InterimHearingItem> interimHearingItems
            = interimHearingList.stream().map(InterimHearingCollection::getValue).toList();

        List<Map<String, Object>> interimDataMap = interimHearingItems.stream()
            .map(obj -> new ObjectMapper().convertValue(obj, new TypeReference<Map<String, Object>>() {
            })).toList();

        when(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(any(FinremCaseDetails.class), any()))
            .thenReturn(notificationRequest);

        interimDataMap.forEach(data -> {
            notificationService.sendInterimHearingNotificationEmailToApplicantSolicitor(callbackRequest.getCaseDetails(), data);
            verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails(), data);

        });
        verify(emailService, times(2)).sendConfirmationEmail(notificationRequest, FR_CONTESTED_INTERIM_HEARING);
    }

    @Test
    void sendInterimHearingNotificationEmailToRespondentSolicitor() {
        FinremCallbackRequest callbackRequest = buildHearingFinremCallbackRequest(INTERIM_HEARING_JSON);

        List<InterimHearingCollection> interimHearingList =
            callbackRequest.getCaseDetails().getData().getInterimWrapper().getInterimHearingsScreenField();

        List<InterimHearingItem> interimHearingItems
            = interimHearingList.stream().map(InterimHearingCollection::getValue).toList();

        List<Map<String, Object>> interimDataMap = interimHearingItems.stream()
            .map(obj -> new ObjectMapper().convertValue(obj, new TypeReference<Map<String, Object>>() {
            })).toList();

        when(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(
            any(FinremCaseDetails.class), any()))
            .thenReturn(notificationRequest);

        interimDataMap.forEach(data -> {
            notificationService.sendInterimHearingNotificationEmailToRespondentSolicitor(callbackRequest.getCaseDetails(), data);
            verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails(), data);
        });
        verify(emailService, times(2)).sendConfirmationEmail(notificationRequest, FR_CONTESTED_INTERIM_HEARING);
    }

    @Test
    void sendInterimHearingNotificationEmailToIntervenerSolicitor() {
        FinremCallbackRequest callbackRequest = buildHearingFinremCallbackRequest(INTERIM_HEARING_JSON);

        List<InterimHearingCollection> interimHearingList =
            callbackRequest.getCaseDetails().getData().getInterimWrapper().getInterimHearingsScreenField();

        List<InterimHearingItem> interimHearingItems
            = interimHearingList.stream().map(InterimHearingCollection::getValue).toList();

        List<Map<String, Object>> interimDataMap = interimHearingItems.stream()
            .map(obj -> new ObjectMapper().convertValue(obj, new TypeReference<Map<String, Object>>() {
            })).toList();

        when(notificationRequestMapper.getNotificationRequestForIntervenerSolicitor(
            any(FinremCaseDetails.class), anyMap(), any(SolicitorCaseDataKeysWrapper.class)))
            .thenReturn(notificationRequest);

        interimDataMap.forEach(data -> {
            notificationService.sendInterimHearingNotificationEmailToIntervenerSolicitor(callbackRequest.getCaseDetails(), data,
                SolicitorCaseDataKeysWrapper.builder().build());
            verify(notificationRequestMapper).getNotificationRequestForIntervenerSolicitor(callbackRequest.getCaseDetails(), data,
                SolicitorCaseDataKeysWrapper.builder().build());
        });
        verify(emailService, times(2)).sendConfirmationEmail(notificationRequest, FR_CONTESTED_INTERIM_HEARING);
    }

    @Test
    void sendConsentedHearingNotificationEmailToApplicantSolicitor() {
        CallbackRequest callbackRequest = buildHearingCallbackRequest(CONSENTED_HEARING_JSON);
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();

        List<ConsentedHearingDataWrapper> hearings = helper.getHearings(caseData);
        List<String> hearingIdsToProcess = List.of("1f7e210d-87d8-4e98-8c48-db15d1dc0d14");
        when(notificationRequestMapper.getNotificationRequestForConsentApplicantSolicitor(any(CaseDetails.class), anyMap())).thenReturn(
            notificationRequest);

        hearings.forEach(hearingData -> {
            if (hearingIdsToProcess.contains(hearingData.getId())) {
                Map<String, Object> data = helper.convertToMap(hearingData.getValue());
                notificationService.sendConsentHearingNotificationEmailToApplicantSolicitor(callbackRequest.getCaseDetails(), data);
                verify(notificationRequestMapper).getNotificationRequestForConsentApplicantSolicitor(callbackRequest.getCaseDetails(), data);
                verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONSENTED_LIST_FOR_HEARING);
            }
        });
    }

    @Test
    void sendConsentedHearingNotificationEmailToRespondentSolicitor() {
        CallbackRequest callbackRequest = buildHearingCallbackRequest(CONSENTED_HEARING_JSON);
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();

        List<ConsentedHearingDataWrapper> hearings = helper.getHearings(caseData);
        List<String> hearingIdsToProcess = List.of("1f7e210d-87d8-4e98-8c48-db15d1dc0d14");
        when(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(any(CaseDetails.class), any())).thenReturn(notificationRequest);


        hearings.forEach(hearingData -> {
            if (hearingIdsToProcess.contains(hearingData.getId())) {
                Map<String, Object> data = helper.convertToMap(hearingData.getValue());
                notificationService.sendConsentHearingNotificationEmailToRespondentSolicitor(callbackRequest.getCaseDetails(), data);
                verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails(), data);
                verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONSENTED_LIST_FOR_HEARING);
            }
        });
    }

    @Test
    void checkIsIntervenerSolicitorDigitalAndEmailPopulated() {
        CallbackRequest callbackRequest = getConsentedCallbackRequest();
        callbackRequest.getCaseDetails().getData().put("intervener1SolEmail", TEST_SOLICITOR_EMAIL);
        IntervenerOne intervenerWrapper = IntervenerOne.builder()
            .intervenerSolEmail(TEST_SOLICITOR_EMAIL).build();
        lenient().when(caseDataService.isContestedApplication(any(CaseDetails.class))).thenReturn(true);
        lenient().when(caseDataService.isNotEmpty(anyString(), anyMap())).thenReturn(true);
        lenient().when(checkSolicitorIsDigitalService.isIntervenerSolicitorDigital(anyString(), anyString())).thenReturn(true);

        boolean actual = notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(intervenerWrapper,
            callbackRequest.getCaseDetails());

        assertTrue(actual);
        verify(checkSolicitorIsDigitalService).isIntervenerSolicitorDigital(callbackRequest.getCaseDetails().getId().toString(),
            CaseRole.INTVR_SOLICITOR_1.getCcdCode());

    }

    @Test
    void checkFinremIsIntervenerSolicitorDigitalAndEmailPopulated() {
        FinremCallbackRequest finremCallbackRequest = getContestedNewCallbackRequest();
        FinremCaseData caseData = finremCallbackRequest.getCaseDetails().getData();
        caseData.getIntervenerOne().setIntervenerSolEmail(TEST_SOLICITOR_EMAIL);
        when(checkSolicitorIsDigitalService.isIntervenerSolicitorDigital(anyString(), anyString())).thenReturn(true);

        boolean actual = notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(caseData.getIntervenerOne(),
            finremCallbackRequest.getCaseDetails());
        assertTrue(actual);
        verify(checkSolicitorIsDigitalService).isIntervenerSolicitorDigital(finremCallbackRequest.getCaseDetails().getId().toString(),
            CaseRole.INTVR_SOLICITOR_1.getCcdCode());
    }

    @Test
    void shouldReturnCaseDataKeysForIntervenersSolicitor() {
        FinremCaseData caseData = FinremCaseData.builder()
            .intervenerOne(IntervenerOne.builder()
                .intervenerSolName("1Name")
                .intervenerSolEmail("1Email")
                .intervenerSolicitorReference("1Ref").build())
            .intervenerTwo(IntervenerTwo.builder()
                .intervenerSolName("2Name")
                .intervenerSolEmail("2Email")
                .intervenerSolicitorReference("2Ref").build())
            .intervenerThree(IntervenerThree.builder()
                .intervenerSolName("3Name")
                .intervenerSolEmail("3Email")
                .intervenerSolicitorReference("3Ref").build())
            .intervenerFour(IntervenerFour.builder()
                .intervenerSolName("4Name")
                .intervenerSolEmail("4Email")
                .intervenerSolicitorReference("4Ref").build())
            .build();

        for (int i = 0; i < 4; i++) {
            SolicitorCaseDataKeysWrapper dataKey = notificationService.getCaseDataKeysForIntervenerSolicitor(caseData.getInterveners().get(i));

            assertEquals((i + 1) + "Email", dataKey.getSolicitorEmailKey());
            assertEquals((i + 1) + "Name", dataKey.getSolicitorNameKey());
            assertEquals((i + 1) + "Ref", dataKey.getSolicitorReferenceKey());
        }
    }

    @Test
    void shouldSendReadyForReviewEmailToJudge() {
        // Arrange
        NotificationRequest judgeNotificationRequest = new NotificationRequest();
        judgeNotificationRequest.setCaseReferenceNumber("123456789");

        // Act
        notificationService.sendContestedReadyToReviewOrderToJudge(judgeNotificationRequest);

        // Assert
        verify(emailService).sendConfirmationEmail(judgeNotificationRequest, FR_CONTESTED_DRAFT_ORDER_READY_FOR_REVIEW_JUDGE);
    }

    @Test
    void shouldSendReadyForReviewEmailToAdmin() {
        when(featureToggleService.isSendToFRCEnabled()).thenReturn(true);
        NotificationRequest request = NotificationRequest.builder()
            .caseReferenceNumber("123456789")
            .notificationEmail("test@test.com")
            .build();

        notificationService.sendContestedReadyToReviewOrderToAdmin(request);

        ArgumentCaptor<NotificationRequest> argumentCaptor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(emailService).sendConfirmationEmail(argumentCaptor.capture(),
            eq(FR_CONTESTED_DRAFT_ORDER_READY_FOR_REVIEW_ADMIN));
        NotificationRequest actual = argumentCaptor.getValue();
        assertEquals("test@test.com", actual.getNotificationEmail());
    }

    @Test
    void shouldSendReadyForReviewEmailToAdminToFrcDisabled() {
        when(featureToggleService.isSendToFRCEnabled()).thenReturn(false);
        NotificationRequest nr = NotificationRequest.builder()
            .caseReferenceNumber("123456789")
            .notificationEmail("test@test.com")
            .build();

        notificationService.sendContestedReadyToReviewOrderToAdmin(nr);

        ArgumentCaptor<NotificationRequest> argumentCaptor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(emailService).sendConfirmationEmail(argumentCaptor.capture(),
            eq(FR_CONTESTED_DRAFT_ORDER_READY_FOR_REVIEW_ADMIN));
        NotificationRequest actual = argumentCaptor.getValue();
        assertEquals("fr_applicant_solicitor1@mailinator.com", actual.getNotificationEmail());
    }

    @Test
    void testSendDraftOrderReviewOverdueToCaseworkerSendToFrcEnabled() {
        when(featureToggleService.isSendToFRCEnabled()).thenReturn(true);
        NotificationRequest nr = NotificationRequest.builder()
            .notificationEmail("test@test.com")
            .build();
        notificationService.sendDraftOrderReviewOverdueToCaseworker(nr);

        ArgumentCaptor<NotificationRequest> argumentCaptor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(emailService).sendConfirmationEmail(argumentCaptor.capture(),
            eq(FR_CONTESTED_DRAFT_ORDER_REVIEW_OVERDUE));
        NotificationRequest actual = argumentCaptor.getValue();
        assertEquals("test@test.com", actual.getNotificationEmail());
    }

    @Test
    void testSendDraftOrderReviewOverdueToCaseworkerSendToFrcDisabled() {
        when(featureToggleService.isSendToFRCEnabled()).thenReturn(false);
        NotificationRequest nr = NotificationRequest.builder()
            .notificationEmail("test@test.com")
            .build();
        notificationService.sendDraftOrderReviewOverdueToCaseworker(nr);

        ArgumentCaptor<NotificationRequest> argumentCaptor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(emailService).sendConfirmationEmail(argumentCaptor.capture(),
            eq(FR_CONTESTED_DRAFT_ORDER_REVIEW_OVERDUE));
        NotificationRequest actual = argumentCaptor.getValue();
        assertEquals("fr_applicant_solicitor1@mailinator.com", actual.getNotificationEmail());
    }

    @Test
    void testSendRefusedDraftOrderOrPsa() {
        NotificationRequest nr = NotificationRequest.builder()
            .notificationEmail("test@test.com")
            .build();
        notificationService.sendRefusedDraftOrderOrPsa(nr);

        ArgumentCaptor<NotificationRequest> argumentCaptor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(emailService).sendConfirmationEmail(argumentCaptor.capture(),
            eq(FR_CONTESTED_DRAFT_ORDER_OR_PSA_REFUSED));
        NotificationRequest actual = argumentCaptor.getValue();
        assertEquals("test@test.com", actual.getNotificationEmail());
    }

    @Test
    void testSendHearingNotificationToSolicitor() {
        NotificationRequest nr = NotificationRequest.builder()
                .notificationEmail("test@test.com")
                .caseReferenceNumber("123")
                .build();
        notificationService.sendHearingNotificationToSolicitor(nr, CaseRole.APP_SOLICITOR.toString());
        assertTrue(logs.getInfos().contains("123 - Sending hearing notification to solicitor with role APP_SOLICITOR"));
        verify(emailService).sendConfirmationEmail(nr, FR_CONTESTED_HEARING_NOTIFICATION_SOLICITOR);
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

    private CallbackRequest getConsentedCallbackRequest() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME, "David");
        caseData.put(CONSENTED_RESPONDENT_LAST_NAME, "Goodman");
        caseData.put(APPLICANT_FIRST_MIDDLE_NAME, "Victoria");
        caseData.put(APPLICANT_LAST_NAME, "Goodman");
        caseData.put(SOLICITOR_EMAIL, TEST_SOLICITOR_EMAIL);
        caseData.put(CONSENTED_SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        caseData.put(SOLICITOR_REFERENCE, TEST_SOLICITOR_REFERENCE);
        caseData.put(RESP_SOLICITOR_EMAIL, TEST_RESP_SOLICITOR_EMAIL);
        caseData.put(RESP_SOLICITOR_NAME, TEST_RESP_SOLICITOR_NAME);
        caseData.put(RESP_SOLICITOR_REFERENCE, TEST_RESP_SOLICITOR_REFERENCE);
        caseData.put(DIVORCE_CASE_NUMBER, TEST_DIVORCE_CASE_NUMBER);
        List<String> natureOfApplication = List.of("Lump Sum Order",
            "Periodical Payment Order",
            "Pension Sharing Order",
            "Pension Attachment Order",
            "Pension Compensation Sharing Order",
            "Pension Compensation Attachment Order",
            "A settlement or a transfer of property",
            "Property Adjustment Order");
        caseData.put("natureOfApplication2", natureOfApplication);
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseTypeId(CaseType.CONSENTED.getCcdType())
                .id(12345L)
                .data(caseData)
                .build())
            .build();
    }

    private FinremCallbackRequest getContestedNewCallbackRequest() {
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

    private Map<String, Object> getCaseData() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(APPLICANT_FIRST_MIDDLE_NAME, "Victoria");
        caseData.put(APPLICANT_LAST_NAME, "Goodman");
        caseData.put(CONTESTED_SOLICITOR_EMAIL, TEST_SOLICITOR_EMAIL);
        caseData.put(CONTESTED_SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        caseData.put(RESP_SOLICITOR_EMAIL, TEST_RESP_SOLICITOR_EMAIL);
        caseData.put(RESP_SOLICITOR_NAME, TEST_RESP_SOLICITOR_NAME);
        caseData.put(RESP_SOLICITOR_REFERENCE, TEST_RESP_SOLICITOR_REFERENCE);
        caseData.put(SOLICITOR_REFERENCE, TEST_SOLICITOR_REFERENCE);
        caseData.put(DIVORCE_CASE_NUMBER, TEST_DIVORCE_CASE_NUMBER);
        caseData.put(GENERAL_APPLICATION_REFER_TO_JUDGE_EMAIL, TEST_JUDGE_EMAIL);
        caseData.put(REGION, MIDLANDS);
        caseData.put(MIDLANDS_FRC_LIST, NOTTINGHAM);
        caseData.put(NOTTINGHAM_COURTLIST, "FR_s_NottinghamList_1");
        caseData.put(BULK_PRINT_LETTER_ID_RES, NOTTINGHAM);
        return caseData;
    }

    private CallbackRequest getContestedCallbackRequest() {
        Map<String, Object> caseData = getCaseData();
        caseData.put(CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME, "David");
        caseData.put(CONTESTED_RESPONDENT_LAST_NAME, "Goodman");
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseTypeId(CaseType.CONTESTED.getCcdType())
                .id(12345L)
                .data(caseData)
                .build())
            .build();
    }

    private CallbackRequest getContestedCallbackRequestUpdateDetails() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME, "David");
        caseData.put(CONTESTED_RESPONDENT_LAST_NAME, "Goodman");
        caseData.put(APPLICANT_FIRST_MIDDLE_NAME, "Victoria");
        caseData.put(APPLICANT_LAST_NAME, "Goodman");
        caseData.put(CONTESTED_SOLICITOR_EMAIL, TEST_SOLICITOR_EMAIL);
        caseData.put(CONTESTED_SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        caseData.put(RESP_SOLICITOR_EMAIL, TEST_RESP_SOLICITOR_EMAIL);
        caseData.put(RESP_SOLICITOR_NAME, TEST_RESP_SOLICITOR_NAME);
        caseData.put(RESP_SOLICITOR_REFERENCE, TEST_RESP_SOLICITOR_REFERENCE);
        caseData.put(SOLICITOR_REFERENCE, TEST_SOLICITOR_REFERENCE);
        caseData.put(DIVORCE_CASE_NUMBER, TEST_DIVORCE_CASE_NUMBER);
        caseData.put(GENERAL_APPLICATION_REFER_TO_JUDGE_EMAIL, TEST_JUDGE_EMAIL);
        caseData.put(REGION, MIDLANDS);
        caseData.put(MIDLANDS_FRC_LIST, NOTTINGHAM);
        caseData.put(NOTTINGHAM_COURTLIST, "FR_s_NottinghamList_1");
        caseData.put(BULK_PRINT_LETTER_ID_RES, NOTTINGHAM);
        caseData.put(INCLUDES_REPRESENTATIVE_UPDATE, YES_VALUE);
        return CallbackRequest.builder()
            .eventId(UPDATE_CONTACT_DETAILS_EVENT)
            .caseDetails(CaseDetails.builder()
                .caseTypeId(CaseType.CONTESTED.getCcdType())
                .id(12345L)
                .data(caseData)
                .build())
            .build();
    }

    private CallbackRequest getConsentedCallbackRequestUpdateDetails() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME, "David");
        caseData.put(CONSENTED_RESPONDENT_LAST_NAME, "Goodman");
        caseData.put(APPLICANT_FIRST_MIDDLE_NAME, "Victoria");
        caseData.put(APPLICANT_LAST_NAME, "Goodman");
        caseData.put(SOLICITOR_EMAIL, TEST_SOLICITOR_EMAIL);
        caseData.put(CONSENTED_SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        caseData.put(SOLICITOR_REFERENCE, TEST_SOLICITOR_REFERENCE);
        caseData.put(RESP_SOLICITOR_EMAIL, TEST_RESP_SOLICITOR_EMAIL);
        caseData.put(RESP_SOLICITOR_NAME, TEST_RESP_SOLICITOR_NAME);
        caseData.put(RESP_SOLICITOR_REFERENCE, TEST_RESP_SOLICITOR_REFERENCE);
        caseData.put(DIVORCE_CASE_NUMBER, TEST_DIVORCE_CASE_NUMBER);
        caseData.put(INCLUDES_REPRESENTATIVE_UPDATE, YES_VALUE);
        List<String> natureOfApplication = List.of("Lump Sum Order",
            "Periodical Payment Order",
            "Pension Sharing Order",
            "Pension Attachment Order",
            "Pension Compensation Sharing Order",
            "Pension Compensation Attachment Order",
            "A settlement or a transfer of property",
            "Property Adjustment Order");
        caseData.put("natureOfApplication2", natureOfApplication);
        return CallbackRequest.builder()
            .eventId(UPDATE_CONTACT_DETAILS_EVENT)
            .caseDetails(CaseDetails.builder()
                .caseTypeId(CaseType.CONSENTED.getCcdType())
                .id(12345L)
                .data(caseData)
                .build())
            .build();
    }

    private FinremCaseDetails getFinremCaseDetails(CaseType caseType) {
        return FinremCaseDetails.builder()
            .data(FinremCaseData.builder()
                .ccdCaseType(caseType)
                .generalEmailWrapper(GeneralEmailWrapper.builder()
                    .generalEmailRecipient(APPLICANT_EMAIL)
                    .generalEmailUploadedDocument(CaseDocument.builder()
                        .documentBinaryUrl("dummyUrl")
                        .build())
                    .build())
                .build())
            .caseType(caseType)
            .build();
    }

    private FinremCallbackRequest buildHearingFinremCallbackRequest(String payloadJson) {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(payloadJson)) {
            return mapper.readValue(resourceAsStream, FinremCallbackRequest.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private CallbackRequest buildHearingCallbackRequest(String payloadJson) {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(payloadJson)) {
            return mapper.readValue(resourceAsStream, CallbackRequest.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
