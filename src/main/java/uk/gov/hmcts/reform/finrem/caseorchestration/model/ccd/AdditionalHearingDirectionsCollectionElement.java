package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION_CT;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdditionalHearingDirectionsCollectionElement implements HearingLocationCollection {
    @JsonProperty("id")
    private String id;
    @JsonProperty("value")
    private AdditionalHearingDirectionsCollection additionalHearingDirections;

    @Override
    @JsonIgnore
    public String getRegion() {
        if (additionalHearingDirections == null) {
            return StringUtils.EMPTY;
        }

        return Objects.toString(additionalHearingDirections.getLocalCourt().get(REGION_CT), StringUtils.EMPTY);
    }
}
