package uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.Address;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MiniFormADetails implements DocumentTemplateDetails {
    @JsonProperty("applicantFMName")
    private String applicantFmName;
    private String applicantLName;
    private String issueDate;
    private String divorceCaseNumber;
    private String solicitorName;
    private String solicitorFirm;
    private String solicitorReference;
    private Address solicitorAddress;
    @JsonProperty("appRespondentFMName")
    private String appRespondentFmName;
    private String appRespondentLName;
    private String appRespondentRep;
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
    private List<String> natureOfApplication2;
    private String natureOfApplication3a;
    private String natureOfApplication3b;
    private String orderForChildrenQuestion1;
    private String natureOfApplication5;
    private List<String> natureOfApplication6;
    private String natureOfApplication7;
    private String authorisationName;
    private String authorisationFirm;
    private String authorisation2b;
    private String authorisation3;
    private String orderType;
}
