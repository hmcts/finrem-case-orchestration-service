package uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CourtDetailsTemplateFields;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GeneralApplicationOrderDetails implements DocumentTemplateDetails {
    private CourtDetailsTemplateFields courtDetails;
    @JsonProperty("applicantName")
    private String applicantName;
    @JsonProperty("respondentName")
    private String respondentName;
    @JsonProperty("letterDate")
    private String letterDate;
    private String divorceCaseNumber;
    private String civilPartnership;
    private String generalApplicationDirectionsJudgeType;
    private String generalApplicationDirectionsJudgeName;
    private String generalApplicationDirectionsCourtOrderDate;
    private String generalApplicationDirectionsTextFromJudge;
}
