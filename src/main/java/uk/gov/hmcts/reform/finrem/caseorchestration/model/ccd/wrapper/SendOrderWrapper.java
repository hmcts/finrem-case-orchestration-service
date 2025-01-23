package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SendOrderEventPostStateOption;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SendOrderWrapper {

    private CaseDocument additionalDocument;

    @Getter(AccessLevel.NONE)
    @JsonProperty("ordersToSend")
    private OrdersToSend ordersToSend;

    private SendOrderEventPostStateOption sendOrderPostStateOption;

    public OrdersToSend getOrdersToSend() {
        if (this.ordersToSend == null) {
            this.ordersToSend = new OrdersToSend();
        }
        return this.ordersToSend;
    }

}
