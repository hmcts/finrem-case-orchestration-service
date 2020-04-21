package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;

import java.io.IOException;
import java.io.InputStream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CaseOrchestrationApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.properties")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Slf4j
@Category(IntegrationTest.class)
public class NotificationsTest {

    private static final String HWF_SUCCESSFUL_URL = "/case-orchestration/notify/hwf-successful";
    private static final String CONSENT_ORDER_MADE_URL = "/case-orchestration/notify/consent-order-made";
    private static final String CONSENT_ORDER_AVAILABLE_URL = "/case-orchestration/notify/consent-order-available";
    private static final String CONSENT_ORDER_NOT_APPROVED_URL = "/case-orchestration/notify/consent-order-not-approved";
    private static final String ASSIGNED_TO_JUDGE_URL = "/case-orchestration/notify/assign-to-judge";
    private static final String NOTIFY_HWF_SUCCESSFUL_CONTEXT_PATH = "/notify/hwf-successful";
    private static final String NOTIFY_CONSENT_ORDER_MADE_CONTEXT_PATH = "/notify/consent-order-made";
    private static final String NOTIFY_CONSENT_ORDER_AVAILABLE_CONTEXT_PATH = "/notify/consent-order-available";
    private static final String NOTIFY_CONSENT_ORDER_NOT_APPROVED_CONTEXT_PATH = "/notify/consent-order-not-approved";
    private static final String NOTIFY_ASSIGN_TO_JUDGE_CONTEXT_PATH = "/notify/assign-to-judge";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc webClient;

    @ClassRule
    public static WireMockClassRule notificationService = new WireMockClassRule(8086);

    private CallbackRequest request;

    @Before
    public void setUp() throws IOException {
        try (InputStream resourceAsStream = getClass().getResourceAsStream("/fixtures/ccd-request-with-solicitor-email-consent.json")) {
            request = objectMapper.readValue(resourceAsStream, CallbackRequest.class);
        }
    }

    @Test
    public void notifyHwfSuccessful() throws Exception {
        stubForNotification(NOTIFY_HWF_SUCCESSFUL_CONTEXT_PATH, HttpStatus.OK.value());
        webClient.perform(MockMvcRequestBuilders.post(HWF_SUCCESSFUL_URL)
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(content().json(expectedCaseData()));
        verify(postRequestedFor(urlEqualTo(NOTIFY_HWF_SUCCESSFUL_CONTEXT_PATH))
            .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE)));
    }

    @Test
    public void notifyConsentOrderMade() throws Exception {
        stubForNotification(NOTIFY_CONSENT_ORDER_MADE_CONTEXT_PATH, HttpStatus.OK.value());
        webClient.perform(MockMvcRequestBuilders.post(CONSENT_ORDER_MADE_URL)
            .contentType(APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(content().json(expectedCaseData()));
        verify(postRequestedFor(urlEqualTo(NOTIFY_CONSENT_ORDER_MADE_CONTEXT_PATH))
            .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE)));
    }

    @Test
    public void notifyConsentOrderAvailable() throws Exception {
        stubForNotification(NOTIFY_CONSENT_ORDER_AVAILABLE_CONTEXT_PATH, HttpStatus.OK.value());
        webClient.perform(MockMvcRequestBuilders.post(CONSENT_ORDER_AVAILABLE_URL)
            .contentType(APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(content().json(expectedCaseData()));
        verify(postRequestedFor(urlEqualTo(NOTIFY_CONSENT_ORDER_AVAILABLE_CONTEXT_PATH))
            .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE)));
    }

    @Test
    public void notifyConsentOrderNotApproved() throws Exception {
        stubForNotification(NOTIFY_CONSENT_ORDER_NOT_APPROVED_CONTEXT_PATH, HttpStatus.OK.value());
        webClient.perform(MockMvcRequestBuilders.post(CONSENT_ORDER_NOT_APPROVED_URL)
            .contentType(APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(content().json(expectedCaseData()));
        verify(postRequestedFor(urlEqualTo(NOTIFY_CONSENT_ORDER_NOT_APPROVED_CONTEXT_PATH))
            .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE)));
    }

    @Test
    public void notifyAssignToJudge() throws Exception {
        stubForNotification(NOTIFY_ASSIGN_TO_JUDGE_CONTEXT_PATH, HttpStatus.OK.value());
        webClient.perform(MockMvcRequestBuilders.post(ASSIGNED_TO_JUDGE_URL)
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(content().json(expectedCaseData()));
        verify(postRequestedFor(urlEqualTo(NOTIFY_ASSIGN_TO_JUDGE_CONTEXT_PATH))
            .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE)));
    }

    private String expectedCaseData() throws JsonProcessingException {
        CaseDetails caseDetails = request.getCaseDetails();
        return objectMapper.writeValueAsString(AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDetails.getData()).build());
    }

    private void stubForNotification(String url, int value) {
        notificationService.stubFor(post(urlEqualTo(url))
            .withHeader(AUTHORIZATION, equalTo(AUTH_TOKEN))
            .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE))
            .willReturn(aResponse().withStatus(value)));
    }
}
