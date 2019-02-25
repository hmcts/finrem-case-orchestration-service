package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class CaseNotes {
    @JsonProperty("caseNoteAuthor")
    private String caseNoteAuthor;
    @JsonProperty("caseNoteDate")
    private Date caseNoteDate;
    @JsonProperty("caseNote")
    private String caseNote;
}