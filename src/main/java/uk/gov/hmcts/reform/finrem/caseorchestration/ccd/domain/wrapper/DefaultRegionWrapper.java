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
public class DefaultRegionWrapper {
    @JsonProperty("regionList")
    private Region regionList;
    @JsonProperty("midlandsFRCList")
    private RegionMidlandsFrc midlandsFrcList;
    @JsonProperty("londonFRCList")
    private RegionLondonFrc londonFrcList;
    @JsonProperty("northWestFRCList")
    private RegionNorthWestFrc northWestFrcList;
    @JsonProperty("northEastFRCList")
    private RegionNorthEastFrc northEastFrcList;
    @JsonProperty("southEastFRCList")
    private RegionSouthEastFrc southEastFrcList;
    @JsonProperty("southWestFRCList")
    private RegionSouthWestFrc southWestFrcList;
    @JsonProperty("walesFRCList")
    private RegionWalesFrc walesFrcList;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.wrapper.DefaultCourtListWrapper courtListWrapper;

    @JsonIgnore
    public uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.wrapper.DefaultCourtListWrapper getDefaultCourtListWrapper() {
        if (courtListWrapper == null) {
            this.courtListWrapper = new DefaultCourtListWrapper();
        }
        return courtListWrapper;
    }
}
