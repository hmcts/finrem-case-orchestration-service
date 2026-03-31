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
import java.util.function.Supplier;

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

    /**
     * Generates an applicant's cover sheet, determines whether it should be public or confidential
     * based on the applicant's address visibility settings, and stores the generated cover sheet
     * accordingly in the case data.
     *
     * @param caseDetails         the {@link FinremCaseDetails} object containing the case information
     * @param authorisationToken  the authorization token used to access and store the generated document
     */
    public void generateAndSetApplicantCoverSheet(FinremCaseDetails caseDetails, final String authorisationToken) {
        FinremCaseData caseData = getCaseData(caseDetails);

        replaceAndStoreCoverSheet(
            generateApplicantCoverSheet(caseDetails, authorisationToken),
            caseData.getContactDetailsWrapper().getApplicantAddressHiddenFromRespondent(),
            authorisationToken,
            caseData.getBulkPrintCoversheetWrapper()::getBulkPrintCoverSheetApp,
            caseData.getBulkPrintCoversheetWrapper()::getBulkPrintCoverSheetAppConfidential,
            caseData.getBulkPrintCoversheetWrapper()::setBulkPrintCoverSheetApp,
            caseData.getBulkPrintCoversheetWrapper()::setBulkPrintCoverSheetAppConfidential
        );
    }

    public CaseDocument generateRespondentCoverSheet(final FinremCaseDetails caseDetails, final String authorisationToken) {
        logCoverSheetGeneration(RESPONDENT);
        return generateCoverSheet(caseDetails, authorisationToken, RESPONDENT);
    }

    /**
     * Generates a respondent's cover sheet, determines if it should be public or confidential
     * based on the respondent's address visibility settings, and updates the case data with
     * the appropriate cover sheet.
     *
     * @param caseDetails         the {@link FinremCaseDetails} object containing the case information
     * @param authorisationToken  the authorization token used to access and store the generated document
     */
    public void generateAndSetRespondentCoverSheet(FinremCaseDetails caseDetails, final String authorisationToken) {
        FinremCaseData caseData = getCaseData(caseDetails);

        replaceAndStoreCoverSheet(
            generateRespondentCoverSheet(caseDetails, authorisationToken),
            caseData.getContactDetailsWrapper().getRespondentAddressHiddenFromApplicant(),
            authorisationToken,
            caseData.getBulkPrintCoversheetWrapper()::getBulkPrintCoverSheetRes,
            caseData.getBulkPrintCoversheetWrapper()::getBulkPrintCoverSheetResConfidential,
            caseData.getBulkPrintCoversheetWrapper()::setBulkPrintCoverSheetRes,
            caseData.getBulkPrintCoversheetWrapper()::setBulkPrintCoverSheetResConfidential
        );
    }

    public CaseDocument generateIntervenerCoverSheet(final FinremCaseDetails caseDetails,
                                                     final String authorisationToken,
                                                     DocumentHelper.PaperNotificationRecipient intervenerRecipient) {
        logCoverSheetGeneration(intervenerRecipient);
        return generateCoverSheet(caseDetails, authorisationToken, intervenerRecipient);
    }

    private void replaceAndStoreCoverSheet(CaseDocument coverSheet,
                                           YesOrNo hiddenFlag,
                                           String authToken,
                                           Supplier<CaseDocument> oldCoverSheetSupplier,
                                           Supplier<CaseDocument> oldCoverSheetConfidentialSupplier,
                                           Consumer<CaseDocument> publicSetter,
                                           Consumer<CaseDocument> confidentialSetter) {
        boolean isHiddenFromPublic = YesOrNo.isYes(hiddenFlag);
        CaseDocument oldCoverSheet = isHiddenFromPublic
            ? oldCoverSheetConfidentialSupplier.get()
            : oldCoverSheetSupplier.get();

        deleteOldCoversheet(oldCoverSheet, authToken);

        publicSetter.accept(isHiddenFromPublic ? null : coverSheet);
        confidentialSetter.accept(isHiddenFromPublic ? coverSheet : null);
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

    private FinremCaseData getCaseData(FinremCaseDetails caseDetails) {
        return caseDetails.getData();
    }

    private void deleteOldCoversheet(CaseDocument coverSheet, String authToken) {
        if (coverSheet != null) {
            genericDocumentService.deleteDocument(coverSheet.getDocumentUrl(), authToken);
        }
    }
}
