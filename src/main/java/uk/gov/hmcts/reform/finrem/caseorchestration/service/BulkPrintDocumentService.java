package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
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
        log.info("Downloading document for bulk print for Case ID: {}", caseId);

        List<byte[]> documents = bulkPrintRequest.getBulkPrintDocuments().stream()
            .map(bulkPrintDocument -> documentConversionService.flattenPdfDocument(service.download(bulkPrintDocument.getBinaryFileUrl(), auth)))
            .toList();
        log.info("Download document count for bulk print {} for Case ID: {} ", documents.size(),
            caseId);

        return documents;
    }

    @SuppressWarnings("java:S3776")
    public void validateEncryptionOnUploadedDocument(CaseDocument caseDocument,
                                                     String caseId,
                                                     List<String> errors,
                                                     String auth) {
        if (caseDocument != null) {
            String documentFilename = caseDocument.getDocumentFilename();
            log.info("checking encryption for file {} for Case ID: {}", documentFilename, caseId);

            if (documentFilename.toLowerCase().endsWith(".doc") || documentFilename.toLowerCase().endsWith(".docx")) {
                handleDocFile(caseDocument, auth, errors, documentFilename);
            } else if (documentFilename.toLowerCase().endsWith(".pdf")) {
                handlePdfFile(caseDocument, auth, errors, documentFilename, caseId);
            }
        }
    }

    private void handleDocFile(CaseDocument caseDocument, String auth, List<String> errors, String documentFilename) {
        Document document = Document.builder().url(caseDocument.getDocumentUrl())
            .binaryUrl(caseDocument.getDocumentBinaryUrl())
            .fileName(caseDocument.getDocumentFilename())
            .build();

        byte[] pdfBytes = documentConversionService.convertDocumentToPdf(document, auth);
        checkIfPdfIsEncrypted(errors, documentFilename, pdfBytes);
    }

    private void handlePdfFile(CaseDocument caseDocument, String auth, List<String> errors, String documentFilename, String caseId) {
        byte[] pdfBytes = service.download(caseDocument.getDocumentBinaryUrl(), auth);

        if (pdfBytes != null) {
            checkIfPdfIsEncrypted(errors, documentFilename, pdfBytes);
        } else {
            String errorMessage = "Uploaded document " + documentFilename + " is empty.";
            log.error("Uploaded document {} for Case ID: {} is empty", documentFilename, caseId);
            errors.add(errorMessage);
        }
    }

    private void checkIfPdfIsEncrypted(List<String> errors, String documentFilename, byte[] pdfBytes) {
        try (PDDocument doc = Loader.loadPDF(pdfBytes)) {
            if (doc.isEncrypted()) {
                errors.add("Uploaded document '" + documentFilename + "' contains some kind of encryption. "
                    + "Please remove encryption before uploading or upload another document.");
            }
        } catch (InvalidPasswordException ipe) {
            String errorMessage = "Uploaded document '" + documentFilename + "' is password protected."
                + " Please remove password and try uploading again.";
            errors.add(errorMessage);
            log.error(ipe.getMessage());
        } catch (IOException exc) {
            String errorMessage = "Failed to parse the documents for " + documentFilename;
            errors.add(errorMessage + "; " + exc.getMessage());
            log.error(exc.getMessage());
        }
    }
}
