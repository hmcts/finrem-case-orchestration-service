package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.TemporaryField;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ExpressCaseParticipation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.LabelForExpressCaseAmendment;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesGpeopvAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyJudiciaryCrAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCruAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminRAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyJudiciaryCruAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesEnaquoAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyJudiciaryCrudAccess;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExpressCaseWrapper {

    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesGpeopvAccess.class, CaseworkerDivorceFinancialremedyJudiciaryCrAccess.class}
    )
    private ExpressCaseParticipation expressCaseParticipation;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Text,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesGpeopvAccess.class}
    )
    private LabelForExpressCaseAmendment labelForExpressCaseAmendment;
    @CCD(
            label = "Express Pilot?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceFinancialremedyCourtadminCruAccess.class}
    )
    private YesOrNo expressPilotQuestion;
    @CCD(
            label = " ",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceFinancialremedyCourtadminRAccess.class, CaseworkerDivorceFinancialremedyJudiciaryCruAccess.class}
    )
    private YesOrNo judgeAgreesCaseIsExpress;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.DynamicMultiSelectList,
            access = {CaseworkerDivorceFinancialremedyCourtadminCruAccess.class}
    )
    private DynamicMultiSelectList confirmRemoveCaseFromExpressPilot;

    @CCD(
            label = "   ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesEnaquoAccess.class}
    )
    @TemporaryField
    private YesOrNo shouldAllocateToExpressPilot;
    @CCD(
            label = "   ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceFinancialremedyJudiciaryCrudAccess.class}
    )
    @TemporaryField
    private YesOrNo showShouldAllocateToExpressPilot;
}
