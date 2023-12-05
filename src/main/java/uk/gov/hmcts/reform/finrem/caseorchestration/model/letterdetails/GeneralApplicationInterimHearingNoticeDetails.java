package uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CourtDetailsTemplateFields;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GeneralApplicationInterimHearingNoticeDetails implements DocumentTemplateDetails {
    private long ccdCaseNumber;
    private String divorceCaseNumber;
    private CourtDetailsTemplateFields courtDetails;
    private String applicantName;
    private String respondentName;
    private String interimHearingType;
    private String hearingVenue;
    private String interimHearingDate;
    private String interimHearingTime;
    private String interimTimeEstimate;
    private String interimAdditionalInformationAboutHearing;
    private String letterDate;
}
