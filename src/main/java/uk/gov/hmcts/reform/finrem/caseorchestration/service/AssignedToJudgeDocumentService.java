package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssignedToJudgeDocumentService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final DocumentHelper documentHelper;

    @Deprecated
    public CaseDocument generateAssignedToJudgeNotificationLetter(CaseDetails caseDetails, String authToken,
                                                                  DocumentHelper.PaperNotificationRecipient recipient) {
        log.info("Generating Assigned To Judge Notification Letter {} from {} for bulk print for {}",
            documentConfiguration.getAssignedToJudgeNotificationTemplate(),
            documentConfiguration.getAssignedToJudgeNotificationFileName(),
            recipient);

        CaseDetails caseDetailsForBulkPrint = documentHelper.prepareLetterTemplateData(caseDetails, recipient);

        CaseDocument generatedAssignedToJudgeNotificationLetter =
            getCaseDocument(authToken, caseDetailsForBulkPrint);

        return generatedAssignedToJudgeNotificationLetter;
    }

    public CaseDocument generateAssignedToJudgeNotificationLetter(FinremCaseDetails caseDetails, String authToken,
                                                                  DocumentHelper.PaperNotificationRecipient recipient) {
        log.info("Generating Assigned To Judge Notification Letter {} from {} for bulk print for {}",
            documentConfiguration.getAssignedToJudgeNotificationTemplate(),
            documentConfiguration.getAssignedToJudgeNotificationFileName(),
            recipient);

        CaseDetails caseDetailsForBulkPrint = documentHelper.prepareLetterTemplateData(caseDetails, recipient);

        CaseDocument generatedAssignedToJudgeNotificationLetter =
            getCaseDocument(authToken, caseDetailsForBulkPrint);

        return generatedAssignedToJudgeNotificationLetter;
    }

    private CaseDocument getCaseDocument(String authToken, CaseDetails caseDetailsForBulkPrint) {

        CaseDocument generatedAssignedToJudgeNotificationLetter = genericDocumentService.generateDocument(authToken,
            caseDetailsForBulkPrint,
            documentConfiguration.getAssignedToJudgeNotificationTemplate(),
            documentConfiguration.getAssignedToJudgeNotificationFileName());

        log.info("Generated Assigned To Judge Notification Letter: {}", generatedAssignedToJudgeNotificationLetter);
        return generatedAssignedToJudgeNotificationLetter;
    }

    @Deprecated
    public CaseDocument generateConsentInContestedAssignedToJudgeNotificationLetter(CaseDetails caseDetails, String authToken,
                                                                                    DocumentHelper.PaperNotificationRecipient recipient) {
        final String templateId = documentConfiguration.getConsentInContestedAssignedToJudgeNotificationTemplate();
        final String templateName = documentConfiguration.getConsentInContestedAssignedToJudgeNotificationFileName();

        log.info("Generating Consent in Contested Assigned To Judge Notification Letter {} from {} for bulk print",
            templateId, templateName);

        CaseDetails caseDetailsForBulkPrint = documentHelper.prepareLetterTemplateData(caseDetails, recipient);

        CaseDocument generatedAssignedToJudgeNotificationLetter = genericDocumentService.generateDocument(authToken,
            caseDetailsForBulkPrint, templateId, templateName);

        log.info("Generated Consent in Contested Assigned To Judge Notification Letter: {}", generatedAssignedToJudgeNotificationLetter);
        return generatedAssignedToJudgeNotificationLetter;
    }

    public CaseDocument generateConsentInContestedAssignedToJudgeNotificationLetter(FinremCaseDetails caseDetails, String authToken,
                                                                                    DocumentHelper.PaperNotificationRecipient recipient) {
        final String templateId = documentConfiguration.getConsentInContestedAssignedToJudgeNotificationTemplate();
        final String templateName = documentConfiguration.getConsentInContestedAssignedToJudgeNotificationFileName();

        log.info("Generating Consent in Contested Assigned To Judge Notification Letter {} from {} for bulk print",
            templateId, templateName);
        CaseDetails caseDetailsForBulkPrint = documentHelper.prepareLetterTemplateData(caseDetails, recipient);

        CaseDocument generatedAssignedToJudgeNotificationLetter = genericDocumentService.generateDocument(authToken,
            caseDetailsForBulkPrint, templateId, templateName);

        log.info("Generated Consent in Contested Assigned To Judge Notification Letter: {}", generatedAssignedToJudgeNotificationLetter);

        return generatedAssignedToJudgeNotificationLetter;
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
