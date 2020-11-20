package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;

import java.io.InputStream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CaseOrchestrationApplication.class)
@PropertySource("classpath:application.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class PBAPaymentConfirmationTest extends BaseTest {

    private static final String PBA_CONFIRMATION_URL = "/case-orchestration/payment-confirmation";

    @Autowired
    private MockMvc webClient;

    @ClassRule public static WireMockClassRule idamService = new WireMockClassRule(4501);

    @Autowired
    private ObjectMapper objectMapper;

    private CallbackRequest request;

    private String idamUrl = "/details";

    @Before
    public void setUp() {
        stubForIdam();
    }

    @Test
    public void shouldDoPbaConfirmation() throws Exception {
        setRequest("/fixtures/pba-validate.json");
        webClient.perform(MockMvcRequestBuilders.post(PBA_CONFIRMATION_URL)
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.confirmation_body",
                containsString("You will now be directed to the case file where you can monitor "
                    + "the progress of your application via the ‘history’ tab. Next:")))
            .andExpect(jsonPath("$.confirmation_body",
                containsString("* Your application will be issued by Court staff and referred to a Judge")))
            .andExpect(jsonPath("$.confirmation_body",
                containsString("* The Judge will consider your application and make an order")));
    }

    @Test
    public void shouldDoHwfConfirmation() throws Exception {
        setRequest("/fixtures/hwf.json");
        webClient.perform(MockMvcRequestBuilders.post(PBA_CONFIRMATION_URL)
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.confirmation_body",
                containsString("The application will be received by Court staff who will:")))
            .andExpect(jsonPath("$.confirmation_body",
                containsString("* Check the application")))
            .andExpect(jsonPath("$.confirmation_body",
                containsString("* Process the application for help with fees")));
    }

    @Test
    public void shouldReturnBadRequestError() throws Exception {
        setRequest("/fixtures/empty-casedata.json");
        webClient.perform(MockMvcRequestBuilders.post(PBA_CONFIRMATION_URL)
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest());
    }

    private void setRequest(String name) throws Exception {
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
}
