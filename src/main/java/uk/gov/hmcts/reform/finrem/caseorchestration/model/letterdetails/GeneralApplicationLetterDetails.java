package uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.FrcCourtDetails;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GeneralApplicationLetterDetails implements DocumentTemplateDetails {
    @JsonProperty("ccdCaseNumber")
    private String ccdCaseNumber;
    private String divorceCaseNumber;
    @JsonProperty("courtDetails")
    private FrcCourtDetails courtDetails;
    @JsonProperty("applicantName")
    private String applicantName;
    @JsonProperty("respondentName")
    private String respondentName;
    @JsonProperty("letterDate")
    private String letterDate;
    @JsonProperty("hearingVenue")
    private String hearingVenue;
    private String generalApplicationDirectionsHearingDate;
    private String generalApplicationDirectionsHearingTime;
    private String generalApplicationDirectionsHearingTimeEstimate;
    private String generalApplicationDirectionsAdditionalInformation;

}
