package uk.gov.hmcts.reform.finrem.caseorchestration.model.fee;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeeResponse {
    @JsonProperty(value = "code")
    private String code;
    @JsonProperty(value = "description")
    private String description;
    @JsonProperty(value = "version")
    private String version;
    @JsonProperty(value = "fee_amount")
    private BigDecimal feeAmount;
}
