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
public class ApprovedOrderNoticeOfHearingDetails implements DocumentTemplateDetails {
    @JsonProperty("HearingType")
    private String hearingType;
    @JsonProperty("HearingVenue")
    private String hearingVenue;
    @JsonProperty("HearingDate")
    private String hearingDate;
    @JsonProperty("HearingTime")
    private String hearingTime;
    @JsonProperty("HearingLength")
    private String hearingLength;
    @JsonProperty("AdditionalHearingDated")
    private String additionalHearingDated;
    @JsonProperty("CourtName")
    private String courtName;
    @JsonProperty("CourtAddress")
    private String courtAddress;
    @JsonProperty("CourtPhone")
    private String courtPhone;
    @JsonProperty("CourtEmail")
    private String courtEmail;
    @JsonProperty("CCDCaseNumber")
    private long ccdCaseNumber;
    @JsonProperty("DivorceCaseNumber")
    private String divorceCaseNumber;
    @JsonProperty("ApplicantName")
    private String applicantName;
    @JsonProperty("RespondentName")
    private String respondentName;
}
