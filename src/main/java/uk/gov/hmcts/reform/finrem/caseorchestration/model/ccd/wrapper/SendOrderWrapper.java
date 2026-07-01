package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.TemporaryField;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SendOrderEventPostStateOption;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SendOrderWrapper {

    @Deprecated
    private CaseDocument additionalDocument;

    @TemporaryField
    private List<DocumentCollectionItem> additionalDocuments;

    @TemporaryField
    private OrdersToSend ordersToSend;

    private SendOrderEventPostStateOption sendOrderPostStateOption;

    /**
     * This field is no longer in use and is deprecated since new draft order flow release.
     *
     * @deprecated This field is no longer in use and is deprecated since new draft order flow release.
     */
    @Deprecated
    private DynamicMultiSelectList ordersToShare;

}
