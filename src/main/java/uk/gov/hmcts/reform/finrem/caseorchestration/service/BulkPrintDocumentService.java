package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDownloadService;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BulkPrintDocumentService {

    private final EvidenceManagementDownloadService service;
    private final DocumentConversionService documentConversionService;

    public List<byte[]> downloadDocuments(BulkPrintRequest bulkPrintRequest, String auth) {
        String caseId = bulkPrintRequest.getCaseId();
        log.info("Downloading document for bulk print for case id {}", caseId);

        List<byte[]> documents = bulkPrintRequest.getBulkPrintDocuments().stream()
            .map(bulkPrintDocument -> service.download(bulkPrintDocument.getBinaryFileUrl(), auth))
            .toList();
        log.info("Download document count for bulk print {} for case id {} ", documents.size(),
            caseId);
        return documents;
    }

    public void validateEncryptionOnUploadedDocument(CaseDocument caseDocument,
                                                     String caseId,
                                                     List<String> errors,
                                                     String auth) {
        if (caseDocument != null) {
            String documentFilename = caseDocument.getDocumentFilename();
            log.info("checking encryption for file {} for caseId {}", documentFilename, caseId);
            byte[] pdfBytes;
            if (documentFilename.endsWith(".doc") || documentFilename.endsWith(".docx")) {
                Document document = Document.builder().url(caseDocument.getDocumentUrl())
                    .binaryUrl(caseDocument.getDocumentBinaryUrl())
                    .fileName(caseDocument.getDocumentFilename())
                    .build();

                pdfBytes = documentConversionService.convertDocumentToPdf(document, auth);
            } else {
                pdfBytes = service.download(caseDocument.getDocumentBinaryUrl(), auth);
            }

            try (PDDocument doc = PDDocument.load(pdfBytes)) {
                if (doc.isEncrypted()) {
                    errors.add("Uploaded document " + documentFilename + " contains encryption. "
                        + "Please remove encryption before uploading or upload another document.");
                }
            } catch (IOException exc) {
                String errorMessage = "Failed to parse the documents for " + documentFilename;
                errors.add(errorMessage);
                log.error(errorMessage);
                log.error(exc.getMessage());
            }
        }
    }
}
