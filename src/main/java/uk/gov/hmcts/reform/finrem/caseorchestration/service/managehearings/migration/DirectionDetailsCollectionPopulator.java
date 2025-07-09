package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.migration;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
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
public class DirectionDetailsCollectionPopulator extends BasePopulator {

    private final HearingsAppender hearingsAppender;

    private final HearingTabItemsAppender hearingTabItemsAppender;

    public DirectionDetailsCollectionPopulator(HearingsAppender hearingsAppender,
                                               HearingTabItemsAppender hearingTabItemsAppender) {
        super(MhMigrationWrapper::getIsDirectionDetailsCollectionMigrated);
        this.hearingsAppender = hearingsAppender;
        this.hearingTabItemsAppender = hearingTabItemsAppender;
    }

    @Override
    public boolean shouldPopulate(FinremCaseData caseData) {
        return !prePopulationChecksFailed(caseData);
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
        // TODO
//        ListForHearingWrapper listForHearingWrapper = caseData.getListForHearingWrapper();
//        hearingTabItemsAppender.appendToHearingTabItems(caseData, HearingTabCollectionItem.builder().value(
//            hearingTabItemsAppender.toHearingTabItem(listForHearingWrapper)).build());
//        hearingsAppender.appendToHearings(caseData, ManageHearingsCollectionItem.builder().value(
//            hearingsAppender.toHearing(listForHearingWrapper)).build());

        caseData.getMhMigrationWrapper().setIsDirectionDetailsCollectionMigrated(YesOrNo.YES);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
