package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.LetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssignedToJudgeDocumentService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final DocumentHelper documentHelper;
    private final LetterDetailsMapper letterDetailsMapper;

    public Document generateAssignedToJudgeNotificationLetter(FinremCaseDetails caseDetails, String authToken,
                                                              DocumentHelper.PaperNotificationRecipient recipient) {
        log.info("Generating Assigned To Judge Notification Letter {} from {} for bulk print for {}",
            documentConfiguration.getAssignedToJudgeNotificationTemplate(),
            documentConfiguration.getAssignedToJudgeNotificationFileName(),
            recipient);

        Map<String, Object> placeHoldersMap = letterDetailsMapper.getLetterDetailsAsMap(caseDetails, recipient,
            caseDetails.getCaseData().getRegionWrapper().getDefaultCourtList());

        Document generatedAssignedToJudgeNotificationLetter = genericDocumentService.generateDocumentFromPlaceholdersMap(
            authToken,
            placeHoldersMap,
            documentConfiguration.getAssignedToJudgeNotificationTemplate(),
            documentConfiguration.getAssignedToJudgeNotificationFileName());

        log.info("Generated Assigned To Judge Notification Letter: {}", generatedAssignedToJudgeNotificationLetter);

        return generatedAssignedToJudgeNotificationLetter;
    }

    public Document generateConsentInContestedAssignedToJudgeNotificationLetter(FinremCaseDetails caseDetails, String authToken,
                                                                                    DocumentHelper.PaperNotificationRecipient recipient) {
        Map<String, Object> placeholdersMap = letterDetailsMapper.getLetterDetailsAsMap(caseDetails, recipient,
            caseDetails.getCaseData().getRegionWrapper().getDefaultCourtList());
        return generateConsentInContestedAssignedToJudgeNotificationLetter(placeholdersMap, authToken);
    }

    private Document generateConsentInContestedAssignedToJudgeNotificationLetter(Map<String, Object> placeholdersMap,
                                                                                     String authToken) {
        log.info("Generating Consent in Contested Assigned To Judge Notification Letter {} from {} for bulk print",
            documentConfiguration.getConsentInContestedAssignedToJudgeNotificationTemplate(),
            documentConfiguration.getConsentInContestedAssignedToJudgeNotificationFileName());

        Document generatedAssignedToJudgeNotificationLetter = genericDocumentService.generateDocumentFromPlaceholdersMap(
            authToken,
            placeholdersMap,
            documentConfiguration.getConsentInContestedAssignedToJudgeNotificationTemplate(),
            documentConfiguration.getConsentInContestedAssignedToJudgeNotificationFileName());

        log.info("Generated Consent in Contested Assigned To Judge Notification Letter: {}", generatedAssignedToJudgeNotificationLetter);

        return generatedAssignedToJudgeNotificationLetter;
    }
}
