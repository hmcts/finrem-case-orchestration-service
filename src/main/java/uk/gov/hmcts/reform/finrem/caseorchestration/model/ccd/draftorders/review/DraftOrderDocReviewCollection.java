package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.HasApprovable;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DraftOrderDocReviewCollection implements HasCaseDocument, HasApprovable {
    private DraftOrderDocumentReview value;
}
