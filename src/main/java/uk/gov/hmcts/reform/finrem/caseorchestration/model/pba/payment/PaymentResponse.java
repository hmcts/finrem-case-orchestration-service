package uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    /**
     * Response message from the payment service when a payment is identified as a duplicate.
     */
    public static final String DUPLICATE_PAYMENT_MESSAGE = "duplicate payment";
    private static final List<String> PAYMENT_STATUS_SUCCESS = List.of("success", "pending");

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
        return Optional.ofNullable(statusHistories).filter(s -> !s.isEmpty())
            .map(s -> s.getFirst().getErrorMessage()).orElse(message);
    }

    public boolean isPaymentSuccess() {
        return Optional.ofNullable(status)
                .map(s -> PAYMENT_STATUS_SUCCESS.contains(s.toLowerCase(ENGLISH)))
                .orElse(false);
    }

    public boolean isDuplicatePayment() {
        return Optional.ofNullable(error)
                .map(e -> e.equals(DUPLICATE_PAYMENT_MESSAGE))
                .orElse(false);
    }
}