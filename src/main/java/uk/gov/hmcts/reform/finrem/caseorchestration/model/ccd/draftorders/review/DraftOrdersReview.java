package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasCaseDocument;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
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

    @JsonProperty("draftOrderDocReviewCollection")
    private List<DraftOrderDocReviewCollection> draftOrderDocReviewCollection;
    @JsonProperty("psaDocReviewCollection")
    private List<PsaDocReviewCollection> psaDocReviewCollection;

    private static final String HEARING_ID_FORMAT = "\\$\\$";

    public static Optional<LocalDate> parseHearingDate(String hearingId) {
        String[] parts = hearingId.split(HEARING_ID_FORMAT);
        if (parts.length > 0 && !parts[0].isEmpty()) {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return Optional.of(LocalDate.parse(parts[0], dateFormatter));
        }
        return Optional.empty();
    }

    public static Optional<String> parseHearingTime(String hearingId) {
        String[] parts = hearingId.split(HEARING_ID_FORMAT);
        if (parts.length > 1 && !parts[1].isEmpty()) {
            return Optional.of(parts[1]);
        }
        return Optional.empty();
    }
    
    public static Optional<String> parseHearingJudge(String hearingId) {
        String[] parts = hearingId.split(HEARING_ID_FORMAT);
        if (parts.length > 2 && !parts[2].isEmpty()) {
            return Optional.of(parts[2]);
        }
        return Optional.empty();
    }

    @JsonIgnore
    public String getHearingId() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = hearingDate != null ? hearingDate.format(dateFormatter) : "N/A";

        return String.format("%s$$%s$$%s", formattedDate,
            Objects.toString(hearingTime, "N/A"),
            Objects.toString(hearingJudge, "N/A"));
    }

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
}
