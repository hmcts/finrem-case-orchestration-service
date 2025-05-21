package uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.OrderSummary;

import java.math.BigDecimal;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentDetailsWrapper {
    private YesOrNo helpWithFeesQuestion;
    @JsonProperty("HWFNumber")
    private String hwfNumber;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal amountToPay;
    @JsonProperty("PBANumber")
    private String pbaNumber;
    @JsonProperty("PBAreference")
    private String pbaReference;
    @JsonProperty("PBAPaymentReference")
    private String pbaPaymentReference;
    private OrderSummary orderSummary;
}
