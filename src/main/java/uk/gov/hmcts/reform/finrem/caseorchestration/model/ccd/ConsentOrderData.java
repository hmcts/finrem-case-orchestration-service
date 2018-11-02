package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ConsentOrderData {
    @JsonProperty("id")
    private String id;
    @JsonProperty("value")
    private ConsentOrder consentOrder;
}
