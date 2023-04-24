package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.PdfDocumentRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.OUT_OF_FAMILY_COURT_RESOLUTION;

public class HearingFastTrackDocumentTest extends AbstractDocumentTest {

    private static final String API_URL = "/case-orchestration/documents/hearing";
    private static final String GENERATE_BULK_PRINT_CONTEXT_PATH = "/version/1/bulk-print";

    @Override
    protected PdfDocumentRequest pdfRequest() {
        return PdfDocumentRequest.builder()
            .accessKey("TESTPDFACCESS")
            .outputName("result.pdf")
            .templateName(documentConfiguration.getFormCFastTrackTemplate(CaseDetails.builder().build()))
            .data(request.getCaseDetails().getData())
            .build();
    }

    protected PdfDocumentRequest pdfGenerationCoverSheetRequest() {
        return PdfDocumentRequest.builder()
            .accessKey("TESTPDFACCESS")
            .outputName("result.pdf")
            .templateName(documentConfiguration.getBulkPrintTemplate())
            .data(request.getCaseDetails().getData())
            .build();
    }

    protected PdfDocumentRequest documentOfcrRequest() {
        return PdfDocumentRequest.builder()
            .accessKey("TESTPDFACCESS")
            .outputName("result.pdf")
            .templateName(documentConfiguration.getOutOfFamilyCourtResolutionTemplate())
            .data(request.getCaseDetails().getData())
            .build();
    }

    protected BulkPrintRequest bulkPrintRequest() {
        List<BulkPrintDocument> caseDocuments = new ArrayList<>();
        caseDocuments.add(BulkPrintDocument.builder()
            .binaryFileUrl(BINARY_URL).fileName(FILE_NAME).build());
        return BulkPrintRequest.builder()
            .caseId("123")
            .letterType("FINANCIAL_REMEDY_PACK")
            .bulkPrintDocuments(caseDocuments)
            .build();
    }

    @Override
    protected String apiUrl() {
        return API_URL;
    }

    @Override
    protected String getTestFixture() {
        return "/fixtures/contested/validate-hearing-with-fastTrackDecision.json";
    }

    @ClassRule
    public static final WireMockClassRule coverSheetGeneratorService = new WireMockClassRule(4010);

    void generateDocumentServiceSuccessStubWithCoverSheet() throws JsonProcessingException {

        documentGeneratorService.stubFor(post(urlPathEqualTo(GENERATE_DOCUMENT_CONTEXT_PATH))
            .withRequestBody(equalToJson(objectMapper.writeValueAsString(pdfGenerationCoverSheetRequest()), true, true))
            .withHeader(CONTENT_TYPE, equalTo("application/json"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(document()))));

        documentGeneratorService.stubFor(post(urlPathEqualTo(GENERATE_DOCUMENT_CONTEXT_PATH))
            .withRequestBody(equalToJson(objectMapper.writeValueAsString(pdfRequest()), true, true))
            .withHeader(CONTENT_TYPE, equalTo("application/json"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(document()))));

        documentGeneratorBulkPrintService.stubFor(post(urlPathEqualTo(GENERATE_BULK_PRINT_CONTEXT_PATH))
            .withRequestBody(equalToJson(objectMapper.writeValueAsString(bulkPrintRequest()), true, true))
            .withHeader(CONTENT_TYPE, equalTo("application/json"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(UUID.randomUUID()))));

        documentGeneratorService.stubFor(post(urlPathEqualTo(GENERATE_DOCUMENT_CONTEXT_PATH))
            .withRequestBody(equalToJson(objectMapper.writeValueAsString(documentOfcrRequest()), true, true))
            .withHeader(CONTENT_TYPE, equalTo("application/json"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(document()))));
    }

    @Test
    public void generateFormC() throws Exception {
        idamServiceStub();
        generateEvidenceUploadServiceSuccessStub();
        generateDocumentServiceSuccessStubWithCoverSheet();
        downloadDocumentServiceStubWith(HttpStatus.OK);
        generateSendLetterServiceStub();

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .content(objectMapper.writeValueAsString(request))
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedCaseData()));
    }

    private String expectedCaseData() throws JsonProcessingException {
        CaseDetails caseDetails = request.getCaseDetails();
        caseDetails.getData().put("formC", caseDocument());
        caseDetails.getData().put(OUT_OF_FAMILY_COURT_RESOLUTION, caseDocument());

        return objectMapper.writeValueAsString(
            AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDetails.getData())
                .warnings(new ArrayList<>())
                .build());
    }
}
