package uk.gov.hmcts.reform.finrem.caseorchestration.model.document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@EqualsAndHashCode
public class HelpWithFeesSuccessLetter {

    @JsonProperty("CaseNumber")
    private String caseNumber;

    @JsonProperty("Reference")
    private String reference;

    @JsonProperty("Addressee")
    private Addressee addressee;

    @JsonProperty("LetterDate")
    private String letterDate;

    @JsonProperty("ApplicantName")
    private String applicantName;

    @JsonProperty("RespondentName")
    private String respondentName;
}
