package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdditionalHearingDirectionsCollection {

    @JsonProperty("localCourt")
    private Map localCourt;

    @JsonProperty("hearingTime")
    private String hearingTime;

    @JsonProperty("timeEstimate")
    private String timeEstimate;

    @JsonProperty("dateOfHearing")
    private String dateOfHearing;

    @JsonProperty("typeOfHearing")
    private String typeOfHearing;

    @JsonProperty("isAnotherHearingYN")
    private String isAnotherHearingYN;

    @JsonProperty("isThisFinalYN")
    private String isThisFinalYN;

    @JsonProperty("anyOtherListingInstructions")
    private String anyOtherListingInstructions;
}
