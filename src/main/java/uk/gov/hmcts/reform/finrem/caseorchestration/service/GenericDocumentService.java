package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.io.Files;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GenericDocumentService {

    private static final String DOCUMENT_CASE_DETAILS_JSON_KEY = "caseDetails";

    private final DocumentManagementService documentManagementService;
    private final BulkPrintDocumentService bulkPrintDocumentService;
    private final BulkPrintDocumentGeneratorService bulkPrintDocumentGeneratorService;
    private final DocumentConversionService documentConversionService;
    private final PdfStampingService pdfStampingService;

    public CaseDocument generateDocument(String authorisationToken, CaseDetails caseDetailsCopy,
                                         String template, String fileName) {
        Map<String, Object> caseData = caseDetailsCopy.getData();
        caseData.computeIfAbsent(DocumentHelper.CASE_NUMBER, k -> caseDetailsCopy.getId());
        Map<String, Object> caseDetailsMap = Collections.singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetailsCopy);
        return generateDocumentFromPlaceholdersMap(authorisationToken, caseDetailsMap, template, fileName,
            caseDetailsCopy.getId().toString());
    }

    public CaseDocument generateDocumentFromPlaceholdersMap(String authorisationToken, Map placeholders,
                                                            String template, String fileName, String caseId) {
        Document generatedPdf = documentManagementService
            .storeDocument(template, fileName, placeholders, authorisationToken, caseId);
        return toCaseDocument(generatedPdf);
    }

    public UUID bulkPrint(BulkPrintRequest bulkPrintRequest, String recipient, String auth) {
        final List<byte[]> documents = bulkPrintDocumentService.downloadDocuments(bulkPrintRequest, auth);
        return bulkPrintDocumentGeneratorService.send(bulkPrintRequest, recipient, documents);
    }

    public void deleteDocument(String documentUrl, String authorisationToken) {
        documentManagementService.deleteDocument(documentUrl, authorisationToken);
    }

    public CaseDocument annexStampDocument(CaseDocument document,
                                           String authorisationToken,
                                           StampType stampType,
                                           String caseId) {
        Document documentWithUrl = Document.builder().url(document.getDocumentUrl())
            .binaryUrl(document.getDocumentBinaryUrl())
            .fileName(document.getDocumentFilename())
            .build();
        Document stampedDocument = pdfStampingService.stampDocument(
            documentWithUrl, authorisationToken, true, stampType, caseId);
        return toCaseDocument(stampedDocument);
    }

    public CaseDocument convertDocumentIfNotPdfAlready(CaseDocument document,
                                                       String authorisationToken,
                                                       String caseId) {
        return !Files.getFileExtension(document.getDocumentFilename()).equalsIgnoreCase("pdf")
            ? convertDocumentToPdf(document, authorisationToken, caseId) : document;
    }

    public CaseDocument convertDocumentToPdf(CaseDocument document, String authorisationToken, String caseId) {
        Document requestDocument = toDocument(document);
        byte[] convertedDocContent =
            documentConversionService.convertDocumentToPdf(requestDocument, authorisationToken);
        String filename = documentConversionService.getConvertedFilename(requestDocument.getFileName());
        Document storedDocument =
            documentManagementService.storeDocument(convertedDocContent, filename, authorisationToken, caseId);
        return toCaseDocument(storedDocument);
    }

    public CaseDocument stampDocument(CaseDocument document,
                                      String authorisationToken,
                                      StampType stampType,
                                      String caseId) {

        Document stampedDocument = pdfStampingService.stampDocument(
            Document.builder().url(document.getDocumentUrl())
                .binaryUrl(document.getDocumentBinaryUrl())
                .fileName(document.getDocumentFilename())
                .build(), authorisationToken, false, stampType, caseId);
        return toCaseDocument(stampedDocument);
    }

    public CaseDocument toCaseDocument(Document document) {
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentBinaryUrl(document.getBinaryUrl());
        caseDocument.setDocumentFilename(document.getFileName());
        caseDocument.setDocumentUrl(document.getUrl());
        return caseDocument;
    }

    public Document toDocument(CaseDocument caseDocument) {
        Document document = new Document();
        document.setBinaryUrl(caseDocument.getDocumentBinaryUrl());
        document.setFileName(caseDocument.getDocumentFilename());
        document.setUrl(caseDocument.getDocumentUrl());
        return document;
    }
}
