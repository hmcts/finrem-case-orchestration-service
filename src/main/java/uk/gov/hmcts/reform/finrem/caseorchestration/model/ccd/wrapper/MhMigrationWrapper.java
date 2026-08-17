package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesIwxsitAccess;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MhMigrationWrapper {

    @CCD(
            label = " ",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesIwxsitAccess.class}
    )
    private YesOrNo isListForHearingsMigrated;

    @CCD(
            label = " ",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesIwxsitAccess.class}
    )
    private YesOrNo isListForInterimHearingsMigrated;

    @CCD(
            label = " ",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesIwxsitAccess.class}
    )
    private YesOrNo isGeneralApplicationMigrated;

    @CCD(
            label = " ",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesIwxsitAccess.class}
    )
    private YesOrNo isDirectionDetailsCollectionMigrated;

    @CCD(
            label = " ",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesIwxsitAccess.class}
    )
    private YesOrNo isHearingDirectionDetailsCollectionMigrated;

    @CCD(
            label = "MH Migration Version",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesIwxsitAccess.class}
    )
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
        isHearingDirectionDetailsCollectionMigrated = null;
        mhMigrationVersion = null;
    }
}
