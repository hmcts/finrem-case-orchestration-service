package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentGenerationRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.util.Collections.emptyList;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService.DATE_BETWEEN_12_AND_16_WEEKS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService.REQUIRED_FIELD_EMPTY_ERROR;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CaseOrchestrationApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.properties")
@AutoConfigureMockMvc
@Category(IntegrationTest.class)
public class HearingNonFastTrackDocumentTest {

    private static final String SUBMIT_SCHEDULING_AND_LISTING_DETAILS = "/case-orchestration/submit-scheduling-and-listing-details";
    private static final String VALIDATE_HEARING =  "/case-orchestration/validate-hearing";

    private static final String JSON_CONTENT_PATH = "/fixtures/contested/validate-hearing-withoutfastTrackDecision.json";
    private static final String GENERATE_DOCUMENT_CONTEXT_PATH = "/version/1/generate-pdf";

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected MockMvc webClient;

    @Autowired
    protected DocumentConfiguration config;

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
    public void whenMissingIssueDateOrHearingDateOrFastTrackDecision_thenErrorIsReturned() throws Exception {
        Stream.of(ISSUE_DATE, HEARING_DATE, FAST_TRACK_DECISION)
            .forEach(this::validateRequestWithMissingField);
    }

    @Test
    public void whenValidatingHearingData_ExpectWarningForHearingDate() throws Exception {
        webClient.perform(MockMvcRequestBuilders.post(VALIDATE_HEARING)
            .content(objectMapper.writeValueAsString(request))
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE)
            .accept(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedValidateHearingResponseData(emptyList(), ImmutableList.of(DATE_BETWEEN_12_AND_16_WEEKS)), true));
    }

    @Test
    public void generateFormCAndFormGSuccess() throws Exception {
        generateDocumentServiceSuccessStub(formCDocumentRequest());
        generateDocumentServiceSuccessStub(formGDocumentRequest());

        MvcResult mvcResult = webClient.perform(MockMvcRequestBuilders.post(SUBMIT_SCHEDULING_AND_LISTING_DETAILS)
            .content(objectMapper.writeValueAsString(request))
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE)
            .accept(APPLICATION_JSON_VALUE))
            .andReturn();

        assertThat(mvcResult.getResponse().getStatus(), is(HttpStatus.OK.value()));
        assertThat(mvcResult.getResponse().getContentAsString(), is(expectedCaseData()));
    }

    @Test
    public void generateFormCAndFormGServiceError() throws Exception {
        generateDocumentServiceSuccessStub(formCDocumentRequest());
        generateDocumentServiceErrorStub(formGDocumentRequest());

        webClient.perform(MockMvcRequestBuilders.post(SUBMIT_SCHEDULING_AND_LISTING_DETAILS)
            .content(objectMapper.writeValueAsString(request))
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE)
            .accept(APPLICATION_JSON_VALUE))
            .andExpect(status().isInternalServerError());
    }

    private void validateRequestWithMissingField(String missingFieldKey) {
        CaseDetails caseDetails = request.getCaseDetails();
        caseDetails.getData().put(missingFieldKey, null);

        try {
            webClient.perform(MockMvcRequestBuilders.post(VALIDATE_HEARING)
                .content(objectMapper.writeValueAsString(request))
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedValidateHearingResponseData(ImmutableList.of(REQUIRED_FIELD_EMPTY_ERROR), emptyList()), true));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private DocumentGenerationRequest formGDocumentRequest() {
        return documentRequest(config.getFormGTemplate(), config.getFormGFileName());
    }

    private DocumentGenerationRequest formCDocumentRequest() {
        return documentRequest(config.getFormCNonFastTrackTemplate(), config.getFormCFileName());
    }

    private String expectedValidateHearingResponseData(List<String> errors, List<String> warnings) throws JsonProcessingException {
        return objectMapper.writeValueAsString(
            AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors)
                .warnings(warnings)
                .build());
    }

    private String expectedCaseData() throws JsonProcessingException {
        CaseDetails caseDetails = request.getCaseDetails();
        caseDetails.getData().put("formC", caseDocument());
        caseDetails.getData().put("formG", caseDocument());

        return objectMapper.writeValueAsString(
            AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDetails.getData())
                .build());
    }

    private DocumentGenerationRequest documentRequest(String template, String fileName) {
        return DocumentGenerationRequest.builder()
            .template(template)
            .fileName(fileName)
            .values(Collections.singletonMap("caseDetails", request.getCaseDetails()))
            .build();
    }

    private void generateDocumentServiceSuccessStub(DocumentGenerationRequest documentRequest) throws JsonProcessingException {
        documentGeneratorService.stubFor(post(urlPathEqualTo(GENERATE_DOCUMENT_CONTEXT_PATH))
            .withRequestBody(equalToJson(objectMapper.writeValueAsString(documentRequest),
                true, true))
            .withHeader(AUTHORIZATION, equalTo(AUTH_TOKEN))
            .withHeader(CONTENT_TYPE, equalTo("application/json"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(document()))));
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
