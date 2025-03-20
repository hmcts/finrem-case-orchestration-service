package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentgenerator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.DocumentConversionService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDownloadService;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BulkPrintDocumentServiceTest {

    private static final String FILE_URL = "http://dm:80/documents/kbjh87y8y9JHVKKKJVJ";
    private static final String FILE_BINARY_URL = "http://dm:80/documents/kbjh87y8y9JHVKKKJVJ/binary";
    private static final String FILE_NAME = "abc.pdf";
    private static final String DOC_FILE_NAME = "abc.docx";
    private static final String XLS_FILE_NAME = "abc.xlsx";
    public static final String AUTH = "auth";
    private final byte[] someBytes = "ainhsdcnoih".getBytes();
    private final byte[] someFlattenedBytes = "ainhsdcnoih_flattened".getBytes();
    @InjectMocks
    private BulkPrintDocumentService service;

    @Mock
    private EvidenceManagementDownloadService evidenceManagementService;
    @Mock
    private DocumentConversionService documentConversionService;


    @Test
    void downloadDocuments() {
        when(evidenceManagementService.download(FILE_URL, AUTH)).thenReturn(someBytes);
        when(documentConversionService.flattenPdfDocument(someBytes)).thenReturn(someFlattenedBytes);

        BulkPrintRequest bulkPrintRequest = BulkPrintRequest.builder()
            .bulkPrintDocuments(singletonList(BulkPrintDocument.builder()
                .binaryFileUrl(FILE_URL)
                .fileName(FILE_NAME)
                .build()))
            .build();

        List<byte[]> result = service.downloadDocuments(bulkPrintRequest, AUTH);
        assertThat(result.get(0), is(equalTo(someFlattenedBytes)));
    }

    @Test
    void validateWordDocumentOnUploadedDocument() {
        Document document = Document.builder().url(FILE_URL)
            .binaryUrl(FILE_BINARY_URL)
            .fileName(DOC_FILE_NAME)
            .build();
        when(documentConversionService.convertDocumentToPdf(document, AUTH)).thenReturn(someBytes);
        CaseDocument caseDocument = TestSetUpUtils.caseDocument(FILE_URL, DOC_FILE_NAME, FILE_BINARY_URL);

        List<String> errors = new ArrayList<>();
        service.validateEncryptionOnUploadedDocument(caseDocument, "1234", errors, AUTH);
        assertTrue(errors.get(0).contains("Failed to parse the documents for abc.docx"));
    }

    @Test
    void validateEncryptionOnUploadedDocumentWhenInvalidByteSupplied() {
        when(evidenceManagementService.download(FILE_BINARY_URL, AUTH)).thenReturn(someBytes);
        when(documentConversionService.flattenPdfDocument(someBytes)).thenReturn(someFlattenedBytes);
        CaseDocument caseDocument = TestSetUpUtils.caseDocument(FILE_URL, FILE_NAME, FILE_BINARY_URL);
        BulkPrintRequest bulkPrintRequest = BulkPrintRequest.builder()
            .bulkPrintDocuments(singletonList(BulkPrintDocument.builder()
                .binaryFileUrl(FILE_BINARY_URL)
                .fileName(FILE_NAME)
                .build()))
            .build();

        List<byte[]> result = service.downloadDocuments(bulkPrintRequest, AUTH);
        assertThat(result.get(0), is(equalTo(someFlattenedBytes)));

        List<String> errors = new ArrayList<>();
        service.validateEncryptionOnUploadedDocument(caseDocument, "1234", errors, AUTH);
        assertEquals("Failed to parse the documents for abc.pdf; Error: End-of-File, expected line at offset 11",
            errors.get(0));
    }

    @Test
    void validateEncryptionOnUploadedDocumentAddErrorOnMessage() throws IOException {
        String fixture = "/fixtures/encryptedDocument.pdf";
        byte[] bytes = loadResource(fixture);
        CaseDocument caseDocument = TestSetUpUtils.caseDocument(FILE_URL, FILE_NAME, FILE_BINARY_URL);

        when(evidenceManagementService.download(FILE_BINARY_URL, AUTH)).thenReturn(bytes);
        List<String> errors = new ArrayList<>();

        service.validateEncryptionOnUploadedDocument(caseDocument, "1234", errors, AUTH);
        assertEquals("Uploaded document 'abc.pdf' contains some kind of encryption. "
            + "Please remove encryption before uploading or upload another document.", errors.get(0));
    }

    @Test
    void validatePasswordProtectedDocumentUploadedThenThrowPasswordProtectedMessage() throws IOException {
        String fixture = "/fixtures/go1protected.pdf";
        byte[] bytes = loadResource(fixture);
        CaseDocument caseDocument = TestSetUpUtils.caseDocument(FILE_URL, FILE_NAME, FILE_BINARY_URL);

        when(evidenceManagementService.download(FILE_BINARY_URL, AUTH)).thenReturn(bytes);
        List<String> errors = new ArrayList<>();

        service.validateEncryptionOnUploadedDocument(caseDocument, "1234", errors, AUTH);
        assertEquals("Uploaded document 'abc.pdf' is password protected. "
            + "Please remove password and try uploading again.", errors.get(0));
    }

    @Test
    void validateEmptyUploadedFileThenDisplayMessage() {
        byte[] bytes = null;
        CaseDocument caseDocument = TestSetUpUtils.caseDocument(FILE_URL, FILE_NAME, FILE_BINARY_URL);

        when(evidenceManagementService.download(FILE_BINARY_URL, AUTH)).thenReturn(bytes);
        List<String> errors = new ArrayList<>();

        service.validateEncryptionOnUploadedDocument(caseDocument, "1234", errors, AUTH);
        assertEquals("Uploaded document abc.pdf is empty.", errors.get(0));
    }

    @Test
    void validateEncryptionOnUploadedDocumentWhenXlsFile() {
        CaseDocument caseDocument = TestSetUpUtils.caseDocument(FILE_URL, XLS_FILE_NAME, FILE_BINARY_URL);

        List<String> errors = new ArrayList<>();
        service.validateEncryptionOnUploadedDocument(caseDocument, "1234", errors, AUTH);
        assertThat(errors).isEmpty();
    }

    @Test
    void validateEncryptionOnUploadedDocumentWhenPdfFile() throws IOException {
        String fixture = "/fixtures/general-application.pdf";
        byte[] bytes = loadResource(fixture);

        CaseDocument caseDocument = CaseDocument.builder()
            .documentUrl(FILE_URL)
            .documentBinaryUrl(FILE_BINARY_URL)
            .documentFilename(fixture)
            .build();

        when(evidenceManagementService.download(FILE_BINARY_URL, AUTH)).thenReturn(bytes);

        List<String> errors = new ArrayList<>();
        service.validateEncryptionOnUploadedDocument(caseDocument, "1234", errors, AUTH);

        verify(evidenceManagementService).download(FILE_BINARY_URL, AUTH);
        assertThat(errors).isEmpty();
    }

    @Test
    void validateEncryptionOnUploadedDocumentWhenOtherFile() {
        CaseDocument caseDocument = CaseDocument.builder()
            .documentUrl(FILE_URL)
            .documentBinaryUrl(FILE_BINARY_URL)
            .documentFilename("abc.png")
            .build();

        List<String> errors = new ArrayList<>();
        service.validateEncryptionOnUploadedDocument(caseDocument, "1234", errors, AUTH);
        assertThat(errors).isEmpty();
    }

    private byte[] loadResource(String testPdf) throws IOException {

        try (InputStream resourceAsStream = getClass().getResourceAsStream(testPdf)) {
            assert resourceAsStream != null;
            return resourceAsStream.readAllBytes();
        }
    }

    @ParameterizedTest
    @CsvSource({
        "test.doc",
        "test.DOC",
        "test.docx",
        "test.DOCX",
        "test.pdf",
        "test.PDF"
    })
    void validateEncryptionOnUploadedDocumentGivenMixedFilenameExtensions(String filename) {

        CaseDocument caseDocument = TestSetUpUtils.caseDocument(FILE_URL, filename, FILE_BINARY_URL);
        Document document = Document.builder().url(FILE_URL)
            .binaryUrl(FILE_BINARY_URL)
            .fileName(filename)
            .build();

        if (filename.toLowerCase().endsWith(".doc") || filename.toLowerCase().endsWith(".docx")) {
            when(documentConversionService.convertDocumentToPdf(document, AUTH)).thenReturn(someBytes);
        } else {
            when(evidenceManagementService.download(FILE_BINARY_URL, AUTH)).thenReturn(someBytes);
        }

        List<String> errors = new ArrayList<>();
        service.validateEncryptionOnUploadedDocument(caseDocument, "1234", errors, AUTH);

        if (filename.toLowerCase().endsWith(".doc") || filename.toLowerCase().endsWith(".docx")) {
            verify(documentConversionService).convertDocumentToPdf(document, AUTH);
        } else {
            verify(evidenceManagementService).download(caseDocument.getDocumentBinaryUrl(), AUTH);
        }
    }
}
