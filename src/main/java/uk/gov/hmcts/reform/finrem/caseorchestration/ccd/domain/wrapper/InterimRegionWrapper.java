package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.wrapper;

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
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.RegionLondonFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.RegionMidlandsFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.RegionNorthEastFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.RegionNorthWestFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.RegionSouthEastFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.RegionSouthWestFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.RegionWalesFrc;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InterimRegionWrapper {
    @JsonProperty("interim_regionList")
    private Region interimRegionList;
    @JsonProperty("interim_midlandsFRCList")
    private RegionMidlandsFrc interimMidlandsFrcList;
    @JsonProperty("interim_londonFRCList")
    private RegionLondonFrc interimLondonFrcList;
    @JsonProperty("interim_northWestFRCList")
    private RegionNorthWestFrc interimNorthWestFrcList;
    @JsonProperty("interim_northEastFRCList")
    private RegionNorthEastFrc interimNorthEastFrcList;
    @JsonProperty("interim_southEastFRCList")
    private RegionSouthEastFrc interimSouthEastFrcList;
    @JsonProperty("interim_southWestFRCList")
    private RegionSouthWestFrc interimSouthWestFrcList;
    @JsonProperty("interim_walesFRCList")
    private RegionWalesFrc interimWalesFrcList;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.wrapper.InterimCourtListWrapper courtListWrapper;

    @JsonIgnore
    public uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.wrapper.InterimCourtListWrapper getCourtListWrapper() {
        if (courtListWrapper == null) {
            this.courtListWrapper = new InterimCourtListWrapper();
        }
        return courtListWrapper;
    }
}
