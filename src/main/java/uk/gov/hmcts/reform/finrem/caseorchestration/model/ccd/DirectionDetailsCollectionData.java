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
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DirectionDetailsCollectionData implements HearingLocationCollection {
    @JsonProperty("id")
    private String id;
    @JsonProperty("value")
    private DirectionDetailsCollection directionDetailsCollection;

    @Override
    @JsonIgnore
    public String getRegion() {
        if (this.directionDetailsCollection == null) {
            return StringUtils.EMPTY;
        }
        return Objects.toString(directionDetailsCollection.getLocalCourt().get(REGION_CT), StringUtils.EMPTY);
    }
}

