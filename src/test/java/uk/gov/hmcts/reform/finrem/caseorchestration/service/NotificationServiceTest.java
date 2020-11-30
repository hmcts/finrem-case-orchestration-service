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
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.NotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.PAPER_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_JUDGE_EMAIL;
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

    @Autowired private NotificationService notificationService;
    @Autowired private RestTemplate restTemplate;

    @MockBean private FeatureToggleService featureToggleService;
    @MockBean private NotificationRequestMapper notificationRequestMapper;

    private CallbackRequest callbackRequest;

    private MockRestServiceServer mockServer;

    @Before
    public void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
        callbackRequest = getConsentedCallbackRequest();

        NotificationRequest notificationRequest = new NotificationRequest();
        when(notificationRequestMapper.createNotificationRequestForAppSolicitor(any(CaseDetails.class))).thenReturn(notificationRequest);
        when(notificationRequestMapper.createNotificationRequestForRespSolicitor(any(CaseDetails.class))).thenReturn(notificationRequest);
    }

    @Test
    public void sendHwfSuccessfulNotificationEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendConsentedHWFSuccessfulConfirmationEmail(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest.getCaseDetails());
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

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void sendAssignToJudgeNotificationEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_ASSIGNED_TO_JUDGE))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendAssignToJudgeConfirmationEmail(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void throwExceptionWhenAssignToJudgeNotificationEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_ASSIGNED_TO_JUDGE))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));


        try {
            notificationService.sendAssignToJudgeConfirmationEmail(getConsentedCallbackRequest().getCaseDetails());
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void sendConsentOrderMadeNotificationEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENT_ORDER_MADE))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendConsentOrderMadeConfirmationEmail(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void throwExceptionWhenConsentOrderMadeNotificationEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENT_ORDER_MADE))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendConsentOrderMadeConfirmationEmail(callbackRequest.getCaseDetails());
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void sendPrepareForHearingNotificationEmailToApplicantSolicitor() {
        callbackRequest = getContestedCallbackRequest();

        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_PREPARE_FOR_HEARING))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendPrepareForHearingEmailApplicant(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void sendPrepareForHearingNotificationEmailToRespondentSolicitor() {
        callbackRequest = getContestedCallbackRequest();

        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_PREPARE_FOR_HEARING))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendPrepareForHearingEmailRespondent(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).createNotificationRequestForRespSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void throwExceptionWhenPrepareForHearingNotificationEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_PREPARE_FOR_HEARING))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendPrepareForHearingEmailApplicant(callbackRequest.getCaseDetails());
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void sendPrepareForHearingAfterSentNotificationEmailApplicant() {
        callbackRequest = getContestedCallbackRequest();

        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_PREPARE_FOR_HEARING_ORDER_SENT))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendPrepareForHearingOrderSentEmailApplicant(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void throwExceptionWhenPrepareForHearingAfterSentNotificationEmailApplicantIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_PREPARE_FOR_HEARING_ORDER_SENT))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendPrepareForHearingOrderSentEmailApplicant(callbackRequest.getCaseDetails());
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void throwExceptionWhenPrepareForHearingAfterSentNotificationEmailRespondentIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_PREPARE_FOR_HEARING_ORDER_SENT))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendPrepareForHearingOrderSentEmailRespondent(callbackRequest.getCaseDetails());
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }

        verify(notificationRequestMapper).createNotificationRequestForRespSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void sendPrepareForHearingAfterSentNotificationEmailRespondent() {
        callbackRequest = getContestedCallbackRequest();
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_PREPARE_FOR_HEARING_ORDER_SENT))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendPrepareForHearingOrderSentEmailRespondent(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).createNotificationRequestForRespSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void sendConsentOrderNotApprovedNotificationEmailToApplicantSolicitor() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENT_ORDER_NOT_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendConsentOrderNotApprovedEmailToApplicantSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void sendConsentOrderNotApprovedNotificationEmailToRespondentSolicitor() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENT_ORDER_NOT_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendConsentOrderNotApprovedEmailToRespondentSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).createNotificationRequestForRespSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void throwExceptionWhenConsentOrderNotApprovedNotificationEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENT_ORDER_NOT_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendConsentOrderNotApprovedEmailToApplicantSolicitor(callbackRequest.getCaseDetails());
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void sendConsentOrderAvailableNotificationEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENT_ORDER_AVAILABLE))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendConsentOrderAvailableEmail(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void throwExceptionWhenConsentOrderAvailableNotificationEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENT_ORDER_AVAILABLE))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendConsentOrderAvailableEmail(callbackRequest.getCaseDetails());
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void sendConsentOrderAvailableNotificationCtscEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENT_ORDER_AVAILABLE_CTSC))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendConsentOrderAvailableCtscEmail(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void throwExceptionWhenConsentOrderAvailableCtscNotificationEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENT_ORDER_AVAILABLE_CTSC))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendConsentOrderAvailableCtscEmail(callbackRequest.getCaseDetails());
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void sendContestedApplicationIssuedEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_APPLICATION_ISSUED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());

        notificationService.sendContestedApplicationIssuedEmailToApplicantSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest.getCaseDetails());
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

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void sendContestedApplicationIssuedRespondentEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_APPLICATION_ISSUED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedApplicationIssuedEmailToRespondentSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).createNotificationRequestForRespSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void throwExceptionWhenContestedApplicationIssuedRespondentEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_APPLICATION_ISSUED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendContestedApplicationIssuedEmailToRespondentSolicitor(callbackRequest.getCaseDetails());
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }

        verify(notificationRequestMapper).createNotificationRequestForRespSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void sendContestOrderApprovedEmailApplicantSolicitor() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTEST_ORDER_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestOrderApprovedEmailApplicant(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void throwExceptionWhenContestOrderApprovedEmailForApplicantSolicitorIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTEST_ORDER_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendContestOrderApprovedEmailApplicant(callbackRequest.getCaseDetails());
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void sendContestOrderApprovedEmailRespondentSolicitor() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTEST_ORDER_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestOrderApprovedEmailRespondent(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).createNotificationRequestForRespSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void throwExceptionWhenContestOrderApprovedEmailForRespondentSolicitorIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTEST_ORDER_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendContestOrderApprovedEmailRespondent(callbackRequest.getCaseDetails());
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }

        verify(notificationRequestMapper).createNotificationRequestForRespSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void sendSolicitorToDraftOrderEmailRespondent() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_DRAFT_ORDER))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendSolicitorToDraftOrderEmailRespondent(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).createNotificationRequestForRespSolicitor(callbackRequest.getCaseDetails());
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

        verify(notificationRequestMapper).createNotificationRequestForRespSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void sendSolicitorToDraftOrderEmailApplicant() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_DRAFT_ORDER))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendSolicitorToDraftOrderEmailApplicant(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest.getCaseDetails());
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

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void sendContestOrderApprovedEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTEST_ORDER_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestOrderApprovedEmailApplicant(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void throwExceptionWhenContestOrderApprovedEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTEST_ORDER_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendContestOrderApprovedEmailApplicant(callbackRequest.getCaseDetails());
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmail() {
        callbackRequest = getContestedCallbackRequest();
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void throwExceptionWhenContestedHwfSuccessfulNotificationEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest.getCaseDetails());
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }
    }

    @Test
    public void sendGeneralEmailConsented() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_GENERAL_EMAIL_CONSENT))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendConsentGeneralEmail(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void sendGeneralEmailContested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_GENERAL_EMAIL_CONTESTED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedGeneralEmail(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void throwExceptionWhenGeneralEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_GENERAL_EMAIL_CONSENT))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendConsentGeneralEmail(callbackRequest.getCaseDetails());
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }
    }

    @Test
    public void sendContestOrderNotApprovedNotificationEmailApplicant() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTEST_ORDER_NOT_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());

        notificationService.sendContestOrderNotApprovedEmailApplicant(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void throwExceptionWhenContestOrderNotApprovedNotificationEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTEST_ORDER_NOT_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendContestOrderNotApprovedEmailApplicant(callbackRequest.getCaseDetails());
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }
    }

    @Test
    public void sendContestOrderNotApprovedNotificationEmailRespondent() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTEST_ORDER_NOT_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestOrderNotApprovedEmailRespondent(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).createNotificationRequestForRespSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void sendContestedConsentOrderApprovedNotificationEmailToApplicantSolicitor() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_CONSENT_ORDER_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedConsentOrderApprovedEmailToApplicantSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void sendContestedConsentOrderApprovedNotificationEmailToRespondentSolicitor() {

        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_CONSENT_ORDER_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedConsentOrderApprovedEmailToRespondentSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).createNotificationRequestForRespSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void sendContestedGeneralApplicationReferToJudgeNotificationEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_GENERAL_APPLICATION_REFER_TO_JUDGE))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andExpect(MockRestRequestMatchers.jsonPath("notificationEmail").value(TEST_JUDGE_EMAIL))
            .andRespond(MockRestResponseCreators.withNoContent());

        callbackRequest = getContestedCallbackRequest();

        notificationService.sendContestedGeneralApplicationReferToJudgeEmail(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void throwExceptionWhenContestedGeneralApplicationReferToJudgeEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_GENERAL_APPLICATION_REFER_TO_JUDGE))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        callbackRequest = getContestedCallbackRequest();

        try {
            notificationService.sendContestedGeneralApplicationReferToJudgeEmail(callbackRequest.getCaseDetails());
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest.getCaseDetails());
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
        notificationService.sendContestedGeneralApplicationOutcomeEmail(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void sendContestedGeneralApplicationOutcomeNotificationEmailToTestAccountWhenSendToFRCToggleFalse() throws IOException {
        when(featureToggleService.isSendToFRCEnabled()).thenReturn(false);

        callbackRequest = getContestedCallbackRequest();

        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_GENERAL_APPLICATION_OUTCOME))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andExpect(MockRestRequestMatchers.jsonPath("notificationEmail")
                .value(TEST_USER_EMAIL))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedGeneralApplicationOutcomeEmail(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void sendContestedConsentGeneralOrderNotificationEmailApplicantSolicitor() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_CONSENT_GENERAL_ORDER))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedConsentGeneralOrderEmailApplicantSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void sendContestedConsentGeneralOrderNotificationEmailRespondentSolicitor() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_CONSENT_GENERAL_ORDER))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedConsentGeneralOrderEmailRespondentSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).createNotificationRequestForRespSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void sendContestedGeneralOrderNotificationEmailApplicant() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_GENERAL_ORDER))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedGeneralOrderEmailApplicant(callbackRequest.getCaseDetails());
    }

    @Test
    public void sendContestedGeneralOrderNotificationEmailRespondent() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_GENERAL_ORDER))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedGeneralOrderEmailRespondent(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).createNotificationRequestForRespSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void sendConsentedGeneralOrderNotificationEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENTED_GENERAL_ORDER))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendConsentedGeneralOrderEmail(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest.getCaseDetails());
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

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void sendContestedConsentOrderNotApprovedNotificationEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_CONSENT_ORDER_NOT_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedConsentOrderNotApprovedEmailApplicantSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest.getCaseDetails());
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

        verify(notificationRequestMapper).createNotificationRequestForAppSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void sendContestedConsentOrderNotApprovedNotificationEmailToRespondentSolicitor() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_CONSENT_ORDER_NOT_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedConsentOrderNotApprovedEmailRespondentSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).createNotificationRequestForRespSolicitor(callbackRequest.getCaseDetails());
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
