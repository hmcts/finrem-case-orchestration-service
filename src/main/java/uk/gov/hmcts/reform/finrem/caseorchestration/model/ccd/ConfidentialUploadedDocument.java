package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConfidentialUploadedDocument {

    @JsonProperty("DocumentType")
    private String documentType;

    @JsonProperty("DocumentLink")
    private CaseDocument documentLink;

    @JsonProperty("DocumentDateAdded")
    private String documentDateAdded;

    @JsonProperty("DocumentFileName")
    private String documentFileName;

    @JsonProperty("DocumentComment")
    private String documentComment;

}
