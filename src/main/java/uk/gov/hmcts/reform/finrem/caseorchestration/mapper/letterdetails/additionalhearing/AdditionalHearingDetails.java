package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.additionalhearing;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.DocumentTemplateDetails;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdditionalHearingDetails implements DocumentTemplateDetails {
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
    private Date additionalHearingDated;
    @JsonProperty("CourtName")
    private String courtName;
    @JsonProperty("CourtAddress")
    private String courtAddress;
    @JsonProperty("CourtPhone")
    private String courtPhone;
    @JsonProperty("CourtEmail")
    private String courtEmail;
    @JsonProperty("CCDCaseNumber")
    private String ccdCaseNumber;
    @JsonProperty("DivorceCaseNumber")
    private String divorceCaseNumber;
    @JsonProperty("ApplicantName")
    private String applicantName;
    @JsonProperty("RespondentName")
    private String respondentName;
}
