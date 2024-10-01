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

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class DraftOrderReview implements HasCaseDocument {

    private List<AgreedDraftOrderCollection> agreedDraftOrderCollection;

    public LocalDate getHearingDate() {
        // TODO
        return LocalDate.of(2024, 1, 1);
    }

    public String getJudgeName() {
        // TODO
        return "FAKE JUDGE NAME";
    }

    public LocalDate getEarliestDraftOrderDate() {
        return agreedDraftOrderCollection.stream()
            .map(AgreedDraftOrderCollection::getValue)
            .map(AgreedDraftOrder::getSubmittedDate)
            .filter(Objects::nonNull)
            .min(Comparator.naturalOrder())
            .orElse(null);
    }


}
