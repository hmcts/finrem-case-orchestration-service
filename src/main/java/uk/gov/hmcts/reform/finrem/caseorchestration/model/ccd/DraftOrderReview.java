package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class DraftOrderReview implements HasCaseDocument {

    private List<AgreedDraftOrderCollection> agreedDraftOrderCollection;

    public LocalDate getHearingDate() {
        // TODO Getting from hearingData in the following structure which is the key of DraftOrderReview
        // 1) DynamicList (contains hearingInfo)
        // 2) Judge Name
        return LocalDate.of(2024, 1, 1);
    }

    public String getJudgeName() {
        // TODO
        return "FAKE JUDGE NAME";
    }

    public LocalDate getEarliestToBeReviewedOrderDate() {
        return agreedDraftOrderCollection.stream()
            .map(AgreedDraftOrderCollection::getValue) // Get AgreedDraftOrder from the collection
            .flatMap(agreedDraftOrder -> Stream.concat(
                agreedDraftOrder.getDraftOrderCollection().stream()
                    .map(DraftOrderCollection::getValue)
                    .filter(draftOrder -> draftOrder.getStatus() == null
                        || OrderStatus.REVIEW_LATER.equals(draftOrder.getStatus())),
                agreedDraftOrder.getPensionSharingAnnexCollection().stream()
                    .map(PensionSharingAnnexCollection::getValue)
                    .filter(pensionSharingAnnex -> pensionSharingAnnex.getStatus() == null
                        || OrderStatus.REVIEW_LATER.equals(pensionSharingAnnex.getStatus()))
            ))
            .map(OrderToBeReviewed::getSubmittedDate) // Extract submittedDate
            .filter(Objects::nonNull) // Ensure the submittedDate is not null
            .min(Comparator.naturalOrder()) // Get the earliest date
            .orElse(null); // Return null if no valid date found
    }

}
