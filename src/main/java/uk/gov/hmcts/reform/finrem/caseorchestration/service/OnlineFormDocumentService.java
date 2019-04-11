package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

@Service
public class OnlineFormDocumentService extends AbstractDocumentService {

    @Autowired
    public OnlineFormDocumentService(DocumentGeneratorClient documentGeneratorClient,
                                     DocumentConfiguration config,
                                     ObjectMapper objectMapper) {
        super(documentGeneratorClient, config, objectMapper);
    }

    public CaseDocument generateMiniFormA(String authorisationToken, CaseDetails caseDetails) {
        return generateDocument(authorisationToken, caseDetails,
                config.getMiniFormTemplate(),
                config.getMiniFormFileName());
    }
}

