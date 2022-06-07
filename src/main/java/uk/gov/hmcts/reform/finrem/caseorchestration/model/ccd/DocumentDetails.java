package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDetails {

    @JsonProperty("DocumentFileName")
    private String documentFileName;

    @JsonProperty("DocumentDateAdded")
    private String documentDateAdded;

    @JsonProperty("DocumentType")
    private String documentType;

    @JsonProperty("remove")
    private String removeLink;
}
