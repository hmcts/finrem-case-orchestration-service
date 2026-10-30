package uk.gov.hmcts.reform.finrem.caseorchestration.service.citizenui;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.BulkPrintCoversheetWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ccd.CoreCaseDataService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.INTERNAL_CHANGE_UPDATE_CASE;

/**
 * Persists applicant and respondent coversheet references after submission.
 *
 * <p>The supplied document has already been generated. This service updates
 * only the recipient's coversheet fields and, when enabled, the deletion bin.
 */
@Service
@RequiredArgsConstructor
public class CoverSheetPersistenceService {

    private static final String APPLICANT_COVERSHEET = "bulkPrintCoverSheetApp";
    private static final String RESPONDENT_COVERSHEET = "bulkPrintCoverSheetRes";
    private static final String APPLICANT_CONFIDENTIAL_COVERSHEET = "bulkPrintCoverSheetAppConfidential";
    private static final String RESPONDENT_CONFIDENTIAL_COVERSHEET = "bulkPrintCoverSheetResConfidential";
    private static final String BIN_FIELD = "bin_fileUrlsCollection";

    private final CoreCaseDataService coreCaseDataService;
    private final FinremCaseDetailsMapper caseDetailsMapper;
    private final FeatureToggleService featureToggleService;

    /**
     * Persists an already-generated coversheet using a separate CCD update.
     *
     * <p>Uses the latest case data to determine confidentiality and copies the saved
     * coversheet references back into the supplied case data. Intended for use after submission.
     *
     * @param caseDetails the case receiving the coversheet
     * @param recipient the applicant or respondent
     * @param coversheet the already-generated coversheet
     * @throws IllegalArgumentException if the recipient is not an applicant or respondent
     * @throws NullPointerException if the coversheet is null
     */
    public void saveCoverSheet(FinremCaseDetails caseDetails, PaperNotificationRecipient recipient, CaseDocument coversheet) {

        if (recipient != APPLICANT && recipient != RESPONDENT) {
            throw new IllegalArgumentException("Unsupported coversheet recipient: " + recipient);
        }

        Objects.requireNonNull(coversheet, "coversheet must not be null");

        CaseDetails updatedCase = coreCaseDataService.performPostSubmitCallback(
                caseDetails.getCaseType(),
                caseDetails.getId(),
                INTERNAL_CHANGE_UPDATE_CASE.getCcdType(),
                latestCase -> buildCoverSheetUpdate(latestCase, recipient, coversheet)
        );

        FinremCaseData savedData = caseDetailsMapper.mapToFinremCaseData(
                updatedCase.getData(), updatedCase.getCaseTypeId()
        );

        copySavedCoverSheet(caseDetails.getData(), savedData, recipient);
    }

    /**
     * Builds the CCD update for the recipient's coversheet using the latest case data.
     *
     * <p>Sets the appropriate public or confidential reference and clears the opposite
     * field. Includes the deletion bin only when replaced documents have been added.
     *
     * @param latestCase the latest case details retrieved for the CCD update
     * @param recipient the applicant or respondent
     * @param coversheet the new coversheet
     * @return the recipient's coversheet fields and, when changed, the deletion bin
     */
    private Map<String, Object> buildCoverSheetUpdate(CaseDetails latestCase, PaperNotificationRecipient recipient,
                                                      CaseDocument coversheet) {

        FinremCaseData caseData = caseDetailsMapper.mapToFinremCaseDetails(latestCase).getData();
        BulkPrintCoversheetWrapper wrapper = caseData.getBulkPrintCoversheetWrapper();
        boolean applicant = recipient == APPLICANT;

        // Capture both references before replacing them, including when confidentiality changes.
        List<CaseDocument> oldCoverSheets = applicant
                ? Arrays.asList(wrapper.getBulkPrintCoverSheetApp(), wrapper.getBulkPrintCoverSheetAppConfidential())
                : Arrays.asList(wrapper.getBulkPrintCoverSheetRes(), wrapper.getBulkPrintCoverSheetResConfidential());

        if (applicant) {
            setApplicantCoverSheet(caseData, coversheet);
        } else {
            setRespondentCoverSheet(caseData, coversheet);
        }

        boolean binUpdated = binOldCoverSheets(caseData, oldCoverSheets, coversheet);

        Map<String, Object> mappedData = caseDetailsMapper.finremCaseDataToMap(caseData);
        String publicField = applicant ? APPLICANT_COVERSHEET : RESPONDENT_COVERSHEET;
        String confidentialField = applicant
                ? APPLICANT_CONFIDENTIAL_COVERSHEET
                : RESPONDENT_CONFIDENTIAL_COVERSHEET;

        Map<String, Object> updates = new HashMap<>();
        updates.put(publicField, mappedData.get(publicField));
        updates.put(confidentialField, mappedData.get(confidentialField));

        if (binUpdated) {
            updates.put(BIN_FIELD, mappedData.get(BIN_FIELD));
        }

        return updates;
    }

    /**
     * Adds replaced coversheets to the deletion bin when the deletion toggle is enabled.
     *
     * <p>Ignores null documents, blank URLs and the new coversheet's URL.
     * Each distinct URL in the supplied list is added at most once per invocation.
     *
     * @param caseData the case data containing the deletion bin
     * @param oldCoverSheets the previous coversheet references, which may contain nulls
     * @param coversheet the new coversheet to exclude from deletion
     * @return true if at least one document was added to the bin; false otherwise
     */
    private boolean binOldCoverSheets(FinremCaseData caseData, List<CaseDocument> oldCoverSheets, CaseDocument coversheet) {

        if (!featureToggleService.isDeleteOldBpCoversheetEnabled()) {
            return false;
        }

        Set<String> binnedUrls = new HashSet<>();
        for (CaseDocument oldCoverSheet : oldCoverSheets) {
            if (oldCoverSheet != null
                    && StringUtils.isNotBlank(oldCoverSheet.getDocumentUrl())
                    && !Objects.equals(oldCoverSheet.getDocumentUrl(), coversheet.getDocumentUrl())
                    && binnedUrls.add(oldCoverSheet.getDocumentUrl())) {
                caseData.getBin().binCaseDocument(oldCoverSheet);
            }
        }

        return !binnedUrls.isEmpty();
    }

    /**
     * Sets the applicant's coversheet reference according to their address confidentiality.
     *
     * <p>Updates the case data in memory and clears the opposite coversheet field.
     *
     * @param caseData the case data to update
     * @param coversheet the new applicant coversheet
     */
    private void setApplicantCoverSheet(FinremCaseData caseData, CaseDocument coversheet) {

        BulkPrintCoversheetWrapper wrapper = caseData.getBulkPrintCoversheetWrapper();

        boolean confidential = YesOrNo.isYes(
                caseData.getContactDetailsWrapper().getApplicantAddressHiddenFromRespondent()
        );

        wrapper.setBulkPrintCoverSheetApp(confidential ? null : coversheet);
        wrapper.setBulkPrintCoverSheetAppConfidential(confidential ? coversheet : null);
    }

    /**
     * Sets the respondent's coversheet reference according to their address confidentiality.
     *
     * <p>Updates the case data in memory and clears the opposite coversheet field.
     *
     * @param caseData the case data to update
     * @param coversheet the new respondent coversheet
     */
    private void setRespondentCoverSheet(FinremCaseData caseData, CaseDocument coversheet) {

        BulkPrintCoversheetWrapper wrapper = caseData.getBulkPrintCoversheetWrapper();
        boolean confidential = YesOrNo.isYes(
                caseData.getContactDetailsWrapper().getRespondentAddressHiddenFromApplicant()
        );

        wrapper.setBulkPrintCoverSheetRes(confidential ? null : coversheet);
        wrapper.setBulkPrintCoverSheetResConfidential(confidential ? coversheet : null);
    }

    /**
     * Copies the recipient's persisted public and confidential coversheet references
     * into the current case data.
     *
     * <p>Leaves the other recipient's coversheet references unchanged.
     *
     * @param currentData the in-memory case data to update
     * @param savedData the case data returned by the CCD update
     * @param recipient the applicant or respondent whose references should be copied
     */
    private void copySavedCoverSheet(FinremCaseData currentData, FinremCaseData savedData, PaperNotificationRecipient recipient) {

        BulkPrintCoversheetWrapper current = currentData.getBulkPrintCoversheetWrapper();
        BulkPrintCoversheetWrapper saved = savedData.getBulkPrintCoversheetWrapper();

        if (recipient == APPLICANT) {
            current.setBulkPrintCoverSheetApp(saved.getBulkPrintCoverSheetApp());
            current.setBulkPrintCoverSheetAppConfidential(saved.getBulkPrintCoverSheetAppConfidential());
        } else {
            current.setBulkPrintCoverSheetRes(saved.getBulkPrintCoverSheetRes());
            current.setBulkPrintCoverSheetResConfidential(saved.getBulkPrintCoverSheetResConfidential());
        }
    }
}
