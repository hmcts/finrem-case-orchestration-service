package uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeeRequest {
    @JsonProperty(value = "calculated_amount")
    private BigDecimal calculatedAmount;

    @JsonProperty(value = "code")
    private String code;

    @JsonProperty(value = "version")
    private String version;

    @JsonProperty(value = "volume")
    @Builder.Default
    private int volume = 1;
}
