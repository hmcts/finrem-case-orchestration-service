package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.migration;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingDirectionDetail;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingDirectionDetailsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.MhMigrationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PartyService;

import java.util.List;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;

@Component
@Slf4j
public class HearingDirectionDetailsCollectionPopulator extends BasePopulator {

    private final HearingsAppender hearingsAppender;

    public HearingDirectionDetailsCollectionPopulator(PartyService partyService, HearingsAppender hearingsAppender) {
        super(partyService, MhMigrationWrapper::getIsHearingDirectionDetailsCollectionMigrated);
        this.hearingsAppender = hearingsAppender;
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
        List<HearingDirectionDetail> hearingDirectionDetails = emptyIfNull(caseData.getHearingDirectionDetailsCollection()).stream()
            .map(HearingDirectionDetailsCollection::getValue)
            .filter(d -> YesOrNo.isYes(d.getIsAnotherHearingYN()))
            .toList();
        log.info("{} - Number of hearing direction details to be migrated: {}", caseData.getCcdCaseId(), hearingDirectionDetails.size());
        hearingDirectionDetails.forEach(directionDetails ->
            hearingsAppender.appendToHearings(caseData, () -> ManageHearingsCollectionItem.builder().value(
                applyCommonMigratedValues(caseData, hearingsAppender.toHearing(directionDetails))
            ).build())
        );

        caseData.getMhMigrationWrapper().setIsHearingDirectionDetailsCollectionMigrated(YesOrNo.YES);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
