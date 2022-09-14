package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
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
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CaseOrchestrationApplication.class)
@PropertySource("classpath:application.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class PBAValidationTest extends BaseTest {

    private static final String PBA_VALIDATE_URL = "/case-orchestration/pba-validate";
    private static final String PBA_VALID_RESPONSE = "{\"pbaNumberValid\": true }";
    private static final String PBA_INVALID_RESPONSE = "{\"pbaNumberValid\": false }";

    @Autowired
    private MockMvc webClient;

    @Autowired
    private ObjectMapper objectMapper;

    @ClassRule
    public static WireMockClassRule pbaServiceClassRule = new WireMockClassRule(8090);

    @ClassRule
    public static WireMockClassRule idamServiceClassRule = new WireMockClassRule(4501);

    private CallbackRequest request;
    private JsonNode pbaValidResponseContent;
    private JsonNode pbaInValidResponseContent;

    @Before
    public void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            pbaValidResponseContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/payment-by-account-full.json").toURI()));
            pbaInValidResponseContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/payment-by-account-not-including-PBA123.json").toURI()));
        } catch (Exception e) {
            fail(e.getMessage());
        }

    }


    @Test
    public void shouldPBAValidationSuccessful() throws Exception {
        setUpPbaValidationRequest("/fixtures/pba-validate.json");
        stubPBAValidate(pbaValidResponseContent.toString());
        stubForIdam();
        webClient.perform(post(PBA_VALIDATE_URL)
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.errors", is(emptyOrNullString())))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldPBAValidationFail() throws Exception {
        setUpPbaValidationRequest("/fixtures/pba-validate.json");
        stubPBAValidate(pbaInValidResponseContent.toString());
        stubForIdam();
        webClient.perform(post(PBA_VALIDATE_URL)
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldNotDoPBAValidationWhenPaymentIsDoneWithHWF() throws Exception {
        setUpPbaValidationRequest("/fixtures/hwf.json");
        webClient.perform(post(PBA_VALIDATE_URL)
                .content(objectMapper.writeValueAsString(request))
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors", is(emptyOrNullString())))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldReturnBadRequestError() throws Exception {
        setUpPbaValidationRequest("/fixtures/empty-casedata.json");
        webClient.perform(post(PBA_VALIDATE_URL)
                .content(objectMapper.writeValueAsString(request))
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest());
    }

    private void setUpPbaValidationRequest(String name) throws IOException {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(name)) {
            request = objectMapper.readValue(resourceAsStream, CallbackRequest.class);
        }
    }

    private void stubPBAValidate(String response) {
        pbaServiceClassRule.stubFor(get("/refdata/external/v1/organisations/pbas")
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(response)));
    }


    private void stubForIdam() {
        idamServiceClassRule.stubFor(get("/details")
            .withHeader(AUTHORIZATION, equalTo(AUTH_TOKEN))
            .withHeader(CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBody("{\"email\": \"test@TEST.com\"}")));
    }
}
