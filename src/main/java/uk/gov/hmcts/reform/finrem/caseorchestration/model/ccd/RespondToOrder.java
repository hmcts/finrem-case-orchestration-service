package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class RespondToOrder {
    @JsonProperty("DocumentType")
    private String documentType;
    @JsonProperty("DocumentLink")
    private CaseDocument documentLink;
    @JsonProperty("DocumentDateAdded")
    private LocalDate documentAdded;
    @JsonProperty("DocumentFileName")
    private String fileName;
}
