package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class ChangeOfRepresentatives {
    @JsonProperty("ChangeOfRepresentation")
    List<ChangeOfRepresentation> changeOfRepresentation;
}
