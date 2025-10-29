package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings;

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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.evidence.FileUploadResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementUploadService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.FileUtils;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@ExtendWith(MockitoExtension.class)
class StaticHearingDocumentServiceTest {

    @InjectMocks
    private StaticHearingDocumentService staticHearingDocumentService;
    @Mock
    private EvidenceManagementUploadService uploadService;
    @Mock
    private NotificationService notificationService;

    @Test
    void givenRespondentDigital_whenIsPdfNcdrCoverSheetRequired_thenReturnsFalse() {
        CaseDetails caseDetails = CaseDetails.builder().build();
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        assertThat(staticHearingDocumentService.isPdfNcdrCoverSheetRequired(caseDetails)).isFalse();
    }

    @Test
    void givenRespondentNotDigital_whenIsPdfNcdrCoverSheetRequired_thenReturnsTrue() {
        CaseDetails caseDetails = CaseDetails.builder().build();
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        assertThat(staticHearingDocumentService.isPdfNcdrCoverSheetRequired(caseDetails)).isTrue();
    }

    @Test
    void givenRespondentDigital_whenIsPdfNcdrCoverSheetRequired_thenReturnsFalse_withFinremCaseDetails() {
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().build();
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        assertThat(staticHearingDocumentService.isPdfNcdrCoverSheetRequired(caseDetails)).isFalse();
    }

    @Test
    void givenRespondentNotDigital_whenIsPdfNcdrCoverSheetRequired_thenReturnsTrue_withFinremCaseDetails() {
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().build();
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        assertThat(staticHearingDocumentService.isPdfNcdrCoverSheetRequired(caseDetails)).isTrue();
    }

    @Test
    void givenNoServerErrors_whenUploadPfdNcdrComplianceLetter_thenReturnCaseDocument() {
        mockUploadDocument(HttpStatus.OK, "http://localhost:1234/5646", "PfdNcdrComplianceLetter.pdf");

        CaseDocument expectedCaseDocument = CaseDocument.builder()
            .documentBinaryUrl("http://localhost:1234/5646/binary")
            .documentFilename("PfdNcdrComplianceLetter.pdf")
            .documentUrl("http://localhost:1234/5646")
            .build();

        CaseDocument caseDocument = staticHearingDocumentService.uploadPfdNcdrComplianceLetter(CONTESTED, AUTH_TOKEN);

        assertThat(caseDocument).isEqualTo(expectedCaseDocument);
    }

    @Test
    void givenBadGateway_whenUploadPfdNcdrComplianceLetter_thenThrowsException() {
        mockUploadDocument(HttpStatus.BAD_GATEWAY, "http://localhost:1234/743543", "PfdNcdrComplianceLetter.pdf");

        assertThatThrownBy(() -> staticHearingDocumentService.uploadPfdNcdrComplianceLetter(CONTESTED, AUTH_TOKEN))
            .isInstanceOf(DocumentStorageException.class)
            .hasMessage("Failed to store PFD NCDR Compliance Letter");
    }

    @Test
    void givenPdfResourceFileNotExists_whenUploadPfdNcdrComplianceLetter_thenThrowsException() {
        try (MockedStatic<FileUtils> fileUtilsMock = mockStatic(FileUtils.class)) {
            fileUtilsMock.when(() -> FileUtils.readResourceAsByteArray(anyString())).thenThrow(new IOException());

            assertThatThrownBy(() -> staticHearingDocumentService.uploadPfdNcdrComplianceLetter(CONTESTED, AUTH_TOKEN))
                .isInstanceOf(DocumentStorageException.class)
                .hasMessage("Failed to get PFD NCDR Compliance Letter");
        }
    }

    @Test
    void givenNoServerErrors_whenUploadPfdNcdrCoverLetter_thenReturnCaseDocument() {
        mockUploadDocument(HttpStatus.OK, "http://localhost:1234/23232", "PfdNcdrCoverLetter.pdf");

        CaseDocument expectedCaseDocument = CaseDocument.builder()
            .documentBinaryUrl("http://localhost:1234/23232/binary")
            .documentFilename("PfdNcdrCoverLetter.pdf")
            .documentUrl("http://localhost:1234/23232")
            .build();

        CaseDocument caseDocument = staticHearingDocumentService.uploadPfdNcdrCoverLetter(CONTESTED, AUTH_TOKEN);

        assertThat(caseDocument).isEqualTo(expectedCaseDocument);
    }

    @Test
    void givenBadGateway_whenUploadPfdNcdrCoverLetter_thenThrowsException() {
        mockUploadDocument(HttpStatus.BAD_GATEWAY, "http://localhost:1234/8766", "PfdNcdrCoverLetter.pdf");

        assertThatThrownBy(() -> staticHearingDocumentService.uploadPfdNcdrCoverLetter(CONTESTED, AUTH_TOKEN))
            .isInstanceOf(DocumentStorageException.class)
            .hasMessage("Failed to store PFD NCDR Cover Letter");
    }

    @Test
    void givenPdfResourceFileNotExists_whenUploadPfdNcdrCoverLetter_thenThrowsException() {
        try (MockedStatic<FileUtils> fileUtilsMock = mockStatic(FileUtils.class)) {
            fileUtilsMock.when(() -> FileUtils.readResourceAsByteArray(anyString())).thenThrow(new IOException());

            assertThatThrownBy(() -> staticHearingDocumentService.uploadPfdNcdrCoverLetter(CONTESTED, AUTH_TOKEN))
                .isInstanceOf(DocumentStorageException.class)
                .hasMessage("Failed to get PFD NCDR Cover Letter");
        }
    }

    @Test
    void givenNoServerErrors_whenUploadOutOfCourtResolution_thenReturnCaseDocument() {
        mockUploadDocument(HttpStatus.OK, "http://localhost:1234/5646", "OutOfFamilyCourtResolution.pdf");

        CaseDocument expectedCaseDocument = CaseDocument.builder()
            .documentBinaryUrl("http://localhost:1234/5646/binary")
            .documentFilename("OutOfFamilyCourtResolution.pdf")
            .documentUrl("http://localhost:1234/5646")
            .build();

        CaseDocument caseDocument = staticHearingDocumentService.uploadOutOfCourtResolutionDocument(CONTESTED, AUTH_TOKEN);

        assertThat(caseDocument).isEqualTo(expectedCaseDocument);
    }

    @Test
    void givenBadGateway_whenUploadOutOfCourtResolution_thenThrowsException() {
        mockUploadDocument(HttpStatus.BAD_GATEWAY, "http://localhost:1234/743543", "OutOfFamilyCourtResolution.pdf");

        assertThatThrownBy(() -> staticHearingDocumentService.uploadOutOfCourtResolutionDocument(CONTESTED, AUTH_TOKEN))
            .isInstanceOf(DocumentStorageException.class)
            .hasMessage("Failed to store Out of Court Resolution Document");
    }

    @Test
    void givenPdfResourceFileNotExists_whenUploadOutOfCourtResolution_thenThrowsException() {
        try (MockedStatic<FileUtils> fileUtilsMock = mockStatic(FileUtils.class)) {
            fileUtilsMock.when(() -> FileUtils.readResourceAsByteArray(anyString())).thenThrow(new IOException());

            assertThatThrownBy(() -> staticHearingDocumentService.uploadOutOfCourtResolutionDocument(CONTESTED, AUTH_TOKEN))
                .isInstanceOf(DocumentStorageException.class)
                .hasMessage("Failed to get Out of Court Resolution Document");
        }
    }

    private void mockUploadDocument(HttpStatus httpStatus, String url, String fileName) {
        FileUploadResponse fileUploadResponse = FileUploadResponse.builder()
            .status(httpStatus)
            .fileUrl(url)
            .fileName(fileName)
            .build();

        when(uploadService.upload(anyList(), any(CaseType.class), anyString())).thenReturn(List.of(fileUploadResponse));
    }
}
