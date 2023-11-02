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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionHighCourtFrc;
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
public class HearingRegionWrapper {
    @JsonProperty("hearing_regionList")
    private Region hearingRegionList;
    @JsonProperty("hearing_midlandsFRCList")
    private RegionMidlandsFrc hearingMidlandsFrcList;
    @JsonProperty("hearing_londonFRCList")
    private RegionLondonFrc hearingLondonFrcList;
    @JsonProperty("hearing_northWestFRCList")
    private RegionNorthWestFrc hearingNorthWestFrcList;
    @JsonProperty("hearing_northEastFRCList")
    private RegionNorthEastFrc hearingNorthEastFrcList;
    @JsonProperty("hearing_southEastFRCList")
    private RegionSouthEastFrc hearingSouthEastFrcList;
    @JsonProperty("hearing_southWestFRCList")
    private RegionSouthWestFrc hearingSouthWestFrcList;
    @JsonProperty("hearing_walesFRCList")
    private RegionWalesFrc hearingWalesFrcList;
    @JsonProperty("hearing_highCourtFRCList")
    private RegionHighCourtFrc hearingHighCourtFrcList;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    HearingCourtWrapper courtListWrapper;

    @JsonIgnore
    public HearingCourtWrapper getCourtListWrapper() {
        if (courtListWrapper == null) {
            this.courtListWrapper = new HearingCourtWrapper();
        }
        return courtListWrapper;
    }
}