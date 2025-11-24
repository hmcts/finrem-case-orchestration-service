package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.migration;

import org.slf4j.Logger;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.PartyOnCase;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.PartyOnCaseCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.MhMigrationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PartyService;

import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class BasePopulator implements Populator {

    protected abstract Logger getLogger();

    Function<MhMigrationWrapper, YesOrNo> migrationFlagExtractor;

    protected final PartyService partyService;

    protected BasePopulator(PartyService partyService, Function<MhMigrationWrapper, YesOrNo> migrationFlagExtractor) {
        this.partyService = partyService;
        this.migrationFlagExtractor = migrationFlagExtractor;
    }

    boolean prePopulationChecksFailed(FinremCaseData caseData) {
        if (!caseData.isContestedApplication()) {
            logReasonToSkip(caseData, "it's not a contested application.");
            return true;
        }
        if (YesOrNo.isYes(migrationFlagExtractor.apply(caseData.getMhMigrationWrapper()))) {
            logReasonToSkip(caseData, "migration had been done.");
            return true;
        }
        return false;
    }

    protected void logReasonToSkip(FinremCaseData caseData, String reason) {
        getLogger().info("{} - Skip populate because {}", caseData.getCcdCaseId(), reason);
    }

    protected Hearing applyCommonMigratedValues(FinremCaseData caseData, Hearing hearing) {
        return applyCommonMigratedValues(partyService.getAllActivePartyList(caseData), hearing);
    }

    protected Hearing applyCommonMigratedValues(DynamicMultiSelectList partiesOnCaseMultiSelectList, Hearing hearing) {
        hearing.setPartiesOnCase(partiesOnCaseMultiSelectList.getValue().stream()
            .map(element -> PartyOnCaseCollectionItem.builder()
                .value(PartyOnCase.builder()
                    .role(element.getCode())
                    .label(element.getLabel())
                    .build())
                .build())
            .collect(Collectors.toList()));
        hearing.setWasMigrated(YesOrNo.YES);
        return hearing;
    }
}
