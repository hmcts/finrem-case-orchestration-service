package uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentRequest {
    @JsonProperty(value = "account_number")
    private String accountNumber;

    @JsonProperty(value = "ccd_case_number")
    private String ccdCaseNumber;

    @JsonProperty(value = "customer_reference")
    private String customerReference;

    @JsonProperty(value = "description")
    @Builder.Default
    private String description = "Financial Remedy Consented Application";

    @JsonProperty(value = "organisation_name")
    private String organisationName;

    @JsonProperty(value = "amount")
    private BigDecimal amount;

    @JsonProperty(value = "currency")
    @Builder.Default
    private String currency = "GBP";

    @JsonProperty(value = "service")
    @Builder.Default
    private String service = "FINREM";

    @JsonProperty(value = "case_type")
    private String caseType;

    @JsonProperty(value = "fees")
    private List<FeeRequest> feesList;
}
