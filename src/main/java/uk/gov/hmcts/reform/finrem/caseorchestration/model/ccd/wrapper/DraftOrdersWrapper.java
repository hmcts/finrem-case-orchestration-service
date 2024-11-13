package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeApproval;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.suggested.SuggestedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadAgreedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadSuggestedDraftOrder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.isJudgeReviewable;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DraftOrdersWrapper implements HasCaseDocument {

    private String typeOfDraftOrder;
    private YesOrNo showUploadPartyQuestion;

    @JsonProperty("hearingsReadyForReview")
    private DynamicList hearingsReadyForReview;

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

    @JsonProperty("judgeApproval")
    private JudgeApproval judgeApproval;

    @JsonIgnore
    public JudgeApproval getJudgeApproval() {
        if (judgeApproval == null) {
            this.judgeApproval = new JudgeApproval();
        }
        return judgeApproval;
    }

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

    @JsonIgnore
    public Stream<DraftOrdersReviewCollection> getOutstandingDraftOrdersReviewCollection() {
        Stream<DraftOrdersReviewCollection> draftOrdersStream = ofNullable(draftOrdersReviewCollection)
            .orElse(List.of())
            .stream()
            .filter(a -> a != null && a.getValue() != null)
            .filter(a -> a.getValue().getDraftOrderDocReviewCollection().stream()
                .anyMatch(draftOrderDoc -> isJudgeReviewable(draftOrderDoc.getValue().getOrderStatus()))
                || a.getValue().getPsaDocReviewCollection().stream()
                    .anyMatch(psa -> isJudgeReviewable(psa.getValue().getOrderStatus())));

        if (hearingsReadyForReview != null) {
            return draftOrdersStream.filter(a ->
                hearingsReadyForReview.getValueCode().equals(a.getValue().getHearingId()));
        } else {
            return draftOrdersStream.sorted(
                Comparator.comparing((DraftOrdersReviewCollection a) -> a.getValue().getHearingDate(), nullsLast(naturalOrder()))
                    .thenComparing(a -> a.getValue().getHearingTime(), nullsLast(naturalOrder()))
                    .thenComparing(a -> a.getValue().getHearingType(), nullsLast(naturalOrder())));
        }
    }

}
