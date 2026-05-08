package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadDocumentTest {

    @Test
    void shouldMapAllFieldsFromScannedDocumentCollection() {
        LocalDateTime scannedDate = LocalDateTime.of(2024, 10, 21, 11, 30, 45);
        LocalDateTime uploadTimestamp = LocalDateTime.of(2024, 10, 20, 9, 15, 10);

        ScannedDocumentCollection scannedDocumentCollection = mock(ScannedDocumentCollection.class);
        ScannedDocument scannedDocument = mock(ScannedDocument.class);
        CaseDocument sourceCaseDocument = mock(CaseDocument.class);

        when(scannedDocumentCollection.getValue()).thenReturn(scannedDocument);
        when(scannedDocument.getFileName()).thenReturn("scanned-document.pdf");
        when(scannedDocument.getScannedDate()).thenReturn(scannedDate);
        when(scannedDocument.getUrl()).thenReturn(sourceCaseDocument);
        when(scannedDocument.getExceptionRecordReference()).thenReturn("EXC-12345");

        when(sourceCaseDocument.getDocumentUrl()).thenReturn("https://doc.example.com");
        when(sourceCaseDocument.getDocumentBinaryUrl()).thenReturn("https://doc.example.com/binary");
        when(sourceCaseDocument.getDocumentFilename()).thenReturn("stored-document.pdf");
        when(sourceCaseDocument.getUploadTimestamp()).thenReturn(uploadTimestamp);

        UploadCaseDocument result = UploadCaseDocument.from(scannedDocumentCollection);

        assertAll(
            () -> assertNotNull(result),
            () -> assertEquals("scanned-document.pdf", result.getFileName()),
            () -> assertEquals(scannedDate, result.getScannedDate()),
            () -> assertEquals("EXC-12345", result.getExceptionRecordReference()),

            () -> assertNotNull(result.getCaseDocuments()),
            () -> assertNotSame(sourceCaseDocument, result.getCaseDocuments()),
            () -> assertEquals("https://doc.example.com", result.getCaseDocuments().getDocumentUrl()),
            () -> assertEquals("https://doc.example.com/binary", result.getCaseDocuments().getDocumentBinaryUrl()),
            () -> assertEquals("stored-document.pdf", result.getCaseDocuments().getDocumentFilename()),
            () -> assertEquals(uploadTimestamp, result.getCaseDocuments().getUploadTimestamp()),

            () -> assertNull(result.getCaseDocumentType()),
            () -> assertNull(result.getCaseDocumentParty()),
            () -> assertNull(result.getCaseDocumentOther()),
            () -> assertNull(result.getCaseDocumentConfidentiality()),
            () -> assertNull(result.getHearingDetails()),
            () -> assertNull(result.getCaseDocumentFdr()),
            () -> assertNull(result.getCaseDocumentUploadDateTime()),
            () -> assertNull(result.getScannedFileName()),
            () -> assertNull(result.getSelectForUpdate())
        );
    }

    @Test
    void shouldHandleNullValuesFromScannedDocumentCollection() {
        ScannedDocumentCollection scannedDocumentCollection = mock(ScannedDocumentCollection.class);
        ScannedDocument scannedDocument = mock(ScannedDocument.class);
        CaseDocument sourceCaseDocument = mock(CaseDocument.class);

        when(scannedDocumentCollection.getValue()).thenReturn(scannedDocument);
        when(scannedDocument.getFileName()).thenReturn(null);
        when(scannedDocument.getScannedDate()).thenReturn(null);
        when(scannedDocument.getUrl()).thenReturn(sourceCaseDocument);
        when(scannedDocument.getExceptionRecordReference()).thenReturn(null);

        when(sourceCaseDocument.getDocumentUrl()).thenReturn(null);
        when(sourceCaseDocument.getDocumentBinaryUrl()).thenReturn(null);
        when(sourceCaseDocument.getDocumentFilename()).thenReturn(null);
        when(sourceCaseDocument.getUploadTimestamp()).thenReturn(null);

        UploadCaseDocument result = UploadCaseDocument.from(scannedDocumentCollection);

        assertAll(
            () -> assertNotNull(result),
            () -> assertNull(result.getFileName()),
            () -> assertNull(result.getScannedDate()),
            () -> assertNull(result.getExceptionRecordReference()),

            () -> assertNotNull(result.getCaseDocuments()),
            () -> assertTrue(result.getCaseDocuments().getDocumentUrl().isBlank()),
            () -> assertNull(result.getCaseDocuments().getDocumentBinaryUrl()),
            () -> assertNull(result.getCaseDocuments().getDocumentFilename()),
            () -> assertNull(result.getCaseDocuments().getUploadTimestamp())
        );
    }
}
