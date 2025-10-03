package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DraftDirectionOrderCollection implements HasCaseDocument, UploadedApprovedOrderHolder {

    public static final DraftDirectionOrderCollection EMPTY_COLLECTION =
        DraftDirectionOrderCollection.builder().value(DraftDirectionOrder.builder().build()).build();

    private DraftDirectionOrder value;
}
