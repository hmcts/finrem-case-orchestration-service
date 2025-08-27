package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentgenerator;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CaseOrchestrationApplication.class)
@TestPropertySource(locations = "/application.properties")
public class DocumentConversionServiceTest {

    @Value("${service.pdf-service.convert-uri}")
    private String pdfServiceUri;
    public static final byte[] CONVERTED_BINARY = "converted".getBytes();
    public static final String AUTH = "auth";

    @Autowired
    private DocumentConversionService documentConversionService;

    @Autowired
    private RestTemplate restTemplate;

    @MockitoBean
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
    public void testFlattenPdfDocument() throws IOException {

        byte[] editedPdfBytes = loadResource("/fixtures/D11Edited.pdf");

        // Ensure the original PDF has an AcroForm and at least one form field
        try (PDDocument originalDoc = Loader.loadPDF(editedPdfBytes)) {
            PDAcroForm originalAcroForm = originalDoc.getDocumentCatalog().getAcroForm();
            assertNotNull("Document should have an AcroForm", originalAcroForm);
            assertFalse("Document should have form fields", originalAcroForm.getFields().isEmpty());
        }

        // Flatten the PDF using the method under test
        byte[] flattenedPdfBytes = documentConversionService.flattenPdfDocument(editedPdfBytes);

        // Load the flattened PDF and check that form fields have been removed/flattened
        try (PDDocument flattenedDoc = Loader.loadPDF(flattenedPdfBytes)) {
            PDAcroForm flattenedAcroForm = flattenedDoc.getDocumentCatalog().getAcroForm();
            assertTrue("AcroForm should be flattened", ObjectUtils.isEmpty(flattenedAcroForm.getFields()));
        }
    }

    @Test
    public void testFlattenAnnotations() throws IOException {

        byte[] editedPdfBytes = loadResource("/fixtures/D11Annotations.pdf");

        // Ensure the original PDF has an AcroForm and at least one form field
        try (PDDocument originalDoc = Loader.loadPDF(editedPdfBytes)) {
            PDAcroForm originalAcroForm = originalDoc.getDocumentCatalog().getAcroForm();
            assertNotNull("Document should have an AcroForm", originalAcroForm);
            for (PDPage page : originalDoc.getPages()) {
                List<PDAnnotation> annotations = page.getAnnotations();
                assertFalse("Document should have Annotations", annotations.isEmpty());
            }
        }

        // Flatten the PDF using the method under test
        byte[] flattenedPdfBytes = documentConversionService.flattenPdfDocument(editedPdfBytes);

        // Load the flattened PDF for validation
        try (PDDocument document = Loader.loadPDF(flattenedPdfBytes)) {
            for (PDPage page : document.getPages()) {
                List<PDAnnotation> annotations = page.getAnnotations();
                assertTrue("Annotations were not removed after flattening.", annotations.isEmpty());
            }
        }

        // Load the flattened PDF and check that form fields have been removed/flattened
        try (PDDocument flattenedDoc = Loader.loadPDF(flattenedPdfBytes)) {
            PDAcroForm flattenedAcroForm = flattenedDoc.getDocumentCatalog().getAcroForm();
            assertTrue("AcroForm should be flattened", ObjectUtils.isEmpty(flattenedAcroForm.getFields()));
        }
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
        mockServer.expect(requestTo(pdfServiceUri))
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
        mockServer.expect(requestTo(pdfServiceUri))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess(CONVERTED_BINARY, MediaType.APPLICATION_OCTET_STREAM));

        when(
            evidenceManagementService.download(documentToConvert.getBinaryUrl(), AUTH))
            .thenReturn("bytes".getBytes());

        documentToConvert.setFileName("file.pdf");
        documentConversionService.convertDocumentToPdf(documentToConvert, AUTH);
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
