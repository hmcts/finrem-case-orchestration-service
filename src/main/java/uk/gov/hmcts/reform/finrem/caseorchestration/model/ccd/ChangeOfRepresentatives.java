package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeOfRepresentatives {
    @JsonProperty("ChangeOfRepresentation")
    List<ChangeOfRepresentation> changeOfRepresentation;

    public void addChangeOfRepresentation(ChangeOfRepresentation toAdd) {
        changeOfRepresentation.add(toAdd);
    }
}
