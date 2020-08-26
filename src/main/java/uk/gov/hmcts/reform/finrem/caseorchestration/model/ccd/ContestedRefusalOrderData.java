package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ContestedRefusalOrderData {
    @JsonProperty("id")
    private String id;
    @JsonProperty("value")
    private ContestedRefusalOrder contestedRefusalOrder;
}
