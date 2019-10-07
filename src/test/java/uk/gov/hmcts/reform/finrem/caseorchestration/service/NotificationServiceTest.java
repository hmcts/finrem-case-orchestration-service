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
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ALLOCATED_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_LETTER_ID_RES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;

public class NotificationServiceTest extends BaseServiceTest {
    private static final String END_POINT_HWF_SUCCESSFUL = "http://localhost:8086/notify/hwf-successful";
    private static final String END_POINT_ASSIGNED_TO_JUDGE = "http://localhost:8086/notify/assign-to-judge";
    private static final String END_POINT_CONSENT_ORDER_MADE = "http://localhost:8086/notify/consent-order-made";
    private static final String END_POINT_CONSENT_ORDER_NOT_APPROVED = "http://localhost:8086/notify/"
            + "consent-order-not-approved";
    private static final String END_POINT_CONSENT_ORDER_AVAILABLE = "http://localhost:8086/notify/"
            + "consent-order-available";
    private static final String END_POINT_CONTESTED_HWF_SUCCESSFUL = "http://localhost:8086/notify/"
            + "contested/hwf-successful";

    @Autowired
    private NotificationService notificationService;
    private CallbackRequest callbackRequest;

    @Autowired
    protected RestTemplate restTemplate;

    protected MockRestServiceServer mockServer;

    @Before
    public void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
        callbackRequest = getCallbackRequest();
    }

    @Test
    public void sendHwfSuccessfulNotificationEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_HWF_SUCCESSFUL))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendHWFSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void throwExceptionWhenHwfSuccessfulNotificationEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_HWF_SUCCESSFUL))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            notificationService.sendHWFSuccessfulConfirmationEmail(callbackRequest);
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is("500 Internal Server Error"));
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
            notificationService.sendAssignToJudgeConfirmationEmail(getCallbackRequest());
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is("500 Internal Server Error"));
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
            assertThat(ex.getMessage(), Is.is("500 Internal Server Error"));
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
            assertThat(ex.getMessage(), Is.is("500 Internal Server Error"));
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
            assertThat(ex.getMessage(), Is.is("500 Internal Server Error"));
        }
    }


    @Test
    public void sendContestedHwfSuccessfulNotificationEmailForNottingham() {
        String courtList = "{\"region\": \"midlands\", \"midlandsList\" :\"nottingham\","
                + " \"nottinghamCourtList\" : \"FR_s_NottinghamList_1\"}";
        callbackRequest = getContestedCallbackRequest(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmailForBirmingham() {
        String courtList = "{\"region\": \"midlands\", \"midlandsList\" :\"birmingham\","
                + " \"nottinghamCourtList\" : \"FR_s_BirminghamList_1\"}";
        callbackRequest = getContestedCallbackRequest(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmailForLondon() {
        String courtList = "{\"region\": \"london\", \"londonList\" :\"cfc\","
                + " \"londonCourtList\" : \"FR_s_cfc_List_1\"}";
        callbackRequest = getContestedCallbackRequest(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmailForLiverPool() {
        String courtList = "{\"region\": \"northwest\", \"northWestList\" :\"liverpool\","
                + " \"liverpoolCourtList\" : \"FR_s_liverpool_List_1\"}";
        callbackRequest = getContestedCallbackRequest(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmailForManchester() {
        String courtList = "{\"region\": \"northwest\", \"northWestList\" :\"manchester\","
                + " \"manchesterCourtList\" : \"FR_s_manchester_List_1\"}";
        callbackRequest = getContestedCallbackRequest(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmailForCleaveLand() {
        String courtList = "{\"region\": \"northeast\", \"northEastList\" :\"cleaveland\","
                + " \"cleavelandCourtList\" : \"FR_s_cleaveland_List_1\"}";
        callbackRequest = getContestedCallbackRequest(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmailForNwYorkshire() {
        String courtList = "{\"region\": \"northeast\", \"northEastList\" :\"nwyorkshire\","
                + " \"nwyorkshireCourtList\" : \"FR_s_nwyorkshire_List_1\"}";
        callbackRequest = getContestedCallbackRequest(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmailForHsYorkshire() {
        String courtList = "{\"region\": \"northeast\", \"northEastList\" :\"hsyorkshire\","
                + " \"hsyorkshireCourtList\" : \"FR_s_hsyorkshire_List_1\"}";
        callbackRequest = getContestedCallbackRequest(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmailForKent() {
        String courtList = "{\"region\": \"southeast\", \"southEastList\" :\"kentfrc\","
                + " \"kentCourtList\" : \"FR_s_kent_List_1\"}";
        callbackRequest = getContestedCallbackRequest(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmailForNewPort() {
        String courtList = "{\"region\": \"wales\", \"walesList\" :\"newport\","
                + " \"newportCourtList\" : \"FR_s_newport_List_1\"}";
        callbackRequest = getContestedCallbackRequest(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmailForSwansea() {
        String courtList = "{\"region\": \"wales\", \"walesList\" :\"swansea\","
                + " \"swanseaCourtList\" : \"FR_s_swansea_List_1\"}";
        callbackRequest = getContestedCallbackRequest(courtList);
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
            assertThat(ex.getMessage(), Is.is("500 Internal Server Error"));
        }
    }

    @Test
    public void sendNoContestedHwfSuccessfulNotificationEmailForNottingham() {
        String courtList = "{\"region\": \"midlands\", \"midlandsList\" :null,"
                + " \"nottinghamCourtList\" :null}";
        callbackRequest = getContestedCallbackRequest(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendNoContestedHwfSuccessfulNotificationEmailForLondon() {
        String courtList = "{\"region\": \"london\", \"londonList\" :null,"
                + " \"londonCourtList\" : null}";
        callbackRequest = getContestedCallbackRequest(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendNoContestedHwfSuccessfulNotificationEmailForNoSelectedRegion() {
        String courtList = "{\"region\": null, \"londonList\" :null,"
                + " \"londonCourtList\" : null}";
        callbackRequest = getContestedCallbackRequest(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendNoContestedHwfSuccessfulNotificationEmailForLiverPool() {
        String courtList = "{\"region\": \"northwest\", \"northWestList\" :null,"
                + " \"liverpoolCourtList\" : null}";
        callbackRequest = getContestedCallbackRequest(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendNoContestedHwfSuccessfulNotificationEmailForKent() {
        String courtList = "{\"region\": \"southeast\", \"southEastList\" :null,"
                + " \"kentCourtList\" : null}";
        callbackRequest = getContestedCallbackRequest(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendNoContestedHwfSuccessfulNotificationEmailForCleaveLand() {
        String courtList = "{\"region\": \"northeast\", \"northEastList\" :null,"
                + " \"cleavelandCourtList\" :null}";
        callbackRequest = getContestedCallbackRequest(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendNoContestedHwfSuccessfulNotificationEmailForSwansea() {
        String courtList = "{\"region\": \"wales\", \"walesList\" :null,"
                + " \"swanseaCourtList\" : null}";
        callbackRequest = getContestedCallbackRequest(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    private CallbackRequest getContestedCallbackRequest(String courtList) {

        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CONTESTED_SOLICITOR_EMAIL, "test@test.com");
        caseData.put(CONTESTED_SOLICITOR_NAME, "solicitorName");
        caseData.put(SOLICITOR_REFERENCE, "56789");
        caseData.put(ALLOCATED_COURT_LIST, courtList);
        caseData.put(BULK_PRINT_LETTER_ID_RES, "notingham");
        return CallbackRequest.builder()
                .caseDetails(CaseDetails.builder()
                        .id(12345L)
                        .data(caseData)
                        .build())
                .build();
    }

    private CallbackRequest getCallbackRequest() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("d81Question", "No");
        caseData.put("solicitorEmail", "test@test.com");
        caseData.put("solicitorName", "solicitorName");
        caseData.put("solicitorReference", "56789");
        return CallbackRequest.builder()
                .caseDetails(CaseDetails.builder()
                        .id(12345L)
                        .data(caseData)
                        .build())
                .build();
    }
}