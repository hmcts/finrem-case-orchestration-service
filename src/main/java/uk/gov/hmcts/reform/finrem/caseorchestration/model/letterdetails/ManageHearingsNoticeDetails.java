package uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ManageHearingsNoticeDetails implements DocumentTemplateDetails {
    private String ccdCaseNumber;
    private String applicantName;
    private String respondentName;
    private String letterDate;
    private String hearingType;
    private String hearingDate;
    private String hearingTime;
    private String hearingTimeEstimate;
    private Map<String, Object> courtDetails;
    private String hearingVenue;
    private String attendance;
    private String additionalHearingInformation;
}
