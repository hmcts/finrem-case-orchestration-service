package uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.managehearings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CourtDetailsTemplateFields;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HearingNoticeLetterDetails implements DocumentTemplateDetails {
    // These are shared for both hearing notice and vacate hearing notice
    private String ccdCaseNumber;
    private String applicantName;
    private String respondentName;
    private String letterDate;
    private String hearingType;
    private String hearingDate;
    private String hearingTime;
    private String hearingTimeEstimate;
    private CourtDetailsTemplateFields courtDetails;
    private String hearingVenue;
    private String attendance;
    private String additionalHearingInformation;
    private String vacateHearingReasons;
    private String typeOfApplication;
    private String civilPartnership;
}
