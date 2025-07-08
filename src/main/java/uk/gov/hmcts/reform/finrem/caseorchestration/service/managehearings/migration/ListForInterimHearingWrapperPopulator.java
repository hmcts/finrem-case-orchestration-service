package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.migration;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.InterimWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.MhMigrationWrapper;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;

@Component
@Slf4j
public class ListForInterimHearingWrapperPopulator extends BasePopulator {

    private final HearingTabItemsAppender hearingTabItemsAppender;

    private final HearingsAppender hearingsAppender;

    public ListForInterimHearingWrapperPopulator(HearingsAppender hearingsAppender,
                                                 HearingTabItemsAppender hearingTabItemsAppender) {
        super(MhMigrationWrapper::getIsListForInterimHearingsMigrated);
        this.hearingsAppender = hearingsAppender;
        this.hearingTabItemsAppender = hearingTabItemsAppender;
    }

    @Override
    public boolean shouldPopulate(FinremCaseData caseData) {
        InterimWrapper interimWrapper = caseData.getInterimWrapper();

        if (prePopulationChecksFailed(caseData)) {
            return false;
        }

        return !emptyIfNull(interimWrapper.getInterimHearings()).isEmpty();
    }

    @Override
    public void populate(FinremCaseData caseData) {
        InterimWrapper interimWrapper = caseData.getInterimWrapper();
        MhMigrationWrapper mhMigrationWrapper = caseData.getMhMigrationWrapper();
        interimWrapper.getInterimHearings().stream().map(InterimHearingCollection::getValue).forEach(interimHearing -> {
            hearingTabItemsAppender.appendToHearingTabItems(caseData, HearingTabCollectionItem.builder().value(
                    hearingTabItemsAppender.toHearingTabItem(interimHearing)).build());
            hearingsAppender.appendToHearings(caseData, ManageHearingsCollectionItem.builder().value(
                    hearingsAppender.toHearing(interimHearing)).build());
        });

        mhMigrationWrapper.setIsListForInterimHearingsMigrated(YesOrNo.YES);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
