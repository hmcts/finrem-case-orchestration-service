package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftOrderReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SuggestedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.UploadSuggestedDraftOrder;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DraftOrdersWrapper implements HasCaseDocument {

    private String typeOfDraftOrder;
    @JsonProperty("uploadSuggestedDraftOrder")
    private UploadSuggestedDraftOrder uploadSuggestedDraftOrder;
    //private UploadAgreedDraftOrder uploadAgreedDraftOrder;

    private List<AgreedDraftOrderCollection> agreedDraftOrderCollection;
    private List<SuggestedDraftOrder> suggestedDraftOrder;
    private List<DraftOrderReviewCollection> draftOrderReviewCollection;

    @JsonIgnore
    public UploadSuggestedDraftOrder getUploadSuggestedDraftOrder() {
        if (uploadSuggestedDraftOrder == null) {
            this.uploadSuggestedDraftOrder = new UploadSuggestedDraftOrder();
        }
        return uploadSuggestedDraftOrder;
    }
}
