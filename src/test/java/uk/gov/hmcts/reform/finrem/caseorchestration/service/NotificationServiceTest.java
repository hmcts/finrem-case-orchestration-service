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
    private static final String END_POINT_PREPARE_FOR_HEARING = "http://localhost:8086/notify/prepare-for-hearing";
    private static final String END_POINT_CONSENT_ORDER_NOT_APPROVED = "http://localhost:8086/notify/consent-order-not-approved";
    private static final String END_POINT_CONSENT_ORDER_AVAILABLE = "http://localhost:8086/notify/consent-order-available";
    private static final String END_POINT_CONTESTED_HWF_SUCCESSFUL = "http://localhost:8086/notify/contested/hwf-successful";
    private static final String END_POINT_CONTESTED_APPLICATION_ISSUED = "http://localhost:8086/notify/contested/application-issued";
    private static final String END_POINT_CONTEST_ORDER_APPROVED = "http://localhost:8086/notify/contested/order-approved";

    private static final String ERROR_500_MESSAGE = "500 Internal Server Error";

    @Autowired
    private NotificationService notificationService;
    private CallbackRequest callbackRequest;

    @Autowired
    protected RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

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
            notificationService.sendAssignToJudgeConfirmationEmail(getCallbackRequest());
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
        caseData.put("solicitorAgreeToReceiveEmails", "Yes");

        callbackRequest = getContestedCallbackRequest(caseData);

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
            assertThat(ex.getMessage(), Is.is("500 Internal Server Error"));
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
            assertThat(ex.getMessage(), Is.is("500 Internal Server Error"));
        }
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmailForNottingham() {
        HashMap<String, Object> courtList = new HashMap<>();
        courtList.put("region", "midlands");
        courtList.put("midlandsList", "nottingham");
        courtList.put("nottinghamCourtList", "FR_s_NottinghamList_1");

        callbackRequest = getContestedCallbackRequest(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmailForBirmingham() {
        HashMap<String, Object> courtList = new HashMap<>();
        courtList.put("region", "midlands");
        courtList.put("midlandsList", "birmingham");
        courtList.put("birminghamCourtList", "FR_s_BirminghamList_1");
        callbackRequest = getContestedCallbackRequest(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmailForLondon() {
        HashMap<String, Object> courtList = new HashMap<>();
        courtList.put("region", "london");
        courtList.put("londonList", "cfc");
        courtList.put("londonCourtList", "FR_s_cfc_List_1");

        callbackRequest = getContestedCallbackRequest(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmailForLiverPool() {
        HashMap<String, Object> courtList = new HashMap<>();
        courtList.put("region", "northwest");
        courtList.put("northWestList", "liverpool");
        courtList.put("liverpoolCourtList", "FR_s_liverpool_List_1");
        callbackRequest = getContestedCallbackRequest(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmailForManchester() {
        HashMap<String, Object> courtList = new HashMap<>();
        courtList.put("region", "northwest");
        courtList.put("northWestList", "manchester");
        courtList.put("manchesterCourtList", "FR_s_manchester_List_1");
        callbackRequest = getContestedCallbackRequest(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmailForCleaveLand() {
        HashMap<String, Object> courtList = new HashMap<>();
        courtList.put("region", "northeast");
        courtList.put("northEastList", "cleaveland");
        courtList.put("cleavelandCourtList", "FR_s_cleaveland_List_1");

        callbackRequest = getContestedCallbackRequest(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmailForNwYorkshire() {
        HashMap<String, Object> courtList = new HashMap<>();
        courtList.put("region", "northeast");
        courtList.put("northEastList", "nwyorkshire");
        courtList.put("cleavelandCourtList", "FR_s_nwyorkshire_List_1");

        callbackRequest = getContestedCallbackRequest(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmailForHsYorkshire() {
        HashMap<String, Object> courtList = new HashMap<>();
        courtList.put("region", "northeast");
        courtList.put("northEastList", "hsyorkshire");
        courtList.put("hsyorkshireCourtList", "FR_s_nwyorkshire_List_1");
        callbackRequest = getContestedCallbackRequest(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmailForKent() {
        HashMap<String, Object> courtList = new HashMap<>();
        courtList.put("region", "southeast");
        courtList.put("southeast", "kentfrc");
        courtList.put("kentCourtList", "FR_s_kent_List_1");
        callbackRequest = getContestedCallbackRequest(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmailForNewPort() {
        HashMap<String, Object> courtList = new HashMap<>();
        courtList.put("region", "wales");
        courtList.put("walesList", "newport");
        courtList.put("newportCourtList", "FR_s_newport_List_1");

        callbackRequest = getContestedCallbackRequest(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmailForSwansea() {

        HashMap<String, Object> courtList = new HashMap<>();
        courtList.put("region", "wales");
        courtList.put("walesList", "swansea");
        courtList.put("swanseaCourtList", "FR_s_swansea_List_1");

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
            assertThat(ex.getMessage(), Is.is(ERROR_500_MESSAGE));
        }
    }

    @Test
    public void sendNoContestedHwfSuccessfulNotificationEmailForNottingham() {

        HashMap<String, Object> courtList = new HashMap<>();
        courtList.put("region", "midlands");
        courtList.put("walesList", null);
        courtList.put("swanseaCourtList", null);

        callbackRequest = getContestedCallbackRequest(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendNoContestedHwfSuccessfulNotificationEmailForLondon() {

        HashMap<String, Object> courtList = new HashMap<>();
        courtList.put("region", "london");
        courtList.put("londonList", null);
        courtList.put("londonCourtList", null);

        callbackRequest = getContestedCallbackRequest(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendNoContestedHwfSuccessfulNotificationEmailForNoSelectedRegion() {

        HashMap<String, Object> courtList = new HashMap<>();
        courtList.put("region", null);
        courtList.put("londonList", null);
        courtList.put("londonCourtList", null);

        callbackRequest = getContestedCallbackRequest(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendNoContestedHwfSuccessfulNotificationEmailForLiverPool() {
        HashMap<String, Object> courtList = new HashMap<>();
        courtList.put("region", "northwest");
        courtList.put("northWestList", null);
        courtList.put("liverpoolCourtList", null);

        callbackRequest = getContestedCallbackRequest(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendNoContestedHwfSuccessfulNotificationEmailForKent() {

        HashMap<String, Object> courtList = new HashMap<>();
        courtList.put("region", "southeast");
        courtList.put("southEastList", null);
        courtList.put("kentCourtList", null);

        callbackRequest = getContestedCallbackRequest(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendNoContestedHwfSuccessfulNotificationEmailForCleaveLand() {
        HashMap<String, Object> courtList = new HashMap<>();
        courtList.put("region", "northeast");
        courtList.put("northEastList", null);
        courtList.put("cleavelandCourtList", null);

        callbackRequest = getContestedCallbackRequest(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    @Test
    public void sendNoContestedHwfSuccessfulNotificationEmailForSwansea() {

        HashMap<String, Object> courtList = new HashMap<>();
        courtList.put("region", "wales");
        courtList.put("walesList", null);
        courtList.put("swanseaCourtList", null);

        callbackRequest = getContestedCallbackRequest(courtList);
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT_CONTESTED_HWF_SUCCESSFUL))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withNoContent());
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
    }

    private CallbackRequest getContestedCallbackRequest(Object courtList) {

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