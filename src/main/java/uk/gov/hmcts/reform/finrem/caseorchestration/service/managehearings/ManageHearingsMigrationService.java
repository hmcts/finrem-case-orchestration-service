package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.MhMigrationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.migration.GeneralApplicationWrapperPopulator;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.migration.ListForHearingWrapperPopulator;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.migration.ListForInterimHearingWrapperPopulator;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManageHearingsMigrationService {

    private final ListForHearingWrapperPopulator listForHearingWrapperPopulator;

    private final ListForInterimHearingWrapperPopulator listForInterimHearingWrapperPopulator;

    private final GeneralApplicationWrapperPopulator generalApplicationWrapperPopulator;

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
     *
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
        if (!listForHearingWrapperPopulator.shouldPopulate(caseData)) {
            log.warn("{} - List for Hearing migration skipped.", caseData.getCcdCaseId());
            return;
        }
        listForHearingWrapperPopulator.populate(caseData);
    }

    public void populateListForInterimHearingWrapper(FinremCaseData caseData) {
        if (!listForInterimHearingWrapperPopulator.shouldPopulate(caseData)) {
            log.warn("{} - List for Interim Hearing migration skipped.", caseData.getCcdCaseId());
            return;
        }

        listForInterimHearingWrapperPopulator.populate(caseData);
    }

    public void populateGeneralApplicationWrapper(FinremCaseData caseData) {
        if (!generalApplicationWrapperPopulator.shouldPopulate(caseData)) {
            log.warn("{} - Existing hearings created with General Application Directions migration skipped.", caseData.getCcdCaseId());
            return;
        }

        generalApplicationWrapperPopulator.populate(caseData);
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
}
