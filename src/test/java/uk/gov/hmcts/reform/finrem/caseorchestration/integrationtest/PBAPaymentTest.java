package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import lombok.extern.slf4j.Slf4j;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;

import java.io.IOException;
import java.io.InputStream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ConsentedStatus.AWAITING_HWF_DECISION;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CaseOrchestrationApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource("classpath:application.properties")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Slf4j
@Category(IntegrationTest.class)
public class PBAPaymentTest {
    private static final String PBA_PAYMENT_URL = "/case-orchestration/pba-payment";
    private static final String FEE_LOOKUP_URL = "/payments/fee-lookup\\?application-type=consented";
    private static final String PBA_URL = "/payments/pba-payment";
    private static final String FEE_RESPONSE = "{\n"
            + "  \"code\": \"FEE0600\",\n"
            + "  \"description\": \"Application (without notice)\",\n"
            + "  \"version\": 1,\n"
            + "  \"fee_amount\": 50\n"
            + "}";

    private static final String PAYMENT_RESPONSE = "{\n"
            + "\"reference\":\"REF0001\","
            + "\"error\":null,"
            + "\"message\":null,"
            + "\"status\":\"success\","
            + "\"statusHistories\":null"
            + "}";

    private static final String PAYMENT_FAILURE_RESPONSE = "{\n"
            + "\"reference\":null,"
            + "\"error\":1,"
            + "\"message\":\"payment failed\","
            + "\"status\":\"failed\""
            + "}";

    @Autowired
    private MockMvc webClient;

    @Autowired
    private ObjectMapper objectMapper;

    private CallbackRequest request;

    @ClassRule
    public static WireMockClassRule feeLookUpService = new WireMockClassRule(9001);

    @Test
    public void shouldDoPBAPayment() throws Exception {
        setUpPbaPayment("/fixtures/pba-payment.json");
        stubFeeLookUp();
        stubPayment(PAYMENT_RESPONSE);
        webClient.perform(MockMvcRequestBuilders.post(PBA_PAYMENT_URL)
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.PBAPaymentReference", is("REF0001")))
                .andExpect(jsonPath("$.data.orderSummary.Fees[0].value.FeeCode", is("FEE0600")))
                .andExpect(jsonPath("$.data.orderSummary.Fees[0].value.FeeAmount", is("5000")))
                .andExpect(jsonPath("$.data.orderSummary.Fees[0].value.FeeDescription",
                        is("Application (without notice)")))
                .andExpect(jsonPath("$.data.orderSummary.Fees[0].value.FeeVersion", is("1")))
                .andExpect(jsonPath("$.data.amountToPay", is("5000")))
                .andExpect(jsonPath("$.errors", is(emptyOrNullString())))
                .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));

        verify(getRequestedFor(urlMatching(FEE_LOOKUP_URL)));
        verify(postRequestedFor(urlMatching(PBA_URL)));
    }

    @Test
    public void shouldNotDoPBAPaymentWhenHWFPayment() throws Exception {
        setUpHwfPayment();
        stubFeeLookUp();
        webClient.perform(MockMvcRequestBuilders.post(PBA_PAYMENT_URL)
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.state", is(AWAITING_HWF_DECISION.toString())))
                .andExpect(jsonPath("$.data.orderSummary.Fees[0].value.FeeCode", is("FEE0600")))
                .andExpect(jsonPath("$.data.orderSummary.Fees[0].value.FeeAmount", is("5000")))
                .andExpect(jsonPath("$.data.orderSummary.Fees[0].value.FeeDescription",
                        is("Application (without notice)")))
                .andExpect(jsonPath("$.data.orderSummary.Fees[0].value.FeeVersion", is("1")))
                .andExpect(jsonPath("$.data.amountToPay", is("5000")))
                .andExpect(jsonPath("$.errors", is(emptyOrNullString())))
                .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldReturnErrorWhenPbaPaymentFails() throws Exception {
        setUpPbaPayment("/fixtures/pba-payment.json");
        stubFeeLookUp();
        stubPayment(PAYMENT_FAILURE_RESPONSE);
        webClient.perform(MockMvcRequestBuilders.post(PBA_PAYMENT_URL)
                .content(objectMapper.writeValueAsString(request))
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
        verify(getRequestedFor(urlMatching(FEE_LOOKUP_URL)));
        verify(postRequestedFor(urlMatching(PBA_URL)));
    }

    @Test
    public void shouldReturnBadRequestError() throws Exception {
        setUpPbaPayment("/fixtures/empty-casedata.json");
        webClient.perform(MockMvcRequestBuilders.post(PBA_PAYMENT_URL)
                .content(objectMapper.writeValueAsString(request))
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    private void stubPayment(String paymentResponse) {
        stubFor(post(PBA_URL)
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(CONTENT_TYPE, javax.ws.rs.core.MediaType.APPLICATION_JSON)
                        .withBody(paymentResponse)));
    }

    private void stubFeeLookUp() {
        stubFor(get(urlMatching(FEE_LOOKUP_URL))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(CONTENT_TYPE, javax.ws.rs.core.MediaType.APPLICATION_JSON)
                        .withBody(FEE_RESPONSE)));
    }

    public void setUpHwfPayment() throws IOException {
        setUpPbaPayment("/fixtures/hwf.json");
    }

    private void setUpPbaPayment(String name) throws IOException {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(
                name)) {
            request = objectMapper.readValue(resourceAsStream, CallbackRequest.class);
        }
    }
}
