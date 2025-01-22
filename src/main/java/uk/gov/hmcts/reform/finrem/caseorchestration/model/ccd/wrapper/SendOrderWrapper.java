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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SendOrderEventPostStateOption;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SendOrderWrapper {

    private CaseDocument additionalDocument;

    @Deprecated
    private DynamicMultiSelectList ordersToShare;

    @Getter(AccessLevel.NONE)
    @JsonProperty("ordersToSend")
    private OrderToSend ordersToSend;

    private SendOrderEventPostStateOption sendOrderPostStateOption;

    public OrderToSend getOrdersToSend() {
        if (this.ordersToSend == null) {
            this.ordersToSend = new OrderToSend();
        }
        return this.ordersToSend;
    }

}
