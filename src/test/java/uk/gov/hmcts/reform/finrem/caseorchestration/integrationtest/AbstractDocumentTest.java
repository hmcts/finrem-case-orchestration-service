package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentGenerationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OptionIdToValueTranslator;

import java.io.IOException;
import java.io.InputStream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CaseOrchestrationApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.properties")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Category(IntegrationTest.class)
public abstract class AbstractDocumentTest {

    private static final String GENERATE_DOCUMENT_CONTEXT_PATH = "/version/1/generate-pdf";
    private static final String TEMP_URL = "http://doc1";
    private static final String DELETE_DOCUMENT_CONTEXT_PATH = "/version/1/delete-pdf-document";
    private static final String IDAM_SERVICE_CONTEXT_PATH = "/details";

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected MockMvc webClient;

    @Autowired
    protected DocumentConfiguration documentConfiguration;

    @Autowired
    protected OptionIdToValueTranslator optionIdToValueTranslator;

    @ClassRule
    public static WireMockClassRule documentGeneratorService = new WireMockClassRule(4009);

    @ClassRule
    public static WireMockClassRule idamService = new WireMockClassRule(4501);

    protected CallbackRequest request;

    @Before
    public void setUp() throws IOException {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(getTestFixture())) {
            request = objectMapper.readValue(resourceAsStream, CallbackRequest.class);
        }
    }

    protected String getTestFixture() {
        return "/fixtures/fee-lookup.json";
    }

    protected abstract DocumentGenerationRequest documentRequest();

    protected abstract String apiUrl();

    @Test
    public void documentGeneratorServiceError() throws Exception {
        generateDocumentServiceErrorStub();

        webClient.perform(MockMvcRequestBuilders.post(apiUrl())
            .content(objectMapper.writeValueAsString(request))
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isInternalServerError());
    }

    void generateDocumentServiceSuccessStub() throws JsonProcessingException {
        documentGeneratorService.stubFor(post(urlPathEqualTo(GENERATE_DOCUMENT_CONTEXT_PATH))
            .withRequestBody(equalToJson(objectMapper.writeValueAsString(documentRequest()), true, true))
            .withHeader(AUTHORIZATION, equalTo(AUTH_TOKEN))
            .withHeader(CONTENT_TYPE, equalTo("application/json"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(objectMapper.writeValueAsString(document()))));
    }

    void deleteDocumentServiceStubWith(HttpStatus status) {
        documentGeneratorService.stubFor(
            delete(urlMatching(DELETE_DOCUMENT_CONTEXT_PATH.concat("\\?fileUrl=").concat(TEMP_URL)))
                .withHeader(AUTHORIZATION, equalTo(AUTH_TOKEN))
                .willReturn(aResponse().withStatus(status.value())));
    }

    private void generateDocumentServiceErrorStub() throws JsonProcessingException {
        documentGeneratorService.stubFor(post(urlPathEqualTo(GENERATE_DOCUMENT_CONTEXT_PATH))
            .withRequestBody(equalToJson(objectMapper.writeValueAsString(documentRequest()), true, true))
            .withHeader(AUTHORIZATION, equalTo(AUTH_TOKEN))
            .withHeader(CONTENT_TYPE, equalTo("application/json"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)));
    }

    void idamServiceStub() throws JsonProcessingException {
        idamService.stubFor(get(urlPathEqualTo(IDAM_SERVICE_CONTEXT_PATH))
            .withHeader(AUTHORIZATION, equalTo(AUTH_TOKEN))
            .withHeader(CONTENT_TYPE, equalTo("application/json"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody("{\"roles\": [\"caseworker-divorce-financialremedy-courtadmin\"]}")));
    }
}
