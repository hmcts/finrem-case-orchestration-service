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
@NoArgsConstructor
@AllArgsConstructor
public class InterimUploadAdditionalDocument {
    @JsonProperty("document_url")
    public String documentUrl;
    @JsonProperty("document_filename")
    public String documentFilename;
    @JsonProperty("document_binary_url")
    public String documentBinaryUrl;
}

