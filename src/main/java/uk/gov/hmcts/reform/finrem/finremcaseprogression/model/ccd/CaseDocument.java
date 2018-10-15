package uk.gov.hmcts.reform.finrem.finremcaseprogression.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class CaseDocument {
    @JsonProperty("document_url")
    private String documentUrl;
    @JsonProperty("document_filename")
    private String documentFilename;
    @JsonProperty("document_binary_url")
    private String documentBinaryUrl;
}
