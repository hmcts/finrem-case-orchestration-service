package uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContestOrderApprovedLetterDetails implements DocumentTemplateDetails {
    @JsonProperty("ApplicantName")
    private String applicantName;
    @JsonProperty("RespondentName")
    private String respondentName;
    @JsonProperty("Court")
    private String court;
    @JsonProperty("JudgeDetails")
    private String judgeDetails;
    @JsonProperty("letterDate")
    private String letterDate;
    private String civilPartnership;
    private String divorceCaseNumber;
    private String orderApprovedDate;
}
