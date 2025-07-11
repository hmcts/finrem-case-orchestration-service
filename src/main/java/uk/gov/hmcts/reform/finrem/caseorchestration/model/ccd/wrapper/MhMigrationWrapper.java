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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MhMigrationWrapper {

    private YesOrNo isListForHearingsMigrated;

    private YesOrNo isListForInterimHearingsMigrated;

    private YesOrNo isGeneralApplicationMigrated;

    private YesOrNo isDirectionDetailsCollectionMigrated;

    private String mhMigrationVersion;

    public void clearAll() {
        isListForHearingsMigrated = null;
        isListForInterimHearingsMigrated = null;
        isGeneralApplicationMigrated = null;
        isDirectionDetailsCollectionMigrated = null;
        mhMigrationVersion = null;
    }
}
