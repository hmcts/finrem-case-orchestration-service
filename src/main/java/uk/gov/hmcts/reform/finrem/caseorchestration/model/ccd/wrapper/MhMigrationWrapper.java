package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.ALWAYS)
public class MhMigrationWrapper {

    private YesOrNo isListForHearingsMigrated;

    private YesOrNo isListForInterimHearingsMigrated;

    private YesOrNo isGeneralApplicationMigrated;

    private YesOrNo isDirectionDetailsCollectionMigrated;

    private String mhMigrationVersion;

    /**
     * Clears all Manage Hearings migration flags and version tracking.
     *
     * <p>
     * This method resets all migration-related fields to {@code null}, effectively
     * indicating that no migration has been applied or tracked. It is typically used
     * to reset the state before reapplying or verifying migration.
     */
    public void clearAll() {
        isListForHearingsMigrated = null;
        isListForInterimHearingsMigrated = null;
        isGeneralApplicationMigrated = null;
        isDirectionDetailsCollectionMigrated = null;
        mhMigrationVersion = null;
    }
}
