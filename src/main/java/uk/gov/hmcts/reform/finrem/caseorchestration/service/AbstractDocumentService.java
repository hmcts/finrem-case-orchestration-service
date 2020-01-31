package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentGenerationRequest;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

public abstract class AbstractDocumentService {
    private static final String DOCUMENT_CASE_DETAILS_JSON_KEY = "caseDetails";

    protected final DocumentConfiguration config;
    private final DocumentClient documentClient;
    protected final ObjectMapper objectMapper;

    public AbstractDocumentService(DocumentClient documentClient,
                                   DocumentConfiguration config,
                                   ObjectMapper objectMapper) {
        this.documentClient = documentClient;
        this.config = config;
        this.objectMapper = objectMapper;
    }

    CaseDocument generateDocument(String authorisationToken, CaseDetails caseDetails,
                                          String template, String fileName) {

        Map<String, Object> caseDetailsMap = Collections.singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails);

        Document miniFormA =
                documentClient.generatePdf(
                        DocumentGenerationRequest.builder()
                                .template(template)
                                .fileName(fileName)
                                .values(caseDetailsMap)
                                .build(),
                        authorisationToken);

        return caseDocument(miniFormA);
    }

    UUID bulkPrint(BulkPrintRequest bulkPrintRequest) {
        return documentClient.bulkPrint(bulkPrintRequest);
    }

    public void deleteDocument(String documentUrl, String authorisationToken) {
        documentClient.deleteDocument(documentUrl, authorisationToken);
    }

    public CaseDocument annexStampDocument(CaseDocument document, String authorisationToken) {
        Document stampedDocument = documentClient.annexStampDocument(toDocument(document), authorisationToken);
        return caseDocument(stampedDocument);
    }

    public CaseDocument stampDocument(CaseDocument document, String authorisationToken) {
        Document stampedDocument = documentClient.stampDocument(toDocument(document), authorisationToken);
        return caseDocument(stampedDocument);
    }

    protected CaseDocument caseDocument(Document miniFormA) {
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentBinaryUrl(miniFormA.getBinaryUrl());
        caseDocument.setDocumentFilename(miniFormA.getFileName());
        caseDocument.setDocumentUrl(miniFormA.getUrl());
        return caseDocument;
    }

    protected Document toDocument(CaseDocument caseDocument) {
        Document document = new Document();
        document.setBinaryUrl(caseDocument.getDocumentBinaryUrl());
        document.setFileName(caseDocument.getDocumentFilename());
        document.setUrl(caseDocument.getDocumentUrl());
        return document;
    }

    CaseDetails copyOf(CaseDetails caseDetails) {
        try {
            return objectMapper.readValue(objectMapper.writeValueAsString(caseDetails), CaseDetails.class);
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }
}
