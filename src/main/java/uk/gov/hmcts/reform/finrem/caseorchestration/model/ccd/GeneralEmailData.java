package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class GeneralEmailData {
    @JsonProperty("generalEmailRecipientAddress")
    private String generalEmailRecipientAddress;
    @JsonProperty("generalEmailCreatedByName")
    private String generalEmailCreatedByName;
    @JsonProperty("generalEmailBody")
    private String generalEmailBody;
}
