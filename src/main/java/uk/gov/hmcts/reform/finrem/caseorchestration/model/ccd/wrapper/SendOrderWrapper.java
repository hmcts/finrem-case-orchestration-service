package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SendOrderEventPostStateOption;

import java.util.List;

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

    private List<OrderToShareCollection> ordersToShareCollection;

    private SendOrderEventPostStateOption sendOrderPostStateOption;

    private DynamicMultiSelectList partiesOnCase;

    @JsonIgnore
    public List<String> getSelectedParties() {
        DynamicMultiSelectList parties = this.getPartiesOnCase();
        return this.getSelectedParties(parties);
    }

    @JsonIgnore
    public List<String> getSelectedParties(DynamicMultiSelectList parties) {
        if (parties == null) {
            return List.of();
        }
        return parties.getValue().stream().map(DynamicMultiSelectListElement::getCode).toList();
    }

}
