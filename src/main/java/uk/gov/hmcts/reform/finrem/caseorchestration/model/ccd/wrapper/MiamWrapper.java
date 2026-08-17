package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamDomesticViolence;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamExemption;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGrounds;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGroundsV2;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamPreviousAttendance;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamPreviousAttendanceV2;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamUrgencyReason;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.DefaultAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesQgxjxfAccess;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MiamWrapper {
    @CCD(label = "        ", searchable = false, typeOverride = FieldType.YesOrNo, access = {DefaultAccess.class})
    @JsonProperty("applicantAttendedMIAM")
    private YesOrNo applicantAttendedMiam;
    @CCD(label = "        ", searchable = false, typeOverride = FieldType.YesOrNo, access = {DefaultAccess.class})
    @JsonProperty("claimingExemptionMIAM")
    private YesOrNo claimingExemptionMiam;
    @CCD(
            label = "        ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesQgxjxfAccess.class}
    )
    @JsonProperty("familyMediatorMIAM")
    private YesOrNo familyMediatorMiam;
    @CCD(
            label = "       ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "FR_ms_MIAMExemptionsChecklist",
            access = {DefaultAccess.class}
    )
    @JsonProperty("MIAMExemptionsChecklist")
    private List<MiamExemption> miamExemptionsChecklist;
    @CCD(
            label = "        ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "FR_ms_MIAMDomesticViolenceChecklist",
            access = {DefaultAccess.class}
    )
    @JsonProperty("MIAMDomesticViolenceChecklist")
    private List<MiamDomesticViolence> miamDomesticViolenceChecklist;
    @CCD(
            label = "        ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "FR_ms_MIAMUrgencyReasonChecklist",
            access = {DefaultAccess.class}
    )
    @JsonProperty("MIAMUrgencyReasonChecklist")
    private List<MiamUrgencyReason> miamUrgencyReasonChecklist;
    @CCD(
            label = "Select one",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesQgxjxfAccess.class}
    )
    @JsonProperty("MIAMPreviousAttendanceChecklist")
    private MiamPreviousAttendance miamPreviousAttendanceChecklist;
    @CCD(label = "Select one", searchable = false, access = {DefaultAccess.class})
    @JsonProperty("MIAMPreviousAttendanceChecklistV2")
    private MiamPreviousAttendanceV2 miamPreviousAttendanceChecklistV2;
    @CCD(
            label = "Select one",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesQgxjxfAccess.class}
    )
    @JsonProperty("MIAMOtherGroundsChecklist")
    private MiamOtherGrounds miamOtherGroundsChecklist;
    @CCD(label = "Select one", searchable = false, access = {DefaultAccess.class})
    @JsonProperty("MIAMOtherGroundsChecklistV2")
    private MiamOtherGroundsV2 miamOtherGroundsChecklistV2;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea, access = {DefaultAccess.class})
    @JsonProperty("evidenceUnavailableDomesticAbuseMIAM")
    private String evidenceUnavailableDomesticAbuseMiam;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea, access = {DefaultAccess.class})
    @JsonProperty("evidenceUnavailableUrgencyMIAM")
    private String evidenceUnavailableUrgencyMiam;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea, access = {DefaultAccess.class})
    @JsonProperty("evidenceUnavailablePreviousAttendanceMIAM")
    private String evidenceUnavailablePreviousAttendanceMiam;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea, access = {DefaultAccess.class})
    @JsonProperty("evidenceUnavailableOtherGroundsMIAM")
    private String evidenceUnavailableOtherGroundsMiam;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea, access = {DefaultAccess.class})
    @JsonProperty("additionalInfoOtherGroundsMIAM")
    private String additionalInfoOtherGroundsMiam;
}
