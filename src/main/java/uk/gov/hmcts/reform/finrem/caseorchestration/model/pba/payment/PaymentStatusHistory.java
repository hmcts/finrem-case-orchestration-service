package uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentStatusHistory {
    @JsonProperty(value = "status")
    private String status;

    @JsonProperty(value = "error_code")
    private String errorCode;

    @JsonProperty(value = "error_message")
    private String errorMessage;
}
