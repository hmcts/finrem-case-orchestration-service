package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentgenerator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDownloadService;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BulkPrintDocumentServiceTest {

    private static final String FILE_URL = "http://dm:80/documents/kbjh87y8y9JHVKKKJVJ";
    private static final String FILE_BINARY_URL = "http://dm:80/documents/kbjh87y8y9JHVKKKJVJ/binary";
    private static final String FILE_NAME = "abc.pdf";
    private static final String NON_PDF_FILE_NAME = "abc.docx";
    public static final String AUTH = "auth";
    private final byte[] someBytes = "ainhsdcnoih".getBytes();
    @InjectMocks
    private BulkPrintDocumentService service;

    @Mock
    private EvidenceManagementDownloadService evidenceManagementService;



    @Test
    void downloadDocuments() {
        when(evidenceManagementService.download(FILE_URL, AUTH)).thenReturn(someBytes);

        BulkPrintRequest bulkPrintRequest = BulkPrintRequest.builder()
            .bulkPrintDocuments(singletonList(BulkPrintDocument.builder()
                .binaryFileUrl(FILE_URL)
                .fileName(FILE_NAME)
                .build()))
            .build();

        List<byte[]> result = service.downloadDocuments(bulkPrintRequest, AUTH);
        assertThat(result.get(0), is(equalTo(someBytes)));
    }


    @Test
    void validateEncryptionOnUploadedDocumentWhenInvalidByteSupplied() throws Exception {
        when(evidenceManagementService.download(FILE_BINARY_URL, AUTH)).thenReturn(someBytes);
        CaseDocument caseDocument = TestSetUpUtils.caseDocument(FILE_URL, FILE_NAME, FILE_BINARY_URL);
        BulkPrintRequest bulkPrintRequest = BulkPrintRequest.builder()
            .bulkPrintDocuments(singletonList(BulkPrintDocument.builder()
                .binaryFileUrl(FILE_BINARY_URL)
                .fileName(FILE_NAME)
                .build()))
            .build();

        List<byte[]> result = service.downloadDocuments(bulkPrintRequest, AUTH);
        assertThat(result.get(0), is(equalTo(someBytes)));

        List<String> errors =  new ArrayList<>();
        service.validateEncryptionOnUploadedDocument(caseDocument, "1234", errors, AUTH);
        assertEquals("Failed to parse the documents for abc.pdf", errors.get(0));
    }

    @Test
    void validateEncryptionOnUploadedDocumentAddErrorOnMessage() throws Exception {
        byte[] bytes = loadResource();
        CaseDocument caseDocument = TestSetUpUtils.caseDocument(FILE_URL, FILE_NAME, FILE_BINARY_URL);

        when(evidenceManagementService.download(FILE_BINARY_URL, AUTH)).thenReturn(bytes);
        List<String> errors =  new ArrayList<>();

        service.validateEncryptionOnUploadedDocument(caseDocument, "1234", errors, AUTH);
        assertEquals("Uploaded document abc.pdf contains encryption. "
            + "Please remove encryption before uploading or upload another document.", errors.get(0));
    }

    @Test
    void ifUploadedDocumentIsNotPdfThenDoNotCheckForEncryption() {
        CaseDocument caseDocument = TestSetUpUtils.caseDocument(FILE_URL, NON_PDF_FILE_NAME, FILE_BINARY_URL);
        List<String> errors =  new ArrayList<>();

        service.validateEncryptionOnUploadedDocument(caseDocument, "1234", errors, AUTH);
        assertEquals(0, errors.size());
    }

    private byte[] loadResource() throws Exception {
        String fixture = "/fixtures/encryptedDocument.pdf";
        try (InputStream resourceAsStream = getClass().getResourceAsStream(fixture)) {
            assert resourceAsStream != null;
            return resourceAsStream.readAllBytes();
        }
    }


}
