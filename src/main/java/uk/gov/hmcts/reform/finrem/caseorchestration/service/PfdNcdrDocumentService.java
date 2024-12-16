package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.DocumentStorageException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.FinremMultipartFile;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.evidence.FileUploadResponse;
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
public class PfdNcdrDocumentService {

    private final EvidenceManagementUploadService uploadService;

    /**
     * Uploads PFD NCDR Compliance Letter document to document store and returns the document.
     *
     * @param caseId case ID
     * @param authToken user authorization token
     * @return uploaded document
     */
    public CaseDocument uploadPfdNcdrComplianceLetter(String caseId, String authToken) {
        byte[] bytes = getPfdNcdrComplianceLetter();

        MultipartFile multipartFile = createMultipartFile("PfdNcdrComplianceLetter.pdf", bytes);

        return uploadDocument(caseId, authToken, multipartFile, "PFD NCDR Compliance Letter");
    }

    /**
     * Uploads PFD NCDR Cover Letter document to document store and returns the document.
     *
     * @param caseId case ID
     * @param authToken user authorization token
     * @return uploaded document
     */
    public CaseDocument uploadPfdNcdrCoverLetter(String caseId, String authToken) {
        byte[] bytes = getPfdNcdrCoverLetter();

        MultipartFile multipartFile = createMultipartFile("PfdNcdrCoverLetter.pdf", bytes);

        return uploadDocument(caseId, authToken, multipartFile, "PFD NCDR Cover Letter");
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

    private CaseDocument uploadDocument(String caseId, String authToken, MultipartFile multipartFile,
                                        String documentName) {
        List<FileUploadResponse> response = uploadService.upload(Collections.singletonList(multipartFile),
            caseId, authToken);
        FileUploadResponse fileUploadResponse = Optional.of(response.get(0))
            .filter(r -> r.getStatus() == HttpStatus.OK)
            .orElseThrow(() -> new DocumentStorageException("Failed to store " + documentName));

        Document document = CONVERTER.apply(fileUploadResponse);
        return CaseDocument.from(document);
    }
}
