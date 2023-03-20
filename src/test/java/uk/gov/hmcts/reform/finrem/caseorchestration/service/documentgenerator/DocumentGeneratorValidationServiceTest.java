package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentgenerator;

import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentValidationResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.DocumentGeneratorValidationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDownloadService;

import java.io.IOException;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@RunWith(MockitoJUnitRunner.class)
public class DocumentGeneratorValidationServiceTest {

    private static final String FILE_BINARY_URL = "http://dm-store:8080/eeww123456/binary";

    @Mock private EvidenceManagementDownloadService evidenceManagementDownloadService;
    @Mock private Tika tika;

    @InjectMocks
    private DocumentGeneratorValidationService underTest;

    @Before
    public void setUp() {
        setField(underTest, "mimeTypes", singletonList("application/pdf"));
        setField(underTest, "fileUploadErrorMessage", "Invalid fileType");
    }

    @Test
    public void shouldReturnSuccessValidateFileType() throws IOException {
        ResponseEntity<byte[]> responseEntity = jsonResponseEntityWithStubBody();
        when(tika.detect(any(), any(Metadata.class))).thenReturn("application/pdf");
        when(evidenceManagementDownloadService.download(FILE_BINARY_URL)).thenReturn(responseEntity);
        DocumentValidationResponse documentValidationResponse = underTest.validateFileType(FILE_BINARY_URL);
        assertThat(documentValidationResponse.getMimeType(), is("application/pdf"));
        assertNull(documentValidationResponse.getErrors());
    }

    @Test
    public void shouldReturnErrorsForInValidateFileType() throws IOException {
        ResponseEntity<byte[]> responseEntity = jsonResponseEntityWithStubBody();
        when(tika.detect(any(), any(Metadata.class))).thenReturn("application/json");
        when(evidenceManagementDownloadService.download(FILE_BINARY_URL)).thenReturn(responseEntity);
        DocumentValidationResponse documentValidationResponse = underTest.validateFileType(FILE_BINARY_URL);
        assertThat(documentValidationResponse.getErrors(), hasItem("Invalid fileType"));
    }

    @Test
    public void shouldThrowErrorForValidateFileType() throws IOException {
        ResponseEntity<byte[]> responseEntity = jsonResponseEntityWithStubBody();
        when(tika.detect(any(), any(Metadata.class))).thenThrow(new IOException());
        when(evidenceManagementDownloadService.download(FILE_BINARY_URL)).thenReturn(responseEntity);
        DocumentValidationResponse documentValidationResponse = underTest.validateFileType(FILE_BINARY_URL);
        assertThat(documentValidationResponse.getErrors(), hasItem("Unable to detect the MimeType due to IOException"));
    }

    @Test
    public void shouldThrowErrorForEmptyFile() {
        ResponseEntity<byte[]> responseEntity = ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(null);
        when(evidenceManagementDownloadService.download(FILE_BINARY_URL)).thenReturn(responseEntity);
        DocumentValidationResponse documentValidationResponse = underTest.validateFileType(FILE_BINARY_URL);
        assertThat(documentValidationResponse.getErrors(), hasItem("Downloaded document is empty"));
    }

    private ResponseEntity<byte[]> jsonResponseEntityWithStubBody() {
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(new byte[1025]);
    }
}
