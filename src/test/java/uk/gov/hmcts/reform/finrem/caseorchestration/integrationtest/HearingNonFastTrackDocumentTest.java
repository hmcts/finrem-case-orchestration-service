package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.google.common.collect.ImmutableList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultHandler;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentGenerationRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.document;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FAST_TRACK_DECISION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ISSUE_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService.DATE_BETWEEN_12_AND_16_WEEKS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService.MUST_FIELD_ERROR;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CaseOrchestrationApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.properties")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Category(IntegrationTest.class)
public class HearingNonFastTrackDocumentTest {

    private static final String GENERATE_DOCUMENT_CONTEXT_PATH = "/version/1/generate-pdf";
    private static final String API_URL = "/case-orchestration/documents/hearing";

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected MockMvc webClient;

    @Autowired
    protected DocumentConfiguration config;

    @ClassRule
    public static WireMockClassRule documentGeneratorService = new WireMockClassRule(4009);

    protected static CallbackRequest request;

    @BeforeClass
    public static void startWiremock() {
        documentGeneratorService.start();
        do {
            System.out.println("Wiremock is running: " + documentGeneratorService.isRunning());
            System.out.println(new Date());
        } while (!documentGeneratorService.isRunning());
    }

    @Before
    public void setUp() throws IOException {
        if (request == null) {
            try (InputStream resourceAsStream = getClass().getResourceAsStream(jsonContent())) {
                request = objectMapper.readValue(resourceAsStream, CallbackRequest.class);
            }
        }
    }

    private String jsonContent() {
        return "/fixtures/contested/validate-hearing-withoutfastTrackDecision.json";
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

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
                .content(objectMapper.writeValueAsString(request))
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedCaseData(), true));
    }

    @Test
    public void generateFormCAndFormGServiceError() throws Exception {
        generateDocumentServiceSuccessStub(formCDocumentRequest());
        generateDocumentServiceErrorStub(formGDocumentRequest());

        System.out.println("Performing generateFormCAndFormGServiceError request");

        MvcResult mvcResult = webClient.perform(MockMvcRequestBuilders.post(API_URL)
                .content(objectMapper.writeValueAsString(request))
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andReturn();
        int status = mvcResult.getResponse().getStatus();
        String body = mvcResult.getResponse().getContentAsString();
        System.out.println(String.format("Status: %d, body: %s", status, body));
        Assert.fail();
//                .andExpect(status().isInternalServerError());
    }

    private void doMissingMustFieldTest(String missingFieldKey) throws Exception {
        CaseDetails caseDetails = request.getCaseDetails();
        caseDetails.getData().put(missingFieldKey, null);

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
                .content(objectMapper.writeValueAsString(request))
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedErrorData(), true));
    }

    private DocumentGenerationRequest formGDocumentRequest() {
        return documentRequest(config.getFormGTemplate(), config.getFormGFileName());
    }

    private DocumentGenerationRequest formCDocumentRequest() {
        return documentRequest(config.getFormCNonFastTrackTemplate(), config.getFormCFileName());
    }

    private String expectedErrorData() throws JsonProcessingException {
        return objectMapper.writeValueAsString(
                AboutToStartOrSubmitCallbackResponse.builder()
                        .errors(ImmutableList.of(MUST_FIELD_ERROR))
                        .build());
    }


    private String expectedCaseData() throws JsonProcessingException {
        CaseDetails caseDetails = request.getCaseDetails();
        caseDetails.getData().put("formC", caseDocument());
        caseDetails.getData().put("formG", caseDocument());

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

    private void generateDocumentServiceSuccessStub(DocumentGenerationRequest documentRequest)
            throws JsonProcessingException {
        documentGeneratorService.stubFor(post(urlPathEqualTo(GENERATE_DOCUMENT_CONTEXT_PATH))
                .withRequestBody(equalToJson(objectMapper.writeValueAsString(documentRequest),
                        true, true))
                .withHeader(AUTHORIZATION, equalTo(AUTH_TOKEN))
                .withHeader(CONTENT_TYPE, equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                        .withBody(objectMapper.writeValueAsString(document()))));
    }

    private void generateDocumentServiceErrorStub(DocumentGenerationRequest documentRequest)
            throws JsonProcessingException {
        documentGeneratorService.stubFor(post(urlPathEqualTo(GENERATE_DOCUMENT_CONTEXT_PATH))
                .withRequestBody(equalToJson(objectMapper.writeValueAsString(documentRequest),
                        true, true))
                .withHeader(AUTHORIZATION, equalTo(AUTH_TOKEN))
                .withHeader(CONTENT_TYPE, equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)));
    }
}
