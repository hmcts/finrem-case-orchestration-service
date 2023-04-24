package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.PdfDocumentRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OptionIdToValueTranslator;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.nio.file.Files.readAllBytes;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.document;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CaseOrchestrationApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.properties")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Category(IntegrationTest.class)
public abstract class AbstractDocumentTest extends BaseTest {

    protected static final String GENERATE_DOCUMENT_CONTEXT_PATH = "/rs/render";
    protected static final String SEND_LETTERS_CONTEXT_PATH = "/letters";

    protected static final String UPLOAD_DOCUMENT_CONTEXT_PATH = "/documents";
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
    public static WireMockClassRule documentGeneratorBulkPrintService = new WireMockClassRule(4009);

    @ClassRule
    public static WireMockClassRule documentGeneratorService = new WireMockClassRule(4001);

    @ClassRule
    public static WireMockClassRule evidenceManagementService = new WireMockClassRule(3405);

    @ClassRule
    public static WireMockClassRule idamService = new WireMockClassRule(4501);

    @ClassRule
    public static WireMockClassRule sendLetterService = new WireMockClassRule(4002);

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

    protected abstract PdfDocumentRequest pdfRequest();

    protected abstract String apiUrl();

    @Test
    public void documentGeneratorServiceError() throws Exception {
        generateDocumentServiceErrorStub();

        webClient.perform(MockMvcRequestBuilders.post(apiUrl())
            .content(objectMapper.writeValueAsString(request))
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE)
            .accept(APPLICATION_JSON_VALUE))
            .andExpect(status().isInternalServerError());
    }

    void generateEvidenceUploadServiceSuccessStub() throws IOException {
        evidenceManagementService.stubFor(post(urlPathEqualTo(UPLOAD_DOCUMENT_CONTEXT_PATH))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(getResponse()))));
    }


    private ObjectNode getResponse() throws IOException {
        final String response = new String(readAllBytes(Paths.get("src/test/resources/fixtures/fileuploadresponseGenerateMiniFormATest.json")));
        return (ObjectNode) new ObjectMapper().readTree(response);
    }

    void generateDocumentServiceSuccessStub() throws JsonProcessingException {
        documentGeneratorService.stubFor(post(urlPathEqualTo(GENERATE_DOCUMENT_CONTEXT_PATH))
            .withRequestBody(equalToJson(objectMapper.writeValueAsString(pdfRequest()), true, true))
            .withHeader(CONTENT_TYPE, equalTo("application/json"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(document()))));
    }


    void deleteDocumentServiceStubWith(HttpStatus status) {
        documentGeneratorService.stubFor(
            delete(urlMatching(DELETE_DOCUMENT_CONTEXT_PATH.concat("\\?fileUrl=").concat(TEMP_URL)))
                .withHeader(AUTHORIZATION, equalTo(AUTH_TOKEN))
                .willReturn(aResponse().withStatus(status.value())));
    }

    void downloadDocumentServiceStubWith(HttpStatus status) throws JsonProcessingException, URISyntaxException {
        evidenceManagementService.stubFor(
            get(urlMatching(new URI(BINARY_URL).getPath()))
                .willReturn(aResponse()
                    .withBody(objectMapper.writeValueAsString(document()))
                    .withStatus(status.value())));
    }

    private void generateDocumentServiceErrorStub() throws JsonProcessingException {
        documentGeneratorService.stubFor(post(urlPathEqualTo(GENERATE_DOCUMENT_CONTEXT_PATH))
            .withRequestBody(equalToJson(objectMapper.writeValueAsString(pdfRequest()), true, true))
            .withHeader(CONTENT_TYPE, equalTo("application/json"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));
    }

    public void generateSendLetterServiceStub() throws JsonProcessingException {
        sendLetterService.stubFor(post(urlPathEqualTo(SEND_LETTERS_CONTEXT_PATH))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(new SendLetterResponse(UUID.randomUUID())))));
    }

    void idamServiceStub() {
        idamService.stubFor(get(urlPathEqualTo(IDAM_SERVICE_CONTEXT_PATH))
            .withHeader(AUTHORIZATION, equalTo(AUTH_TOKEN))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody("{\"roles\": [\"caseworker-divorce-financialremedy-courtadmin\"]}")));
    }
}
