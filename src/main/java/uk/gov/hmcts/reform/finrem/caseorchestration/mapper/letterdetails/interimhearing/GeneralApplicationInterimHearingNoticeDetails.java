package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.interimhearing;

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
public class GeneralApplicationInterimHearingNoticeDetails implements DocumentTemplateDetails {
    private long ccdCaseNumber;
    private String divorceCaseNumber;
    private FrcCourtDetails courtDetails;
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
