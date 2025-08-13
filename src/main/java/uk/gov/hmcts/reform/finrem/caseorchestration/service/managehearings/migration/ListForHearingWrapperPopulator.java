package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.migration;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ListForHearingWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.MhMigrationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PartyService;

/**
 * Populator component responsible for migrating hearing details from the
 * {@link ListForHearingWrapper} into both hearing tab items and hearings collections
 * within {@link FinremCaseData}, if migration has not yet occurred.
 *
 * <p>
 * This class uses {@link HearingsAppender}
 * to perform the actual appending and conversion logic.
 * </p>
 */
@Component
@Slf4j
public class ListForHearingWrapperPopulator extends BasePopulator {

    private final HearingsAppender hearingsAppender;

    public ListForHearingWrapperPopulator(PartyService partyService, HearingsAppender hearingsAppender) {
        super(partyService, MhMigrationWrapper::getIsListForHearingsMigrated);
        this.hearingsAppender = hearingsAppender;
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
        if (prePopulationChecksFailed(caseData)) {
            return false;
        }
        ListForHearingWrapper listForHearingWrapper = caseData.getListForHearingWrapper();
        if (listForHearingWrapper.getHearingType() == null) {
            logReasonToSkip(caseData, "hearing type is null.");
            return false;
        }
        return true;
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
        hearingsAppender.appendToHearings(caseData, () -> {
            var hearing = hearingsAppender.toHearing(listForHearingWrapper);
            if (listForHearingWrapper.getHearingRegionWrapper().isEmpty()) {
                hearingsAppender.populateDefaultCourt(hearing, caseData);
            }
            return ManageHearingsCollectionItem.builder()
                .value(applyCommonMigratedValues(caseData, hearing))
                .build();
        });

        caseData.getMhMigrationWrapper().setIsListForHearingsMigrated(YesOrNo.YES);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
