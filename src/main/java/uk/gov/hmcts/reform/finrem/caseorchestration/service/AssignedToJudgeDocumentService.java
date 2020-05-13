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
public class AssignedToJudgeDocumentService extends AbstractDocumentService {

    @Autowired
    public AssignedToJudgeDocumentService(DocumentClient documentClient, DocumentConfiguration config,
                                          ObjectMapper objectMapper) {
        super(documentClient, config, objectMapper);
    }

    public CaseDocument generateAssignedToJudgeNotificationLetter(CaseDetails caseDetails, String authToken) {
        log.info("Generating Assigned To Judge Notification Letter {} from {} for bulk print",
                config.getAssignedToJudgeNotificationTemplate(),
                config.getAssignedToJudgeNotificationFileName());

        CaseDetails caseDetailsForBulkPrint = prepareNotificationLetter(caseDetails);

        CaseDocument generatedAssignedToJudgeNotificationLetter =
            generateDocument(authToken, caseDetailsForBulkPrint,
                config.getAssignedToJudgeNotificationTemplate(),
                config.getAssignedToJudgeNotificationFileName());

        log.info("Generated Assigned To Judge Notification Letter: {}", generatedAssignedToJudgeNotificationLetter);

        return generatedAssignedToJudgeNotificationLetter;
    }
}