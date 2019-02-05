package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrderData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentRequest;

import java.util.Collections;
import java.util.Date;
import java.util.UUID;

@Service
public class DocumentService {

    private static final String DOCUMENT_CASE_DETAILS_JSON_KEY = "caseDetails";

    private final DocumentConfiguration documentConfiguration;
    private final DocumentGeneratorClient documentGeneratorClient;

    @Autowired
    public DocumentService(DocumentGeneratorClient documentGeneratorClient,
                           DocumentConfiguration documentConfiguration) {
        this.documentGeneratorClient = documentGeneratorClient;
        this.documentConfiguration = documentConfiguration;
    }

    public CaseDocument generateMiniFormA(String authorisationToken, CaseDetails caseDetails) {
        return generateDocument(authorisationToken, caseDetails,
                documentConfiguration.getMiniFormTemplate(),
                documentConfiguration.getMiniFormFileName());
    }

    public ConsentOrderData generateConsentOrderNotApproved(String authorisationToken, CaseDetails caseDetails) {
        CaseDocument caseDocument =
                generateDocument(authorisationToken, caseDetails,
                        documentConfiguration.getRejectedOrderTemplate(),
                        documentConfiguration.getRejectedOrderFileName());
        return consentOrderData(caseDocument);
    }

    private CaseDocument generateDocument(String authorisationToken, CaseDetails caseDetails,
                                          String template, String fileName) {
        Document miniFormA =
                documentGeneratorClient.generatePDF(
                        DocumentRequest.builder()
                                .template(template)
                                .fileName(fileName)
                                .values(Collections.singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails))
                                .build(),
                        authorisationToken);

        return caseDocument(miniFormA);
    }

    private ConsentOrderData consentOrderData(CaseDocument caseDocument) {
        ConsentOrder consentOrder = new ConsentOrder();
        consentOrder.setDocumentType(documentConfiguration.getRejectedOrderDocType());
        consentOrder.setDocumentDateAdded(new Date());
        consentOrder.setDocumentLink(caseDocument);

        ConsentOrderData consentOrderData = new ConsentOrderData();
        consentOrderData.setId(UUID.randomUUID().toString());
        consentOrderData.setConsentOrder(consentOrder);

        return consentOrderData;
    }

    private CaseDocument caseDocument(Document miniFormA) {
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentBinaryUrl(miniFormA.getBinaryUrl());
        caseDocument.setDocumentFilename(miniFormA.getFileName());
        caseDocument.setDocumentUrl(miniFormA.getUrl());
        return caseDocument;
    }
}
