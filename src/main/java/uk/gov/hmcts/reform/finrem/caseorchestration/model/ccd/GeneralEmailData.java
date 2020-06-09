package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class GeneralEmailData {
    @JsonProperty("generalEmailRecipient")
    private String generalEmailRecipient;
    @JsonProperty("generalEmailCreatedBy")
    private String generalEmailCreatedBy;
    @JsonProperty("generalEmailBody")
    private String generalEmailBody;
}
