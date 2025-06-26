package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingTypeDirection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsCollectionItem;
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
     * Migrates hearing information from the {@code ListForHearingWrapper} (legacy fields) to a {@link Hearing}
     * object and appends it to the {@code ManageHearingsWrapper} collection, if applicable.
     *
     * <p>This method checks if the case has already been migrated and if the hearing data is available. If both
     * conditions are met, a new hearing is constructed and added to the list of hearings.</p>
     *
     * @param caseData the case data containing hearing information to migrate
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

            Hearing newHearing = Hearing.builder()
                .hearingMode(null) // no Hearing attendance field
                .hearingDate(hearingDate)
                .hearingTime(hearingTime)
                .hearingType(hearingType.toHearingType())
                .additionalHearingInformation(additionalInformationAboutHearing)
                .hearingTimeEstimate(timeEstimate)
                .hearingCourtSelection(hearingRegionWrapper.toCourt())
                //.partiesOnCaseMultiSelectList(listForHearingWrapper)
                .build();

            appendToHearings(caseData, ManageHearingsCollectionItem.builder().value(newHearing).build());
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
        if (YesOrNo.isNoOrNull(mhMigrationWrapper.getIsListForHearingsMigrated())) {
            return false;
        }
        return listForHearingWrapper.getHearingType() != null;
    }

    /**
     * Appends a new hearing entry to the Manage Hearings collection. If the collection is not yet initialised,
     * it is created.
     *
     * @param caseData                     the case data to update
     * @param newManageHearingsCollectionItem the hearing item to add
     */
    private void appendToHearings(FinremCaseData caseData, ManageHearingsCollectionItem newManageHearingsCollectionItem) {
        ManageHearingsWrapper manageHearingsWrapper = caseData.getManageHearingsWrapper();
        if (manageHearingsWrapper.getHearings() == null) {
            manageHearingsWrapper.setHearings(new ArrayList<>());
        }
        manageHearingsWrapper.getHearings().add(newManageHearingsCollectionItem);
    }

}
