package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.MhMigrationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.migration.DirectionDetailsCollectionPopulator;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.migration.GeneralApplicationWrapperPopulator;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.migration.HearingDirectionDetailsCollectionPopulator;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.migration.ListForHearingWrapperPopulator;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.migration.ListForInterimHearingWrapperPopulator;

import java.util.List;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.utils.ListUtils.nullIfEmpty;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManageHearingsMigrationService {

    private final ListForHearingWrapperPopulator listForHearingWrapperPopulator;

    private final ListForInterimHearingWrapperPopulator listForInterimHearingWrapperPopulator;

    private final GeneralApplicationWrapperPopulator generalApplicationWrapperPopulator;

    private final DirectionDetailsCollectionPopulator directionDetailsCollectionPopulator;

    private final HearingDirectionDetailsCollectionPopulator hearingDirectionDetailsCollectionPopulator;

    private final ManageHearingActionService manageHearingActionService;

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
     * Populates the {@code ListForHearingWrapper} with hearing details if they have not already been migrated.
     *
     * <p>
     * This method checks whether migration is necessary by calling {@code shouldPopulate()} on the
     * {@code listForHearingWrapperPopulator}. If migration is required, it extracts hearing information such as
     * hearing type, date, time, duration, court, and additional notes, and populates a new hearing entry into
     * the case data. Once completed, the migration flag in {@code MhMigrationWrapper} is updated to indicate success.
     *
     * @param caseData the {@link FinremCaseData} containing hearing information and migration flags
     */

    public void populateListForHearingWrapper(FinremCaseData caseData) {
        if (!listForHearingWrapperPopulator.shouldPopulate(caseData)) {
            log.warn("{} - List for Hearing migration skipped.", caseData.getCcdCaseId());
            return;
        }
        listForHearingWrapperPopulator.populate(caseData);
    }

    /**
     * Populates the {@code ListForInterimHearingWrapper} with interim hearing details if they have not already been migrated.
     *
     * <p>
     * This method determines whether interim hearing migration is required by calling {@code shouldPopulate()} on
     * the {@code listForInterimHearingWrapperPopulator}. If so, it maps the relevant interim hearing fields into
     * the appropriate wrapper structure in {@link FinremCaseData}, and updates the migration flag accordingly.
     *
     * @param caseData the {@link FinremCaseData} containing interim hearing data and migration state
     */
    public void populateListForInterimHearingWrapper(FinremCaseData caseData) {
        if (!listForInterimHearingWrapperPopulator.shouldPopulate(caseData)) {
            log.warn("{} - List for Interim Hearing migration skipped.", caseData.getCcdCaseId());
            return;
        }

        listForInterimHearingWrapperPopulator.populate(caseData);
    }

    /**
     * Populates the general application wrapper with hearing details created through General Application Directions,
     * if they have not already been migrated.
     *
     * <p>
     * This method checks whether any hearing data created from General Application Directions events require migration
     * by invoking {@code shouldPopulate()} on the {@code generalApplicationWrapperPopulator}. If so, it migrates the
     * relevant data into the general application section of the case and sets the migration flag in {@code MhMigrationWrapper}.
     *
     * @param caseData the {@link FinremCaseData} containing general application and hearing data
     */
    public void populateGeneralApplicationWrapper(FinremCaseData caseData) {
        if (!generalApplicationWrapperPopulator.shouldPopulate(caseData)) {
            log.warn("{} - Existing hearings created with General Application Directions migration skipped.", caseData.getCcdCaseId());
            return;
        }

        generalApplicationWrapperPopulator.populate(caseData);
    }

    /**
     * Populates the {@code directionDetailsCollection} with hearing-related information extracted from Process Order events,
     * if they have not already been migrated.
     *
     * <p>
     * The method checks if migration is needed by calling {@code shouldPopulate()} on the
     * {@code directionDetailsCollectionPopulator}. If migration is necessary, it transforms Process Order-related hearing
     * data into direction details entries and adds them to the case data. It also marks the migration flag as complete.
     *
     * @param caseData the {@link FinremCaseData} containing direction order information and migration markers
     */
    public void populateDirectionDetailsCollection(FinremCaseData caseData) {
        if (!directionDetailsCollectionPopulator.shouldPopulate(caseData)) {
            log.warn("{} - Existing hearings created with Process Order migration skipped.", caseData.getCcdCaseId());
            return;
        }

        directionDetailsCollectionPopulator.populate(caseData);
    }

    /**
     * Populates the {@code hearingDirectionDetailsCollection} with hearing-related information extracted from
     * Upload Approved Order events, if they have not already been migrated.
     *
     * <p>
     * The method checks if migration is needed by calling {@code shouldPopulate()} on the
     * {@code hearingDirectionDetailsCollectionPopulator}. If migration is necessary, it transforms Upload Approved Order-related
     * hearing data into direction details entries and adds them to the case data. It also marks the migration flag as complete.
     *
     * @param caseData the {@link FinremCaseData} containing hearing direction order information and migration markers
     */
    public void populateHearingDirectionDetailsCollection(FinremCaseData caseData) {
        if (!hearingDirectionDetailsCollectionPopulator.shouldPopulate(caseData)) {
            log.warn("{} - Existing hearings created with Upload Approved Order migration skipped.", caseData.getCcdCaseId());
            return;
        }

        hearingDirectionDetailsCollectionPopulator.populate(caseData);
    }

    /**
     * Determines whether the case has already undergone Manage Hearings migration.
     *
     * @param caseData the case data to evaluate
     * @return {@code true} if migration has been marked; {@code false} otherwise
     */
    public boolean wasMigrated(FinremCaseData caseData) {
        MhMigrationWrapper mhMigrationWrapper = caseData.getMhMigrationWrapper();
        return mhMigrationWrapper.getMhMigrationVersion() != null;
    }

    /**
     * Executes the Manage Hearings data migration process on the given case data.
     *
     * <p>
     * The migration populates the necessary wrappers and collections required
     * for hearings and general applications, marks the migration version,
     * and updates the hearing tab data accordingly.
     *
     * @param caseData the case data to be migrated
     * @param mhMigrationVersion the version string indicating the migration applied
     */
    public void runManageHearingMigration(FinremCaseData caseData, String mhMigrationVersion) {
        populateListForHearingWrapper(caseData);
        populateListForInterimHearingWrapper(caseData);
        populateGeneralApplicationWrapper(caseData);
        populateDirectionDetailsCollection(caseData);
        populateHearingDirectionDetailsCollection(caseData);
        markCaseDataMigrated(caseData, mhMigrationVersion);
        // updates the hearing tab data accordingly.
        manageHearingActionService.updateTabData(caseData);
    }

    /**
     * Rolls back Manage Hearings migration data in the given case data.
     *
     * <p>This method clears all data in the {@code MhMigrationWrapper}.</p>
     *
     * <p>It then removes all hearings from the {@code ManageHearingsWrapper} where {@code wasMigrated} is {@code YES},
     * retaining only those hearings where {@code wasMigrated} is {@code NO} or {@code null}.
     * If no hearings remain after filtering, the hearings list is set to {@code null}.</p>
     *
     * <p>Finally, it updates the hearing tab data to reflect the rolled-back state.</p>
     *
     * @param caseData the financial remedy case data to roll back
     */

    public void revertManageHearingMigration(FinremCaseData caseData) {
        caseData.getMhMigrationWrapper().clearAll();
        List<ManageHearingsCollectionItem> hearings = caseData.getManageHearingsWrapper().getHearings();

        List<ManageHearingsCollectionItem> filteredHearings = emptyIfNull(hearings).stream()
            .filter(item -> item.getValue() != null && item.getValue().getWasMigrated() != YesOrNo.YES)
            .toList();
        caseData.getManageHearingsWrapper().setHearings(nullIfEmpty(filteredHearings));

        manageHearingActionService.updateTabData(caseData);
    }
}
