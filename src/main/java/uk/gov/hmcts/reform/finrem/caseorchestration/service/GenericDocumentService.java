package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.io.Files;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentGenerationRequest;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GenericDocumentService {

    private static final String DOCUMENT_CASE_DETAILS_JSON_KEY = "caseDetails";

    private final DocumentClient documentClient;

    public CaseDocument generateDocument(String authorisationToken, CaseDetails caseDetails,
                                         String template, String fileName) {

        Map<String, Object> caseDetailsMap = Collections.singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails);
        Document generatedPdf = documentClient.generatePdf(
            DocumentGenerationRequest.builder().template(template).fileName(fileName).values(caseDetailsMap).build(),
            authorisationToken,
            caseDetails.getCaseTypeId()
        );

        return toCaseDocument(generatedPdf);
    }

    public UUID bulkPrint(BulkPrintRequest bulkPrintRequest, String authorisationToken) {
        return documentClient.bulkPrint(bulkPrintRequest, authorisationToken);
    }

    public void deleteDocument(String documentUrl, String authorisationToken) {
        documentClient.deleteDocument(documentUrl, authorisationToken);
    }

    public CaseDocument annexStampDocument(CaseDocument document, String authorisationToken, String caseTypeId) {
        Document stampedDocument = documentClient.annexStampDocument(toDocument(document), authorisationToken, caseTypeId);
        return toCaseDocument(stampedDocument);
    }

    public CaseDocument convertDocumentIfNotPdfAlready(CaseDocument document, String authorisationToken, String caseTypeId) {
        return !Files.getFileExtension(document.getDocumentFilename()).equalsIgnoreCase("pdf")
            ? convertDocumentToPdf(document, authorisationToken, caseTypeId) : document;
    }

    public CaseDocument convertDocumentToPdf(CaseDocument document, String authorisationToken, String caseTypeId) {
        return toCaseDocument(documentClient.convertDocumentToPdf(authorisationToken, toDocument(document), caseTypeId));
    }

    public CaseDocument stampDocument(CaseDocument document, String authorisationToken, String caseTypeId) {
        Document stampedDocument = documentClient.stampDocument(toDocument(document), authorisationToken, caseTypeId);
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
