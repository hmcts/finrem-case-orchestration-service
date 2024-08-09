package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.NotificationServiceConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremNotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckSolicitorIsDigitalService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_JUDGE_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_EMAIL;
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

public class FinremNotificationServiceTest extends BaseServiceTest {

    private static final String END_POINT_HWF_SUCCESSFUL = "http://localhost:8086/notify/hwf-successful";
    private static final String END_POINT_ASSIGNED_TO_JUDGE = "http://localhost:8086/notify/assign-to-judge";
    private static final String END_POINT_CONSENT_ORDER_MADE = "http://localhost:8086/notify/consent-order-made";
    private static final String END_POINT_PREPARE_FOR_HEARING = "http://localhost:8086/notify/prepare-for-hearing";
    private static final String END_POINT_CONSENT_ORDER_NOT_APPROVED = "http://localhost:8086/notify/consent-order-not-approved";
    private static final String END_POINT_CONSENT_ORDER_NOT_APPROVED_SENT = "http://localhost:8086/notify/consent-order-not-approved-sent";
    private static final String END_POINT_CONSENT_ORDER_AVAILABLE = "http://localhost:8086/notify/consent-order-available";
    private static final String END_POINT_CONSENT_ORDER_AVAILABLE_CTSC = "http://localhost:8086/notify/consent-order-available-ctsc";
    private static final String END_POINT_CONTESTED_HWF_SUCCESSFUL = "http://localhost:8086/notify/contested/hwf-successful";
    private static final String END_POINT_CONTESTED_APPLICATION_ISSUED = "http://localhost:8086/notify/contested/application-issued";
    private static final String END_POINT_CONTEST_ORDER_APPROVED = "http://localhost:8086/notify/contested/order-approved";
    private static final String END_POINT_CONTESTED_DRAFT_ORDER = "http://localhost:8086/notify/contested/draft-order";
    private static final String END_POINT_GENERAL_EMAIL_CONSENT = "http://localhost:8086/notify/general-email";
    private static final String END_POINT_GENERAL_EMAIL_CONTESTED = "http://localhost:8086/notify/contested/general-email";
    private static final String END_POINT_CONTEST_ORDER_NOT_APPROVED = "http://localhost:8086/notify/contested/order-not-approved";
    private static final String END_POINT_CONTESTED_CONSENT_ORDER_APPROVED = "http://localhost:8086/notify/contested/consent-order-approved";
    private static final String END_POINT_CONTESTED_CONSENT_GENERAL_ORDER = "http://localhost:8086/notify/contested/consent-general-order";
    private static final String END_POINT_CONTESTED_GENERAL_ORDER = "http://localhost:8086/notify/contested/general-order";
    private static final String END_POINT_CONSENTED_GENERAL_ORDER = "http://localhost:8086/notify/general-order";
    private static final String END_POINT_CONTESTED_GENERAL_APPLICATION_REFER_TO_JUDGE =
        "http://localhost:8086/notify/contested/general-application-refer-to-judge";
    private static final String END_POINT_CONTESTED_GENERAL_APPLICATION_OUTCOME =
        "http://localhost:8086/notify/contested/general-application-outcome";
    private static final String END_POINT_CONTESTED_CONSENT_ORDER_NOT_APPROVED = "http://localhost:8086/notify/contested/consent-order-not-approved";
    private static final String END_POINT_CONTESTED_INTERIM_HEARING = "http://localhost:8086/notify/contested/prepare-for-interim-hearing-sent";
    private static final String END_POINT_TRANSFER_TO_LOCAL_COURT = "http://localhost:8086/notify/transfer-to-local-court";
    private static final String END_POINT_NOTICE_OF_CHANGE_CONSENTED = "http://localhost:8086/notify/notice-of-change";
    private static final String END_POINT_NOTICE_OF_CHANGE_CONTESTED = "http://localhost:8086/notify/contested/notice-of-change";
    private static final String END_POINT_NOC_CASEWORKER_CONTESTED = "http://localhost:8086/notify/contested/notice-of-change/caseworker";
    private static final String END_POINT_NOC_CASEWORKER_CONSENTED = "http://localhost:8086/notify/notice-of-change/caseworker";
    private static final String END_POINT_UPDATE_FRC_INFORMATION = "http://localhost:8086/notify/contested/update-frc-information";
    private static final String END_POINT_UPDATE_FRC_INFO_COURT = "http://localhost:8086/notify/contested/update-frc-information/court";

    private static final String ERROR_500_MESSAGE = "500 Internal Server Error";
    private static final String TEST_USER_EMAIL = "fr_applicant_sol@sharklasers.com";
    private static final String NOTTINGHAM_FRC_EMAIL = "FRCNottingham@justice.gov.uk";


    @Autowired
    private NotificationService notificationService;

    @MockBean
    private FeatureToggleService featureToggleService;
    @MockBean
    private FinremNotificationRequestMapper notificationRequestMapper;
    @MockBean
    private CheckSolicitorIsDigitalService checkSolicitorIsDigitalService;
    @MockBean
    private CaseDataService caseDataService;
    @SpyBean
    private NotificationServiceConfiguration notificationServiceConfiguration;

    private FinremCallbackRequest callbackRequest;
    private FinremCallbackRequest newCallbackRequest;

    private MockRestServiceServer mockServer;
    private SolicitorCaseDataKeysWrapper dataKeysWrapper;

    @Autowired
    private RestTemplate restTemplate;

    @Before
    public void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
        callbackRequest = getConsentedNewCallbackRequest();
        newCallbackRequest = getConsentedNewCallbackRequest();
        dataKeysWrapper = SolicitorCaseDataKeysWrapper.builder().build();

        NotificationRequest notificationRequest = new NotificationRequest();
        when(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(any(FinremCaseDetails.class))).thenReturn(notificationRequest);
        when(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(any(FinremCaseDetails.class), anyBoolean()))
            .thenReturn(notificationRequest);
        when(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(any(FinremCaseDetails.class))).thenReturn(notificationRequest);
        when(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(any(FinremCaseDetails.class), anyBoolean()))
            .thenReturn(notificationRequest);
        when(notificationRequestMapper.getNotificationRequestForIntervenerSolicitor(any(FinremCaseDetails.class),
            any(SolicitorCaseDataKeysWrapper.class))).thenReturn(notificationRequest);
    }

    @Test
    public void sendHwfSuccessfulNotificationEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendConsentedHWFSuccessfulConfirmationEmail(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_HWF_SUCCESSFUL));
    }

    @Test
    public void throwExceptionWhenHwfSuccessfulNotificationEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendConsentedHWFSuccessfulConfirmationEmail(callbackRequest.getCaseDetails());
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void sendAssignToJudgeNotificationEmailToApplicantSolicitor() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_ASSIGNED_TO_JUDGE))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendAssignToJudgeConfirmationEmailToApplicantSolicitor(newCallbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(newCallbackRequest.getCaseDetails(), true);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_ASSIGNED_TO_JUDGE));
    }

    @Test
    public void sendAssignToJudgeNotificationEmailToRespondentSolicitor() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_ASSIGNED_TO_JUDGE))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendAssignToJudgeConfirmationEmailToRespondentSolicitor(newCallbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(newCallbackRequest.getCaseDetails(), true);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_ASSIGNED_TO_JUDGE));
    }


    @Test
    public void throwExceptionWhenAssignToJudgeNotificationEmailToAppSolicitorIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_ASSIGNED_TO_JUDGE))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        try {
            notificationService.sendAssignToJudgeConfirmationEmailToApplicantSolicitor(newCallbackRequest.getCaseDetails());
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(newCallbackRequest.getCaseDetails(), true);
    }

    @Test
    public void throwExceptionWhenAssignToJudgeNotificationEmailToRespSolicitorIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_ASSIGNED_TO_JUDGE))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        try {
            notificationService.sendAssignToJudgeConfirmationEmailToRespondentSolicitor(newCallbackRequest.getCaseDetails());
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(newCallbackRequest.getCaseDetails(), true);
    }

    @Test
    public void sendConsentOrderMadeConfirmationEmailToApplicantSolicitor() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENT_ORDER_MADE))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendConsentOrderMadeConfirmationEmailToApplicantSolicitor(newCallbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(newCallbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENT_ORDER_MADE));
    }

    @Test
    public void sendConsentOrderMadeConfirmationEmailToRespondentSolicitor() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENT_ORDER_MADE))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendConsentOrderMadeConfirmationEmailToRespondentSolicitor(newCallbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(newCallbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENT_ORDER_MADE));
    }

    @Test
    public void throwExceptionWhenConsentOrderMadeNotificationEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENT_ORDER_MADE))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendConsentOrderMadeConfirmationEmailToApplicantSolicitor(newCallbackRequest.getCaseDetails());
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(newCallbackRequest.getCaseDetails());
    }

    @Test
    public void sendPrepareForHearingNotificationEmailToApplicantSolicitor() {
        callbackRequest = getContestedNewCallbackRequest();

        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_PREPARE_FOR_HEARING))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendPrepareForHearingEmailApplicant(newCallbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(newCallbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_PREPARE_FOR_HEARING));
    }

    @Test
    public void sendPrepareForHearingNotificationEmailToRespondentSolicitor() {
        callbackRequest = getContestedNewCallbackRequest();

        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_PREPARE_FOR_HEARING))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendPrepareForHearingEmailRespondent(newCallbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(newCallbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_PREPARE_FOR_HEARING));
    }

    @Test
    public void throwExceptionWhenPrepareForHearingNotificationEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_PREPARE_FOR_HEARING))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendPrepareForHearingEmailApplicant(newCallbackRequest.getCaseDetails());
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(newCallbackRequest.getCaseDetails());
    }

    @Test
    public void sendConsentOrderNotApprovedNotificationEmailToApplicantSolicitor() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENT_ORDER_NOT_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendConsentOrderNotApprovedEmailToApplicantSolicitor(newCallbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(newCallbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENT_ORDER_NOT_APPROVED));
    }

    @Test
    public void sendConsentOrderNotApprovedNotificationEmailToRespondentSolicitor() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENT_ORDER_NOT_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendConsentOrderNotApprovedEmailToRespondentSolicitor(newCallbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(newCallbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENT_ORDER_NOT_APPROVED));
    }

    @Test
    public void throwExceptionWhenConsentOrderNotApprovedNotificationEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENT_ORDER_NOT_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendConsentOrderNotApprovedEmailToApplicantSolicitor(newCallbackRequest.getCaseDetails());
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(newCallbackRequest.getCaseDetails());
    }

    @Test
    public void sendConsentOrderAvailableNotificationEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENT_ORDER_AVAILABLE))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendConsentOrderAvailableEmailToApplicantSolicitor(newCallbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(newCallbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENT_ORDER_AVAILABLE));
    }

    @Test
    public void throwExceptionWhenConsentOrderAvailableNotificationEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENT_ORDER_AVAILABLE))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendConsentOrderAvailableEmailToApplicantSolicitor(newCallbackRequest.getCaseDetails());
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(newCallbackRequest.getCaseDetails());
    }

    @Test
    public void sendConsentOrderAvailableNotificationCtscEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENT_ORDER_AVAILABLE_CTSC))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendConsentOrderAvailableCtscEmail(newCallbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(newCallbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENT_ORDER_AVAILABLE_CTSC));
    }

    @Test
    public void throwExceptionWhenConsentOrderAvailableCtscNotificationEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENT_ORDER_AVAILABLE_CTSC))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendConsentOrderAvailableCtscEmail(newCallbackRequest.getCaseDetails());
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(newCallbackRequest.getCaseDetails());
    }

    @Test
    public void throwExceptionWhenConsentOrderAvailableEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENT_ORDER_AVAILABLE))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        try {
            notificationService.sendConsentOrderAvailableEmailToRespondentSolicitor(newCallbackRequest.getCaseDetails());
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(newCallbackRequest.getCaseDetails());
    }

    @Test
    public void sendContestedApplicationIssuedEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_APPLICATION_ISSUED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());

        notificationService.sendContestedApplicationIssuedEmailToApplicantSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_APPLICATION_ISSUED));
    }

    @Test
    public void throwExceptionWhenContestedApplicationIssuedEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_APPLICATION_ISSUED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendContestedApplicationIssuedEmailToApplicantSolicitor(callbackRequest.getCaseDetails());
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void sendContestOrderApprovedEmailApplicantSolicitor() {
        notificationService.sendContestOrderApprovedEmailApplicant(newCallbackRequest.getCaseDetails());

        verify(notificationRequestMapper)
            .getNotificationRequestForApplicantSolicitor(newCallbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTEST_ORDER_APPROVED_APPLICANT));
    }

    @Test
    public void throwExceptionWhenContestOrderApprovedEmailForApplicantSolicitorIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTEST_ORDER_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendContestOrderApprovedEmailApplicant(newCallbackRequest.getCaseDetails());
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }

        verify(notificationRequestMapper)
            .getNotificationRequestForApplicantSolicitor(newCallbackRequest.getCaseDetails());
    }

    @Test
    public void sendContestOrderApprovedEmailRespondentSolicitor() {
        notificationService.sendContestOrderApprovedEmailRespondent(newCallbackRequest.getCaseDetails());

        verify(notificationRequestMapper)
            .getNotificationRequestForRespondentSolicitor(newCallbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTEST_ORDER_APPROVED_RESPONDENT));
    }

    @Test
    public void throwExceptionWhenContestOrderApprovedEmailForRespondentSolicitorIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTEST_ORDER_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendContestOrderApprovedEmailRespondent(newCallbackRequest.getCaseDetails());
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }

        verify(notificationRequestMapper)
            .getNotificationRequestForRespondentSolicitor(newCallbackRequest.getCaseDetails());
    }

    @Test
    public void sendFinremContestOrderApprovedEmailIntervener1Solicitor() {
        notificationService.sendContestOrderApprovedEmailIntervener(newCallbackRequest.getCaseDetails(),
            dataKeysWrapper, IntervenerType.INTERVENER_ONE);

        verify(notificationRequestMapper)
            .getNotificationRequestForIntervenerSolicitor(newCallbackRequest.getCaseDetails(), dataKeysWrapper);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTEST_ORDER_APPROVED_INTERVENER1));
    }

    @Test
    public void sendFinremContestOrderApprovedEmailIntervener2Solicitor() {
        notificationService.sendContestOrderApprovedEmailIntervener(newCallbackRequest.getCaseDetails(),
            dataKeysWrapper, IntervenerType.INTERVENER_TWO);

        verify(notificationRequestMapper)
            .getNotificationRequestForIntervenerSolicitor(newCallbackRequest.getCaseDetails(), dataKeysWrapper);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTEST_ORDER_APPROVED_INTERVENER2));
    }

    @Test
    public void sendFinremContestOrderApprovedEmailIntervener3Solicitor() {
        notificationService.sendContestOrderApprovedEmailIntervener(newCallbackRequest.getCaseDetails(),
            dataKeysWrapper, IntervenerType.INTERVENER_THREE);

        verify(notificationRequestMapper)
            .getNotificationRequestForIntervenerSolicitor(newCallbackRequest.getCaseDetails(), dataKeysWrapper);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTEST_ORDER_APPROVED_INTERVENER3));
    }

    @Test
    public void sendFinremContestOrderApprovedEmailIntervener4Solicitor() {
        notificationService.sendContestOrderApprovedEmailIntervener(newCallbackRequest.getCaseDetails(),
            dataKeysWrapper, IntervenerType.INTERVENER_FOUR);

        verify(notificationRequestMapper)
            .getNotificationRequestForIntervenerSolicitor(newCallbackRequest.getCaseDetails(), dataKeysWrapper);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTEST_ORDER_APPROVED_INTERVENER4));
    }

    @Test
    public void sendSolicitorToDraftOrderEmailRespondent() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_DRAFT_ORDER))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendSolicitorToDraftOrderEmailRespondent(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_DRAFT_ORDER));
    }

    @Test
    public void throwExceptionWhenSendSolicitorToDraftOrderEmailRespondentIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_DRAFT_ORDER))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendSolicitorToDraftOrderEmailRespondent(callbackRequest.getCaseDetails());
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void sendSolicitorToDraftOrderEmailApplicant() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_DRAFT_ORDER))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendSolicitorToDraftOrderEmailApplicant(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_DRAFT_ORDER));
    }

    @Test
    public void throwExceptionWhenSendSolicitorToDraftOrderEmailApplicant() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_DRAFT_ORDER))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendSolicitorToDraftOrderEmailApplicant(callbackRequest.getCaseDetails());
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void throwExceptionWhenContestOrderApprovedEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTEST_ORDER_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendContestOrderApprovedEmailApplicant(newCallbackRequest.getCaseDetails());
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }

        verify(notificationRequestMapper)
            .getNotificationRequestForApplicantSolicitor(newCallbackRequest.getCaseDetails());
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmail() {
        callbackRequest = getContestedNewCallbackRequest();
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(newCallbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(newCallbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_HWF_SUCCESSFUL));
    }

    @Test
    public void throwExceptionWhenContestedHwfSuccessfulNotificationEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendContestedHwfSuccessfulConfirmationEmail(newCallbackRequest.getCaseDetails());
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }
    }

    @Test
    public void sendGeneralEmailConsented() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_GENERAL_EMAIL_CONSENT))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendConsentGeneralEmail(callbackRequest.getCaseDetails(), anyString());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENT_GENERAL_EMAIL));
    }

    @Test
    public void sendGeneralEmailContested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_GENERAL_EMAIL_CONTESTED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedGeneralEmail(callbackRequest.getCaseDetails(), anyString());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_GENERAL_EMAIL));
    }

    @Test
    public void throwExceptionWhenGeneralEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_GENERAL_EMAIL_CONSENT))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendConsentGeneralEmail(callbackRequest.getCaseDetails(), anyString());
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }
    }

    @Test
    public void sendContestOrderNotApprovedNotificationEmailApplicant() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTEST_ORDER_NOT_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());

        notificationService.sendContestOrderNotApprovedEmailApplicant(newCallbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(newCallbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTEST_ORDER_NOT_APPROVED));
    }

    @Test
    public void throwExceptionWhenContestOrderNotApprovedNotificationEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTEST_ORDER_NOT_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendContestOrderNotApprovedEmailApplicant(newCallbackRequest.getCaseDetails());
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }
    }

    @Test
    public void sendContestOrderNotApprovedNotificationEmailRespondent() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTEST_ORDER_NOT_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestOrderNotApprovedEmailRespondent(newCallbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(newCallbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTEST_ORDER_NOT_APPROVED));
    }

    @Test
    public void sendContestedConsentOrderApprovedNotificationEmailToApplicantSolicitor() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_CONSENT_ORDER_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedConsentOrderApprovedEmailToApplicantSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_CONSENT_ORDER_APPROVED));
    }

    @Test
    public void sendContestedConsentOrderApprovedNotificationEmailToRespondentSolicitor() {

        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_CONSENT_ORDER_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedConsentOrderApprovedEmailToRespondentSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_CONSENT_ORDER_APPROVED));
    }

    @Test
    public void sendContestedGeneralApplicationReferToJudgeNotificationEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_GENERAL_APPLICATION_REFER_TO_JUDGE))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andExpect(MockRestRequestMatchers.jsonPath("notificationEmail").value(TEST_JUDGE_EMAIL))
            .andRespond(MockRestResponseCreators.withNoContent());

        callbackRequest = getContestedNewCallbackRequest();

        notificationService.sendContestedGeneralApplicationReferToJudgeEmail(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_GENERAL_APPLICATION_REFER_TO_JUDGE));
    }

    @Test
    public void throwExceptionWhenContestedGeneralApplicationReferToJudgeEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_GENERAL_APPLICATION_REFER_TO_JUDGE))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        callbackRequest = getContestedNewCallbackRequest();

        try {
            notificationService.sendContestedGeneralApplicationReferToJudgeEmail(callbackRequest.getCaseDetails());
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void sendContestedGeneralApplicationOutcomeNotificationEmailWhenSendToFRCToggleTrue() throws IOException {
        when(featureToggleService.isSendToFRCEnabled()).thenReturn(true);

        callbackRequest = getContestedNewCallbackRequest();

        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_GENERAL_APPLICATION_OUTCOME))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andExpect(MockRestRequestMatchers.jsonPath("notificationEmail")
                .value(NOTTINGHAM_FRC_EMAIL))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedGeneralApplicationOutcomeEmail(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_GENERAL_APPLICATION_OUTCOME));
    }

    @Test
    public void sendContestedGeneralApplicationOutcomeNotificationEmailToTestAccountWhenSendToFRCToggleFalse() throws IOException {
        when(featureToggleService.isSendToFRCEnabled()).thenReturn(false);

        callbackRequest = getContestedNewCallbackRequest();

        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_GENERAL_APPLICATION_OUTCOME))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andExpect(MockRestRequestMatchers.jsonPath("notificationEmail")
                .value("fr_applicant_solicitor1@mailinator.com"))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedGeneralApplicationOutcomeEmail(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_GENERAL_APPLICATION_OUTCOME));
    }

    @Test
    public void sendContestedConsentGeneralOrderNotificationEmailApplicantSolicitor() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_CONSENT_GENERAL_ORDER))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedConsentGeneralOrderEmailApplicantSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_GENERAL_ORDER_CONSENT));
    }

    @Test
    public void sendContestedConsentGeneralOrderNotificationEmailRespondentSolicitor() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_CONSENT_GENERAL_ORDER))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedConsentGeneralOrderEmailRespondentSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_GENERAL_ORDER_CONSENT));
    }

    @Test
    public void sendContestedGeneralOrderNotificationEmailApplicant() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_GENERAL_ORDER))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedGeneralOrderEmailApplicant(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_GENERAL_ORDER));
    }

    @Test
    public void sendContestedGeneralOrderNotificationEmailRespondent() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_GENERAL_ORDER))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedGeneralOrderEmailRespondent(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_GENERAL_ORDER));
    }

    @Test
    public void sendConsentedGeneralOrderNotificationEmailApplicant() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENTED_GENERAL_ORDER))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendConsentedGeneralOrderEmailToApplicantSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENTED_GENERAL_ORDER));
    }

    @Test
    public void sendConsentedGeneralOrderNotificationEmailRespondent() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENTED_GENERAL_ORDER))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendConsentedGeneralOrderEmailToRespondentSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENTED_GENERAL_ORDER));
    }

    @Test
    public void throwExceptionWhenContestedConsentOrderApprovedNotificationEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_CONSENT_ORDER_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendContestedConsentOrderApprovedEmailToApplicantSolicitor(callbackRequest.getCaseDetails());
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void sendContestedConsentOrderNotApprovedNotificationEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_CONSENT_ORDER_NOT_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedConsentOrderNotApprovedEmailApplicantSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_CONSENT_ORDER_NOT_APPROVED));
    }

    @Test
    public void throwExceptionWhenContestedConsentOrderNotApprovedNotificationEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_CONSENT_ORDER_NOT_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendContestedConsentOrderNotApprovedEmailApplicantSolicitor(callbackRequest.getCaseDetails());
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void sendContestedConsentOrderNotApprovedNotificationEmailToRespondentSolicitor() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_CONSENT_ORDER_NOT_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedConsentOrderNotApprovedEmailRespondentSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_CONSENT_ORDER_NOT_APPROVED));
    }

    @Test
    public void sendConsentOrderNotApprovedSentEmailToApplicantSolicitor() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENT_ORDER_NOT_APPROVED_SENT))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendConsentOrderNotApprovedSentEmailToApplicantSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENT_ORDER_NOT_APPROVED_SENT));
    }

    @Test
    public void sendConsentOrderNotApprovedSentEmailToRespondentSolicitor() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENT_ORDER_NOT_APPROVED_SENT))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendConsentOrderNotApprovedSentEmailToRespondentSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENT_ORDER_NOT_APPROVED_SENT));
    }

    @Test
    public void shouldEmailRespondentSolicitor() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(RESP_SOLICITOR_EMAIL, TEST_USER_EMAIL);
        caseData.put(RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT, YES_VALUE);

        when(caseDataService.isPaperApplication(anyMap())).thenReturn(false);
        when(caseDataService.isRespondentRepresentedByASolicitor(any())).thenReturn(true);
        when(caseDataService.isNotEmpty(RESP_SOLICITOR_EMAIL, caseData)).thenReturn(true);

        assertTrue(notificationService.isRespondentSolicitorEmailCommunicationEnabled(caseData));
    }

    @Test
    public void shouldNotEmailRespondentSolicitor() {
        when(caseDataService.isPaperApplication(any(FinremCaseData.class))).thenReturn(true);
        when(caseDataService.isRespondentRepresentedByASolicitor(any())).thenReturn(false);

        assertFalse(notificationService.isRespondentSolicitorEmailCommunicationEnabled(new HashMap<>()));
    }

    @Test
    public void shouldEmailRespondentSolicitorWhenNullEmailConsent() {
        when(caseDataService.isPaperApplication(any(FinremCaseData.class))).thenReturn(false);
        when(caseDataService.isRespondentRepresentedByASolicitor(any())).thenReturn(true);
        when(caseDataService.isNotEmpty(any(), any())).thenReturn(true);

        Map<String, Object> caseData = new HashMap<>();
        caseData.put(RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT, null);

        boolean isRespondentCommunicationEnabled = notificationService.isRespondentSolicitorEmailCommunicationEnabled(caseData);

        assertTrue(isRespondentCommunicationEnabled);

    }

    @Test
    public void shouldEmailContestedAppSolicitor() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CONTESTED_SOLICITOR_EMAIL, TEST_USER_EMAIL);
        caseData.put(APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED, YES_VALUE);

        when(caseDataService.isPaperApplication(any(FinremCaseData.class))).thenReturn(false);
        when(caseDataService.isApplicantRepresentedByASolicitor(any())).thenReturn(true);
        when(caseDataService.isNotEmpty(CONTESTED_SOLICITOR_EMAIL, caseData)).thenReturn(true);

        assertTrue(notificationService.isContestedApplicantSolicitorEmailCommunicationEnabled(caseData));
    }

    @Test
    public void shouldNotEmailContestedAppSolicitor() {
        when(caseDataService.isPaperApplication(any(FinremCaseData.class))).thenReturn(true);
        when(caseDataService.isApplicantRepresentedByASolicitor(any())).thenReturn(false);

        assertFalse(notificationService.isContestedApplicantSolicitorEmailCommunicationEnabled(new HashMap<>()));
    }

    @Test
    public void sendTransferToCourtEmailConsented() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_TRANSFER_TO_LOCAL_COURT))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendTransferToLocalCourtEmail(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_TRANSFER_TO_LOCAL_COURT));
    }

    @Test
    public void throwExceptionWhenTransferToCourtEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_TRANSFER_TO_LOCAL_COURT))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendTransferToLocalCourtEmail(callbackRequest.getCaseDetails());
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }
    }

    @Test
    public void sendInterimNotificationEmailToApplicantSolicitor() {
        callbackRequest = getContestedNewCallbackRequest();

        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_INTERIM_HEARING))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendInterimNotificationEmailToApplicantSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_INTERIM_HEARING));
    }

    @Test
    public void sendInterimNotificationEmailToRespondentSolicitor() {
        callbackRequest = getContestedNewCallbackRequest();

        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_INTERIM_HEARING))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendInterimNotificationEmailToRespondentSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_INTERIM_HEARING));
    }

    @Test
    public void givenContestedCaseWhenSendNoticeOfChangeEmailThenSendNoticeOfChangeContestedEmail() {
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setName(TEST_SOLICITOR_NAME);
        notificationRequest.setNotificationEmail("test@test.com");
        when(notificationRequestMapper.getNotificationRequestForNoticeOfChange(any())).thenReturn(notificationRequest);
        FinremCaseDetails caseDetails = getContestedNewCallbackRequest().getCaseDetails();

        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_NOTICE_OF_CHANGE_CONTESTED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());

        notificationService.sendNoticeOfChangeEmail(caseDetails);

        verify(notificationRequestMapper).getNotificationRequestForNoticeOfChange(caseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_NOTICE_OF_CHANGE));
    }

    @Test
    public void givenConsentedCaseWhenSendNoticeOfChangeEmailThenSendNoticeOfChangeContestedEmail() {
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setName(TEST_SOLICITOR_NAME);
        notificationRequest.setNotificationEmail("test@test.com");
        when(notificationRequestMapper.getNotificationRequestForNoticeOfChange(any())).thenReturn(notificationRequest);
        when(caseDataService.isConsentedApplication(any(FinremCaseDetails.class))).thenReturn(true);

        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_NOTICE_OF_CHANGE_CONSENTED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());

        notificationService.sendNoticeOfChangeEmail(newCallbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForNoticeOfChange(newCallbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENTED_NOTICE_OF_CHANGE));
    }

    @Test
    public void givenConsentedCaseWhenSendNoticeOfChangeEmailWithoutNotificationEmail_thenNotSendNoticeOfChangeContestedEmail() {
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setName(TEST_SOLICITOR_NAME);
        when(notificationRequestMapper.getNotificationRequestForNoticeOfChange(any())).thenReturn(notificationRequest);
        when(caseDataService.isConsentedApplication(any(FinremCaseDetails.class))).thenReturn(true);

        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_NOTICE_OF_CHANGE_CONSENTED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());

        notificationService.sendNoticeOfChangeEmail(newCallbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForNoticeOfChange(newCallbackRequest.getCaseDetails());
        verify(emailService, times(0)).sendConfirmationEmail(any(), eq(FR_CONSENTED_NOTICE_OF_CHANGE));
    }

    @Test
    public void givenContestedCaseAndNonDigitalSol_whenSendNocEmail_thenNotSendContestedEmail() {
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setName(TEST_SOLICITOR_NAME);
        when(notificationRequestMapper.getNotificationRequestForNoticeOfChange(any())).thenReturn(notificationRequest);
        when(checkSolicitorIsDigitalService.isApplicantSolicitorDigital(anyString())).thenReturn(false);
        FinremCaseDetails caseDetails = getContestedNewCallbackRequest().getCaseDetails();

        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_NOTICE_OF_CHANGE_CONTESTED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());

        notificationService.sendNoticeOfChangeEmail(caseDetails);

        verify(notificationRequestMapper).getNotificationRequestForNoticeOfChange(caseDetails);

    }

    @Test
    public void givenContestedCase_whenSendNoCCaseworkerEmail_thenSendContestedEmail() {
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setName(TEST_SOLICITOR_NAME);
        when(notificationRequestMapper.getNotificationRequestForNoticeOfChange(any())).thenReturn(notificationRequest);
        when(checkSolicitorIsDigitalService.isApplicantSolicitorDigital(anyString())).thenReturn(true);
        FinremCaseDetails caseDetails = getContestedNewCallbackRequest().getCaseDetails();

        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_NOC_CASEWORKER_CONTESTED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());

        notificationService.sendNoticeOfChangeEmailCaseworker(caseDetails);

        verify(notificationRequestMapper).getNotificationRequestForNoticeOfChange(caseDetails);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_NOC_CASEWORKER));
    }

    @Test
    public void givenConsentedCase_whenSendNoCCaseworkerEmail_thenSendConsentedEmail() {
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setName(TEST_SOLICITOR_NAME);
        when(notificationRequestMapper.getNotificationRequestForNoticeOfChange(any())).thenReturn(notificationRequest);
        when(checkSolicitorIsDigitalService.isApplicantSolicitorDigital(anyString())).thenReturn(true);
        when(caseDataService.isConsentedApplication(any(FinremCaseDetails.class))).thenReturn(true);

        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_NOC_CASEWORKER_CONSENTED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());

        notificationService.sendNoticeOfChangeEmailCaseworker(newCallbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForNoticeOfChange(newCallbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENTED_NOC_CASEWORKER));

    }

    @Test
    public void givenContestedCaseAndNonDigitalSol_whenSendNocEmail_thenNotSendContestedEmailCaseworker() {
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setName(TEST_SOLICITOR_NAME);
        when(notificationRequestMapper.getNotificationRequestForNoticeOfChange(any())).thenReturn(notificationRequest);
        when(checkSolicitorIsDigitalService.isApplicantSolicitorDigital(anyString())).thenReturn(false);
        FinremCaseDetails caseDetails = getContestedNewCallbackRequest().getCaseDetails();

        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_NOC_CASEWORKER_CONTESTED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());

        notificationService.sendNoticeOfChangeEmailCaseworker(caseDetails);

        verify(notificationRequestMapper).getNotificationRequestForNoticeOfChange(caseDetails);
        verifyNoMoreInteractions(emailService);

    }

    public void sendUpdateFrcInformationEmailToAppSolicitor() {
        newCallbackRequest = getContestedNewCallbackRequest();

        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_UPDATE_FRC_INFORMATION))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendUpdateFrcInformationEmailToAppSolicitor(newCallbackRequest.getCaseDetails());
        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(newCallbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_UPDATE_FRC_SOL));
    }

    @Test
    public void sendUpdateFrcInformationEmailToRespSolicitor() {
        newCallbackRequest = getContestedNewCallbackRequest();

        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_UPDATE_FRC_INFORMATION))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendUpdateFrcInformationEmailToRespondentSolicitor(newCallbackRequest.getCaseDetails());
        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(newCallbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_UPDATE_FRC_SOL));
    }

    @Test
    public void sendUpdateFrcInformationEmailToCourt() throws JsonProcessingException {
        when(featureToggleService.isSendToFRCEnabled()).thenReturn(true);

        newCallbackRequest = getContestedNewCallbackRequest();
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_UPDATE_FRC_INFO_COURT))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());

        notificationService.sendUpdateFrcInformationEmailToCourt(newCallbackRequest.getCaseDetails());
        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(newCallbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_UPDATE_FRC_COURT));
    }

    @Test
    public void sendConsentOrderAvailableEmailToIntervenerSolicitor() {
        notificationService.sendConsentOrderAvailableEmailToIntervenerSolicitor(callbackRequest.getCaseDetails(),
            dataKeysWrapper);

        verify(notificationRequestMapper).getNotificationRequestForIntervenerSolicitor(callbackRequest.getCaseDetails(), dataKeysWrapper);
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENT_ORDER_AVAILABLE));
    }

    @Test
    public void sendPrepareForHearingAfterSentNotificationEmailRespondent() {
        notificationService.sendPrepareForHearingOrderSentEmailRespondent(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(any(), eq(FR_CONTESTED_PREPARE_FOR_HEARING_ORDER_SENT));
    }
}
