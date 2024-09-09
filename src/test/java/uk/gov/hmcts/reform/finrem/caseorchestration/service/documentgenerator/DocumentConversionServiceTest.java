package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentgenerator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.DocumentConversionException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.DocumentConversionService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDownloadService;

import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CaseOrchestrationApplication.class)
@TestPropertySource(locations = "/application.properties")
public class DocumentConversionServiceTest {

    public static final String PDF_SERVICE_URI = "http://localhost:4001/rs/convert";
    public static final byte[] CONVERTED_BINARY = "converted".getBytes();
    public static final String AUTH = "auth";

    @Autowired
    private DocumentConversionService documentConversionService;

    @Autowired
    private RestTemplate restTemplate;

    @MockBean
    private EvidenceManagementDownloadService evidenceManagementService;

    private MockRestServiceServer mockServer;

    private Document documentToConvert;

    @Before
    public void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
        documentToConvert = new Document();
        documentToConvert.setFileName("file.docx");
        documentToConvert.setUrl("docUrl.com");
        documentToConvert.setBinaryUrl("binaryUrl.com");
    }

    @Test
    public void flattenPdfDocument() throws IOException {

        // Note: Already flat PDFs are unchanged by flattening (see flattenFlattenedD11.pdf)
        String editedPdfFixture = "/fixtures/D11Edited.pdf";
        byte[] editedPdfBytes = loadResource(editedPdfFixture);

        String flatPdfFixture = "/fixtures/D11Edited-flattened.pdf";
        byte[] expectedFlatPdfBytes = loadResource(flatPdfFixture);

        byte[] result = documentConversionService.flattenPdfDocument(editedPdfBytes);

        assertThat(expectedFlatPdfBytes, is(result));
    }

    @Test
    public void doNotFlattenPdfDocumentWithNoFromLayer() throws IOException {

        String editedPdfFixture = "/fixtures/D81_consent_order.pdf";
        byte[] pdfBytes = loadResource(editedPdfFixture);
        byte[] result = documentConversionService.flattenPdfDocument(pdfBytes);

        assertThat(pdfBytes, is(result));
    }

    @Test
    public void flattenNonPdfDocumentHandleException() throws IOException {

        String toBeFlattenedFile = "/fixtures/MockD11Word.docx";
        byte[] toBeFlattenedbytes = loadResource(toBeFlattenedFile);

        byte[] result = documentConversionService.flattenPdfDocument(toBeFlattenedbytes);

        assertThat(toBeFlattenedbytes, is(result));
    }

    @Test
    public void convertWordToPdf() {
        mockServer.expect(requestTo(PDF_SERVICE_URI))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess(CONVERTED_BINARY, MediaType.APPLICATION_OCTET_STREAM));

        when(
            evidenceManagementService.download(documentToConvert.getBinaryUrl(), AUTH))
            .thenReturn("bytes".getBytes());

        byte[] result = documentConversionService.convertDocumentToPdf(documentToConvert, AUTH);
        assertThat(result, is(notNullValue()));
        assertThat(result, is(CONVERTED_BINARY));
    }

    @Test(expected = DocumentConversionException.class)
    public void convertWordToPdfFailsWhenAlreadyPdf() throws Exception {
        mockServer.expect(requestTo(PDF_SERVICE_URI))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess(CONVERTED_BINARY, MediaType.APPLICATION_OCTET_STREAM));

        when(
            evidenceManagementService.download(documentToConvert.getBinaryUrl(), AUTH))
            .thenReturn("bytes".getBytes());

        documentToConvert.setFileName("file.pdf");
        byte[] result = documentConversionService.convertDocumentToPdf(documentToConvert, AUTH);
    }

    @Test
    public void getConvertedFilename() {
        assertThat(documentConversionService.getConvertedFilename("nodot"), is("nodot.pdf"));
        assertThat(documentConversionService.getConvertedFilename("word.docx"), is("word.pdf"));
    }

    private byte[] loadResource(String testPdf) throws IOException {

        try (InputStream resourceAsStream = getClass().getResourceAsStream(testPdf)) {
            assert resourceAsStream != null;
            return resourceAsStream.readAllBytes();
        }
    }
}
