package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderRefusal;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class OrderRefusalData {
    @JsonProperty("id")
    private String id;
    @JsonProperty("value")
    private OrderRefusal orderRefusal;
}
