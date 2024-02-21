package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import org.junit.Before;
import org.junit.ClassRule;
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
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.PdfDocumentRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OptionIdToValueTranslator;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
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

    protected static final String UPLOAD_DOCUMENT_CONTEXT_PATH = "/cases/documents";
    private static final String IDAM_SERVICE_CONTEXT_PATH = "/details";

    private static final String IDAM_SERVICE_USER_INFO = "/o/userinfo";

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
    public static WireMockClassRule evidenceManagementService = new WireMockClassRule(4455);

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


    void generateEvidenceUploadServiceSuccessStub() throws IOException {
        evidenceManagementService.stubFor(post(urlPathEqualTo(UPLOAD_DOCUMENT_CONTEXT_PATH))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(getResponse()))));
    }

    private UploadResponse getResponse() {
        Document document = buildDocument();
        return new UploadResponse(Collections.singletonList(document));
    }

    private Document buildDocument() {
        Date dateToUse = java.sql.Timestamp.valueOf(LocalDateTime.of(2021, 11, 2, 12, 25, 30, 1234));
        return Document.builder()
            .createdOn(dateToUse)
            .createdBy("someUser")
            .lastModifiedBy("someUser")
            .modifiedOn(dateToUse)
            .originalDocumentName("app_docs.pdf")
            .mimeType("application/pdf")
            .links(getLinks())
            .build();
    }

    private Document.Links getLinks() {
        Document.Links links = new Document.Links();
        links.binary = new Document.Link();
        links.self = new Document.Link();
        links.binary.href = "http://dm-store:8080/documents/d607c045-878e-475f-ab8e-b2f667d8af64/binary";
        links.self.href = "http://dm-store:8080/documents/d607c045-878e-475f-ab8e-b2f667d8af64";
        return links;
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

    void idamServiceStub() {
        idamService.stubFor(get(urlPathEqualTo(IDAM_SERVICE_CONTEXT_PATH))
            .withHeader(AUTHORIZATION, equalTo(AUTH_TOKEN))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody("{\"roles\": [\"caseworker-divorce-financialremedy-courtadmin\"]}")));

        idamService.stubFor(get(urlPathEqualTo(IDAM_SERVICE_USER_INFO))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody("{\"roles\": [\"caseworker-divorce-financialremedy-courtadmin\"]}")));
    }
}
