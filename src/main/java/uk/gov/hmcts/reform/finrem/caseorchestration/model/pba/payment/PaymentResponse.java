package uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Optional;

import static java.util.Locale.ENGLISH;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentResponse {
    @JsonProperty(value = "reference")
    private String reference;

    @JsonProperty(value = "error")
    private String error;

    @JsonProperty(value = "message")
    private String message;

    @JsonProperty(value = "status")
    private String status;

    @JsonProperty(value = "status_histories")
    private List<PaymentStatusHistory> statusHistories;


    public String getPaymentError() {
        return Optional.ofNullable(statusHistories)
                .map(s -> (s.size() > 0 ? s.get(0).getErrorMessage() : message))
                .orElse(message);

    }

    public boolean isPaymentSuccess() {
        ImmutableList<String> paymentSuccess = ImmutableList.of("success", "pending");
        return Optional.ofNullable(status)
                .map(s -> paymentSuccess.contains(s.toLowerCase(ENGLISH)))
                .orElse(false);
    }
}