package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourtRefData {
    @JsonProperty("epimms_id")
    private String epimmsId;
    @JsonProperty("region_id")
    private String regionId;
    @JsonProperty("court_name")
    private String courtName;

}
