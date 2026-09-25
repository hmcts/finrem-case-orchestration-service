package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
@AllArgsConstructor
public class CourtRefData {
    @JsonProperty("epimms_id")
    String epimmsId;
    @JsonProperty("region_id")
    String regionId;
    @JsonProperty("fr_court_name")
    String courtName;

}
