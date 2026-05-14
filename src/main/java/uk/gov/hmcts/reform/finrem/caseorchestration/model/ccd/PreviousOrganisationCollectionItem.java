package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PreviousOrganisationCollectionItem {

    @JsonProperty("id")
    private String id;

    @JsonProperty("value")
    private PreviousOrganisation value;
}
