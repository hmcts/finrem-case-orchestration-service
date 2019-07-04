package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentRequest;

import java.io.IOException;
import java.util.Collections;

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
        Document miniFormA =
                documentClient.generatePDF(
                        DocumentRequest.builder()
                                .template(template)
                                .fileName(fileName)
                                .values(Collections.singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails))
                                .build(),
                        authorisationToken);

        return caseDocument(miniFormA);
    }


    void deleteDocument(String documentUrl, String authorisationToken) {
        documentClient.deleteDocument(documentUrl, authorisationToken);
    }

    void generateApprovedConsentOrder(CallbackRequest callbackRequest, String authorisationToken) {
        documentClient.generateApprovedConsentOrder(callbackRequest, authorisationToken);
    }

    private CaseDocument caseDocument(Document miniFormA) {
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentBinaryUrl(miniFormA.getBinaryUrl());
        caseDocument.setDocumentFilename(miniFormA.getFileName());
        caseDocument.setDocumentUrl(miniFormA.getUrl());
        return caseDocument;
    }

    CaseDetails copyOf(CaseDetails caseDetails) {
        try {
            return objectMapper
                    .readValue(objectMapper.writeValueAsString(caseDetails), CaseDetails.class);
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }
}
