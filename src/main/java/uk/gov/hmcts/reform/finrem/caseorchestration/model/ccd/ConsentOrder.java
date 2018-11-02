package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ConsentOrder {
    @JsonProperty("DocumentType")
    private String documentType;
    @JsonProperty("DocumentEmailContent")
    private String documentEmailContent;
    @JsonProperty("DocumentLink")
    private CaseDocument documentLink;
    @JsonProperty("DocumentDateAdded")
    private Date documentDateAdded;
    @JsonProperty("DocumentComment")
    private String documentComment;
    @JsonProperty("DocumentFileName")
    private String documentFileName;
}