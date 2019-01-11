package uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentRequest {
    @JsonProperty(value = "account_number")
    private String accountNumber;

    @JsonProperty(value = "amount")
    private long amount;

    @JsonProperty(value = "currency")
    private String currency = "GBP";

    @JsonProperty(value = "service")
    private String service = "FINREM";

    @JsonProperty(value = "fees")
    private List<FeeRequest> feesList;
}
