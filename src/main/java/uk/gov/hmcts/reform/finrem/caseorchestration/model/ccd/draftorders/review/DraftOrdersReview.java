package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Reviewable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FRFlAssignToJudge;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_draftOrdersReview", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class DraftOrdersReview implements HasCaseDocument {
    @CCD(label = "Type of Hearing", searchable = false)
    private String hearingType;
    @CCD(label = "Hearing Date", searchable = false)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate hearingDate;
    @CCD(label = "Hearing Time", searchable = false)
    private String hearingTime;
    @CCD(
            label = "Hearing Judge",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_fl_AssignToJudge",
            typeParameterClass = FRFlAssignToJudge.class
    )
    private String hearingJudge;

    @CCD(
            label = "Uploaded draft orders",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_draftOrderDocumentReview"
    )
    @JsonProperty("draftOrderDocReviewCollection")
    private List<DraftOrderDocReviewCollection> draftOrderDocReviewCollection;
    @CCD(
            label = "Uploaded pension sharing annexes",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_psaDocumentReview"
    )
    @JsonProperty("psaDocReviewCollection")
    private List<PsaDocReviewCollection> psaDocReviewCollection;

    @JsonIgnore
    public List<DraftOrderDocReviewCollection> getDraftOrderDocReviewCollection() {
        if (this.draftOrderDocReviewCollection == null) {
            this.draftOrderDocReviewCollection = new ArrayList<>();
        }
        return this.draftOrderDocReviewCollection;
    }

    @JsonIgnore
    public List<PsaDocReviewCollection> getPsaDocReviewCollection() {
        if (this.psaDocReviewCollection == null) {
            this.psaDocReviewCollection = new ArrayList<>();
        }
        return this.psaDocReviewCollection;
    }

    @JsonIgnore
    public LocalDate getEarliestToBeReviewedOrderDate() {
        // Collect the concatenated streams into a list to avoid reusing the stream
        List<? extends Reviewable> reviewables = Stream.concat(
                ofNullable(draftOrderDocReviewCollection).orElse(List.of()).stream().map(DraftOrderDocReviewCollection::getValue),
                ofNullable(psaDocReviewCollection).orElse(List.of()).stream().map(PsaDocReviewCollection::getValue))
            .toList();

        // Process the collected list to find the earliest date
        return reviewables.stream()
            .filter(r -> OrderStatus.TO_BE_REVIEWED.equals(r.getOrderStatus()))
            .filter(r -> r.getNotificationSentDate() == null)
            .map(Reviewable::getSubmittedDate)
            .filter(Objects::nonNull)  // Ensure the date is not null
            .map(LocalDateTime::toLocalDate)  // Convert LocalDateTime to LocalDate
            .min(LocalDate::compareTo)  // Find the minimum LocalDate
            .orElse(null);  // Return null if no dates are found
    }

    @JsonIgnore
    public Reviewable getLatestToBeReviewedOrder() {
        // Collect reviewable items from both collections
        List<? extends Reviewable> reviewables = Stream.concat(
                ofNullable(draftOrderDocReviewCollection).orElse(List.of()).stream().map(DraftOrderDocReviewCollection::getValue),
                ofNullable(psaDocReviewCollection).orElse(List.of()).stream().map(PsaDocReviewCollection::getValue))
            .toList();

        // Find the item with the latest submission date
        return reviewables.stream()
            .filter(r -> OrderStatus.TO_BE_REVIEWED.equals(r.getOrderStatus()))
            .filter(r -> r.getSubmittedDate() != null)
            .max(Comparator.comparing(Reviewable::getSubmittedDate)) // Find the latest by submission date
            .orElse(null);
    }
}
