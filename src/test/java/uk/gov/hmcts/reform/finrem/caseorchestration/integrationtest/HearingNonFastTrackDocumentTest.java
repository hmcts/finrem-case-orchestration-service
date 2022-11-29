package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentGenerationRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.document;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FAST_TRACK_DECISION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ISSUE_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.OUT_OF_FAMILY_COURT_RESOLUTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService.DATE_BETWEEN_12_AND_16_WEEKS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService.REQUIRED_FIELD_EMPTY_ERROR;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CaseOrchestrationApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.properties")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Category(IntegrationTest.class)
public class HearingNonFastTrackDocumentTest extends BaseTest {

    private static final String GENERATE_DOCUMENT_CONTEXT_PATH = "/version/1/generate-pdf";
    private static final String GENERATE_BULK_PRINT_CONTEXT_PATH = "/version/1/bulk-print";
    private static final String API_URL = "/case-orchestration/documents/hearing";
    private static final String JSON_CONTENT_PATH = "/fixtures/contested/validate-hearing-withoutfastTrackDecision.json";

    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected MockMvc webClient;
    @Autowired protected DocumentConfiguration config;

    @ClassRule
    public static WireMockClassRule documentGeneratorServiceClass = new WireMockClassRule(4009);
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
        generateDocumentServiceSuccessStub(formCDocumentRequest());
        generateDocumentServiceSuccessStub(formGDocumentRequest());
        generateDocumentServiceSuccessStub(formOutOfFaimilyCourtResolutionDocumentRequest());

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
        generateDocumentServiceSuccessStub(formCDocumentRequest());
        generateDocumentServiceErrorStub(formGDocumentRequest());

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

    private DocumentGenerationRequest formOutOfFaimilyCourtResolutionDocumentRequest() {
        return documentRequest(config.getOutOfFamilyCourtResolutionTemplate(), config.getOutOfFamilyCourtResolutionName());
    }

    private DocumentGenerationRequest formGDocumentRequest() {
        return documentRequest(config.getFormGTemplate(), config.getFormGFileName());
    }

    private DocumentGenerationRequest formCDocumentRequest() {
        return documentRequest(config.getFormCNonFastTrackTemplate(), config.getFormCFileName());
    }

    private DocumentGenerationRequest coverSheetDocumentRequest() {
        return documentRequest(config.getBulkPrintTemplate(), config.getBulkPrintFileName());
    }

    private String expectedErrorData() throws JsonProcessingException {
        return objectMapper.writeValueAsString(
            AboutToStartOrSubmitCallbackResponse.builder()
                .errors(ImmutableList.of(REQUIRED_FIELD_EMPTY_ERROR))
                .build());
    }

    private String expectedCaseData() throws JsonProcessingException {
        CaseDetails caseDetails = request.getCaseDetails();
        caseDetails.getData().put("formC", caseDocument());
        caseDetails.getData().put("formG", caseDocument());
        caseDetails.getData().put(OUT_OF_FAMILY_COURT_RESOLUTION, caseDocument());
        caseDetails.getData().put("bulkPrintCoverSheetApp", caseDocument());
        caseDetails.getData().put("bulkPrintCoverSheetRes", caseDocument());

        return objectMapper.writeValueAsString(
            AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDetails.getData())
                .warnings(ImmutableList.of(DATE_BETWEEN_12_AND_16_WEEKS))
                .build());
    }

    private DocumentGenerationRequest documentRequest(String template, String fileName) {
        return DocumentGenerationRequest.builder()
            .template(template)
            .fileName(fileName)
            .values(Collections.singletonMap("caseDetails", request.getCaseDetails()))
            .build();
    }

    protected BulkPrintRequest bulkPrintRequest() {
        List<BulkPrintDocument> caseDocuments = new ArrayList<>();
        caseDocuments.add(BulkPrintDocument.builder()
            .binaryFileUrl("http://dm-store/lhjbyuivu87y989hijbb/binary")
            .fileName("app_docs.pdf")
            .build());
        return BulkPrintRequest.builder()
            .caseId("123")
            .letterType("FINANCIAL_REMEDY_PACK")
            .bulkPrintDocuments(caseDocuments)
            .build();
    }

    private void generateDocumentServiceSuccessStub(DocumentGenerationRequest documentRequest) throws JsonProcessingException {
        documentGeneratorService.stubFor(post(urlPathEqualTo(GENERATE_DOCUMENT_CONTEXT_PATH))
            .withRequestBody(equalToJson(objectMapper.writeValueAsString(coverSheetDocumentRequest()), true, true))
            .withHeader(AUTHORIZATION, equalTo(AUTH_TOKEN))
            .withHeader(CONTENT_TYPE, equalTo("application/json"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(document()))));

        documentGeneratorService.stubFor(post(urlPathEqualTo(GENERATE_DOCUMENT_CONTEXT_PATH))
            .withRequestBody(equalToJson(objectMapper.writeValueAsString(documentRequest),
                true, true))
            .withHeader(AUTHORIZATION, equalTo(AUTH_TOKEN))
            .withHeader(CONTENT_TYPE, equalTo("application/json"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(document()))));

        documentGeneratorService.stubFor(post(urlPathEqualTo(GENERATE_BULK_PRINT_CONTEXT_PATH))
            .withRequestBody(equalToJson(objectMapper.writeValueAsString(bulkPrintRequest()), true, true))
            .withHeader(CONTENT_TYPE, equalTo("application/json"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(UUID.randomUUID()))));
    }

    private void generateDocumentServiceErrorStub(DocumentGenerationRequest documentRequest) throws JsonProcessingException {
        documentGeneratorService.stubFor(post(urlPathEqualTo(GENERATE_DOCUMENT_CONTEXT_PATH))
            .withRequestBody(equalToJson(objectMapper.writeValueAsString(documentRequest),
                true, true))
            .withHeader(AUTHORIZATION, equalTo(AUTH_TOKEN))
            .withHeader(CONTENT_TYPE, equalTo("application/json"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));
    }
}
