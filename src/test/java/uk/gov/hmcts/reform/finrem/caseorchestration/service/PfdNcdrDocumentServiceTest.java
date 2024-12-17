package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.DocumentStorageException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.evidence.FileUploadResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementUploadService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.FileUtils;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class PfdNcdrDocumentServiceTest {

    @InjectMocks
    private PfdNcdrDocumentService pfdNcdrDocumentService;
    @Mock
    private EvidenceManagementUploadService uploadService;
    @Mock
    private NotificationService notificationService;

    @Test
    void whenRespondentDigital_thenPfdNcdrCoverSheetNotRequired() {
        CaseDetails caseDetails = CaseDetails.builder().build();
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        assertThat(pfdNcdrDocumentService.isPdfNcdrCoverSheetRequired(caseDetails)).isFalse();
    }

    @Test
    void whenRespondentNonDigital_thenPfdNcdrCoverSheetRequired() {
        CaseDetails caseDetails = CaseDetails.builder().build();
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        assertThat(pfdNcdrDocumentService.isPdfNcdrCoverSheetRequired(caseDetails)).isTrue();
    }

    @Test
    void whenUploadPfdNcdrComplianceLetter_thenReturnCaseDocument() {
        mockUploadDocument(HttpStatus.OK, "http://localhost:1234/5646", "PfdNcdrComplianceLetter.pdf");

        CaseDocument expectedCaseDocument = CaseDocument.builder()
            .documentBinaryUrl("http://localhost:1234/5646/binary")
            .documentFilename("PfdNcdrComplianceLetter.pdf")
            .documentUrl("http://localhost:1234/5646")
            .build();

        CaseDocument caseDocument = pfdNcdrDocumentService.uploadPfdNcdrComplianceLetter("1234", AUTH_TOKEN);

        assertThat(caseDocument).isEqualTo(expectedCaseDocument);
    }

    @Test
    void whenUploadPfdNcdrComplianceLetterReturnsError_thenThrowsException() {
        mockUploadDocument(HttpStatus.BAD_GATEWAY, "http://localhost:1234/743543", "PfdNcdrComplianceLetter.pdf");

        assertThatThrownBy(() -> pfdNcdrDocumentService.uploadPfdNcdrComplianceLetter("1234", AUTH_TOKEN))
            .isInstanceOf(DocumentStorageException.class)
            .hasMessage("Failed to store PFD NCDR Compliance Letter");
    }

    @Test
    void whenPfdNcdrComplianceLetterResourceFileNotFound_thenThrowsException() {
        try (MockedStatic<FileUtils> fileUtilsMock = mockStatic(FileUtils.class)) {
            fileUtilsMock.when(() -> FileUtils.readResourceAsByteArray(anyString())).thenThrow(new IOException());

            assertThatThrownBy(() -> pfdNcdrDocumentService.uploadPfdNcdrComplianceLetter("1234", AUTH_TOKEN))
                .isInstanceOf(DocumentStorageException.class)
                .hasMessage("Failed to get PFD NCDR Compliance Letter");
        }
    }

    @Test
    void whenUploadPfdNcdrCoverLetter_thenReturnCaseDocument() {
        mockUploadDocument(HttpStatus.OK, "http://localhost:1234/23232", "PfdNcdrCoverLetter.pdf");

        CaseDocument expectedCaseDocument = CaseDocument.builder()
            .documentBinaryUrl("http://localhost:1234/23232/binary")
            .documentFilename("PfdNcdrCoverLetter.pdf")
            .documentUrl("http://localhost:1234/23232")
            .build();

        CaseDocument caseDocument = pfdNcdrDocumentService.uploadPfdNcdrCoverLetter("1234", AUTH_TOKEN);

        assertThat(caseDocument).isEqualTo(expectedCaseDocument);
    }

    @Test
    void whenUploadPfdNcdrCoverLetterReturnsError_thenThrowsException() {
        mockUploadDocument(HttpStatus.BAD_GATEWAY, "http://localhost:1234/8766", "PfdNcdrCoverLetter.pdf");

        assertThatThrownBy(() -> pfdNcdrDocumentService.uploadPfdNcdrCoverLetter("1234", AUTH_TOKEN))
            .isInstanceOf(DocumentStorageException.class)
            .hasMessage("Failed to store PFD NCDR Cover Letter");
    }

    @Test
    void whenPfdNcdrCoverLetterResourceFileNotFound_thenThrowsException() {
        try (MockedStatic<FileUtils> fileUtilsMock = mockStatic(FileUtils.class)) {
            fileUtilsMock.when(() -> FileUtils.readResourceAsByteArray(anyString())).thenThrow(new IOException());

            assertThatThrownBy(() -> pfdNcdrDocumentService.uploadPfdNcdrCoverLetter("1234", AUTH_TOKEN))
                .isInstanceOf(DocumentStorageException.class)
                .hasMessage("Failed to get PFD NCDR Cover Letter");
        }
    }

    private void mockUploadDocument(HttpStatus httpStatus, String url, String fileName) {
        FileUploadResponse fileUploadResponse = FileUploadResponse.builder()
            .status(httpStatus)
            .fileUrl(url)
            .fileName(fileName)
            .build();

        when(uploadService.upload(anyList(), anyString(), anyString())).thenReturn(List.of(fileUploadResponse));
    }
}
