package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasCaseDocument;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefusedOrderCollection implements HasCaseDocument {

    private UUID id;

    private RefusedOrder value;
}