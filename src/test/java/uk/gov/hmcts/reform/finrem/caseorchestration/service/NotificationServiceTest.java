package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.NotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.PAPER_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_JUDGE_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_EMAIL;

public class NotificationServiceTest extends BaseServiceTest {
    private static final String END_POINT_HWF_SUCCESSFUL = "http://localhost:8086/notify/hwf-successful";
    private static final String END_POINT_ASSIGNED_TO_JUDGE = "http://localhost:8086/notify/assign-to-judge";
    private static final String END_POINT_CONSENT_ORDER_MADE = "http://localhost:8086/notify/consent-order-made";
    private static final String END_POINT_PREPARE_FOR_HEARING = "http://localhost:8086/notify/prepare-for-hearing";
    private static final String END_POINT_PREPARE_FOR_HEARING_ORDER_SENT = "http://localhost:8086/notify/contested/prepare-for-hearing-order-sent";
    private static final String END_POINT_CONSENT_ORDER_NOT_APPROVED = "http://localhost:8086/notify/consent-order-not-approved";
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
    private static final String END_POINT_CONTESTED_GENERAL_APPLICATION_REFER_TO_JUDGE = "http://localhost:8086/notify/contested/general-application-refer-to-judge";
    private static final String END_POINT_CONTESTED_GENERAL_APPLICATION_OUTCOME = "http://localhost:8086/notify/contested/general-application-outcome";
    private static final String END_POINT_CONTESTED_CONSENT_ORDER_NOT_APPROVED = "http://localhost:8086/notify/contested/consent-order-not-approved";

    private static final String ERROR_500_MESSAGE = "500 Internal Server Error";
    private static final String TEST_USER_EMAIL = "fr_applicant_sol@sharklasers.com";
    private static final String NOTTINGHAM_FRC_EMAIL = "FRCNottingham@justice.gov.uk";

    @Autowired
    private NotificationService notificationService;

    private CallbackRequest callbackRequest;

    @Autowired
    private RestTemplate restTemplate;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private NotificationRequestMapper notificationRequestMapper;

    private MockRestServiceServer mockServer;

    @Before
    public void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
        callbackRequest = getConsentedCallbackRequest();

        NotificationRequest notificationRequest = new NotificationRequest();
        when(notificationRequestMapper.createNotificationRequestForAppSolicitor(any(CallbackRequest.class))).thenReturn(notificationRequest);
        when(notificationRequestMapper.createNotificationRequestForRespSolicitor(any(CallbackRequest.class))).thenReturn(notificationRequest);
    }

    @Test
    public void sendHwfSuccessfulNotificationEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendConsentedHWFSuccessfulConfirmationEmail(callbackRequest);

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest);
    }

    @Test
    public void throwExceptionWhenHwfSuccessfulNotificationEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendConsentedHWFSuccessfulConfirmationEmail(callbackRequest);
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest);
    }

    @Test
    public void sendAssignToJudgeNotificationEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_ASSIGNED_TO_JUDGE))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendAssignToJudgeConfirmationEmail(callbackRequest);

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest);
    }

    @Test
    public void throwExceptionWhenAssignToJudgeNotificationEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_ASSIGNED_TO_JUDGE))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));


        try {
            notificationService.sendAssignToJudgeConfirmationEmail(getConsentedCallbackRequest());
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest);
    }

    @Test
    public void sendConsentOrderMadeNotificationEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENT_ORDER_MADE))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendConsentOrderMadeConfirmationEmail(callbackRequest);

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest);
    }

    @Test
    public void throwExceptionWhenConsentOrderMadeNotificationEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENT_ORDER_MADE))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendConsentOrderMadeConfirmationEmail(callbackRequest);
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest);
    }

    @Test
    public void sendPrepareForHearingNotificationEmail() {
        HashMap<String, Object> caseData = new HashMap<>();
        caseData.put(APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED, "Yes");

        callbackRequest = getContestedCallbackRequest();

        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_PREPARE_FOR_HEARING))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendPrepareForHearingEmail(callbackRequest);

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest);
    }

    @Test
    public void throwExceptionWhenPrepareForHearingNotificationEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_PREPARE_FOR_HEARING))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendPrepareForHearingEmail(callbackRequest);
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest);
    }

    @Test
    public void sendPrepareForHearingAfterSentNotificationEmailApplicant() {
        callbackRequest = getContestedCallbackRequest();
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_PREPARE_FOR_HEARING_ORDER_SENT))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendPrepareForHearingOrderSentEmailApplicant(callbackRequest);

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest);
    }

    @Test
    public void throwExceptionWhenPrepareForHearingAfterSentNotificationEmailApplicantIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_PREPARE_FOR_HEARING_ORDER_SENT))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendPrepareForHearingOrderSentEmailApplicant(callbackRequest);
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest);
    }

    @Test
    public void sendPrepareForHearingAfterSentNotificationEmailRespondent() {
        callbackRequest = getContestedCallbackRequest();
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_PREPARE_FOR_HEARING_ORDER_SENT))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendPrepareForHearingOrderSentEmailRespondent(callbackRequest);

        verify(notificationRequestMapper).createNotificationRequestForRespSolicitor(callbackRequest);
    }

    @Test
    public void throwExceptionWhenPrepareForHearingAfterSentNotificationEmailRespondentIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_PREPARE_FOR_HEARING_ORDER_SENT))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendPrepareForHearingOrderSentEmailRespondent(callbackRequest);
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }

        verify(notificationRequestMapper, never()).createNotificationRequestForAppSolicitor(callbackRequest);
    }

    @Test
    public void sendConsentOrderNotApprovedNotificationEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENT_ORDER_NOT_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendConsentOrderNotApprovedEmail(callbackRequest);

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest);
    }

    @Test
    public void throwExceptionWhenConsentOrderNotApprovedNotificationEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENT_ORDER_NOT_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendConsentOrderNotApprovedEmail(callbackRequest);
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest);
    }

    @Test
    public void sendConsentOrderAvailableNotificationEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENT_ORDER_AVAILABLE))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendConsentOrderAvailableEmail(callbackRequest);

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest);
    }

    @Test
    public void throwExceptionWhenConsentOrderAvailableNotificationEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENT_ORDER_AVAILABLE))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendConsentOrderAvailableEmail(callbackRequest);
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest);
    }

    @Test
    public void sendConsentOrderAvailableNotificationCtscEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENT_ORDER_AVAILABLE_CTSC))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendConsentOrderAvailableCtscEmail(callbackRequest);

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest);
    }

    @Test
    public void throwExceptionWhenConsentOrderAvailableCtscNotificationEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENT_ORDER_AVAILABLE_CTSC))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendConsentOrderAvailableCtscEmail(callbackRequest);
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest);
    }

    @Test
    public void sendContestedApplicationIssuedEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_APPLICATION_ISSUED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedApplicationIssuedEmail(callbackRequest);

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest);
    }

    @Test
    public void throwExceptionWhenContestedApplicationIssuedEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_APPLICATION_ISSUED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendContestedApplicationIssuedEmail(callbackRequest);
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest);
    }

    @Test
    public void sendContestOrderApprovedEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTEST_ORDER_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestOrderApprovedEmail(callbackRequest);

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest);
    }

    @Test
    public void throwExceptionWhenContestOrderApprovedEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTEST_ORDER_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendContestOrderApprovedEmail(callbackRequest);
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest);
    }

    @Test
    public void sendSolicitorToDraftOrderEmailRespondent() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_DRAFT_ORDER))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendSolicitorToDraftOrderEmailRespondent(callbackRequest);

        verify(notificationRequestMapper).createNotificationRequestForRespSolicitor(callbackRequest);
    }

    @Test
    public void throwExceptionWhenSendSolicitorToDraftOrderEmailRespondentIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_DRAFT_ORDER))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendSolicitorToDraftOrderEmailRespondent(callbackRequest);
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }

        verify(notificationRequestMapper).createNotificationRequestForRespSolicitor(callbackRequest);
    }

    @Test
    public void sendSolicitorToDraftOrderEmailApplicant() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_DRAFT_ORDER))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendSolicitorToDraftOrderEmailApplicant(callbackRequest);

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest);
    }

    @Test
    public void throwExceptionWhenSendSolicitorToDraftOrderEmailApplicant() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_DRAFT_ORDER))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendSolicitorToDraftOrderEmailApplicant(callbackRequest);
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest);
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmail() {
        callbackRequest = getContestedCallbackRequest();
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest);
    }

    @Test
    public void throwExceptionWhenContestedHwfSuccessfulNotificationEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }
    }

    @Test
    public void sendGeneralEmailConsented() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_GENERAL_EMAIL_CONSENT))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendConsentGeneralEmail(callbackRequest);

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest);
    }

    @Test
    public void sendGeneralEmailContested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_GENERAL_EMAIL_CONTESTED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedGeneralEmail(callbackRequest);

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest);
    }

    @Test
    public void throwExceptionWhenGeneralEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_GENERAL_EMAIL_CONSENT))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendConsentGeneralEmail(callbackRequest);
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }
    }

    @Test
    public void sendContestOrderNotApprovedNotificationEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTEST_ORDER_NOT_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestOrderNotApprovedEmail(callbackRequest);

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest);
    }

    @Test
    public void throwExceptionWhenContestOrderNotApprovedNotificationEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTEST_ORDER_NOT_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendContestOrderNotApprovedEmail(callbackRequest);
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }
    }

    @Test
    public void sendContestedConsentOrderApprovedNotificationEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_CONSENT_ORDER_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedConsentOrderApprovedEmail(callbackRequest);

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest);
    }

    @Test
    public void sendContestedGeneralApplicationReferToJudgeNotificationEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_GENERAL_APPLICATION_REFER_TO_JUDGE))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andExpect(MockRestRequestMatchers.jsonPath("notificationEmail").value(TEST_JUDGE_EMAIL))
            .andRespond(MockRestResponseCreators.withNoContent());

        callbackRequest = getContestedCallbackRequest();

        notificationService.sendContestedGeneralApplicationReferToJudgeEmail(callbackRequest);

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest);
    }

    @Test
    public void throwExceptionWhenContestedGeneralApplicationReferToJudgeEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_GENERAL_APPLICATION_REFER_TO_JUDGE))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        callbackRequest = getContestedCallbackRequest();

        try {
            notificationService.sendContestedGeneralApplicationReferToJudgeEmail(callbackRequest);
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest);
    }

    @Test
    public void sendContestedGeneralApplicationOutcomeNotificationEmailWhenSendToFRCToggleTrue() throws IOException {
        when(featureToggleService.isSendToFRCEnabled()).thenReturn(true);
        callbackRequest = getContestedCallbackRequest();

        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_GENERAL_APPLICATION_OUTCOME))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andExpect(MockRestRequestMatchers.jsonPath("notificationEmail")
                .value(NOTTINGHAM_FRC_EMAIL))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedGeneralApplicationOutcomeEmail(callbackRequest);

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest);
    }

    @Test
    public void sendContestedGeneralApplicationOutcomeNotificationEmailToTestAccountWhenSendToFRCToggleFalse()
        throws IOException {
        when(featureToggleService.isSendToFRCEnabled()).thenReturn(false);
        callbackRequest = getContestedCallbackRequest();

        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_GENERAL_APPLICATION_OUTCOME))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andExpect(MockRestRequestMatchers.jsonPath("notificationEmail")
                .value(TEST_USER_EMAIL))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedGeneralApplicationOutcomeEmail(callbackRequest);

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest);
    }

    @Test
    public void sendContestedConsentGeneralOrderNotificationEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_CONSENT_GENERAL_ORDER))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedConsentGeneralOrderEmail(callbackRequest);

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest);
    }

    @Test
    public void sendContestedGeneralOrderNotificationEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_GENERAL_ORDER))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedGeneralOrderEmail(callbackRequest);

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest);
    }

    @Test
    public void sendConsentedGeneralOrderNotificationEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENTED_GENERAL_ORDER))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendConsentedGeneralOrderEmail(callbackRequest);

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest);
    }

    @Test
    public void throwExceptionWhenContestedConsentOrderApprovedNotificationEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_CONSENT_ORDER_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendContestedConsentOrderApprovedEmail(callbackRequest);
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest);
    }

    @Test
    public void sendContestedConsentOrderNotApprovedNotificationEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_CONSENT_ORDER_NOT_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedConsentOrderNotApprovedEmail(callbackRequest);

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest);
    }

    @Test
    public void throwExceptionWhenContestedConsentOrderNotApprovedNotificationEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_CONSENT_ORDER_NOT_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendContestedConsentOrderNotApprovedEmail(callbackRequest);
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest);
    }

    @Test
    public void shouldEmailRespondentSolicitor_shouldReturnTrue() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(PAPER_APPLICATION, NO_VALUE);
        caseData.put(CONTESTED_RESPONDENT_REPRESENTED, YES_VALUE);
        caseData.put(RESP_SOLICITOR_EMAIL, TEST_USER_EMAIL);

        assertTrue(notificationService.shouldEmailRespondentSolicitor(caseData));
    }

    @Test
    public void shouldEmailRespondentSolicitor_paperApplication() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(PAPER_APPLICATION, YES_VALUE);
        caseData.put(CONTESTED_RESPONDENT_REPRESENTED, YES_VALUE);
        caseData.put(RESP_SOLICITOR_EMAIL, TEST_USER_EMAIL);

        assertFalse(notificationService.shouldEmailRespondentSolicitor(caseData));
    }

    @Test
    public void shouldEmailRespondentSolicitor_notRepresented() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(PAPER_APPLICATION, NO_VALUE);
        caseData.put(CONTESTED_RESPONDENT_REPRESENTED, NO_VALUE);
        caseData.put(RESP_SOLICITOR_EMAIL, TEST_USER_EMAIL);

        assertFalse(notificationService.shouldEmailRespondentSolicitor(caseData));
    }

    @Test
    public void shouldEmailRespondentSolicitor_emailNotProvided() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(PAPER_APPLICATION, NO_VALUE);
        caseData.put(CONTESTED_RESPONDENT_REPRESENTED, YES_VALUE);

        assertFalse(notificationService.shouldEmailRespondentSolicitor(caseData));
    }
}