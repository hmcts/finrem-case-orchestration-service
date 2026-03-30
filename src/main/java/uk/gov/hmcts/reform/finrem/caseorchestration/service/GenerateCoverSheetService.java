package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.bulkprint.BulkPrintCoverLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("java:S1133")
public class GenerateCoverSheetService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final BulkPrintCoverLetterDetailsMapper bulkPrintCoverLetterDetailsMapper;

    public CaseDocument generateApplicantCoverSheet(final FinremCaseDetails caseDetails, final String authorisationToken) {
        logCoverSheetGeneration(DocumentHelper.PaperNotificationRecipient.APPLICANT);

        return generateCoverSheet(caseDetails, authorisationToken, DocumentHelper.PaperNotificationRecipient.APPLICANT);
    }

    public void generateAndSetApplicantCoverSheet(FinremCaseDetails caseDetails, final String authorisationToken) {
        CaseDocument applicantCoverSheet = generateApplicantCoverSheet(caseDetails, authorisationToken);

        if (YesOrNo.isYes(caseDetails.getData().getContactDetailsWrapper().getApplicantAddressHiddenFromRespondent())) {
            log.info("Applicant has been marked as confidential, adding coversheet to confidential field for caseId {}", caseDetails.getId());
            caseDetails.getData().getBulkPrintCoversheetWrapper().setBulkPrintCoverSheetApp(null);
            caseDetails.getData().getBulkPrintCoversheetWrapper().setBulkPrintCoverSheetAppConfidential(applicantCoverSheet);
        } else {
            caseDetails.getData().getBulkPrintCoversheetWrapper().setBulkPrintCoverSheetApp(applicantCoverSheet);
        }
    }

    public CaseDocument generateRespondentCoverSheet(final FinremCaseDetails caseDetails, final String authorisationToken) {
        logCoverSheetGeneration(DocumentHelper.PaperNotificationRecipient.RESPONDENT);

        return generateCoverSheet(caseDetails, authorisationToken, DocumentHelper.PaperNotificationRecipient.RESPONDENT);
    }

    public void generateAndSetRespondentCoverSheet(FinremCaseDetails caseDetails, final String authorisationToken) {
        CaseDocument respondentCoverSheet = generateRespondentCoverSheet(caseDetails, authorisationToken);

        if (YesOrNo.isYes(caseDetails.getData().getContactDetailsWrapper().getRespondentAddressHiddenFromApplicant())) {
            log.info("Respondent has been marked as confidential, adding coversheet to confidential field for caseId {}", caseDetails.getId());
            caseDetails.getData().getBulkPrintCoversheetWrapper().setBulkPrintCoverSheetRes(null);
            caseDetails.getData().getBulkPrintCoversheetWrapper().setBulkPrintCoverSheetResConfidential(respondentCoverSheet);
        } else {
            caseDetails.getData().getBulkPrintCoversheetWrapper().setBulkPrintCoverSheetRes(respondentCoverSheet);
        }
    }

    public CaseDocument generateIntervenerCoverSheet(final FinremCaseDetails caseDetails,
                                                     final String authorisationToken,
                                                     DocumentHelper.PaperNotificationRecipient recipient) {
        log.info("Generating Intervener cover sheet {} from {} for bulk print", documentConfiguration.getBulkPrintFileName(),
            documentConfiguration.getBulkPrintTemplate());

        return generateCoverSheet(caseDetails, authorisationToken, recipient);
    }

    private CaseDocument generateCoverSheet(FinremCaseDetails caseDetails,
                                            String authorisationToken,
                                            DocumentHelper.PaperNotificationRecipient recipient) {

        Map<String, Object> placeholdersMap = bulkPrintCoverLetterDetailsMapper
            .getLetterDetailsAsMap(caseDetails, recipient, caseDetails.getData().getRegionWrapper().getDefaultCourtList());

        return genericDocumentService.generateDocumentFromPlaceholdersMap(authorisationToken, placeholdersMap,
            documentConfiguration.getBulkPrintTemplate(), documentConfiguration.getBulkPrintFileName(),
            caseDetails.getCaseType());
    }

    private void logCoverSheetGeneration(DocumentHelper.PaperNotificationRecipient recipient) {
        log.info("Generating {} cover sheet {} from {} for bulk print", recipient, documentConfiguration.getBulkPrintFileName(),
            documentConfiguration.getBulkPrintTemplate());
    }
}
