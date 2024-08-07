package uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PropertyAdjustmentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class ContestedMiniFormADetails implements DocumentTemplateDetails {
    private String fastTrackDecision;
    private String issueDate;
    private String divorceCaseNumber;
    private String caseNumber;
    private String typeOfApplication;
    private String applicantRepresented;
    @JsonProperty("applicantFMName")
    private String applicantFmName;
    private String applicantLName;
    private String applicantSolicitorName;
    private String applicantSolicitorFirm;
    private String solicitorReference;
    private Address applicantSolicitorAddress;
    private String applicantAddressConfidential;
    private Address applicantAddress;
    private String applicantPhone;
    private String applicantEmail;
    @JsonProperty("respondentFMName")
    private String respondentFmName;
    private String respondentLName;
    private String respondentRepresented;
    private String respondentAddressConfidential;
    private Address respondentAddress;
    private String respondentPhone;
    private String respondentEmail;
    @JsonProperty("rSolicitorName")
    private String respondentSolicitorName;
    @JsonProperty("rSolicitorFirm")
    private String respondentSolicitorFirm;
    @JsonProperty("rSolicitorReference")
    private String respondentSolicitorReference;
    @JsonProperty("rSolicitorAddress")
    private Address respondentSolicitorAddress;
    @JsonProperty("rSolicitorPhone")
    private String respondentSolicitorPhone;
    @JsonProperty("rSolicitorEmail")
    private String respondentSolicitorEmail;
    private List<String> natureOfApplicationChecklist;
    private List<String> natureOfApplicationChecklistSchedule;
    private String propertyAddress;
    private String mortgageDetail;
    List<PropertyAdjustmentOrderCollection> propertyAdjustmentOrderDetail;
    private String paymentForChildrenDecision;
    private YesOrNo benefitForChildrenDecision;
    private YesOrNo benefitForChildrenDecisionSchedule;
    private List<String> benefitPaymentChecklist;
    private List<String> benefitPaymentChecklistSchedule;
    private String natureOfApplication7;
    private String authorisationName;
    private String authorisationFirm;
    private String authorisation2b;
    private String authorisation3;
    private String mediatorRegistrationNumber;
    private String familyMediatorServiceName;
    private String soleTraderName;
    @JsonProperty("claimingExemptionMIAM")
    private String claimingExemptionMiam;
    @JsonProperty("familyMediatorMIAM")
    private String familyMediatorMiam;
    @JsonProperty("applicantAttendedMIAM")
    private String applicantAttendedMiam;
    @JsonProperty("MIAMExemptionsChecklist")
    private List<String> miamExemptionsChecklist;
    @JsonProperty("MIAMDomesticViolenceChecklist")
    private List<String> miamDomesticViolenceChecklist;
    @JsonProperty("MIAMUrgencyReasonChecklist")
    private List<String> miamUrgencyReasonChecklist;
    @JsonProperty("MIAMPreviousAttendanceChecklist")
    private String miamPreviousAttendanceChecklist;
    @JsonProperty("MIAMOtherGroundsChecklist")
    private String miamOtherGroundsChecklist;
    @JsonProperty("evidenceUnavailableDomesticAbuseMIAM")
    private String evidenceUnavailableDomesticAbuseMiam;
    @JsonProperty("evidenceUnavailableUrgencyMIAM")
    private String evidenceUnavailableUrgencyMiam;
    @JsonProperty("evidenceUnavailablePreviousAttendanceMIAM")
    private String evidenceUnavailablePreviousAttendanceMiam;
    @JsonProperty("evidenceUnavailableOtherGroundsMIAM")
    private String evidenceUnavailableOtherGroundsMiam;
    @JsonProperty("additionalInfoOtherGroundsMIAM")
    private String additionalInfoOtherGroundsMiam;
}
