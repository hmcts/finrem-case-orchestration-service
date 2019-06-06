package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(NotificationsController.class)
public class NotificationsControllerTest {
    private static final String HWF_SUCCESSFUL_EMAIL_URL = "/case-orchestration/notify/hwf-successful";
    private static final String ASSIGN_TO_JUDGE_URL = "/case-orchestration/notify/assign-to-judge";
    private static final String CONSENT_ORDER_MADE_URL = "/case-orchestration/notify/consent-order-made";
    private static final String CONSENT_ORDER_NOT_APPROVED_URL = "/case-orchestration/notify/"
            + "consent-order-not-approved";
    private static final String CONSENT_ORDER_AVAILABLE_URL = "/case-orchestration/notify/"
            + "consent-order-available";

    @Autowired
    private WebApplicationContext applicationContext;

    @MockBean
    private NotificationService notificationService;
    private MockMvc mockMvc;

    private JsonNode requestContent;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
    }

    @Test
    public void sendHwfSuccessfulConfirmationEmail() throws Exception {
        buildCcdRequest();
        mockMvc.perform(post(HWF_SUCCESSFUL_EMAIL_URL)
                .content(requestContent.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(notificationService, times(1))
                .sendHWFSuccessfulConfirmationEmail(any(CallbackRequest.class));

    }

    @Test
    public void shouldNotSendHwfSuccessfulConfirmationEmail() throws Exception {
        buildRequest();
        mockMvc.perform(post(HWF_SUCCESSFUL_EMAIL_URL)
                .content(requestContent.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verifyNoMoreInteractions(notificationService);

    }

    @Test
    public void sendAssignToJudgeConfirmationEmail() throws Exception {
        buildCcdRequest();
        mockMvc.perform(post(ASSIGN_TO_JUDGE_URL)
                .content(requestContent.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(notificationService, times(1))
                .sendAssignToJudgeConfirmationEmail(any(CallbackRequest.class));

    }

    @Test
    public void shouldNotSendAssignToJudgeConfirmationEmail() throws Exception {
        buildRequest();
        mockMvc.perform(post(ASSIGN_TO_JUDGE_URL)
                .content(requestContent.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verifyNoMoreInteractions(notificationService);

    }

    @Test
    public void sendConsentOrderMadeConfirmationEmail() throws Exception {
        buildCcdRequest();
        mockMvc.perform(post(CONSENT_ORDER_MADE_URL)
                .content(requestContent.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(notificationService, times(1))
                .sendConsentOrderMadeConfirmationEmail(any(CallbackRequest.class));

    }

    @Test
    public void shouldNotSendConsentOrderMadeConfirmationEmail() throws Exception {
        buildRequest();
        mockMvc.perform(post(CONSENT_ORDER_MADE_URL)
                .content(requestContent.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void sendConsentOrderNotApprovedEmail() throws Exception {
        buildCcdRequest();
        mockMvc.perform(post(CONSENT_ORDER_NOT_APPROVED_URL)
                .content(requestContent.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(notificationService, times(1))
                .sendConsentOrderNotApprovedEmail(any(CallbackRequest.class));

    }

    @Test
    public void shouldNotSendConsentOrderNotApprovedEmail() throws Exception {
        buildRequest();
        mockMvc.perform(post(CONSENT_ORDER_NOT_APPROVED_URL)
                .content(requestContent.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verifyNoMoreInteractions(notificationService);

    }

    @Test
    public void sendConsentOrderAvailableEmail() throws Exception {
        buildCcdRequest();
        mockMvc.perform(post(CONSENT_ORDER_AVAILABLE_URL)
                .content(requestContent.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(notificationService, times(1))
                .sendConsentOrderAvailableEmail(any(CallbackRequest.class));

    }

    @Test
    public void shouldNotSendConsentOrderAvailableEmail() throws Exception {
        buildRequest();
        mockMvc.perform(post(CONSENT_ORDER_AVAILABLE_URL)
                .content(requestContent.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verifyNoMoreInteractions(notificationService);

    }

    private void buildCcdRequest() throws IOException, URISyntaxException {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/ccd-request-with-solicitor-email-consent.json").toURI()));
    }

    private void buildRequest() throws IOException, URISyntaxException {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/model/ccd-request.json").toURI()));
    }
}