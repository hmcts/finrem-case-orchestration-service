package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.PdfDocumentRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentGenerationRequest;
import uk.gov.hmcts.reform.sendletter.api.LetterStatus;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.nio.file.Files.readAllBytes;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.document;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FAST_TRACK_DECISION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ISSUE_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.OUT_OF_FAMILY_COURT_RESOLUTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService.REQUIRED_FIELD_EMPTY_ERROR;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CaseOrchestrationApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.properties")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Category(IntegrationTest.class)
public class HearingNonFastTrackDocumentTest extends BaseTest {
    private static final String IDAM_SERVICE_CONTEXT_PATH = "/details";
    private static final String GENERATE_DOCUMENT_CONTEXT_PATH = "/rs/render";
    protected static final String UPLOAD_DOCUMENT_CONTEXT_PATH = "/documents";

    protected static final String SEND_LETTER_CONTEXT_PATH = "/letters";

    private static final String API_URL = "/case-orchestration/documents/hearing";
    private static final String JSON_CONTENT_PATH = "/fixtures/contested/validate-hearing-withoutfastTrackDecision.json";

    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected MockMvc webClient;
    @Autowired protected DocumentConfiguration config;

    @ClassRule
    public static WireMockClassRule documentGeneratorServiceClass = new WireMockClassRule(4001);

    @ClassRule
    public static WireMockClassRule sendLetterService = new WireMockClassRule(4002);

    @ClassRule
    public static WireMockClassRule dmStoreService = new WireMockClassRule(3405);

    @ClassRule
    public static WireMockClassRule documentGeneratorBulkPrintService = new WireMockClassRule(4009);

    @ClassRule
    public static WireMockClassRule idamService = new WireMockClassRule(4501);
    @Rule
    public WireMockClassRule documentGeneratorService = documentGeneratorServiceClass;

    private static String requestJson;
    protected CallbackRequest request;

    @BeforeClass
    public static void loadJsonString() throws IOException {
        requestJson = Resources.toString(HearingNonFastTrackDocumentTest.class.getResource(JSON_CONTENT_PATH), StandardCharsets.UTF_8);
    }

    @Before
    public void setUp() throws IOException {
        request = objectMapper.readValue(requestJson, CallbackRequest.class);
        request.getCaseDetails().getData().put("hearingDate", LocalDate.now().plusDays(100));
        request.getCaseDetails().getData().put("issueDate", LocalDate.now());
    }

    @Test
    public void missingIssueDate() throws Exception {
        doMissingMustFieldTest(ISSUE_DATE);
    }

    @Test
    public void missingHearingDate() throws Exception {
        doMissingMustFieldTest(HEARING_DATE);
    }

    @Test
    public void missingFastTrackDecision() throws Exception {
        doMissingMustFieldTest(FAST_TRACK_DECISION);
    }

    @Test
    public void generateFormCAndFormGSuccess() throws Exception {
        idamServiceStub();
        UUID uuid = UUID.randomUUID();
        generateSendLetterSuccessStub(uuid);
        generateConfirmLetterCreatedStub(uuid);
        generateEvidenceDownloadServiceSuccessStub();
        generateEvidenceUploadServiceSuccessStub();
        generateDocumentServiceSuccessStub(pdfGenerationRequest(config.getFormCNonFastTrackTemplate(request.getCaseDetails())));
        generateDocumentServiceSuccessStub(pdfGenerationRequest(config.getFormGTemplate(request.getCaseDetails())));
        generateDocumentServiceSuccessStub(pdfGenerationRequest(config.getOutOfFamilyCourtResolutionTemplate()));

        MvcResult mvcResult;
        int requestsMade = 0;
        do {
            if (requestsMade > 0) {
                Thread.sleep(100);
            }

            mvcResult = webClient.perform(MockMvcRequestBuilders.post(API_URL)
                .content(objectMapper.writeValueAsString(request))
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE))
                .andReturn();
        } while (++requestsMade < 5 && mvcResult.getResponse().getStatus() == HttpStatus.INTERNAL_SERVER_ERROR.value());

        if (requestsMade > 1) {
            System.out.println("generateFormCAndFormGSuccess requests made: " + requestsMade);
        }

        assertThat(mvcResult.getResponse().getStatus(), is(HttpStatus.OK.value()));
        assertThat(objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {}),
            is(objectMapper.readValue(expectedCaseData(), new TypeReference<HashMap<String, Object>>(){})));
    }

    @Test
    public void generateFormCAndFormGServiceError() throws Exception {
        idamServiceStub();
        generateEvidenceUploadServiceSuccessStub();
        generateDocumentServiceSuccessStub(pdfGenerationRequest(config.getFormCNonFastTrackTemplate(request.getCaseDetails())));
        generateDocumentServiceErrorStub(pdfGenerationRequest(config.getFormGTemplate(request.getCaseDetails())));

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .content(objectMapper.writeValueAsString(request))
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE)
            .accept(APPLICATION_JSON_VALUE))
            .andExpect(status().isInternalServerError());
    }

    private void doMissingMustFieldTest(String missingFieldKey) throws Exception {
        CaseDetails caseDetails = request.getCaseDetails();
        caseDetails.getData().put(missingFieldKey, null);

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .content(objectMapper.writeValueAsString(request))
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE)
            .accept(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedErrorData(), true));
    }

    protected PdfDocumentRequest pdfGenerationRequest(String template) {
        return PdfDocumentRequest.builder()
            .accessKey("TESTPDFACCESS")
            .outputName("result.pdf")
            .templateName(template)
            .data(request.getCaseDetails().getData())
            .build();
    }

    private String expectedErrorData() throws JsonProcessingException {
        return objectMapper.writeValueAsString(
            AboutToStartOrSubmitCallbackResponse.builder()
                .errors(ImmutableList.of(REQUIRED_FIELD_EMPTY_ERROR))
                .build());
    }

    private String expectedCaseData() throws JsonProcessingException {
        CaseDetails caseDetails = request.getCaseDetails();
        caseDetails.getData().put("hearingDate", LocalDate.now().plusDays(100));
        caseDetails.getData().put("issueDate", LocalDate.now());
        caseDetails.getData().put("formC", caseDocument());
        caseDetails.getData().put("formG", caseDocument());
        caseDetails.getData().put(OUT_OF_FAMILY_COURT_RESOLUTION, caseDocument());
        caseDetails.getData().put("bulkPrintCoverSheetApp", caseDocument());
        caseDetails.getData().put("bulkPrintCoverSheetRes", caseDocument());

        return objectMapper.writeValueAsString(
            AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDetails.getData())
                .warnings(ImmutableList.of())
                .build());
    }

    private DocumentGenerationRequest documentRequest(String template, String fileName) {
        CaseDetails caseDetails = request.getCaseDetails();
        caseDetails.getData().put("hearingDate", LocalDate.now().plusDays(100));
        caseDetails.getData().put("issueDate", LocalDate.now());
        return DocumentGenerationRequest.builder()
            .template(template)
            .fileName(fileName)
            .values(Collections.singletonMap("caseDetails", caseDetails))
            .build();
    }

    protected BulkPrintRequest bulkPrintRequest() {
        List<BulkPrintDocument> caseDocuments = new ArrayList<>();
        caseDocuments.add(BulkPrintDocument.builder()
            .binaryFileUrl(BINARY_URL)
            .fileName(FILE_NAME)
            .build());
        return BulkPrintRequest.builder()
            .caseId("123")
            .letterType("FINANCIAL_REMEDY_PACK")
            .bulkPrintDocuments(caseDocuments)
            .build();
    }

    private void generateDocumentServiceSuccessStub(PdfDocumentRequest documentRequest) throws JsonProcessingException {
        documentGeneratorService.stubFor(post(urlPathEqualTo(GENERATE_DOCUMENT_CONTEXT_PATH))
            .withRequestBody(equalToJson(objectMapper.writeValueAsString(pdfGenerationRequest(config.getBulkPrintTemplate())), true, true))
            .withHeader(CONTENT_TYPE, equalTo("application/json"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(document()))));

        documentGeneratorService.stubFor(post(urlPathEqualTo(GENERATE_DOCUMENT_CONTEXT_PATH))
            .withRequestBody(equalToJson(objectMapper.writeValueAsString(documentRequest),
                true, true))
            .withHeader(CONTENT_TYPE, equalTo("application/json"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(document()))));

    }

    private void generateEvidenceDownloadServiceSuccessStub() throws JsonProcessingException {
        dmStoreService.stubFor(get(urlMatching("/([a-z1-9]*)/binary"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(document()))));
    }

    private void generateDocumentServiceErrorStub(PdfDocumentRequest documentRequest) throws JsonProcessingException {
        documentGeneratorService.stubFor(post(urlPathEqualTo(GENERATE_DOCUMENT_CONTEXT_PATH))
            .withRequestBody(equalToJson(objectMapper.writeValueAsString(documentRequest),
                true, true))
            .withHeader(CONTENT_TYPE, equalTo("application/json"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));
    }

    private void idamServiceStub() {
        idamService.stubFor(get(urlPathEqualTo(IDAM_SERVICE_CONTEXT_PATH))
            .withHeader(AUTHORIZATION, equalTo(AUTH_TOKEN))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody("{\"roles\": [\"caseworker-divorce-financialremedy-courtadmin\"]}")));
    }

    void generateEvidenceUploadServiceSuccessStub() throws IOException {
        dmStoreService.stubFor(post(urlPathEqualTo(UPLOAD_DOCUMENT_CONTEXT_PATH))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(getResponse()))));
    }

    void generateSendLetterSuccessStub(UUID uuid) throws IOException {
        sendLetterService.stubFor(post(urlPathEqualTo(SEND_LETTER_CONTEXT_PATH))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(new SendLetterResponse(uuid)))));
    }

    void generateConfirmLetterCreatedStub(UUID uuid) throws IOException {

        LetterStatus letterStatus = new LetterStatus(uuid, "Created", "checksum",
            ZonedDateTime.now(), ZonedDateTime.now().plusHours(1),
            ZonedDateTime.now().plusHours(2), Collections.emptyMap(), 1);

        sendLetterService.stubFor(get(urlPathEqualTo(SEND_LETTER_CONTEXT_PATH + "/" + uuid))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(letterStatus))));


    }

    private ObjectNode getResponse() throws IOException {
        final String response = new String(readAllBytes(Paths.get("src/test/resources/fixtures/fileuploadresponseGenerateMiniFormATest.json")));
        return (ObjectNode) new ObjectMapper().readTree(response);
    }
}
