package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

@Service
public class OrderApprovedDocumentService extends AbstractDocumentService {

    @Autowired
    public OrderApprovedDocumentService(DocumentClient documentClient,
                                        DocumentConfiguration config, ObjectMapper objectMapper) {
        super(documentClient, config, objectMapper);
    }

    public CaseDocument generateApprovedConsentOrderLetter(CaseDetails caseDetails, String authorisationToken) {
        return generateDocument(authorisationToken, caseDetails,
                config.getApprovedConsentOrderTemplate(),
                config.getApprovedConsentOrderFileName());
    }
}