package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssignedToJudgeDocumentService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final DocumentHelper documentHelper;

    public CaseDocument generateAssignedToJudgeNotificationLetter(CaseDetails caseDetails, String authToken) {
        log.info("Generating Assigned To Judge Notification Letter {} from {} for bulk print",
            documentConfiguration.getAssignedToJudgeNotificationTemplate(),
            documentConfiguration.getAssignedToJudgeNotificationFileName());

        CaseDetails caseDetailsForBulkPrint = documentHelper.prepareLetterToApplicantTemplateData(caseDetails);

        CaseDocument generatedAssignedToJudgeNotificationLetter = genericDocumentService.generateDocument(authToken,
            caseDetailsForBulkPrint,
            documentConfiguration.getAssignedToJudgeNotificationTemplate(),
            documentConfiguration.getAssignedToJudgeNotificationFileName());

        log.info("Generated Assigned To Judge Notification Letter: {}", generatedAssignedToJudgeNotificationLetter);

        return generatedAssignedToJudgeNotificationLetter;
    }

    public CaseDocument generateApplicantConsentInContestedAssignedToJudgeNotificationLetter(CaseDetails caseDetails, String authToken) {
        return generateConsentInContestedAssignedToJudgeNotificationLetter(
            documentHelper.prepareLetterToApplicantTemplateData(caseDetails), authToken);
    }

    public CaseDocument generateRespondentConsentInContestedAssignedToJudgeNotificationLetter(CaseDetails caseDetails, String authToken) {
        return generateConsentInContestedAssignedToJudgeNotificationLetter(
            documentHelper.prepareLetterToRespondentTemplateData(caseDetails), authToken);
    }

    private CaseDocument generateConsentInContestedAssignedToJudgeNotificationLetter(CaseDetails caseDetails, String authToken) {
        log.info("Generating Consent in Contested Assigned To Judge Notification Letter {} from {} for bulk print",
            documentConfiguration.getConsentInContestedAssignedToJudgeNotificationTemplate(),
            documentConfiguration.getConsentInContestedAssignedToJudgeNotificationFileName());

        CaseDocument generatedAssignedToJudgeNotificationLetter = genericDocumentService.generateDocument(authToken,
            caseDetails,
            documentConfiguration.getConsentInContestedAssignedToJudgeNotificationTemplate(),
            documentConfiguration.getConsentInContestedAssignedToJudgeNotificationFileName());

        log.info("Generated Consent in Contested Assigned To Judge Notification Letter: {}", generatedAssignedToJudgeNotificationLetter);

        return generatedAssignedToJudgeNotificationLetter;
    }
}
