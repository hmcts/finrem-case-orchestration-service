package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.formc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.FrcCourtDetails;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FormCLetterDetails implements DocumentTemplateDetails {
    private String divorceCaseNumber;
    private FrcCourtDetails courtDetails;
    @JsonProperty("applicantFMName")
    private String applicantFmName;
    private String applicantLName;
    @JsonProperty("respondentFMName")
    private String respondentFmName;
    private String respondentLName;
    private String solicitorReference;
    @JsonProperty("rSolicitorReference")
    private String respondentSolicitorReference;
    private String hearingDateLess35Days;
    private String hearingDateLess14Days;
    private String hearingDate;
    private String hearingTime;
    private String timeEstimate;
    private String additionalInformationAboutHearing;
    private String formCCreatedDate;
    private String eventDatePlus21Days;
}
