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
    @JsonProperty("otsw")
    private OrdersToShareWrapper ordersToShareWrapper;

    private SendOrderEventPostStateOption sendOrderPostStateOption;

    public OrdersToShareWrapper getOrdersToShareWrapper() {
        if (this.ordersToShareWrapper == null) {
            this.ordersToShareWrapper = new OrdersToShareWrapper();
        }
        return this.ordersToShareWrapper;
    }

}
