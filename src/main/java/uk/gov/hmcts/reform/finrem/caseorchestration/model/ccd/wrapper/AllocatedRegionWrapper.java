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
public class AllocatedRegionWrapper {

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
    @JsonProperty("highCourtFRCList")
    private RegionHighCourtFrc highCourtFrcList;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    DefaultCourtListWrapper courtListWrapper;

    @JsonIgnore
    public DefaultCourtListWrapper getDefaultCourtListWrapper() {
        if (courtListWrapper == null) {
            this.courtListWrapper = new DefaultCourtListWrapper();
        }
        return courtListWrapper;
    }
}
