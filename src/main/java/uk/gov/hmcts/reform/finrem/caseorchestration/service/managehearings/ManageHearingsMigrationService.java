package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.tabdata.managehearings.HearingTabDataMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingTypeDirection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.HearingRegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ListForHearingWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.MhMigrationWrapper;

import java.time.LocalDate;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManageHearingsMigrationService {

    private final HearingTabDataMapper hearingTabDataMapper;

    /**
     * Marks the case data as migrated to a specified Manage Hearings migration version.
     *
     * @param caseData           the case data to update
     * @param mhMigrationVersion the version string representing the applied migration
     */
    public void markCaseDataMigrated(FinremCaseData caseData, String mhMigrationVersion) {
        caseData.getMhMigrationWrapper().setMhMigrationVersion(mhMigrationVersion);
    }

    /**
     * Populates a new {@link HearingTabItem} in the hearing tab of the case if the hearing
     * details have not already been migrated.
     * <p>
     * This method checks if the {@code ListForHearingWrapper} needs to be processed by evaluating
     * both the {@code MhMigrationWrapper} and the current state of the wrapper itself.
     * If migration is needed, it extracts hearing-related fields such as type, date, time,
     * time estimate, court information, and additional information.
     * These are used to build a new {@code HearingTabItem}, which is then added to the case data.
     * Once added, the migration flag is set to {@code YES}.
     *
     * @param caseData the {@link FinremCaseData} containing the hearing details and wrappers
     */
    public void populateListForHearingWrapper(FinremCaseData caseData) {
        ListForHearingWrapper listForHearingWrapper = caseData.getListForHearingWrapper();
        MhMigrationWrapper mhMigrationWrapper = caseData.getMhMigrationWrapper();

        if (shouldPopulateListForHearingWrapper(mhMigrationWrapper, listForHearingWrapper)) {
            // Type of Hearing
            HearingTypeDirection hearingType = listForHearingWrapper.getHearingType();
            // Hearing Date
            LocalDate hearingDate = listForHearingWrapper.getHearingDate();
            // Hearing Time
            String hearingTime = listForHearingWrapper.getHearingTime();
            // Time Estimate
            String timeEstimate = listForHearingWrapper.getTimeEstimate();
            // Additional information about the hearing
            String additionalInformationAboutHearing = listForHearingWrapper.getAdditionalInformationAboutHearing();
            // Hearing Court - Please state in which Financial Remedies Court Zone the applicant resides
            HearingRegionWrapper hearingRegionWrapper = listForHearingWrapper.getHearingRegionWrapper();

            HearingTabItem newHearingTabItem = HearingTabItem.builder()
                .tabHearingType(hearingType.getId())
                .tabCourtSelection(hearingTabDataMapper.getCourtName(hearingRegionWrapper.toCourt()))
                .tabDateTime(hearingTabDataMapper.getFormattedDateTime(hearingDate, hearingTime))
                .tabTimeEstimate(timeEstimate)
                //.tabConfidentialParties(getConfidentialParties(hearing))
                .tabAdditionalInformation(hearingTabDataMapper
                    .getAdditionalInformation(additionalInformationAboutHearing))
                //.tabHearingDocuments(mapHearingDocumentsToTabData(
                // hearingDocumentsCollection, hearingCollectionItem.getId(), hearing))
                .build();

            appendToHearingTabItems(caseData, HearingTabCollectionItem.builder().value(newHearingTabItem).build());
            caseData.getMhMigrationWrapper().setIsListForHearingsMigrated(YesOrNo.YES);
        }
    }

    /**
     * Determines whether the case has already undergone Manage Hearings migration.
     *
     * @param caseData the case data to evaluate
     * @return {@code true} if migration has been marked; {@code false} otherwise
     */
    public boolean wasMigrated(FinremCaseData caseData) {
        MhMigrationWrapper mhMigrationWrapper = caseData.getMhMigrationWrapper();
        return mhMigrationWrapper.getMhMigrationVersion()  != null;
    }

    /**
     * Determines whether hearing details should be migrated from the legacy fields to the modern structure.
     *
     * @param mhMigrationWrapper      the migration wrapper with migration flags
     * @param listForHearingWrapper   the wrapper containing legacy hearing data
     * @return {@code true} if hearing details should be populated; {@code false} otherwise
     */
    private boolean shouldPopulateListForHearingWrapper(MhMigrationWrapper mhMigrationWrapper,
                                                        ListForHearingWrapper listForHearingWrapper) {
        if (YesOrNo.isYes(mhMigrationWrapper.getIsListForHearingsMigrated())) {
            return false;
        }
        return listForHearingWrapper.getHearingType() != null;
    }

    private void appendToHearingTabItems(FinremCaseData caseData, HearingTabCollectionItem hearingTabCollectionItem) {
        ManageHearingsWrapper manageHearingsWrapper = caseData.getManageHearingsWrapper();
        if (manageHearingsWrapper.getHearingTabItems() == null) {
            manageHearingsWrapper.setHearingTabItems(new ArrayList<>());
        }
        manageHearingsWrapper.getHearingTabItems().add(hearingTabCollectionItem);
    }

}
