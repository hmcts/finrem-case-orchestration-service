package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.migration;

import org.slf4j.Logger;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.MhMigrationWrapper;

import java.util.function.Function;

public abstract class BasePopulator implements Populator {

    protected abstract Logger getLogger();

    Function<MhMigrationWrapper, YesOrNo> migrationFlagExtractor;

    public BasePopulator(Function<MhMigrationWrapper, YesOrNo> migrationFlagExtractor) {
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
}
