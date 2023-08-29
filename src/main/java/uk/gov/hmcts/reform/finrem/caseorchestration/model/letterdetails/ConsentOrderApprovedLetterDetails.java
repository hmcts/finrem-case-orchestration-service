package uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConsentOrderApprovedLetterDetails implements DocumentTemplateDetails {
    @JsonProperty("divorceCaseNumber")
    private String divorceCaseNumber;
    @JsonProperty("applicantFMName")
    private String applicantFirstName;
    @JsonProperty("applicantLName")
    private String applicantLastName;
    @JsonProperty("appRespondentFMName")
    private String respondentFirstName;
    @JsonProperty("appRespondentLName")
    private String respondentLastName;
    @JsonProperty("orderDirectionJudge")
    private String orderDirectionJudge;
    @JsonProperty("orderDirectionJudgeName")
    private String orderDirectionJudgeName;
    @JsonProperty("civilPartnership")
    private String civilPartnership;
    @JsonProperty("orderDirectionDate")
    private String orderDirectionDate;
    @JsonProperty("servePensionProviderResponsibility")
    private String servePensionProviderResponsibility;
    @JsonProperty("servePensionProvider")
    private String servePensionProvider;
    @JsonProperty("servePensionProviderOther")
    private String servePensionProviderOther;
    private String orderType;
}
