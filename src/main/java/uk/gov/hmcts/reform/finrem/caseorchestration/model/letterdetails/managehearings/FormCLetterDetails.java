package uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.managehearings;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class FormCLetterDetails implements DocumentTemplateDetails {
    private String caseNumber;
    private CourtDetailsTemplateFields courtDetails;
    @JsonProperty(value = "applicantFMName")
    private String applicantFmName;
    private String applicantLName;
    @JsonProperty(value = "respondentFMName")
    private String respondentFmName;
    private String respondentLName;
    private String solicitorReference;
    private String rSolicitorReference;
    private String hearingDateLess35Days;
    private String hearingDateLess28Days;
    private String hearingDateLess21Days;
    private String hearingDateLess14Days;
    private String hearingDateLess7Days;
    private String hearingDate;
    private String hearingTime;
    private String timeEstimate;
    private String additionalInformationAboutHearing;
    private String attendance;
    private String formCCreatedDate;
    private String formCCreatedDatePlus28Days;
    private String eventDatePlus21Days;
}
