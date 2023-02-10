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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckSolicitorIsDigitalService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_JUDGE_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT;

public class FinremNotificationServiceTest extends BaseServiceTest {

    private static final String END_POINT_HWF_SUCCESSFUL = "http://localhost:8086/notify/hwf-successful";
    private static final String END_POINT_ASSIGNED_TO_JUDGE = "http://localhost:8086/notify/assign-to-judge";
    private static final String END_POINT_CONSENT_ORDER_MADE = "http://localhost:8086/notify/consent-order-made";
    private static final String END_POINT_PREPARE_FOR_HEARING = "http://localhost:8086/notify/prepare-for-hearing";
    private static final String END_POINT_PREPARE_FOR_HEARING_ORDER_SENT = "http://localhost:8086/notify/contested/prepare-for-hearing-order-sent";
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

    private static final String TEST_JSON = "/fixtures/contested/interim-hearing-two-collection.json";

    @Autowired
    private NotificationService notificationService;
    @Autowired
    private RestTemplate restTemplate;

    @MockBean
    private FeatureToggleService featureToggleService;
    @MockBean
    private RestTemplate mockRestTemplate;
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

    @Before
    public void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
        callbackRequest = getConsentedNewCallbackRequest();
        newCallbackRequest = getConsentedNewCallbackRequest();

        NotificationRequest notificationRequest = new NotificationRequest();
        when(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(any(FinremCaseDetails.class))).thenReturn(notificationRequest);
        when(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(any(FinremCaseDetails.class))).thenReturn(notificationRequest);
    }

    @Test
    public void sendHwfSuccessfulNotificationEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendConsentedHWFSuccessfulConfirmationEmail(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
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

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(newCallbackRequest.getCaseDetails());
    }

    @Test
    public void sendAssignToJudgeNotificationEmailToRespondentSolicitor() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_ASSIGNED_TO_JUDGE))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendAssignToJudgeConfirmationEmailToRespondentSolicitor(newCallbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(newCallbackRequest.getCaseDetails());
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

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(newCallbackRequest.getCaseDetails());
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

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(newCallbackRequest.getCaseDetails());
    }

    @Test
    public void sendConsentOrderMadeConfirmationEmailToApplicantSolicitor() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENT_ORDER_MADE))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendConsentOrderMadeConfirmationEmailToApplicantSolicitor(newCallbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(newCallbackRequest.getCaseDetails());
    }

    @Test
    public void sendConsentOrderMadeConfirmationEmailToRespondentSolicitor() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENT_ORDER_MADE))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendConsentOrderMadeConfirmationEmailToRespondentSolicitor(newCallbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(newCallbackRequest.getCaseDetails());
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
    }

    @Test
    public void sendPrepareForHearingNotificationEmailToRespondentSolicitor() {
        callbackRequest = getContestedNewCallbackRequest();

        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_PREPARE_FOR_HEARING))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendPrepareForHearingEmailRespondent(newCallbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(newCallbackRequest.getCaseDetails());
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
    public void sendPrepareForHearingAfterSentNotificationEmailApplicant() {
        callbackRequest = getContestedNewCallbackRequest();

        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_PREPARE_FOR_HEARING_ORDER_SENT))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendPrepareForHearingOrderSentEmailApplicant(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
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

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
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

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void sendPrepareForHearingAfterSentNotificationEmailRespondent() {
        callbackRequest = getContestedNewCallbackRequest();
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_PREPARE_FOR_HEARING_ORDER_SENT))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendPrepareForHearingOrderSentEmailRespondent(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void sendConsentOrderNotApprovedNotificationEmailToApplicantSolicitor() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENT_ORDER_NOT_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendConsentOrderNotApprovedEmailToApplicantSolicitor(newCallbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(newCallbackRequest.getCaseDetails());
    }

    @Test
    public void sendConsentOrderNotApprovedNotificationEmailToRespondentSolicitor() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENT_ORDER_NOT_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendConsentOrderNotApprovedEmailToRespondentSolicitor(newCallbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(newCallbackRequest.getCaseDetails());
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
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTEST_ORDER_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestOrderApprovedEmailApplicant(newCallbackRequest.getCaseDetails());

        verify(notificationRequestMapper, timeout(100).times(1))
            .getNotificationRequestForApplicantSolicitor(newCallbackRequest.getCaseDetails());
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

        verify(notificationRequestMapper, timeout(100).times(1))
            .getNotificationRequestForApplicantSolicitor(newCallbackRequest.getCaseDetails());
    }

    @Test
    public void sendContestOrderApprovedEmailRespondentSolicitor() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTEST_ORDER_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestOrderApprovedEmailRespondent(newCallbackRequest.getCaseDetails());

        verify(notificationRequestMapper, timeout(100).times(1))
            .getNotificationRequestForRespondentSolicitor(newCallbackRequest.getCaseDetails());
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

        verify(notificationRequestMapper, timeout(100).times(1))
            .getNotificationRequestForRespondentSolicitor(newCallbackRequest.getCaseDetails());
    }

    @Test
    public void sendSolicitorToDraftOrderEmailRespondent() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_DRAFT_ORDER))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendSolicitorToDraftOrderEmailRespondent(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
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
    public void sendContestOrderApprovedEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTEST_ORDER_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestOrderApprovedEmailApplicant(newCallbackRequest.getCaseDetails());

        verify(notificationRequestMapper, timeout(100).times(1))
            .getNotificationRequestForApplicantSolicitor(newCallbackRequest.getCaseDetails());
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

        verify(notificationRequestMapper, timeout(100).times(1))
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
        notificationService.sendConsentGeneralEmail(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void sendGeneralEmailContested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_GENERAL_EMAIL_CONTESTED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedGeneralEmail(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
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

        notificationService.sendContestOrderNotApprovedEmailApplicant(newCallbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(newCallbackRequest.getCaseDetails());
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
    }

    @Test
    public void sendContestedConsentOrderApprovedNotificationEmailToApplicantSolicitor() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_CONSENT_ORDER_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedConsentOrderApprovedEmailToApplicantSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void sendContestedConsentOrderApprovedNotificationEmailToRespondentSolicitor() {

        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_CONSENT_ORDER_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedConsentOrderApprovedEmailToRespondentSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
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
    }

    @Test
    public void sendContestedConsentGeneralOrderNotificationEmailApplicantSolicitor() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_CONSENT_GENERAL_ORDER))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedConsentGeneralOrderEmailApplicantSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void sendContestedConsentGeneralOrderNotificationEmailRespondentSolicitor() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_CONSENT_GENERAL_ORDER))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedConsentGeneralOrderEmailRespondentSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
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

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void sendConsentedGeneralOrderNotificationEmailApplicant() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENTED_GENERAL_ORDER))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendConsentedGeneralOrderEmailToApplicantSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void sendConsentedGeneralOrderNotificationEmailRespondent() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENTED_GENERAL_ORDER))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendConsentedGeneralOrderEmailToRespondentSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
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
    }

    @Test
    public void sendConsentOrderNotApprovedSentEmailToApplicantSolicitor() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENT_ORDER_NOT_APPROVED_SENT))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendConsentOrderNotApprovedSentEmailToApplicantSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void sendConsentOrderNotApprovedSentEmailToRespondentSolicitor() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENT_ORDER_NOT_APPROVED_SENT))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendConsentOrderNotApprovedSentEmailToRespondentSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void shouldEmailRespondentSolicitor() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(RESP_SOLICITOR_EMAIL, TEST_USER_EMAIL);
        caseData.put(RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT, YES_VALUE);

        when(caseDataService.isPaperApplication(any())).thenReturn(false);
        when(caseDataService.isRespondentRepresentedByASolicitor(any())).thenReturn(true);
        when(caseDataService.isNotEmpty(RESP_SOLICITOR_EMAIL, caseData)).thenReturn(true);

        assertTrue(notificationService.isRespondentSolicitorEmailCommunicationEnabled(caseData));
    }

    @Test
    public void shouldNotEmailRespondentSolicitor() {
        when(caseDataService.isPaperApplication(any())).thenReturn(true);
        when(caseDataService.isRespondentRepresentedByASolicitor(any())).thenReturn(false);

        assertFalse(notificationService.isRespondentSolicitorEmailCommunicationEnabled(new HashMap<>()));
    }

    @Test
    public void shouldEmailRespondentSolicitorWhenNullEmailConsent() {
        when(caseDataService.isPaperApplication(any())).thenReturn(false);
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

        when(caseDataService.isPaperApplication(any())).thenReturn(false);
        when(caseDataService.isApplicantRepresentedByASolicitor(any())).thenReturn(true);
        when(caseDataService.isNotEmpty(CONTESTED_SOLICITOR_EMAIL, caseData)).thenReturn(true);

        assertTrue(notificationService.isContestedApplicantSolicitorEmailCommunicationEnabled(caseData));
    }

    @Test
    public void shouldNotEmailContestedAppSolicitor() {
        when(caseDataService.isPaperApplication(any())).thenReturn(true);
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
    }

    @Test
    public void sendInterimNotificationEmailToRespondentSolicitor() {
        callbackRequest = getContestedNewCallbackRequest();

        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_INTERIM_HEARING))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendInterimNotificationEmailToRespondentSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void givenContestedCaseWhenSendNoticeOfChangeEmailThenSendNoticeOfChangeContestedEmail() {
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setName(TEST_SOLICITOR_NAME);
        when(notificationRequestMapper.getNotificationRequestForNoticeOfChange(any())).thenReturn(notificationRequest);
        when(checkSolicitorIsDigitalService.isApplicantSolicitorDigital(anyString())).thenReturn(true);
        FinremCaseDetails caseDetails = getContestedNewCallbackRequest().getCaseDetails();

        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_NOTICE_OF_CHANGE_CONTESTED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());

        notificationService.sendNoticeOfChangeEmail(caseDetails);

        verify(notificationRequestMapper).getNotificationRequestForNoticeOfChange(caseDetails);
        verify(notificationServiceConfiguration).getContestedNoticeOfChange();
    }

    @Test
    public void givenConsentedCaseWhenSendNoticeOfChangeEmailThenSendNoticeOfChangeContestedEmail() {
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setName(TEST_SOLICITOR_NAME);
        when(notificationRequestMapper.getNotificationRequestForNoticeOfChange(any())).thenReturn(notificationRequest);
        when(checkSolicitorIsDigitalService.isApplicantSolicitorDigital(anyString())).thenReturn(true);
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);

        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_NOTICE_OF_CHANGE_CONSENTED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());

        notificationService.sendNoticeOfChangeEmail(newCallbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForNoticeOfChange(newCallbackRequest.getCaseDetails());
        verify(notificationServiceConfiguration).getConsentedNoticeOfChange();
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
        verify(notificationServiceConfiguration).getContestedNoticeOfChange();
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
        verify(notificationServiceConfiguration).getContestedNoCCaseworker();
    }

    @Test
    public void givenConsentedCase_whenSendNoCCaseworkerEmail_thenSendConsentedEmail() {
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setName(TEST_SOLICITOR_NAME);
        when(notificationRequestMapper.getNotificationRequestForNoticeOfChange(any())).thenReturn(notificationRequest);
        when(checkSolicitorIsDigitalService.isApplicantSolicitorDigital(anyString())).thenReturn(true);
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);

        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_NOC_CASEWORKER_CONSENTED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());

        notificationService.sendNoticeOfChangeEmailCaseworker(newCallbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForNoticeOfChange(newCallbackRequest.getCaseDetails());
        verify(notificationServiceConfiguration).getConsentedNoCCaseworker();
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
        verify(notificationServiceConfiguration).getContestedNoCCaseworker();
        verify(mockRestTemplate, never()).exchange(any(), eq(HttpMethod.POST), any(), eq(String.class));
    }

    public void sendUpdateFrcInformationEmailToAppSolicitor() {
        newCallbackRequest = getContestedNewCallbackRequest();

        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_UPDATE_FRC_INFORMATION))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendUpdateFrcInformationEmailToAppSolicitor(newCallbackRequest.getCaseDetails());
        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(newCallbackRequest.getCaseDetails());
    }

    @Test
    public void sendUpdateFrcInformationEmailToRespSolicitor() {
        newCallbackRequest = getContestedNewCallbackRequest();

        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_UPDATE_FRC_INFORMATION))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendUpdateFrcInformationEmailToRespondentSolicitor(newCallbackRequest.getCaseDetails());
        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(newCallbackRequest.getCaseDetails());
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
    }

}
