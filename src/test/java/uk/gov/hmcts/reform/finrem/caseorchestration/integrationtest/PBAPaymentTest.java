package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest;

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
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;

import java.io.IOException;
import java.io.InputStream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
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
public class PBAPaymentTest extends BaseTest {
    private static final String PBA_PAYMENT_URL = "/case-orchestration/pba-payment";
    private static final String ASSIGN_APPLICANT_SOLICITOR_URL = "/case-orchestration/assign-applicant-solicitor";
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

    private static final String PRD_RESPONSE = "{\n"
        + "\"contactInformation\":[],"
        + "\"name\":\"Org Name\","
        + "\"organisationIdentifier\":\"RG-123456789\""
        + "}";

    @Autowired
    private MockMvc webClient;

    @Autowired
    private ObjectMapper objectMapper;

    private CallbackRequest request;

    @ClassRule public static WireMockClassRule feeLookUpService = new WireMockClassRule(9001);
    @ClassRule public static WireMockClassRule idamService = new WireMockClassRule(4501);
    @ClassRule public static WireMockClassRule acaService = new WireMockClassRule(4454);
    @ClassRule public static WireMockClassRule dataStoreService = new WireMockClassRule(4452);
    @ClassRule public static WireMockClassRule prdService = new WireMockClassRule(8090);

    private String idamUrl = "/details";
    private String acaUrl = "/case-assignments";
    private String dataStoreUrl = "/case-users";
    private String prdUrl = "/refdata/external/v1/organisations";

    @Before
    public void setUp() {
        stubForIdam();
        stubForAca(HttpStatus.OK);
        stubForDataStore();
        stubForPrd();
    }

    @Test
    public void shouldDoPBAPayment() throws Exception {
        setUpPbaPayment("/fixtures/pba-payment.json");
        stubFeeLookUp();
        stubPayment(PAYMENT_RESPONSE);
        webClient.perform(MockMvcRequestBuilders.post(PBA_PAYMENT_URL)
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.PBAPaymentReference", is("REF0001")))
            .andExpect(jsonPath("$.data.orderSummary.Fees[0].value.FeeCode", is("FEE0600")))
            .andExpect(jsonPath("$.data.orderSummary.Fees[0].value.FeeAmount", is("5000")))
            .andExpect(jsonPath("$.data.orderSummary.Fees[0].value.FeeDescription", is("Application (without notice)")))
            .andExpect(jsonPath("$.data.orderSummary.Fees[0].value.FeeVersion", is("1")))
            .andExpect(jsonPath("$.data.amountToPay", is("5000")))
            .andExpect(jsonPath("$.errors", is(emptyOrNullString())))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));

        verify(postRequestedFor(urlMatching(acaUrl)));
    }

    @Test
    public void shouldNotDoPBAPaymentWhenHWFPayment() throws Exception {
        setUpHwfPayment();
        stubFeeLookUp();
        webClient.perform(MockMvcRequestBuilders.post(PBA_PAYMENT_URL)
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.state", is(AWAITING_HWF_DECISION.toString())))
            .andExpect(jsonPath("$.data.orderSummary.Fees[0].value.FeeCode", is("FEE0600")))
            .andExpect(jsonPath("$.data.orderSummary.Fees[0].value.FeeAmount", is("5000")))
            .andExpect(jsonPath("$.data.orderSummary.Fees[0].value.FeeDescription", is("Application (without notice)")))
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
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldFailWhenAcaCallFails() throws Exception {
        setUpPbaPayment("/fixtures/pba-payment.json");
        stubForAca(HttpStatus.NOT_FOUND);

        webClient.perform(MockMvcRequestBuilders.post(ASSIGN_APPLICANT_SOLICITOR_URL)
            .content(objectMapper.writeValueAsString(request))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));

        verify(postRequestedFor(urlMatching(acaUrl)));
    }

    @Test
    public void shouldDoAssignApplicantSolicitor() throws Exception {
        setUpPbaPayment("/fixtures/pba-payment.json");
        stubForAca(HttpStatus.OK);

        webClient.perform(MockMvcRequestBuilders.post(ASSIGN_APPLICANT_SOLICITOR_URL)
            .content(objectMapper.writeValueAsString(request))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.authorisation3", is(notNullValue())))
            .andExpect(jsonPath("$.errors", is(emptyOrNullString())))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));

        verify(postRequestedFor(urlMatching(acaUrl)));
    }

    @Test
    public void shouldReturnBadRequestError() throws Exception {
        setUpPbaPayment("/fixtures/empty-casedata.json");

        webClient.perform(MockMvcRequestBuilders.post(PBA_PAYMENT_URL)
            .content(objectMapper.writeValueAsString(request))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest());
    }

    private void stubPayment(String paymentResponse) {
        feeLookUpService.stubFor(post(PBA_URL)
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(paymentResponse)));
    }

    private void stubFeeLookUp() {
        feeLookUpService.stubFor(get(urlMatching(FEE_LOOKUP_URL))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(FEE_RESPONSE)));
    }

    public void setUpHwfPayment() throws IOException {
        setUpPbaPayment("/fixtures/hwf.json");
    }

    private void setUpPbaPayment(String name) throws IOException {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(name)) {
            request = objectMapper.readValue(resourceAsStream, CallbackRequest.class);
        }
    }

    private void stubForIdam() {
        idamService.stubFor(get(urlEqualTo(idamUrl))
            .withHeader(AUTHORIZATION, equalTo(AUTH_TOKEN))
            .withHeader(CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBody("{\"id\": \"1234\"}".getBytes())
            ));
    }

    private void stubForAca(HttpStatus httpStatus) {
        acaService.stubFor(post(urlEqualTo(acaUrl))
            .withHeader(AUTHORIZATION, equalTo(AUTH_TOKEN))
            .withHeader(CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .willReturn(
                aResponse()
                    .withStatus(httpStatus.value())
            ));
    }

    private void stubForDataStore() {
        dataStoreService.stubFor(delete(urlEqualTo(dataStoreUrl))
            .withHeader(AUTHORIZATION, equalTo(AUTH_TOKEN))
            .withHeader(CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())
            ));
    }

    private void stubForPrd() {
        prdService.stubFor(get(urlEqualTo(prdUrl))
            .withHeader(AUTHORIZATION, equalTo(AUTH_TOKEN))
            .withHeader(CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBody(PRD_RESPONSE)
            ));
    }
}
