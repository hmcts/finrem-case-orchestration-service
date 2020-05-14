package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

@Service
@Slf4j
public class HelpWithFeesDocumentService extends AbstractDocumentService {

    @Autowired
    public HelpWithFeesDocumentService(DocumentClient documentClient, DocumentConfiguration config,
                                       ObjectMapper objectMapper) {
        super(documentClient, config, objectMapper);
    }

    public CaseDocument generateHwfSuccessfulNotificationLetter(CaseDetails caseDetails, String authToken) {
        log.info("Generating Help With Fees Successful Notification Letter {} from {} for bulk print",
            config.getHelpWithFeesSuccessfulNotificationFileName(),
            config.getHelpWithFeesSuccessfulNotificationTemplate());

        CaseDetails caseDetailsForBulkPrint = prepareNotificationLetter(caseDetails);

        CaseDocument generatedHwfSuccessfulNotificationLetter =
            generateDocument(authToken, caseDetailsForBulkPrint,
                config.getHelpWithFeesSuccessfulNotificationTemplate(),
                config.getHelpWithFeesSuccessfulNotificationFileName());

        log.info("Generated Help With Fees Successful Notification Letter: {}", generatedHwfSuccessfulNotificationLetter);

        return generatedHwfSuccessfulNotificationLetter;
    }
}