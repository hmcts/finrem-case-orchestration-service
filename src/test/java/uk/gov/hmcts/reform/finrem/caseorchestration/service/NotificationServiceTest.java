package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDetails;

import static org.junit.Assert.assertThat;

public class NotificationServiceTest extends BaseServiceTest {
    private static final String AUTH_TOKEN = "Bearer eyJhbGciOiJIUzI1NiJ9";
    private static final String END_POINT_HWF_SUCCESSFUL = "http://localhost:8086/notify/hwf-successful";
    private static final String END_POINT_ASSIGNED_TO_JUDGE = "http://localhost:8086/notify/assign-to-judge";
    private static final String END_POINT_CONSENT_ORDER_MADE = "http://localhost:8086/notify/consent-order-made";
    private static final String END_POINT_CONSENT_ORDER_NOT_APPROVED = "http://localhost:8086/notify/"
            + "consent-order-not-approved";
    private static final String END_POINT_CONSENT_ORDER_AVAILABLE = "http://localhost:8086/notify/"
            + "consent-order-available";

    @Autowired
    private NotificationService notificationService;
    private CCDRequest ccdRequest;

    @Autowired
    protected RestTemplate restTemplate;

    protected MockRestServiceServer mockServer;

    @Before
    public void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
        ccdRequest = getCcdRequest();
    }

    @Test
    public void sendHwfSuccessfulNotificationEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_HWF_SUCCESSFUL))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendHWFSuccessfulConfirmationEmail(ccdRequest, AUTH_TOKEN);
    }

    private CCDRequest getCcdRequest() {
        CaseData caseData = new CaseData();
        caseData.setSolicitorEmail("test@test.com");
        caseData.setSolicitorName("Padmaja");
        caseData.setSolicitorReference("56789");
        CCDRequest ccdRequest = new CCDRequest();
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId("12345");
        ccdRequest.setCaseDetails(caseDetails);
        return ccdRequest;
    }

    @Test
    public void throwExceptionWhenHwfSuccessfulNotificationEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_HWF_SUCCESSFUL))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendHWFSuccessfulConfirmationEmail(ccdRequest, AUTH_TOKEN);
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is("500 Internal Server Error"));
        }

    }

    @Test
    public void sendAssignToJudgeNotificationEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_ASSIGNED_TO_JUDGE))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendAssignToJudgeConfirmationEmail(ccdRequest, AUTH_TOKEN);
    }

    @Test
    public void throwExceptionWhenAssignToJudgeNotificationEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_ASSIGNED_TO_JUDGE))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        CaseData caseData = new CaseData();
        caseData.setSolicitorEmail("test@test.com");
        caseData.setSolicitorName("Padmaja");
        caseData.setSolicitorReference("56789");
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId("12345");
        CCDRequest ccdRequest = new CCDRequest();
        ccdRequest.setCaseDetails(caseDetails);

        try {
            notificationService.sendAssignToJudgeConfirmationEmail(ccdRequest, AUTH_TOKEN);
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is("500 Internal Server Error"));
        }

    }

    @Test
    public void sendConsentOrderMadeNotificationEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENT_ORDER_MADE))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendConsentOrderMadeConfirmationEmail(ccdRequest, AUTH_TOKEN);
    }

    @Test
    public void throwExceptionWhenConsentOrderMadeNotificationEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENT_ORDER_MADE))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendConsentOrderMadeConfirmationEmail(ccdRequest, AUTH_TOKEN);
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is("500 Internal Server Error"));
        }

    }

    @Test
    public void sendConsentOrderNotApprovedNotificationEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENT_ORDER_NOT_APPROVED))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendConsentOrderNotApprovedEmail(ccdRequest, AUTH_TOKEN);
    }

    @Test
    public void throwExceptionWhenConsentOrderNotApprovedNotificationEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENT_ORDER_NOT_APPROVED))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendConsentOrderNotApprovedEmail(ccdRequest, AUTH_TOKEN);
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is("500 Internal Server Error"));
        }
    }

    @Test
    public void sendConsentOrderAvailableNotificationEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENT_ORDER_AVAILABLE))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendConsentOrderAvailableEmail(ccdRequest, AUTH_TOKEN);
    }

    @Test
    public void throwExceptionWhenConsentOrderAvailableNotificationEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONSENT_ORDER_AVAILABLE))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendConsentOrderAvailableEmail(ccdRequest, AUTH_TOKEN);
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is("500 Internal Server Error"));
        }
    }
}