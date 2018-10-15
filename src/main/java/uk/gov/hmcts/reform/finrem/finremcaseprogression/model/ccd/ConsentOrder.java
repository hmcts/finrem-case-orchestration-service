package uk.gov.hmcts.reform.finrem.finremcaseprogression.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ConsentOrder {
    @JsonProperty("DocumentType")
    private String DocumentType;
    @JsonProperty("DocumentEmailContent")
    private String DocumentEmailContent;
    @JsonProperty("DocumentLink")
    private CaseDocument DocumentLink;
    @JsonProperty("DocumentDateAdded")
    private LocalDate DocumentDateAdded;
    @JsonProperty("DocumentComment")
    private String DocumentComment;
    @JsonProperty("DocumentFileName")
    private String DocumentFileName;
}
