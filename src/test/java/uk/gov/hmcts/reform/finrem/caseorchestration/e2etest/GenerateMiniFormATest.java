package uk.gov.hmcts.reform.finrem.caseorchestration.e2etest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.document;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CaseOrchestrationApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.properties")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class GenerateMiniFormATest {

    private static final String AUTH_TOKEN = "LIUGBYUFUYVbnvhchvUFJHVJBlBJHBUYCUV";
    private static final String API_URL = "/case-orchestration/generate-mini-form-a";
    private static final String GENERATE_DOCUMENT_CONTEXT_PATH = "/version/1/generatePDF";
    private static final String DELETE_DOCUMENT_CONTEXT_PATH = "/version/1/delete-pdf-document";
    public static final String TEMP_URL = "url";
    public static final String TEMP_URL_BINARY = "url_binary";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc webClient;

    @Value("${document.miniFormA.template}")
    private String miniFormATemplate;

    @ClassRule
    public static WireMockClassRule documentGeneratorService = new WireMockClassRule(4009);

    private CCDRequest request;

    @Before
    public void setUp() throws IOException {
        try (InputStream resourceAsStream = getClass().getResourceAsStream("/fixtures/fee-lookup.json")) {
            request = objectMapper.readValue(resourceAsStream, CCDRequest.class);
        }
    }

    @Test
    public void generateMiniFormA() throws Exception {
        generateDocumentServiceSuccessStub();

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
                .content(objectMapper.writeValueAsString(request))
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedCaseData()));
    }

    @Test
    public void documentGeneratorServiceError() throws Exception {
        generateDocumentServiceErrorStub();

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
                .content(objectMapper.writeValueAsString(request))
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void deleteExistingMiniFormAWithSuccess() throws Exception {
        doTestDeleteMiniFormA(HttpStatus.OK);
    }

    @Test
    public void deleteMiniFormAErrorShouldNotAffectNewMiniFormGeneration() throws Exception {
        doTestDeleteMiniFormA(HttpStatus.NOT_FOUND);
    }

    private void doTestDeleteMiniFormA(HttpStatus miniFormAServiceStatus) throws Exception {
        CaseDetails caseDetails = request.getCaseDetails();
        caseDetails.getCaseData().setMiniFormA(caseDocument(TEMP_URL, TEMP_URL_BINARY));

        generateDocumentServiceSuccessStub();
        deleteDocumentServiceStubWith(miniFormAServiceStatus);

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
                .content(objectMapper.writeValueAsString(request))
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedCaseData()));
    }

    private String expectedCaseData() throws JsonProcessingException {
        CaseDetails caseDetails = request.getCaseDetails();
        caseDetails.getCaseData().setMiniFormA(caseDocument());

        return objectMapper.writeValueAsString(new CCDCallbackResponse(caseDetails.getCaseData(),
                new ArrayList<>(), new ArrayList<>()));
    }

    private DocumentRequest documentRequest() {
        return DocumentRequest.builder()
                .template(miniFormATemplate)
                .values(Collections.singletonMap("caseDetails", request.getCaseDetails()))
                .build();
    }

    private void deleteDocumentServiceStubWith(HttpStatus status) {
        documentGeneratorService.stubFor(
                delete(urlMatching(DELETE_DOCUMENT_CONTEXT_PATH.concat("\\?fileUrl=").concat(TEMP_URL_BINARY)))
                .withHeader(AUTHORIZATION, equalTo(AUTH_TOKEN))
                .willReturn(aResponse().withStatus(status.value())));
    }

    private void generateDocumentServiceSuccessStub() throws JsonProcessingException {
        documentGeneratorService.stubFor(post(urlPathEqualTo(GENERATE_DOCUMENT_CONTEXT_PATH))
                .withRequestBody(equalToJson(objectMapper.writeValueAsString(documentRequest()), true, true))
                .withHeader(AUTHORIZATION, equalTo(AUTH_TOKEN))
                .withHeader(CONTENT_TYPE, equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                        .withBody(objectMapper.writeValueAsString(document()))));
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
}
