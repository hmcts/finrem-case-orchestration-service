package uk.gov.hmcts.reform.finrem.caseorchestration.model.document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class NoticeOfHearingLetterDetails {
    private Map<String, Object> courtDetails;
    private long ccdCaseNumber;
    private String divorceCaseNumber;
    private String applicantName;
    private String respondentName;
    private String hearingVenue;
    private String HearingDate;
    private String HearingTime;
    private String HearingLength;
    private String generalApplicationDirectionsHearingInformation;
    private String HearingType;
    private String letterDate;
}
