package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.migration;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.InterimWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.MhMigrationWrapper;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;

@Component
@Slf4j
public class ListForInterimHearingWrapperPopulator extends BasePopulator {

    private final HearingsAppender hearingsAppender;

    public ListForInterimHearingWrapperPopulator(HearingsAppender hearingsAppender) {
        super(MhMigrationWrapper::getIsListForInterimHearingsMigrated);
        this.hearingsAppender = hearingsAppender;
    }

    @Override
    public boolean shouldPopulate(FinremCaseData caseData) {
        if (prePopulationChecksFailed(caseData)) {
            return false;
        }
        InterimWrapper interimWrapper = caseData.getInterimWrapper();
        if (emptyIfNull(interimWrapper.getInterimHearings()).isEmpty()) {
            logReasonToSkip(caseData, "collection \"interimHearings\" is empty.");
            return false;
        }
        return true;
    }

    @Override
    public void populate(FinremCaseData caseData) {
        InterimWrapper interimWrapper = caseData.getInterimWrapper();
        interimWrapper.getInterimHearings().stream().map(InterimHearingCollection::getValue).forEach(interimHearing -> {
            hearingsAppender.appendToHearings(caseData, ManageHearingsCollectionItem.builder().value(
                    hearingsAppender.toHearing(interimHearing)).build());
        });

        caseData.getMhMigrationWrapper().setIsListForInterimHearingsMigrated(YesOrNo.YES);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
