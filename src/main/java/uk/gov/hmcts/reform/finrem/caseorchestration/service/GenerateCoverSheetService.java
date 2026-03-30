package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.bulkprint.BulkPrintCoverLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.util.Map;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenerateCoverSheetService {

    private static final DocumentHelper.PaperNotificationRecipient APPLICANT =
        DocumentHelper.PaperNotificationRecipient.APPLICANT;
    private static final DocumentHelper.PaperNotificationRecipient RESPONDENT =
        DocumentHelper.PaperNotificationRecipient.RESPONDENT;

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final BulkPrintCoverLetterDetailsMapper bulkPrintCoverLetterDetailsMapper;

    public CaseDocument generateApplicantCoverSheet(final FinremCaseDetails caseDetails, final String authorisationToken) {
        logCoverSheetGeneration(APPLICANT);
        return generateCoverSheet(caseDetails, authorisationToken, APPLICANT);
    }

    public void generateAndSetApplicantCoverSheet(FinremCaseDetails caseDetails, final String authorisationToken) {
        FinremCaseData caseData = getCaseDaa(caseDetails);
        storeCoverSheet(
            caseDetails,
            generateApplicantCoverSheet(caseDetails, authorisationToken),
            caseData.getContactDetailsWrapper().getApplicantAddressHiddenFromRespondent(),
            caseData.getBulkPrintCoversheetWrapper()::setBulkPrintCoverSheetApp,
            caseData.getBulkPrintCoversheetWrapper()::setBulkPrintCoverSheetAppConfidential,
            APPLICANT
        );
    }

    public CaseDocument generateRespondentCoverSheet(final FinremCaseDetails caseDetails, final String authorisationToken) {
        logCoverSheetGeneration(RESPONDENT);
        return generateCoverSheet(caseDetails, authorisationToken, RESPONDENT);
    }

    public void generateAndSetRespondentCoverSheet(FinremCaseDetails caseDetails, final String authorisationToken) {
        FinremCaseData caseData = getCaseDaa(caseDetails);

        storeCoverSheet(
            caseDetails,
            generateRespondentCoverSheet(caseDetails, authorisationToken),
            caseData.getContactDetailsWrapper().getRespondentAddressHiddenFromApplicant(),
            caseData.getBulkPrintCoversheetWrapper()::setBulkPrintCoverSheetRes,
            caseData.getBulkPrintCoversheetWrapper()::setBulkPrintCoverSheetResConfidential,
            RESPONDENT
        );
    }

    public CaseDocument generateIntervenerCoverSheet(final FinremCaseDetails caseDetails,
                                                     final String authorisationToken,
                                                     DocumentHelper.PaperNotificationRecipient intervenerRecipient) {
        logCoverSheetGeneration(intervenerRecipient);
        return generateCoverSheet(caseDetails, authorisationToken, intervenerRecipient);
    }

    private void storeCoverSheet(FinremCaseDetails caseDetails,
                                 CaseDocument coverSheet,
                                 YesOrNo hiddenFlag,
                                 Consumer<CaseDocument> publicSetter,
                                 Consumer<CaseDocument> confidentialSetter,
                                 DocumentHelper.PaperNotificationRecipient recipient) {
        if (YesOrNo.isYes(hiddenFlag)) {
            log.info("{} has been marked as confidential, adding coversheet to confidential field for caseId {}",
                recipient, caseDetails.getId());
            publicSetter.accept(null);
            confidentialSetter.accept(coverSheet);
        } else {
            publicSetter.accept(coverSheet);
        }
    }

    private CaseDocument generateCoverSheet(FinremCaseDetails caseDetails,
                                            String authorisationToken,
                                            DocumentHelper.PaperNotificationRecipient recipient) {
        Map<String, Object> placeholdersMap = bulkPrintCoverLetterDetailsMapper.getLetterDetailsAsMap(
            caseDetails,
            recipient,
            caseDetails.getData().getRegionWrapper().getDefaultCourtList()
        );

        return genericDocumentService.generateDocumentFromPlaceholdersMap(
            authorisationToken,
            placeholdersMap,
            documentConfiguration.getBulkPrintTemplate(),
            documentConfiguration.getBulkPrintFileName(),
            caseDetails.getCaseType()
        );
    }

    private void logCoverSheetGeneration(DocumentHelper.PaperNotificationRecipient recipient) {
        log.info("Generating {} cover sheet {} from {} for bulk print",
            recipient,
            documentConfiguration.getBulkPrintFileName(),
            documentConfiguration.getBulkPrintTemplate());
    }

    private FinremCaseData getCaseDaa(FinremCaseDetails caseDetails) {
        return caseDetails.getData();
    }
}
