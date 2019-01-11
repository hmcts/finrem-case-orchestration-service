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
public class PaymentResponse {
    @JsonProperty(value = "reference")
    private String reference;

    @JsonProperty(value = "status")
    private String status;

    @JsonProperty(value = "status_histories")
    private List<PaymentStatusHistory> statusHistories;

    public boolean isPaymentSuccess() {
        return status.equalsIgnoreCase("Success");
    }
}