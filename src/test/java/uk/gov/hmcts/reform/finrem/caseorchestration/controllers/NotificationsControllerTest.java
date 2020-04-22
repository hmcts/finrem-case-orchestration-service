package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.bsp.common.utils.ResourceLoader;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkprint.AssignedToJudgeBulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkprint.HelpWithFeesBulkPrintService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@RunWith(SpringRunner.class)
@WebMvcTest(NotificationsController.class)
public class NotificationsControllerTest {
    private static final String HWF_SUCCESSFUL_CALLBACK_URL = "/case-orchestration/notify/hwf-successful";
    private static final String ASSIGN_TO_JUDGE_CALLBACK_URL = "/case-orchestration/notify/assign-to-judge";
    private static final String CONSENT_ORDER_MADE_URL = "/case-orchestration/notify/consent-order-made";
    private static final String CONSENT_ORDER_NOT_APPROVED_URL = "/case-orchestration/notify/consent-order-not-approved";
    private static final String CONSENT_ORDER_AVAILABLE_URL = "/case-orchestration/notify/consent-order-available";
    private static final String CCD_REQUEST_JSON = "fixtures/model/ccd-request.json";
    private static final String CCD_REQUEST_WITH_SOL_EMAIL_CONSENT_JSON = "fixtures/ccd-request-with-solicitor-email-consent.json";
    private static final String CCD_REQUEST_WITH_BULK_PRINT_LETTER_CONSENT_JSON =
            "fixtures/ccd-request-with-solicitor-bulk-print-letter-consent.json";

    @Autowired
    private WebApplicationContext applicationContext;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private BulkPrintService bulkPrintService;

    @MockBean
    private HelpWithFeesBulkPrintService helpWithFeesBulkPrintService;

    @MockBean
    private AssignedToJudgeBulkPrintService assignedToJudgeBulkPrintService;

    @Value("${feature.hwf-Successful-notification-letter}")
    private boolean hwfSuccessfulNotificationLetterFeature;

    @Value("${feature.assigned-to-judge-notification-letter}")
    private boolean assignedToJudgeNotificationLetterFeature;

    private MockMvc mockMvc;
    private String requestContent;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
    }

    @Test
    public void sendHwfSuccessfulConfirmationEmail() throws Exception {
        buildCcdRequest(CCD_REQUEST_WITH_SOL_EMAIL_CONSENT_JSON);
        mockMvc.perform(post(HWF_SUCCESSFUL_CALLBACK_URL)
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .content(requestContent)
                .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

        verify(notificationService, times(1))
                .sendHWFSuccessfulConfirmationEmail(any(CallbackRequest.class));
    }

    @Test
    public void shouldNotSendHwfSuccessfulConfirmationEmail() throws Exception {
        buildCcdRequest(CCD_REQUEST_JSON);
        mockMvc.perform(post(HWF_SUCCESSFUL_CALLBACK_URL)
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .content(requestContent)
                .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void sendHwfSuccessfulConfirmationBulkPrintLetter() throws Exception {

        if (hwfSuccessfulNotificationLetterFeature) {
            buildCcdRequest(CCD_REQUEST_WITH_BULK_PRINT_LETTER_CONSENT_JSON);
            mockMvc.perform(post(HWF_SUCCESSFUL_CALLBACK_URL)
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .content(requestContent)
                .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

            when(bulkPrintService.sendNotificationLetterForBulkPrint(any(), any())).thenReturn(UUID.randomUUID());

            verify(helpWithFeesBulkPrintService, times(1))
                .generateHwfSuccessfulLetter(eq(AUTH_TOKEN), any(CaseDetails.class));
            verifyNoMoreInteractions(notificationService);
        }
    }

    @Test
    public void sendAssignToJudgeConfirmationEmail() throws Exception {
        buildCcdRequest(CCD_REQUEST_WITH_SOL_EMAIL_CONSENT_JSON);
        mockMvc.perform(post(ASSIGN_TO_JUDGE_CALLBACK_URL)
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .content(requestContent)
                .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

        verify(notificationService, times(1))
                .sendAssignToJudgeConfirmationEmail(any(CallbackRequest.class));
    }

    @Test
    public void shouldNotSendAssignToJudgeConfirmationEmail() throws Exception {
        buildCcdRequest(CCD_REQUEST_JSON);
        mockMvc.perform(post(ASSIGN_TO_JUDGE_CALLBACK_URL)
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .content(requestContent)
                .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void sendJudgeAssignedToCaseBulkPrintLetter() throws Exception {

        if (assignedToJudgeNotificationLetterFeature) {
            buildCcdRequest(CCD_REQUEST_WITH_BULK_PRINT_LETTER_CONSENT_JSON);
            mockMvc.perform(post(ASSIGN_TO_JUDGE_CALLBACK_URL)
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .content(requestContent)
                .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

            when(bulkPrintService.sendNotificationLetterForBulkPrint(any(), any())).thenReturn(UUID.randomUUID());

            verify(assignedToJudgeBulkPrintService, times(1))
                .generateJudgeAssignedToCaseLetter(eq(AUTH_TOKEN), any(CaseDetails.class));
            verifyNoMoreInteractions(notificationService);
        }
    }

    @Test
    public void sendConsentOrderMadeConfirmationEmail() throws Exception {
        buildCcdRequest(CCD_REQUEST_WITH_SOL_EMAIL_CONSENT_JSON);
        mockMvc.perform(post(CONSENT_ORDER_MADE_URL)
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .content(requestContent)
                .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

        verify(notificationService, times(1))
                .sendConsentOrderMadeConfirmationEmail(any(CallbackRequest.class));
    }

    @Test
    public void shouldNotSendConsentOrderMadeConfirmationEmail() throws Exception {
        buildCcdRequest(CCD_REQUEST_JSON);
        mockMvc.perform(post(CONSENT_ORDER_MADE_URL)
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .content(requestContent)
                .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void sendConsentOrderNotApprovedEmail() throws Exception {
        buildCcdRequest(CCD_REQUEST_WITH_SOL_EMAIL_CONSENT_JSON);
        mockMvc.perform(post(CONSENT_ORDER_NOT_APPROVED_URL)
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .content(requestContent)
                .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

        verify(notificationService, times(1))
                .sendConsentOrderNotApprovedEmail(any(CallbackRequest.class));
    }

    @Test
    public void shouldNotSendConsentOrderNotApprovedEmail() throws Exception {
        buildCcdRequest(CCD_REQUEST_JSON);
        mockMvc.perform(post(CONSENT_ORDER_NOT_APPROVED_URL)
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .content(requestContent)
                .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void sendConsentOrderAvailableEmail() throws Exception {
        buildCcdRequest(CCD_REQUEST_WITH_SOL_EMAIL_CONSENT_JSON);
        mockMvc.perform(post(CONSENT_ORDER_AVAILABLE_URL)
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .content(requestContent)
                .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

        verify(notificationService, times(1))
                .sendConsentOrderAvailableEmail(any(CallbackRequest.class));
    }

    @Test
    public void shouldNotSendConsentOrderAvailableEmail() throws Exception {
        buildCcdRequest(CCD_REQUEST_JSON);
        mockMvc.perform(post(CONSENT_ORDER_AVAILABLE_URL)
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .content(requestContent)
                .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void sendConsentedHwfSuccessfulConfirmationEmail() throws Exception {
        buildCcdRequest("fixtures/contested/hwf.json");
        mockMvc.perform(post(HWF_SUCCESSFUL_CALLBACK_URL)
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .content(requestContent)
                .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

        verify(notificationService, times(1))
                .sendContestedHwfSuccessfulConfirmationEmail(any(CallbackRequest.class));
    }

    @Test
    public void shouldNotSendContestedHwfSuccessfulEmail() throws Exception {
        buildCcdRequest("fixtures/contested/contested-hwf-without-solicitor-consent.json");
        mockMvc.perform(post(HWF_SUCCESSFUL_CALLBACK_URL)
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .content(requestContent)
                .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

        verifyNoMoreInteractions(notificationService);
    }

    private void buildCcdRequest(String fileName) throws Exception {
        requestContent = ResourceLoader.loadResourceAsString(fileName);
    }
}