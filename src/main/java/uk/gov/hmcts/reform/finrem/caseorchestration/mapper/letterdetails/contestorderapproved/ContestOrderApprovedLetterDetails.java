package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.contestorderapproved;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContestOrderApprovedLetterDetails {
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
}
