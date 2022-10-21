package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.wrapper.CourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.wrapper.DefaultCourtListWrapper;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Court implements CourtListWrapper {
    private Region region;
    private RegionMidlandsFrc midlandsList;
    private RegionLondonFrc londonList;
    private RegionNorthWestFrc northWestList;
    private RegionNorthEastFrc northEastList;
    private RegionSouthEastFrc southEastList;
    private RegionSouthWestFrc southWestList;
    private RegionWalesFrc walesList;
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

    @JsonIgnore
    @Override
    public NottinghamCourt getNottinghamCourt() {
        return getDefaultCourtListWrapper().getNottinghamCourtList();
    }

    @JsonIgnore
    @Override
    public CfcCourt getCfcCourt() {
        return getDefaultCourtListWrapper().getCfcCourtList();
    }

    @JsonIgnore
    @Override
    public BirminghamCourt getBirminghamCourt() {
        return getDefaultCourtListWrapper().getBirminghamCourtList();
    }

    @JsonIgnore
    @Override
    public LiverpoolCourt getLiverpoolCourt() {
        return getDefaultCourtListWrapper().getLiverpoolCourtList();
    }

    @JsonIgnore
    @Override
    public ManchesterCourt getManchesterCourt() {
        return getDefaultCourtListWrapper().getManchesterCourtList();
    }

    @JsonIgnore
    @Override
    public LancashireCourt getLancashireCourt() {
        return getDefaultCourtListWrapper().getLancashireCourtList();
    }

    @JsonIgnore
    @Override
    public ClevelandCourt getClevelandCourt(boolean isConsented) {
        return isConsented ? getDefaultCourtListWrapper().getClevelandCourtList()
            : getDefaultCourtListWrapper().getCleavelandCourtList();
    }

    @JsonIgnore
    @Override
    public NwYorkshireCourt getNwYorkshireCourt() {
        return getDefaultCourtListWrapper().getNwYorkshireCourtList();
    }

    @JsonIgnore
    @Override
    public HumberCourt getHumberCourt() {
        return getDefaultCourtListWrapper().getHumberCourtList();
    }

    @JsonIgnore
    @Override
    public KentSurreyCourt getKentSurreyCourt() {
        return getDefaultCourtListWrapper().getKentSurreyCourtList();
    }

    @JsonIgnore
    @Override
    public BedfordshireCourt getBedfordshireCourt() {
        return getDefaultCourtListWrapper().getBedfordshireCourtList();
    }

    @JsonIgnore
    @Override
    public ThamesValleyCourt getThamesValleyCourt() {
        return getDefaultCourtListWrapper().getThamesValleyCourtList();
    }

    @JsonIgnore
    @Override
    public DevonCourt getDevonCourt() {
        return getDefaultCourtListWrapper().getDevonCourtList();
    }

    @JsonIgnore
    @Override
    public DorsetCourt getDorsetCourt() {
        return getDefaultCourtListWrapper().getDorsetCourtList();
    }

    @JsonIgnore
    @Override
    public BristolCourt getBristolCourt() {
        return getDefaultCourtListWrapper().getBristolCourtList();
    }

    @JsonIgnore
    @Override
    public NewportCourt getNewportCourt() {
        return getDefaultCourtListWrapper().getNewportCourtList();
    }

    @JsonIgnore
    @Override
    public SwanseaCourt getSwanseaCourt() {
        return getDefaultCourtListWrapper().getSwanseaCourtList();
    }

    @JsonIgnore
    @Override
    public NorthWalesCourt getNorthWalesCourt() {
        return getDefaultCourtListWrapper().getNorthWalesCourtList();
    }
}
