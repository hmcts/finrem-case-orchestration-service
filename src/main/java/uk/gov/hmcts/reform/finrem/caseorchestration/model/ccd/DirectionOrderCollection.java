package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.WithAttachmentsCollection;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DirectionOrderCollection implements HasCaseDocument, WithAttachmentsCollection, UploadedApprovedOrderHolder {

    @JsonProperty("id")
    private String id;
    @JsonProperty("value")
    private DirectionOrder value;

    public static final DirectionOrderCollection EMPTY_COLLECTION =
        DirectionOrderCollection.builder().value(DirectionOrder.builder().build()).build();
}
