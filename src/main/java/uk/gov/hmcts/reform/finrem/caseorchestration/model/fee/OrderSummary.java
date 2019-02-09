package uk.gov.hmcts.reform.finrem.caseorchestration.model.fee;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderSummary {
    @JsonProperty("PaymentReference")
    private String paymentReference;

    @JsonProperty("PaymentTotal")
    private String paymentTotal;

    @JsonProperty("Fees")
    private List<FeeItem> fees;
}