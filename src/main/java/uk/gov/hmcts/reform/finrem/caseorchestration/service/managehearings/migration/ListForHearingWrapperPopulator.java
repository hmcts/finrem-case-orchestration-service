package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ListForHearingWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.MhMigrationWrapper;

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

    @Override
    public boolean shouldPopulate(FinremCaseData caseData) {
        MhMigrationWrapper mhMigrationWrapper = caseData.getMhMigrationWrapper();
        ListForHearingWrapper listForHearingWrapper = caseData.getListForHearingWrapper();
        return caseData.isContestedApplication()
            && !YesOrNo.isYes(mhMigrationWrapper.getIsListForHearingsMigrated())
            && listForHearingWrapper.getHearingType() != null;
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
    @Override
    public void populate(FinremCaseData caseData) {
        ListForHearingWrapper listForHearingWrapper = caseData.getListForHearingWrapper();
        MhMigrationWrapper mhMigrationWrapper = caseData.getMhMigrationWrapper();

        hearingTabItemsAppender.appendToHearingTabItems(caseData, HearingTabCollectionItem.builder().value(
            hearingTabItemsAppender.toHearingTabItem(listForHearingWrapper)).build());
        hearingsAppender.appendToHearings(caseData, ManageHearingsCollectionItem.builder().value(
            hearingsAppender.toHearing(caseData)).build());

        mhMigrationWrapper.setIsListForHearingsMigrated(YesOrNo.YES);
    }
}
