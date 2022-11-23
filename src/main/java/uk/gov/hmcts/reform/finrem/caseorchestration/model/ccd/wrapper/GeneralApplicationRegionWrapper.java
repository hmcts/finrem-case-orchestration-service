package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionLondonFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionMidlandsFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionNorthEastFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionNorthWestFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionSouthEastFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionSouthWestFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionWalesFrc;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeneralApplicationRegionWrapper {
    @JsonProperty("generalApplicationDirections_regionList")
    private Region generalApplicationDirectionsRegionList;
    @JsonProperty("generalApplicationDirections_midlandsFRCList")
    private RegionMidlandsFrc generalApplicationDirectionsMidlandsFrcList;
    @JsonProperty("generalApplicationDirections_londonFRCList")
    private RegionLondonFrc generalApplicationDirectionsLondonFrcList;
    @JsonProperty("generalApplicationDirections_northWestFRCList")
    private RegionNorthWestFrc generalApplicationDirectionsNorthWestFrcList;
    @JsonProperty("generalApplicationDirections_northEastFRCList")
    private RegionNorthEastFrc generalApplicationDirectionsNorthEastFrcList;
    @JsonProperty("generalApplicationDirections_southEastFRCList")
    private RegionSouthEastFrc generalApplicationDirectionsSouthEastFrcList;
    @JsonProperty("generalApplicationDirections_southWestFRCList")
    private RegionSouthWestFrc generalApplicationDirectionsSouthWestFrcList;
    @JsonProperty("generalApplicationDirections_walesFRCList")
    private RegionWalesFrc generalApplicationDirectionsWalesFrcList;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    GeneralApplicationCourtListWrapper courtListWrapper;

    @JsonIgnore
    public GeneralApplicationCourtListWrapper getCourtListWrapper() {
        if (courtListWrapper == null) {
            this.courtListWrapper = new GeneralApplicationCourtListWrapper();
        }
        return courtListWrapper;
    }
}
