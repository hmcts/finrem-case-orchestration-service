package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.io.Files;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentGenerationRequest;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GenericDocumentService {

    private static final String DOCUMENT_CASE_DETAILS_JSON_KEY = "caseDetails";

    private final DocumentClient documentClient;

    public Document generateDocument(String authorisationToken, CaseDetails caseDetails,
                                         String template, String fileName) {
        Map<String, Object> caseDetailsMap = Collections.singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails);
        return generateDocumentFromPlaceholdersMap(authorisationToken, caseDetailsMap, template, fileName);

    }

    public Document generateDocumentFromPlaceholdersMap(String authorisationToken, Map placeholders,
                                                            String template, String fileName) {
        return documentClient.generatePdf(
            DocumentGenerationRequest.builder().template(template).fileName(fileName).values(placeholders).build(),
            authorisationToken);
    }


    public UUID bulkPrint(BulkPrintRequest bulkPrintRequest) {
        return documentClient.bulkPrint(bulkPrintRequest);
    }

    public void deleteDocument(String documentUrl, String authorisationToken) {
        documentClient.deleteDocument(documentUrl, authorisationToken);
    }

    public CaseDocument annexStampDocument(CaseDocument document, String authorisationToken) {
        Document stampedDocument = documentClient.annexStampDocument(toDocument(document), authorisationToken);
        return toCaseDocument(stampedDocument);
    }

    public CaseDocument convertDocumentIfNotPdfAlready(CaseDocument document, String authorisationToken) {
        return !Files.getFileExtension(document.getDocumentFilename()).equalsIgnoreCase("pdf")
            ? convertDocumentToPdf(document, authorisationToken) : document;
    }

    public CaseDocument convertDocumentToPdf(CaseDocument document, String authorisationToken) {
        return toCaseDocument(documentClient.convertDocumentToPdf(authorisationToken, toDocument(document)));
    }

    public CaseDocument stampDocument(CaseDocument document, String authorisationToken) {
        Document stampedDocument = documentClient.stampDocument(toDocument(document), authorisationToken);
        return toCaseDocument(stampedDocument);
    }

    public Document stampDocument(Document document, String authorisationToken) {
        return documentClient.stampDocument(document, authorisationToken);
    }

    public CaseDocument toCaseDocument(Document document) {
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentBinaryUrl(document.getBinaryUrl());
        caseDocument.setDocumentFilename(document.getFilename());
        caseDocument.setDocumentUrl(document.getUrl());
        return caseDocument;
    }

    public Document toDocument(CaseDocument caseDocument) {
        Document document = new Document();
        document.setBinaryUrl(caseDocument.getDocumentBinaryUrl());
        document.setFilename(caseDocument.getDocumentFilename());
        document.setUrl(caseDocument.getDocumentUrl());
        return document;
    }
}
