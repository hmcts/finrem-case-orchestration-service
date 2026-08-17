package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.caseflag.caseflag.CaseFlag;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesEnaquoAccess;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaseFlagsWrapper {

    @CCD(
            label = "Case Flags",
            hint = "Case Flags",
            searchable = false,
            typeOverride = FieldType.Flags,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesEnaquoAccess.class}
    )
    private CaseFlag caseFlags;
    @CCD(
            label = "Flags for Applicant",
            hint = "Flags for Applicant",
            searchable = false,
            typeOverride = FieldType.Flags,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesEnaquoAccess.class}
    )
    private CaseFlag applicantFlags;
    @CCD(
            label = "Flags for Respondent",
            hint = "Flags for Respondent",
            searchable = false,
            typeOverride = FieldType.Flags,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesEnaquoAccess.class}
    )
    private CaseFlag respondentFlags;
}
