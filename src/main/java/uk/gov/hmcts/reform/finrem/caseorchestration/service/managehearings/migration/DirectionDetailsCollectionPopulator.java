package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.migration;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetail;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetailCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.MhMigrationWrapper;

import java.util.List;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;

@Component
@Slf4j
public class DirectionDetailsCollectionPopulator extends BasePopulator {

    private final HearingsAppender hearingsAppender;

    public DirectionDetailsCollectionPopulator(HearingsAppender hearingsAppender) {
        super(MhMigrationWrapper::getIsDirectionDetailsCollectionMigrated);
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
        List<DirectionDetail> directionDetailStream = emptyIfNull(caseData.getDirectionDetailsCollection()).stream()
                .map(DirectionDetailCollection::getValue)
                .filter(d -> YesOrNo.isYes(d.getIsAnotherHearingYN()))
                .toList();
        log.info("{} - Number of direction details to be migrated: {}", caseData.getCcdCaseId(), directionDetailStream.size());
        directionDetailStream.forEach(directionDetails -> {
            hearingsAppender.appendToHearings(caseData, ManageHearingsCollectionItem.builder().value(
                hearingsAppender.toHearing(directionDetails)).build());
        });

        caseData.getMhMigrationWrapper().setIsDirectionDetailsCollectionMigrated(YesOrNo.YES);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
