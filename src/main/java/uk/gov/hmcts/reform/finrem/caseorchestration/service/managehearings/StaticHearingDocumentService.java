package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.DocumentStorageException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.FinremMultipartFile;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.evidence.FileUploadResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementUploadService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.FileUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.DocumentManagementService.CONVERTER;

@Service
@RequiredArgsConstructor
public class StaticHearingDocumentService {

    private final EvidenceManagementUploadService uploadService;
    private final NotificationService notificationService;

    /**
     * Checks if a PFD NCDR Cover Letter is required on a case. It is required if the respondent solicitor is not digital
     * or the respondent is a LiP.
     *
     * @param caseDetails case details
     * @return true if PFD NCDR Cover Letter is required, false otherwise
     */
    public boolean isPdfNcdrCoverSheetRequired(CaseDetails caseDetails) {
        return !notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails);
    }

    /**
     * Checks if a PFD NCDR Cover Letter is required on a case. It is required if the respondent solicitor is not digital
     * or the respondent is a LiP.
     *
     * @param caseDetails finrem case details
     * @return true if PFD NCDR Cover Letter is required, false otherwise
     */
    public boolean isPdfNcdrCoverSheetRequired(FinremCaseDetails caseDetails) {
        return !notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails);
    }

    /**
     * Uploads PFD NCDR Compliance Letter document to document store and returns the document.
     *
     * @param caseType CCD case type
     * @param authToken user authorization token
     * @return uploaded document
     */
    public CaseDocument uploadPfdNcdrComplianceLetter(CaseType caseType, String authToken) {
        byte[] bytes = getPfdNcdrComplianceLetter();

        MultipartFile multipartFile = createMultipartFile("PfdNcdrComplianceLetter.pdf", bytes);

        return uploadDocument(caseType, authToken, multipartFile, "PFD NCDR Compliance Letter");
    }

    /**
     * Uploads Out Of Court Resolution document to document store and returns the document.
     *
     * @param caseType CCD case type
     * @param authToken user authorization token
     * @return uploaded document
     */
    public CaseDocument uploadOutOfCourtResolutionDocument(CaseType caseType, String authToken) {
        byte[] bytes = getOutOfCourtResolutionDocument();

        MultipartFile multipartFile = createMultipartFile("OutOfFamilyCourtResolution.pdf", bytes);

        return uploadDocument(caseType, authToken, multipartFile, "Out of Court Resolution Document");
    }

    /**
     * Uploads PFD NCDR Cover Letter document to document store and returns the document.
     *
     * @param caseType CCD case type
     * @param authToken user authorization token
     * @return uploaded document
     */
    public CaseDocument uploadPfdNcdrCoverLetter(CaseType caseType, String authToken) {
        byte[] bytes = getPfdNcdrCoverLetter();

        MultipartFile multipartFile = createMultipartFile("PfdNcdrCoverLetter.pdf", bytes);

        return uploadDocument(caseType, authToken, multipartFile, "PFD NCDR Cover Letter");
    }

    private byte[] getOutOfCourtResolutionDocument() {
        String filename = "documents/out-of-court-resolution-doc.pdf";
        try {
            return FileUtils.readResourceAsByteArray(filename);
        } catch (IOException e) {
            throw new DocumentStorageException("Failed to get Out of Court Resolution Document", e);
        }
    }

    private byte[] getPfdNcdrComplianceLetter() {
        String filename = "documents/pfd-ncdr-compliance-letter.pdf";
        try {
            return FileUtils.readResourceAsByteArray(filename);
        } catch (IOException e) {
            throw new DocumentStorageException("Failed to get PFD NCDR Compliance Letter", e);
        }
    }

    private byte[] getPfdNcdrCoverLetter() {
        try {
            return FileUtils.readResourceAsByteArray("documents/pfd-ncdr-cover-letter.pdf");
        } catch (IOException e) {
            throw new DocumentStorageException("Failed to get PFD NCDR Cover Letter", e);
        }
    }

    private MultipartFile createMultipartFile(String name, byte[] content) {
        return FinremMultipartFile.builder()
            .name(name)
            .contentType(APPLICATION_PDF_VALUE)
            .content(content)
            .build();
    }

    private CaseDocument uploadDocument(CaseType caseType, String authToken, MultipartFile multipartFile,
                                        String documentName) {
        List<FileUploadResponse> response = uploadService.upload(Collections.singletonList(multipartFile),
            caseType, authToken);
        FileUploadResponse fileUploadResponse = Optional.of(response.getFirst())
            .filter(r -> r.getStatus() == HttpStatus.OK)
            .orElseThrow(() -> new DocumentStorageException("Failed to store " + documentName));

        Document document = CONVERTER.apply(fileUploadResponse);
        return CaseDocument.from(document);
    }
}
