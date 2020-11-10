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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CASE_TYPE_ID_CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CASE_TYPE_ID_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.PAPER_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_JUDGE_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BIRMINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BIRMINGHAM_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_LETTER_ID_RES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CFC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLEAVELAND;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLEAVELAND_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_REFER_TO_JUDGE_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HSYORKSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HSYORKSHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENTFRC_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LIVERPOOL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LIVERPOOL_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MANCHESTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MANCHESTER_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NEWPORT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NEWPORT_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHEAST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWEST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NWYORKSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NWYORKSHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SWANSEA;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SWANSEA_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES_FRC_LIST;

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
    protected RestTemplate restTemplate;

    @MockBean
    protected FeatureToggleService featureToggleService;

    private MockRestServiceServer mockServer;

    @Before
    public void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
        callbackRequest = getConsentedCallbackRequest();
    }

    @Test
    public void shouldCreateNotificationRequestForAppSolicitorForConsentedJourney() {
        NotificationRequest notificationRequest = notificationService.createNotificationRequestForAppSolicitor(callbackRequest);

        assertEquals("12345", notificationRequest.getCaseReferenceNumber());
        assertEquals(TEST_SOLICITOR_REFERENCE, notificationRequest.getSolicitorReferenceNumber());
        assertEquals(TEST_DIVORCE_CASE_NUMBER, notificationRequest.getDivorceCaseNumber());
        assertEquals(TEST_SOLICITOR_NAME, notificationRequest.getName());
        assertEquals(TEST_SOLICITOR_EMAIL, notificationRequest.getNotificationEmail());
        assertEquals("consented", notificationRequest.getCaseType());
    }

    @Test
    public void shouldCreateNotificationRequestForAppSolicitorForContestedJourney() {
        callbackRequest = getContestedCallbackRequest(WALES, WALES_FRC_LIST,
            SWANSEA, SWANSEA_COURTLIST, "FR_swansea_hc_list_1");

        NotificationRequest notificationRequest = notificationService.createNotificationRequestForAppSolicitor(callbackRequest);

        assertEquals("12345", notificationRequest.getCaseReferenceNumber());
        assertEquals(TEST_SOLICITOR_REFERENCE, notificationRequest.getSolicitorReferenceNumber());
        assertEquals(TEST_DIVORCE_CASE_NUMBER, notificationRequest.getDivorceCaseNumber());
        assertEquals(TEST_SOLICITOR_NAME, notificationRequest.getName());
        assertEquals(TEST_SOLICITOR_EMAIL, notificationRequest.getNotificationEmail());
        assertEquals("contested", notificationRequest.getCaseType());
        assertEquals("swansea", notificationRequest.getSelectedCourt());
    }

    @Test
    public void shouldCreateNotificationRequestForRespSolicitorForConsentedJourney() {
        callbackRequest = getConsentedCallbackRequestForRespSolicitor();
        NotificationRequest notificationRequest = notificationService.createNotificationRequestForRespSolicitor(callbackRequest);

        assertEquals("12345", notificationRequest.getCaseReferenceNumber());
        assertEquals(TEST_RESP_SOLICITOR_REFERENCE, notificationRequest.getSolicitorReferenceNumber());
        assertEquals(TEST_DIVORCE_CASE_NUMBER, notificationRequest.getDivorceCaseNumber());
        assertEquals(TEST_RESP_SOLICITOR_NAME, notificationRequest.getName());
        assertEquals(TEST_RESP_SOLICITOR_EMAIL, notificationRequest.getNotificationEmail());
        assertEquals("consented", notificationRequest.getCaseType());
    }

    @Test
    public void shouldCreateNotificationRequestForRespSolicitorForContestedJourney() {
        callbackRequest = getContestedCallbackRequestForRespSolicitor(WALES, WALES_FRC_LIST,
            SWANSEA, SWANSEA_COURTLIST, "FR_swansea_hc_list_1");

        NotificationRequest notificationRequest = notificationService.createNotificationRequestForRespSolicitor(callbackRequest);

        assertEquals("12345", notificationRequest.getCaseReferenceNumber());
        assertEquals(TEST_RESP_SOLICITOR_REFERENCE, notificationRequest.getSolicitorReferenceNumber());
        assertEquals(TEST_DIVORCE_CASE_NUMBER, notificationRequest.getDivorceCaseNumber());
        assertEquals(TEST_RESP_SOLICITOR_NAME, notificationRequest.getName());
        assertEquals(TEST_RESP_SOLICITOR_EMAIL, notificationRequest.getNotificationEmail());
        assertEquals("contested", notificationRequest.getCaseType());
        assertEquals("swansea", notificationRequest.getSelectedCourt());
    }

    @Test
    public void sendHwfSuccessfulNotificationEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendConsentedHWFSuccessfulConfirmationEmail(callbackRequest);
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
    }

    @Test
    public void sendAssignToJudgeNotificationEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_ASSIGNED_TO_JUDGE))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendAssignToJudgeConfirmationEmail(callbackRequest);
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
    }

    @Test
    public void sendConsentOrderMadeNotificationEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENT_ORDER_MADE))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendConsentOrderMadeConfirmationEmail(callbackRequest);
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
    }

    @Test
    public void sendPrepareForHearingNotificationEmail() {
        HashMap<String, Object> caseData = new HashMap<>();
        caseData.put(APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED, "Yes");

        callbackRequest = getContestedCallbackRequest(WALES, WALES_FRC_LIST,
            SWANSEA, SWANSEA_COURTLIST, "FR_swansea_hc_list_1");

        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_PREPARE_FOR_HEARING))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendPrepareForHearingEmail(callbackRequest);
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
    }

    @Test
    public void sendPrepareForHearingAfterSentNotificationEmail() {
        callbackRequest = getContestedCallbackRequest(WALES, WALES_FRC_LIST,
            SWANSEA, SWANSEA_COURTLIST, "FR_swansea_hc_list_1");
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_PREPARE_FOR_HEARING_ORDER_SENT))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendPrepareForHearingOrderSentEmail(callbackRequest);
    }

    @Test
    public void throwExceptionWhenPrepareForHearingAfterSentNotificationEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_PREPARE_FOR_HEARING_ORDER_SENT))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendPrepareForHearingOrderSentEmail(callbackRequest);
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }
    }

    @Test
    public void sendConsentOrderNotApprovedNotificationEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENT_ORDER_NOT_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendConsentOrderNotApprovedEmail(callbackRequest);
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
    }

    @Test
    public void sendConsentOrderAvailableNotificationEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENT_ORDER_AVAILABLE))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendConsentOrderAvailableEmail(callbackRequest);
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
    }

    @Test
    public void sendConsentOrderAvailableNotificationCtscEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENT_ORDER_AVAILABLE_CTSC))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendConsentOrderAvailableCtscEmail(callbackRequest);
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
    }

    @Test
    public void sendContestedApplicationIssuedEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_APPLICATION_ISSUED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedApplicationIssuedEmailToApplicantSolicitor(callbackRequest);
    }

    @Test
    public void throwExceptionWhenContestedApplicationIssuedEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_APPLICATION_ISSUED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendContestedApplicationIssuedEmailToApplicantSolicitor(callbackRequest);
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }
    }

    @Test
    public void sendContestedApplicationIssuedRespondentEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_APPLICATION_ISSUED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedApplicationIssuedEmailToRespondentSolicitor(callbackRequest);
    }

    @Test
    public void throwExceptionWhenContestedApplicationIssuedRespondentEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_APPLICATION_ISSUED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendContestedApplicationIssuedEmailToRespondentSolicitor(callbackRequest);
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }
    }

    @Test
    public void sendContestOrderApprovedEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTEST_ORDER_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestOrderApprovedEmail(callbackRequest);
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
    }

    @Test
    public void sendSolicitorToDraftOrderEmailRespondent() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_DRAFT_ORDER))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withNoContent());

        notificationService.sendSolicitorToDraftOrderEmailRespondent(callbackRequest);
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
    }

    @Test
    public void sendSolicitorToDraftOrderEmailApplicant() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_DRAFT_ORDER))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendSolicitorToDraftOrderEmailApplicant(callbackRequest);
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
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmailForNottingham() {
        callbackRequest = getContestedCallbackRequest(MIDLANDS, MIDLANDS_FRC_LIST,
            NOTTINGHAM, NOTTINGHAM_COURTLIST,  "FR_s_NottinghamList_1");
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmailForMidlandsEmpty() {
        callbackRequest = getContestedCallbackRequest(MIDLANDS, MIDLANDS_FRC_LIST, "empty", "empty", "empty");
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmailForBirmingham() {
        callbackRequest = getContestedCallbackRequest(MIDLANDS, MIDLANDS_FRC_LIST,
            BIRMINGHAM, BIRMINGHAM_COURTLIST,  "FR_birmingham_hc_list_1");
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmailForLondon() {
        callbackRequest = getContestedCallbackRequest(LONDON, LONDON_FRC_LIST, CFC, "empty", "empty");
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmailForLiverpool() {
        callbackRequest = getContestedCallbackRequest(NORTHWEST, NORTHWEST_FRC_LIST,
            LIVERPOOL, LIVERPOOL_COURTLIST, "FR_liverpool_hc_list_1");
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmailForNorthWestEmpty() {
        callbackRequest = getContestedCallbackRequest(NORTHWEST, NORTHWEST_FRC_LIST, "empty", "empty", "empty");
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmailForManchester() {
        callbackRequest = getContestedCallbackRequest(NORTHWEST, NORTHWEST_FRC_LIST,
            MANCHESTER, MANCHESTER_COURTLIST, "FR_manchester_hc_list_1");
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmailForCleaveLand() {
        callbackRequest = getContestedCallbackRequest(NORTHEAST, NORTHEAST_FRC_LIST,
            CLEAVELAND, CLEAVELAND_COURTLIST, "FR_cleaveland_hc_list_1");
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmailForNwYorkshire() {
        callbackRequest = getContestedCallbackRequest(NORTHEAST, NORTHEAST_FRC_LIST,
            NWYORKSHIRE, NWYORKSHIRE_COURTLIST, "FR_nw_yorkshire_hc_list_1");
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmailForHsYorkshire() {
        callbackRequest = getContestedCallbackRequest(NORTHEAST, NORTHEAST_FRC_LIST,
            HSYORKSHIRE, HSYORKSHIRE_COURTLIST, "FR_humber_hc_list_1");
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmailForKent() {
        callbackRequest = getContestedCallbackRequest(SOUTHEAST, SOUTHEAST_FRC_LIST,
            KENT, KENTFRC_COURTLIST, "FR_kent_surrey_hc_list_1");
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmailForNewPort() {
        callbackRequest = getContestedCallbackRequest(WALES, WALES_FRC_LIST,
            NEWPORT, NEWPORT_COURTLIST, "FR_newport_hc_list_1");
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmailForSwansea() {
        callbackRequest = getContestedCallbackRequest(WALES, WALES_FRC_LIST,
            SWANSEA, SWANSEA_COURTLIST, "FR_swansea_hc_list_1");
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmailForWalesEmpty() {
        callbackRequest = getContestedCallbackRequest(WALES, WALES_FRC_LIST, "empty", "empty", "empty");
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
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
    public void sendNoContestedHwfSuccessfulNotificationEmailForNottingham() {
        callbackRequest = getContestedCallbackRequest(MIDLANDS, MIDLANDS_FRC_LIST,
            NOTTINGHAM, NOTTINGHAM_COURTLIST,  "FR_s_NottinghamList_1");
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendNoContestedHwfSuccessfulNotificationEmailForLondon() {
        callbackRequest = getContestedCallbackRequest(LONDON, LONDON_FRC_LIST, CFC, "empty", "empty");
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendNoContestedHwfSuccessfulNotificationEmailForLondonEmpty() {
        callbackRequest = getContestedCallbackRequest(LONDON, LONDON_FRC_LIST, "empty", "empty", "empty");
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendNoContestedHwfSuccessfulNotificationEmailForNoSelectedRegion() {
        callbackRequest = getContestedCallbackRequest(null, LONDON_FRC_LIST, null, "empty", "empty");
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendNoContestedHwfSuccessfulNotificationEmailForLiverPool() {
        callbackRequest = getContestedCallbackRequest(NORTHWEST, NORTHWEST_FRC_LIST,
            LIVERPOOL, LIVERPOOL_COURTLIST, "FR_liverpool_hc_list_1");
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendNoContestedHwfSuccessfulNotificationEmailForKent() {
        callbackRequest = getContestedCallbackRequest(SOUTHEAST, SOUTHEAST_FRC_LIST,
            KENT, KENTFRC_COURTLIST, "FR_kent_surrey_hc_list_1");
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendNoContestedHwfSuccessfulNotificationEmailForSouthEastEmpty() {
        callbackRequest = getContestedCallbackRequest(SOUTHEAST, SOUTHEAST_FRC_LIST, "empty", "empty", "empty");
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendNoContestedHwfSuccessfulNotificationEmailForCleaveLand() {
        callbackRequest = getContestedCallbackRequest(NORTHEAST, NORTHEAST_FRC_LIST,
            CLEAVELAND, CLEAVELAND_COURTLIST, "FR_cleaveland_hc_list_1");
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendNoContestedHwfSuccessfulNotificationEmailForSwansea() {
        callbackRequest = getContestedCallbackRequest(WALES, WALES_FRC_LIST,
            SWANSEA, SWANSEA_COURTLIST, "FR_swansea_hc_list_1");
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendGeneralEmailConsented() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_GENERAL_EMAIL_CONSENT))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendConsentGeneralEmail(callbackRequest);
    }

    @Test
    public void sendGeneralEmailContested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_GENERAL_EMAIL_CONTESTED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedGeneralEmail(callbackRequest);
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

    private CallbackRequest getContestedCallbackRequest(String regionValue, String frcList, String frcValue,
                                                        String courtList, String courtValue) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CONTESTED_SOLICITOR_EMAIL, TEST_SOLICITOR_EMAIL);
        caseData.put(CONTESTED_SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        caseData.put(SOLICITOR_REFERENCE, TEST_SOLICITOR_REFERENCE);
        caseData.put(DIVORCE_CASE_NUMBER, TEST_DIVORCE_CASE_NUMBER);
        caseData.put(GENERAL_APPLICATION_REFER_TO_JUDGE_EMAIL, TEST_JUDGE_EMAIL);
        caseData.put(REGION, regionValue);
        caseData.put(frcList, frcValue);
        caseData.put(courtList, courtValue);
        caseData.put(BULK_PRINT_LETTER_ID_RES, NOTTINGHAM);
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseTypeId(CASE_TYPE_ID_CONTESTED)
                .id(12345L)
                .data(caseData)
                .build())
            .build();
    }

    private CallbackRequest getContestedCallbackRequestForRespSolicitor(String regionValue, String frcList, String frcValue,
                                                                        String courtList, String courtValue) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(RESP_SOLICITOR_EMAIL, TEST_RESP_SOLICITOR_EMAIL);
        caseData.put(RESP_SOLICITOR_NAME, TEST_RESP_SOLICITOR_NAME);
        caseData.put(RESP_SOLICITOR_REFERENCE, TEST_RESP_SOLICITOR_REFERENCE);
        caseData.put(DIVORCE_CASE_NUMBER, TEST_DIVORCE_CASE_NUMBER);
        caseData.put(GENERAL_APPLICATION_REFER_TO_JUDGE_EMAIL, TEST_JUDGE_EMAIL);
        caseData.put(REGION, regionValue);
        caseData.put(frcList, frcValue);
        caseData.put(courtList, courtValue);
        caseData.put(BULK_PRINT_LETTER_ID_RES, NOTTINGHAM);
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseTypeId(CASE_TYPE_ID_CONTESTED)
                .id(12345L)
                .data(caseData)
                .build())
            .build();
    }

    private CallbackRequest getConsentedCallbackRequest() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(SOLICITOR_EMAIL, TEST_SOLICITOR_EMAIL);
        caseData.put(CONSENTED_SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        caseData.put(SOLICITOR_REFERENCE, TEST_SOLICITOR_REFERENCE);
        caseData.put(DIVORCE_CASE_NUMBER, TEST_DIVORCE_CASE_NUMBER);
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseTypeId(CASE_TYPE_ID_CONSENTED)
                .id(12345L)
                .data(caseData)
                .build())
            .build();
    }

    private CallbackRequest getConsentedCallbackRequestForRespSolicitor() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(RESP_SOLICITOR_EMAIL, TEST_RESP_SOLICITOR_EMAIL);
        caseData.put(RESP_SOLICITOR_NAME, TEST_RESP_SOLICITOR_NAME);
        caseData.put(RESP_SOLICITOR_REFERENCE, TEST_RESP_SOLICITOR_REFERENCE);
        caseData.put(DIVORCE_CASE_NUMBER, TEST_DIVORCE_CASE_NUMBER);
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseTypeId(CASE_TYPE_ID_CONSENTED)
                .id(12345L)
                .data(caseData)
                .build())
            .build();
    }

    @Test
    public void sendContestOrderNotApprovedNotificationEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTEST_ORDER_NOT_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestOrderNotApprovedEmail(callbackRequest);
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
    public void sendContestedConsentOrderApprovedNotificationEmailToApplicantSolicitor() {

        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_CONSENT_ORDER_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedConsentOrderApprovedEmailToApplicantSolicitor(callbackRequest);
    }

    @Test
    public void sendContestedConsentOrderApprovedNotificationEmailToRespondentSolicitor() {

        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_CONSENT_ORDER_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedConsentOrderApprovedEmailToRespondentSolicitor(callbackRequest);
    }

    @Test
    public void sendContestedGeneralApplicationReferToJudgeNotificationEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_GENERAL_APPLICATION_REFER_TO_JUDGE))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andExpect(MockRestRequestMatchers.jsonPath("notificationEmail").value(TEST_JUDGE_EMAIL))
            .andRespond(MockRestResponseCreators.withNoContent());

        callbackRequest = getContestedCallbackRequest(WALES, WALES_FRC_LIST,
            SWANSEA, SWANSEA_COURTLIST, "FR_swansea_hc_list_1");

        notificationService.sendContestedGeneralApplicationReferToJudgeEmail(callbackRequest);
    }

    @Test
    public void throwExceptionWhenContestedGeneralApplicationReferToJudgeEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_GENERAL_APPLICATION_REFER_TO_JUDGE))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        callbackRequest = getContestedCallbackRequest(WALES, WALES_FRC_LIST,
            SWANSEA, SWANSEA_COURTLIST, "FR_swansea_hc_list_1");

        try {
            notificationService.sendContestedGeneralApplicationReferToJudgeEmail(callbackRequest);
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }
    }

    @Test
    public void sendContestedGeneralApplicationOutcomeNotificationEmailWhenSendToFRCToggleTrue() throws IOException {
        when(featureToggleService.isSendToFRCEnabled()).thenReturn(true);
        callbackRequest = getContestedCallbackRequest(MIDLANDS, MIDLANDS_FRC_LIST,
            NOTTINGHAM, NOTTINGHAM_COURTLIST,  "FR_s_NottinghamList_1");

        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_GENERAL_APPLICATION_OUTCOME))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andExpect(MockRestRequestMatchers.jsonPath("notificationEmail")
                .value(NOTTINGHAM_FRC_EMAIL))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedGeneralApplicationOutcomeEmail(callbackRequest);
    }

    @Test
    public void sendContestedGeneralApplicationOutcomeNotificationEmailToTestAccountWhenSendToFRCToggleFalse()
        throws IOException {
        when(featureToggleService.isSendToFRCEnabled()).thenReturn(false);
        callbackRequest = getContestedCallbackRequest(MIDLANDS, MIDLANDS_FRC_LIST,
            NOTTINGHAM, NOTTINGHAM_COURTLIST,  "FR_s_NottinghamList_1");

        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_GENERAL_APPLICATION_OUTCOME))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andExpect(MockRestRequestMatchers.jsonPath("notificationEmail")
                .value(TEST_USER_EMAIL))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedGeneralApplicationOutcomeEmail(callbackRequest);
    }

    @Test
    public void sendContestedConsentGeneralOrderNotificationEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_CONSENT_GENERAL_ORDER))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedConsentGeneralOrderEmail(callbackRequest);
    }

    @Test
    public void sendContestedGeneralOrderNotificationEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_GENERAL_ORDER))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedGeneralOrderEmail(callbackRequest);
    }

    @Test
    public void sendConsentedGeneralOrderNotificationEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENTED_GENERAL_ORDER))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendConsentedGeneralOrderEmail(callbackRequest);
    }

    @Test
    public void throwExceptionWhenContestedConsentOrderApprovedNotificationEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_CONSENT_ORDER_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendContestedConsentOrderApprovedEmailToApplicantSolicitor(callbackRequest);
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }
    }

    @Test
    public void sendContestedConsentOrderNotApprovedNotificationEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_CONSENT_ORDER_NOT_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedConsentOrderNotApprovedEmail(callbackRequest);
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