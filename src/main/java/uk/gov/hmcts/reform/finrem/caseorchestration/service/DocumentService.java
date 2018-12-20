package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentRequest;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class DocumentService {

    private static final String DOCUMENT_CASE_DETAILS_JSON_KEY = "caseDetails";

    @Value("${document.miniFormA.template}")
    private String miniFormATemplate;

    private final DocumentClient documentClient;

    @Autowired
    public DocumentService(DocumentClient documentClient) {
        this.documentClient = documentClient;
    }

    public CaseDocument generateMiniFormA(String authorisationToken, CaseDetails caseDetails) {
        Document miniFormA =
                documentClient.generatePDF(
                        DocumentRequest.builder()
                                .template(miniFormATemplate)
                                .values(Collections.singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails))
                                .build(),
                        authorisationToken);

        deleteOldMiniFormA(caseDetails.getCaseData(), authorisationToken);

        return caseDocument(miniFormA);
    }

    private void deleteOldMiniFormA(CaseData caseData, String authorisationToken) {
        if (caseData != null && caseData.getMiniFormA() != null) {
            String documentUrl = caseData.getMiniFormA().getDocumentUrl();
            CompletableFuture.runAsync(() -> {
                try {
                    documentClient.deleteDocument(documentUrl, authorisationToken);
                } catch (Exception e) {
                    log.info("Failed to delete existing mini-form-a. Error occurred: {}", e.getMessage());
                }
            });
        }
    }

    private CaseDocument caseDocument(Document miniFormA) {
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentBinaryUrl(miniFormA.getBinaryUrl());
        caseDocument.setDocumentFilename(miniFormA.getFileName());
        caseDocument.setDocumentUrl(miniFormA.getUrl());
        return caseDocument;
    }
}
