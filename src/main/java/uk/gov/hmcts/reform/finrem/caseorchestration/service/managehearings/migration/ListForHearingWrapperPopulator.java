package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ListForHearingWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.MhMigrationWrapper;

/**
 * Populator component responsible for migrating hearing details from the
 * {@link ListForHearingWrapper} into both hearing tab items and hearings collections
 * within {@link FinremCaseData}, if migration has not yet occurred.
 *
 * <p>
 * This class uses {@link HearingsAppender} and {@link HearingTabItemsAppender}
 * to perform the actual appending and conversion logic.
 * </p>
 */
@Component
@Slf4j
public class ListForHearingWrapperPopulator implements Populator {

    private final HearingsAppender hearingsAppender;

    private final HearingTabItemsAppender hearingTabItemsAppender;

    public ListForHearingWrapperPopulator(HearingsAppender hearingsAppender,
                                          HearingTabItemsAppender hearingTabItemsAppender) {
        this.hearingsAppender = hearingsAppender;
        this.hearingTabItemsAppender = hearingTabItemsAppender;
    }

    /**
     * Determines whether the hearing list should be populated for the given case data.
     *
     * <p>
     * The population occurs only if the application is contested, the list for hearings
     * has not already been migrated, and the hearing type is set.
     * </p>
     *
     * @param caseData the case data to evaluate
     * @return {@code true} if the hearing list should be populated, {@code false} otherwise
     */
    @Override
    public boolean shouldPopulate(FinremCaseData caseData) {
        MhMigrationWrapper mhMigrationWrapper = caseData.getMhMigrationWrapper();
        ListForHearingWrapper listForHearingWrapper = caseData.getListForHearingWrapper();
        return caseData.isContestedApplication()
            && !YesOrNo.isYes(mhMigrationWrapper.getIsListForHearingsMigrated())
            && listForHearingWrapper.getHearingType() != null;
    }

    /**
     * Populates the hearing data into the case data.
     *
     * <p>
     * This method appends a hearing tab item and a hearing entry based on the current
     * state of the case data, and marks the list for hearings as migrated.
     * </p>
     *
     * @param caseData the case data to populate
     */
    @Override
    public void populate(FinremCaseData caseData) {
        ListForHearingWrapper listForHearingWrapper = caseData.getListForHearingWrapper();
        MhMigrationWrapper mhMigrationWrapper = caseData.getMhMigrationWrapper();

        hearingTabItemsAppender.appendToHearingTabItems(caseData, HearingTabCollectionItem.builder().value(
            hearingTabItemsAppender.toHearingTabItem(listForHearingWrapper)).build());
        hearingsAppender.appendToHearings(caseData, ManageHearingsCollectionItem.builder().value(
            hearingsAppender.toHearing(listForHearingWrapper)).build());

        mhMigrationWrapper.setIsListForHearingsMigrated(YesOrNo.YES);
    }
}
