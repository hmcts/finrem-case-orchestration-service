package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
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
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.PdfDocumentRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocument;
import uk.gov.hmcts.reform.sendletter.api.LetterStatus;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.lang.Thread.sleep;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.document;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_CONFIDENTIAL_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_C;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_G;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_ADDITIONAL_DOC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_ADDITIONAL_INFO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MINI_FORM_A;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.OUT_OF_FAMILY_COURT_RESOLUTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_CONFIDENTIAL_ADDRESS;

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
    protected static final String UPLOAD_DOCUMENT_CONTEXT_PATH = "/cases/documents";
    private static final String IDAM_SERVICE_USER_INFO = "/o/userinfo";
    protected static final String SEND_LETTER_CONTEXT_PATH = "/letters";
    private static final String SUBMITTED_HANDLER_URL = "/case-orchestration/ccdSubmittedEvent";
    private static final String ABOUTTOSUBMIT_HANDLER_URL = "/case-orchestration/ccdAboutToSubmitEvent";

    private static final String JSON_CONTENT_PATH = "/fixtures/contested/validate-hearing-withoutfastTrackDecision.json";

    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected MockMvc webClient;
    @Autowired
    protected DocumentConfiguration config;

    @ClassRule
    public static WireMockClassRule documentGeneratorServiceClass = new WireMockClassRule(4001);

    @ClassRule
    public static WireMockClassRule sendLetterService = new WireMockClassRule(4002);

    @ClassRule
    public static WireMockClassRule dmStoreService = new WireMockClassRule(4455);

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
        Map<String, Object> data = request.getCaseDetails().getData();
        data.put(FORM_C, caseDocument());
        data.put(FORM_G, caseDocument());
        data.put(MINI_FORM_A, caseDocument());
        data.put(OUT_OF_FAMILY_COURT_RESOLUTION, caseDocument());
        data.put(HEARING_ADDITIONAL_DOC, caseDocument());

        MvcResult mvcResult;
        int requestsMade = 0;
        do {
            if (requestsMade > 0) {
                sleep(100);
            }

            mvcResult = webClient.perform(MockMvcRequestBuilders.post(SUBMITTED_HANDLER_URL)
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
        HashMap<String, Object> actual = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {
        });
        HashMap<String, Object> expected = objectMapper.readValue(expectedCaseData(), new TypeReference<>() {
        });

        assertThat(objectMapper.readTree(stringify(actual)), is(objectMapper.readTree(stringify(expected))));

    }

    @Test
    public void populateBulkPrintFieldSuccess() throws Exception {
        idamServiceStub();
        generateEvidenceUploadServiceSuccessStub();
        generateDocumentServiceSuccessStub(pdfGenerationRequest(config.getFormCNonFastTrackTemplate(request.getCaseDetails())));
        generateDocumentServiceSuccessStub(pdfGenerationRequest(config.getFormGTemplate(request.getCaseDetails())));
        Map<String, Object> data = request.getCaseDetails().getData();
        data.put(FORM_C, caseDocument());
        data.put(FORM_G, caseDocument());
        data.put(MINI_FORM_A, caseDocument());
        data.put(HEARING_ADDITIONAL_DOC, caseDocument());
        data.put(APPLICANT_CONFIDENTIAL_ADDRESS, "No");
        data.put(RESPONDENT_CONFIDENTIAL_ADDRESS, "No");
        data.put(HEARING_ADDITIONAL_INFO, "Yes");

        MvcResult mvcResult;
        int requestsMade = 0;
        do {
            if (requestsMade > 0) {
                sleep(100);
            }

            mvcResult = webClient.perform(MockMvcRequestBuilders.post(ABOUTTOSUBMIT_HANDLER_URL)
                            .content(objectMapper.writeValueAsString(request))
                            .header(AUTHORIZATION, AUTH_TOKEN)
                            .contentType(APPLICATION_JSON_VALUE)
                            .accept(APPLICATION_JSON_VALUE))
                    .andReturn();
        } while (++requestsMade < 5 && mvcResult.getResponse().getStatus() == HttpStatus.INTERNAL_SERVER_ERROR.value());

        if (requestsMade > 1) {
            System.out.println("populateBulkPrintFieldsSuccess requests made: " + requestsMade);
        }

        assertThat(mvcResult.getResponse().getStatus(), is(HttpStatus.OK.value()));
        HashMap<String, Object> actual = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {
        });
        HashMap<String, Object> expected = objectMapper.readValue(aboutToSubmitExpectedCaseData(), new TypeReference<>() {
        });

        assertThat(objectMapper.readTree(stringify(actual)), is(objectMapper.readTree(stringify(expected))));
    }

    protected PdfDocumentRequest pdfGenerationRequest(String template) {
        return PdfDocumentRequest.builder()
            .accessKey("TESTPDFACCESS")
            .outputName("result.pdf")
            .templateName(template)
            .data(request.getCaseDetails().getData())
            .build();
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
//        List<AdditionalHearingDocumentCollection> collection = List.of(AdditionalHearingDocumentCollection.builder()
//            .value(AdditionalHearingDocument.builder().document(caseDocument()).build()).build());
//        caseDetails.getData().put("additionalHearingDocuments", collection);

        return objectMapper.writeValueAsString(
            AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDetails.getData())
                .warnings(ImmutableList.of())
                .errors(ImmutableList.of())
                .build());
    }

    private String aboutToSubmitExpectedCaseData() throws JsonProcessingException {
        CaseDetails caseDetails = request.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();
        caseData.put("hearingDate", LocalDate.now().plusDays(100));
        caseData.put("issueDate", LocalDate.now());
        caseData.put("formC", caseDocument());
        caseData.put("formG", caseDocument());
        caseData.put("bulkPrintCoverSheetApp", caseDocument());
        caseData.put("bulkPrintCoverSheetRes", caseDocument());
        caseData.put("additionalListOfHearingDocuments", caseDocument());
        List<AdditionalHearingDocumentCollection> collection = List.of(AdditionalHearingDocumentCollection.builder()
            .value(AdditionalHearingDocument.builder().document(caseDocument()).build()).build());
        caseData.put("additionalHearingDocuments", collection);

        return objectMapper.writeValueAsString(
                AboutToStartOrSubmitCallbackResponse.builder()
                        .data(caseDetails.getData())
                        .warnings(ImmutableList.of())
                        .errors(ImmutableList.of())
                        .build());
    }

    private void generateDocumentServiceSuccessStub(PdfDocumentRequest documentRequest) throws JsonProcessingException {
        documentGeneratorService.stubFor(post(urlPathEqualTo(GENERATE_DOCUMENT_CONTEXT_PATH))
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
        dmStoreService.stubFor(get(urlMatching("\\/cases\\/documents\\/(.*?)\\/binary"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(getResponse()))));
    }

    private void idamServiceStub() {
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
        links.binary.href =
            "http://dm-store:8080/documents/d607c045-878e-475f-ab8e-b2f667d8af64/binary";
        links.self.href =
            "http://dm-store:8080/documents/d607c045-878e-475f-ab8e-b2f667d8af64";
        return links;
    }

    private String stringify(Object object) throws Exception {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        return ow.writeValueAsString(object);
    }

}
