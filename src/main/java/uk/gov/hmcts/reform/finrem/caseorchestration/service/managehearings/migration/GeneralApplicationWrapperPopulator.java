package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.migration;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationRegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.MhMigrationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PartyService;

@Component
@Slf4j
public class GeneralApplicationWrapperPopulator extends BasePopulator {

    private final HearingsAppender hearingsAppender;

    public GeneralApplicationWrapperPopulator(PartyService partyService, HearingsAppender hearingsAppender) {
        super(partyService, MhMigrationWrapper::getIsGeneralApplicationMigrated);
        this.hearingsAppender = hearingsAppender;
    }

    @Override
    public boolean shouldPopulate(FinremCaseData caseData) {
        if (prePopulationChecksFailed(caseData)) {
            return false;
        }

        GeneralApplicationWrapper generalApplicationWrapper = caseData.getGeneralApplicationWrapper();
        if (generalApplicationWrapper.getGeneralApplicationDirectionsHearingDate() == null) {
            logReasonToSkip(caseData, "hearing date is null.");
            return false;
        }

        return true;
    }

    @Override
    public void populate(FinremCaseData caseData) {
        GeneralApplicationWrapper generalApplicationWrapper = caseData.getGeneralApplicationWrapper();
        GeneralApplicationRegionWrapper generalApplicationRegionWrapper = caseData.getRegionWrapper().getGeneralApplicationRegionWrapper();
        hearingsAppender.appendToHearings(caseData, () -> ManageHearingsCollectionItem.builder().value(
            applyCommonMigratedValues(caseData, hearingsAppender.toHearing(generalApplicationWrapper, generalApplicationRegionWrapper))).build());

        caseData.getMhMigrationWrapper().setIsGeneralApplicationMigrated(YesOrNo.YES);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
