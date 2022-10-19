package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.MiamDomesticViolence;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.MiamExemption;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.MiamOtherGrounds;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.MiamPreviousAttendance;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.MiamUrgencyReason;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.YesOrNo;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MiamWrapper {
    @JsonProperty("applicantAttendedMIAM")
    private YesOrNo applicantAttendedMiam;
    @JsonProperty("claimingExemptionMIAM")
    private YesOrNo claimingExemptionMiam;
    @JsonProperty("familyMediatorMIAM")
    private YesOrNo familyMediatorMiam;
    @JsonProperty("MIAMExemptionsChecklist")
    private List<MiamExemption> miamExemptionsChecklist;
    @JsonProperty("MIAMDomesticViolenceChecklist")
    private List<MiamDomesticViolence> miamDomesticViolenceChecklist;
    @JsonProperty("MIAMUrgencyReasonChecklist")
    private List<MiamUrgencyReason> miamUrgencyReasonChecklist;
    @JsonProperty("MIAMPreviousAttendanceChecklist")
    private MiamPreviousAttendance miamPreviousAttendanceChecklist;
    @JsonProperty("MIAMOtherGroundsChecklist")
    private MiamOtherGrounds miamOtherGroundsChecklist;
}
