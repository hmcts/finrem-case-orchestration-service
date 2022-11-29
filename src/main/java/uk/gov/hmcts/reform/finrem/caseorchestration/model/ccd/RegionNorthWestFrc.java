package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public enum RegionNorthWestFrc {
    @JsonProperty("liverpool")
    LIVERPOOL,
    @JsonProperty("manchester")
    MANCHESTER,
    @JsonProperty("lancashire")
    LANCASHIRE
}
