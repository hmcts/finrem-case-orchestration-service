package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Reviewable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DraftOrdersReview implements HasCaseDocument {
    private String hearingType;
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate hearingDate;
    private String hearingTime;
    private String hearingJudge;

    private List<DraftOrderDocReviewCollection> draftOrderDocReviewCollection;
    private List<PsaDocReviewCollection> psaDocReviewCollection;

    public List<DraftOrderDocReviewCollection> getDraftOrderDocReviewCollection() {
        if (this.draftOrderDocReviewCollection == null) {
            this.draftOrderDocReviewCollection = new ArrayList<>();
        }
        return this.draftOrderDocReviewCollection;
    }

    public List<PsaDocReviewCollection> getPsaDocReviewCollection() {
        if (this.psaDocReviewCollection == null) {
            this.psaDocReviewCollection = new ArrayList<>();
        }
        return this.psaDocReviewCollection;
    }

    public LocalDate getEarliestToBeReviewedOrderDate() {
        // Collect the concatenated streams into a list to avoid reusing the stream
        List<? extends Reviewable> reviewables = Stream.concat(
                draftOrderDocReviewCollection.stream().map(DraftOrderDocReviewCollection::getValue),
                psaDocReviewCollection.stream().map(PsaDocReviewCollection::getValue))
            .toList();

        // Process the collected list to find the earliest date
        return reviewables.stream()
            .filter(r -> r.getOrderStatus() == null || OrderStatus.nonProcessedOrderStatuses().contains(r.getOrderStatus()))
            .filter(r -> r.getNotificationSentDate() == null)
            .map(Reviewable::getSubmittedDate)
            .filter(Objects::nonNull)  // Ensure the date is not null
            .map(LocalDateTime::toLocalDate)  // Convert LocalDateTime to LocalDate
            .min(LocalDate::compareTo)  // Find the minimum LocalDate
            .orElse(null);  // Return null if no dates are found
    }

}
