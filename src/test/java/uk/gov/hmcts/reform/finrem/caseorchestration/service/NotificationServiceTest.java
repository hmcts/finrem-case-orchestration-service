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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ALLOCATED_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BIRMINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BIRMINGHAM_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_LETTER_ID_RES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CFC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLEAVELAND;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLEAVELAND_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_NAME;
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
    private static final String END_POINT_CONSENTED_GENERAL_ORDER = "http://localhost:8086/notify/consented/general-order";
    private static final String END_POINT_CONTESTED_GENERAL_APPLICATION_REFER_TO_JUDGE = "http://localhost:8086/notify/contested/general-application-refer-to-judge";
    private static final String END_POINT_CONTESTED_GENERAL_APPLICATION_OUTCOME = "http://localhost:8086/notify/contested/general-application-outcome";
    private static final String END_POINT_CONTESTED_CONSENT_ORDER_NOT_APPROVED = "http://localhost:8086/notify/contested/consent-order-not-approved";

    private static final String ERROR_500_MESSAGE = "500 Internal Server Error";
    private static final String DUMMY_EMAIL = "some@person.email";
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
        when(featureToggleService.isContestedCourtDetailsMigrationEnabled()).thenReturn(true);
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
        notificationService.sendContestedApplicationIssuedEmail(callbackRequest);
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
    public void sendSolicitorToDraftOrderEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_DRAFT_ORDER))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendSolicitorToDraftOrderEmail(callbackRequest);
    }

    @Test
    public void throwExceptionWhenSolicitorToDraftOrderEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_DRAFT_ORDER))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendSolicitorToDraftOrderEmail(callbackRequest);
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
        caseData.put(CONTESTED_SOLICITOR_EMAIL, "test@test.com");
        caseData.put(CONTESTED_SOLICITOR_NAME, "solicitorName");
        caseData.put(SOLICITOR_REFERENCE, "56789");
        caseData.put(GENERAL_APPLICATION_REFER_TO_JUDGE_EMAIL, DUMMY_EMAIL);
        caseData.put(REGION, regionValue);
        caseData.put(frcList, frcValue);
        caseData.put(courtList, courtValue);
        caseData.put(BULK_PRINT_LETTER_ID_RES, "nottingham");
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseTypeId("FinancialRemedyContested")
                .id(12345L)
                .data(caseData)
                .build())
            .build();
    }

    private CallbackRequest getContestedCallbackRequestAllocatedCourtList(Map<String, Object> allocatedCourtList) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CONTESTED_SOLICITOR_EMAIL, "test@test.com");
        caseData.put(CONTESTED_SOLICITOR_NAME, "solicitorName");
        caseData.put(SOLICITOR_REFERENCE, "56789");
        caseData.put(ALLOCATED_COURT_LIST, allocatedCourtList);
        caseData.put(BULK_PRINT_LETTER_ID_RES, "nottingham");
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseTypeId("FinancialRemedyContested")
                .id(12345L)
                .data(caseData)
                .build())
            .build();
    }

    private CallbackRequest getConsentedCallbackRequest() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(SOLICITOR_EMAIL, "test@test.com");
        caseData.put(CONSENTED_SOLICITOR_NAME, "solicitorName");
        caseData.put(SOLICITOR_REFERENCE, "56789");
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseTypeId("FinancialRemedyMVP2")
                .id(12345L)
                .data(caseData)
                .build())
            .build();
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmailForNottinghamAllocatedCourtList() {
        when(featureToggleService.isContestedCourtDetailsMigrationEnabled()).thenReturn(false);

        HashMap<String, Object> courtList = new HashMap<>();
        courtList.put("region", "midlands");
        courtList.put("midlandsList", "nottingham");
        courtList.put("nottinghamCourtList", "FR_s_NottinghamList_1");

        callbackRequest = getContestedCallbackRequestAllocatedCourtList(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmailForBirminghamAllocatedCourtList() {
        when(featureToggleService.isContestedCourtDetailsMigrationEnabled()).thenReturn(false);

        HashMap<String, Object> courtList = new HashMap<>();
        courtList.put("region", "midlands");
        courtList.put("midlandsList", "birmingham");
        courtList.put("birminghamCourtList", "FR_s_BirminghamList_1");
        callbackRequest = getContestedCallbackRequestAllocatedCourtList(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmailForLondonAllocatedCourtList() {
        when(featureToggleService.isContestedCourtDetailsMigrationEnabled()).thenReturn(false);

        HashMap<String, Object> courtList = new HashMap<>();
        courtList.put("region", "london");
        courtList.put("londonList", "cfc");
        courtList.put("londonCourtList", "FR_s_cfc_List_1");

        callbackRequest = getContestedCallbackRequestAllocatedCourtList(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmailForLiverpoolAllocatedCourtList() {
        when(featureToggleService.isContestedCourtDetailsMigrationEnabled()).thenReturn(false);

        HashMap<String, Object> courtList = new HashMap<>();
        courtList.put("region", "northwest");
        courtList.put("northWestList", "liverpool");
        courtList.put("liverpoolCourtList", "FR_s_liverpool_List_1");
        callbackRequest = getContestedCallbackRequestAllocatedCourtList(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmailForManchesterAllocatedCourtList() {
        when(featureToggleService.isContestedCourtDetailsMigrationEnabled()).thenReturn(false);

        HashMap<String, Object> courtList = new HashMap<>();
        courtList.put("region", "northwest");
        courtList.put("northWestList", "manchester");
        courtList.put("manchesterCourtList", "FR_s_manchester_List_1");
        callbackRequest = getContestedCallbackRequestAllocatedCourtList(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmailForCleaveLandAllocatedCourtList() {
        when(featureToggleService.isContestedCourtDetailsMigrationEnabled()).thenReturn(false);

        HashMap<String, Object> courtList = new HashMap<>();
        courtList.put("region", "northeast");
        courtList.put("northEastList", "cleaveland");
        courtList.put("cleavelandCourtList", "FR_s_cleaveland_List_1");

        callbackRequest = getContestedCallbackRequestAllocatedCourtList(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmailForNwYorkshireAllocatedCourtList() {
        when(featureToggleService.isContestedCourtDetailsMigrationEnabled()).thenReturn(false);

        HashMap<String, Object> courtList = new HashMap<>();
        courtList.put("region", "northeast");
        courtList.put("northEastList", "nwyorkshire");
        courtList.put("cleavelandCourtList", "FR_s_nwyorkshire_List_1");

        callbackRequest = getContestedCallbackRequestAllocatedCourtList(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmailForHsYorkshireAllocatedCourtList() {
        when(featureToggleService.isContestedCourtDetailsMigrationEnabled()).thenReturn(false);

        HashMap<String, Object> courtList = new HashMap<>();
        courtList.put("region", "northeast");
        courtList.put("northEastList", "hsyorkshire");
        courtList.put("hsyorkshireCourtList", "FR_s_nwyorkshire_List_1");
        callbackRequest = getContestedCallbackRequestAllocatedCourtList(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmailForKentAllocatedCourtList() {
        when(featureToggleService.isContestedCourtDetailsMigrationEnabled()).thenReturn(false);

        HashMap<String, Object> courtList = new HashMap<>();
        courtList.put("region", "southeast");
        courtList.put("southeast", "kentfrc");
        courtList.put("kentCourtList", "FR_s_kent_List_1");
        callbackRequest = getContestedCallbackRequestAllocatedCourtList(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmailForNewPortAllocatedCourtList() {
        when(featureToggleService.isContestedCourtDetailsMigrationEnabled()).thenReturn(false);

        HashMap<String, Object> courtList = new HashMap<>();
        courtList.put("region", "wales");
        courtList.put("walesList", "newport");
        courtList.put("newportCourtList", "FR_s_newport_List_1");

        callbackRequest = getContestedCallbackRequestAllocatedCourtList(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmailForSwanseaAllocatedCourtList() {
        when(featureToggleService.isContestedCourtDetailsMigrationEnabled()).thenReturn(false);

        HashMap<String, Object> courtList = new HashMap<>();
        courtList.put("region", "wales");
        courtList.put("walesList", "swansea");
        courtList.put("swanseaCourtList", "FR_s_swansea_List_1");

        callbackRequest = getContestedCallbackRequestAllocatedCourtList(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendNoContestedHwfSuccessfulNotificationEmailForNottinghamAllocatedCourtList() {
        when(featureToggleService.isContestedCourtDetailsMigrationEnabled()).thenReturn(false);

        HashMap<String, Object> courtList = new HashMap<>();
        courtList.put("region", "midlands");
        courtList.put("walesList", null);
        courtList.put("swanseaCourtList", null);

        callbackRequest = getContestedCallbackRequestAllocatedCourtList(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendNoContestedHwfSuccessfulNotificationEmailForLondonAllocatedCourtList() {
        when(featureToggleService.isContestedCourtDetailsMigrationEnabled()).thenReturn(false);

        HashMap<String, Object> courtList = new HashMap<>();
        courtList.put("region", "london");
        courtList.put("londonList", null);
        courtList.put("londonCourtList", null);

        callbackRequest = getContestedCallbackRequestAllocatedCourtList(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendNoContestedHwfSuccessfulNotificationEmailForNoSelectedRegionAllocatedCourtList() {
        when(featureToggleService.isContestedCourtDetailsMigrationEnabled()).thenReturn(false);

        HashMap<String, Object> courtList = new HashMap<>();
        courtList.put("region", null);
        courtList.put("londonList", null);
        courtList.put("londonCourtList", null);

        callbackRequest = getContestedCallbackRequestAllocatedCourtList(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendNoContestedHwfSuccessfulNotificationEmailForLiverPoolAllocatedCourtList() {
        when(featureToggleService.isContestedCourtDetailsMigrationEnabled()).thenReturn(false);

        HashMap<String, Object> courtList = new HashMap<>();
        courtList.put("region", "northwest");
        courtList.put("northWestList", null);
        courtList.put("liverpoolCourtList", null);

        callbackRequest = getContestedCallbackRequestAllocatedCourtList(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendNoContestedHwfSuccessfulNotificationEmailForKentAllocatedCourtList() {
        when(featureToggleService.isContestedCourtDetailsMigrationEnabled()).thenReturn(false);

        HashMap<String, Object> courtList = new HashMap<>();
        courtList.put("region", "southeast");
        courtList.put("southEastList", null);
        courtList.put("kentCourtList", null);

        callbackRequest = getContestedCallbackRequestAllocatedCourtList(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendNoContestedHwfSuccessfulNotificationEmailForCleaveLandAllocatedCourtList() {
        when(featureToggleService.isContestedCourtDetailsMigrationEnabled()).thenReturn(false);

        HashMap<String, Object> courtList = new HashMap<>();
        courtList.put("region", "northeast");
        courtList.put("northEastList", null);
        courtList.put("cleavelandCourtList", null);

        callbackRequest = getContestedCallbackRequestAllocatedCourtList(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendNoContestedHwfSuccessfulNotificationEmailForSwanseaAllocatedCourtList() {
        when(featureToggleService.isContestedCourtDetailsMigrationEnabled()).thenReturn(false);

        HashMap<String, Object> courtList = new HashMap<>();
        courtList.put("region", "wales");
        courtList.put("walesList", null);
        courtList.put("swanseaCourtList", null);

        callbackRequest = getContestedCallbackRequestAllocatedCourtList(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
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
    public void sendContestedConsentOrderApprovedNotificationEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_CONSENT_ORDER_APPROVED))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedConsentOrderApprovedEmail(callbackRequest);
    }

    @Test
    public void sendContestedGeneralApplicationReferToJudgeNotificationEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_GENERAL_APPLICATION_REFER_TO_JUDGE))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andExpect(MockRestRequestMatchers.jsonPath("notificationEmail").value(DUMMY_EMAIL))
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
            notificationService.sendContestedConsentOrderApprovedEmail(callbackRequest);
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
}