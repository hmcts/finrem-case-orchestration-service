package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentRequest;

import java.util.Collections;

@Service
public class DocumentService {

    private static final String DOCUMENT_CASE_DETAILS_JSON_KEY = "caseDetails";

    @Value("${document.miniFormA.template}")
    private String miniFormATemplate;

    private final DocumentGeneratorClient documentGeneratorClient;

    @Autowired
    public DocumentService(DocumentGeneratorClient documentGeneratorClient) {
        this.documentGeneratorClient = documentGeneratorClient;
    }

    public CaseDocument generateMiniFormA(String authorisationToken, CaseDetails caseDetails) {
        Document miniFormA =
                documentGeneratorClient.generatePDF(
                        DocumentRequest.builder()
                                .template(miniFormATemplate)
                                .values(Collections.singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails))
                                .build(),
                        authorisationToken);

        return caseDocument(miniFormA);
    }

    private CaseDocument caseDocument(Document miniFormA) {
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentBinaryUrl(miniFormA.getBinaryUrl());
        caseDocument.setDocumentFilename(miniFormA.getFileName());
        caseDocument.setDocumentUrl(miniFormA.getUrl());
        return caseDocument;
    }

    public CaseDocument generateMiniFormA(String authorisationToken,
                                          uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails) {
        Document miniFormA =
                documentGeneratorClient.generatePDF(
                        DocumentRequest.builder()
                                .template(miniFormATemplate)
                                .values(Collections.singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails))
                                .build(),
                        authorisationToken);

        return caseDocument(miniFormA);
    }


}
