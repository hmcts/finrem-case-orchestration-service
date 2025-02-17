package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.HasApprovable;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.WithAttachmentsCollection;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AgreedDraftOrderCollection implements HasCaseDocument, HasApprovable, WithAttachmentsCollection {
    private AgreedDraftOrder value;
}
