package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.suggested.SuggestedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadAgreedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadSuggestedDraftOrder;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DraftOrdersWrapper implements HasCaseDocument {

    private String typeOfDraftOrder;
    private YesOrNo showUploadPartyQuestion;

    @JsonProperty("uploadSuggestedDraftOrder")
    private UploadSuggestedDraftOrder uploadSuggestedDraftOrder;
    @JsonProperty("uploadAgreedDraftOrder")
    private UploadAgreedDraftOrder uploadAgreedDraftOrder;

    @JsonProperty("draftOrdersReviewCollection")
    private List<DraftOrdersReviewCollection> draftOrdersReviewCollection;

    @JsonProperty("agreedDraftOrderCollection")
    private List<AgreedDraftOrderCollection> agreedDraftOrderCollection;
    @JsonProperty("suggestedDraftOrderCollection")
    private List<SuggestedDraftOrderCollection> suggestedDraftOrderCollection;

    public void appendAgreedDraftOrderCollection(List<AgreedDraftOrderCollection> newAgreedDraftOrderCollection) {
        if (agreedDraftOrderCollection == null) {
            agreedDraftOrderCollection = new ArrayList<>();
        }
        agreedDraftOrderCollection.addAll(newAgreedDraftOrderCollection);
    }

    public void appendDraftOrdersReviewCollection(List<DraftOrdersReviewCollection> newDraftOrdersReviewCollection) {
        if (draftOrdersReviewCollection == null) {
            draftOrdersReviewCollection = new ArrayList<>();
        }
        draftOrdersReviewCollection.addAll(newDraftOrdersReviewCollection);
    }
}
