package uk.gov.hmcts.reform.finrem.caseorchestration.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;

    @Service
    @Slf4j
    public class ConsentOrderApprovedDocumentService extends AbstractDocumentService {

        @Autowired
        public ConsentOrderApprovedDocumentService(DocumentClient documentClient,
                                                   DocumentConfiguration config,
                                                   ObjectMapper objectMapper) {
            super(documentClient, config, objectMapper);
        }

        public void sendForApprovedConsentOrder(CallbackRequest callbackRequest, String authorisationToken) {
            generateApprovedConsentOrder(callbackRequest,authorisationToken);
        }
}