package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.contestordernotapproved;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.FrcCourtDetails;

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
}
