package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class PensionDocument {
    @JsonProperty("typeOfDocument")
    private String type;
    @JsonProperty("uploadedDocument")
    private CaseDocument document;
}
