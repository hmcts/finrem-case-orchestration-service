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
public class ContestedDraftOrderNotApprovedDetails implements DocumentTemplateDetails {
    @JsonProperty("ApplicantName")
    private String applicantName;
    @JsonProperty("RespondentName")
    private String respondentName;
    @JsonProperty("Court")
    private String court;
    @JsonProperty("JudgeDetails")
    private String judgeDetails;
    @JsonProperty("ContestOrderNotApprovedRefusalReasonsFormatted")
    private String contestOrderNotApprovedRefusalReasons;
    private String divorceCaseNumber;
    private String civilPartnership;
    private String refusalOrderDate;
}
