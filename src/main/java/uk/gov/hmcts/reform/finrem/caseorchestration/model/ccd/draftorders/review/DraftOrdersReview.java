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
import java.util.ArrayList;
import java.util.List;

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
