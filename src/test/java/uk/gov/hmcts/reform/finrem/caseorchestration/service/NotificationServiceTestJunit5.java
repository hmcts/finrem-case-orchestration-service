package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDownloadService;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_DOCUMENT_BINARY_URL;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTestJunit5 {

    @Mock
    private EvidenceManagementDownloadService evidenceManagementDownloadService;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void shouldReturnByteArrayWhenDownloadSucceeds() {
        // Given
        CaseDocument caseDocument = CaseDocument.builder().documentBinaryUrl(TEST_DOCUMENT_BINARY_URL).build();
        byte[] expectedBytes = "test content".getBytes();
        ByteArrayResource resource = new ByteArrayResource(expectedBytes);
        ResponseEntity<Resource> responseEntity = new ResponseEntity<>(resource, HttpStatus.OK);

        when(evidenceManagementDownloadService.downloadInResponseEntity(TEST_DOCUMENT_BINARY_URL, AUTH_TOKEN)).thenReturn(responseEntity);

        // When
        byte[] result = notificationService.getByteArray(caseDocument, AUTH_TOKEN);

        // Then
        assertArrayEquals(expectedBytes, result);
    }

    @Test
    void shouldThrowExceptionWhenDownloadFails() {
        // Given
        CaseDocument caseDocument = CaseDocument.builder().documentBinaryUrl(TEST_DOCUMENT_BINARY_URL).build();
        ResponseEntity<Resource> responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);

        when(evidenceManagementDownloadService.downloadInResponseEntity(caseDocument.getDocumentBinaryUrl(), AUTH_TOKEN))
            .thenReturn(responseEntity);

        // When & Then
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class,
            () -> notificationService.getByteArray(caseDocument, AUTH_TOKEN));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void shouldReturnEmptyByteArrayWhenResourceIsNull() {
        // Given
        CaseDocument caseDocument = CaseDocument.builder().documentBinaryUrl(TEST_DOCUMENT_BINARY_URL).build();
        ResponseEntity<Resource> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);

        when(evidenceManagementDownloadService.downloadInResponseEntity(caseDocument.getDocumentBinaryUrl(), AUTH_TOKEN))
            .thenReturn(responseEntity);

        // When
        byte[] result = notificationService.getByteArray(caseDocument, AUTH_TOKEN);

        // Then
        assertArrayEquals(new byte[0], result);
    }
}
