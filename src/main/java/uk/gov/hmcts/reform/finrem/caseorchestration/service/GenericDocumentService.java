package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.io.Files;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
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
        CaseType caseType = CaseType.forValue(caseDetailsCopy.getCaseTypeId());
        return generateDocumentFromPlaceholdersMap(authorisationToken, caseDetailsMap, template, fileName, caseType);
    }

    public CaseDocument generateDocumentFromPlaceholdersMap(String authorisationToken, Map placeholders,
                                                            String template, String fileName, CaseType caseType) {
        Document generatedPdf = documentManagementService
            .storeDocument(template, fileName, placeholders, authorisationToken, caseType);
        return CaseDocument.from(generatedPdf);
    }

    public UUID bulkPrint(BulkPrintRequest bulkPrintRequest) {
        final List<byte[]> documents = bulkPrintDocumentService.downloadDocuments(bulkPrintRequest,
            bulkPrintRequest.getAuthorisationToken());
        return bulkPrintDocumentGeneratorService.send(bulkPrintRequest, documents);
    }

    public void deleteDocument(String documentUrl, String authorisationToken) {
        documentManagementService.deleteDocument(documentUrl, authorisationToken);
    }

    public CaseDocument annexStampDocument(CaseDocument document,
                                           String authorisationToken,
                                           StampType stampType,
                                           CaseType caseType) {
        Document documentWithUrl = Document.builder().url(document.getDocumentUrl())
            .binaryUrl(document.getDocumentBinaryUrl())
            .fileName(document.getDocumentFilename())
            .build();
        Document stampedDocument = pdfStampingService.stampDocument(documentWithUrl, authorisationToken, true,
            stampType, caseType);
        return CaseDocument.from(stampedDocument);
    }

    public CaseDocument convertDocumentIfNotPdfAlready(CaseDocument document,
                                                       String authorisationToken,
                                                       CaseType caseType) {
        return !Files.getFileExtension(document.getDocumentFilename()).equalsIgnoreCase("pdf")
            ? convertDocumentToPdf(document, authorisationToken, caseType) : document;
    }

    public CaseDocument convertDocumentToPdf(CaseDocument document, String authorisationToken, CaseType caseType) {
        Document requestDocument = toDocument(document);
        byte[] convertedDocContent =
            documentConversionService.convertDocumentToPdf(requestDocument, authorisationToken);
        String filename = documentConversionService.getConvertedFilename(requestDocument.getFileName());
        Document storedDocument =
            documentManagementService.storeDocument(convertedDocContent, filename, authorisationToken, caseType);
        return CaseDocument.from(storedDocument);
    }

    public CaseDocument stampDocument(CaseDocument document,
                                      String authorisationToken,
                                      StampType stampType,
                                      CaseType caseType) {
        CaseDocument pdfCaseDocument = convertDocumentIfNotPdfAlready(document, authorisationToken, caseType);
        log.info("Pdf conversion if document is not pdf original {} pdfdocument {} for Case type: {}",
            document.getDocumentFilename(), pdfCaseDocument.getDocumentFilename(), caseType);
        Document stampedDocument = pdfStampingService.stampDocument(
            Document.builder().url(pdfCaseDocument.getDocumentUrl())
                .binaryUrl(pdfCaseDocument.getDocumentBinaryUrl())
                .fileName(pdfCaseDocument.getDocumentFilename())
                .build(), authorisationToken, false, stampType, caseType);
        return CaseDocument.from(stampedDocument);
    }

    public Document toDocument(CaseDocument caseDocument) {
        Document document = new Document();
        document.setBinaryUrl(caseDocument.getDocumentBinaryUrl());
        document.setFileName(caseDocument.getDocumentFilename());
        document.setUrl(caseDocument.getDocumentUrl());
        return document;
    }
}
