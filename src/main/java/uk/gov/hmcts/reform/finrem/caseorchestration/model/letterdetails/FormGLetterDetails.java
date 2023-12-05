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
public class FormGLetterDetails implements DocumentTemplateDetails {
    private String divorceCaseNumber;
    private CourtDetailsTemplateFields courtDetails;
    @JsonProperty("applicantFMName")
    private String applicantFmName;
    private String applicantLName;
    @JsonProperty("respondentFMName")
    private String respondentFmName;
    private String respondentLName;
    private String solicitorReference;
    @JsonProperty("rSolicitorReference")
    private String respondentSolicitorReference;
    private String hearingDate;
    private String hearingTime;
}
