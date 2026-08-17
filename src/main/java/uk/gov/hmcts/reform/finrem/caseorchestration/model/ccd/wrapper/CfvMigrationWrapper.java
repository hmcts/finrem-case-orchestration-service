package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesEhdmahAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedySolicitorRPlus1RolesQrvrunAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedySuperuserRAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceSystemupdateCrudAccess;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CfvMigrationWrapper {
    @CCD(
            label = " ",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesEhdmahAccess.class, CaseworkerDivorceFinancialremedySolicitorRPlus1RolesQrvrunAccess.class, CaseworkerDivorceFinancialremedySuperuserRAccess.class}
    )
    private YesOrNo isCfvCategoriesAppliedFlag;
    @CCD(label = "CFV Migration Version", searchable = false, access = {CaseworkerDivorceSystemupdateCrudAccess.class})
    private String cfvMigrationVersion;
    @CCD(label = "CFV Migration Version", access = {CaseworkerDivorceSystemupdateCrudAccess.class})
    private String cfvSearchableMigrationVersion;
}
