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
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.APPSOLICITORCAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCruAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceSystemupdateCrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceSystemupdateCudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCruPlus1RolesChccdnAccess;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RefugeWrapper {

    @CCD(
            label = "Is the Respondent currently a resident in a refuge?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {APPSOLICITORCAccess.class, CaseworkerDivorceFinancialremedyCourtadminCruAccess.class, CaseworkerDivorceSystemupdateCrudAccess.class}
    )
    private YesOrNo respondentInRefugeQuestion;
    @CCD(
            label = "Is the Respondent currently a resident in a refuge?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceSystemupdateCudAccess.class}
    )
    private YesOrNo respondentInRefugeTab;
    @CCD(
            label = "Is the Applicant currently a resident in a refuge?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceFinancialremedyCourtadminCruPlus1RolesChccdnAccess.class, CaseworkerDivorceSystemupdateCrudAccess.class}
    )
    private YesOrNo applicantInRefugeQuestion;
    @CCD(
            label = "Is the Applicant currently a resident in a refuge?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceSystemupdateCudAccess.class}
    )
    private YesOrNo applicantInRefugeTab;

}
