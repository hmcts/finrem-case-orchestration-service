package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.BedfordshireCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.BirminghamCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.BristolCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.CfcCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.ClevelandCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.DevonCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.DorsetCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.HumberCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.KentSurreyCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.LancashireCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.LiverpoolCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.ManchesterCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.NewportCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.NorthWalesCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.NottinghamCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.NwYorkshireCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.SwanseaCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.ThamesValleyCourt;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeneralApplicationCourtListWrapper implements CourtListWrapper {
    @JsonProperty("generalApplicationDirections_nottinghamCourtList")
    private NottinghamCourt generalApplicationDirectionsNottinghamCourtList;
    @JsonProperty("generalApplicationDirections_cfcCourtList")
    private CfcCourt generalApplicationDirectionsCfcCourtList;
    @JsonProperty("generalApplicationDirections_birminghamCourtList")
    private BirminghamCourt generalApplicationDirectionsBirminghamCourtList;
    @JsonProperty("generalApplicationDirections_liverpoolCourtList")
    private LiverpoolCourt generalApplicationDirectionsLiverpoolCourtList;
    @JsonProperty("generalApplicationDirections_manchesterCourtList")
    private ManchesterCourt generalApplicationDirectionsManchesterCourtList;
    @JsonProperty("generalApplicationDirections_lancashireCourtList")
    private LancashireCourt generalApplicationDirectionsLancashireCourtList;
    @JsonProperty("generalApplicationDirections_cleavelandCourtList")
    private ClevelandCourt generalApplicationDirectionsClevelandCourtList;
    @JsonProperty("generalApplicationDirections_nwyorkshireCourtList")
    private NwYorkshireCourt generalApplicationDirectionsNwYorkshireCourtList;
    @JsonProperty("generalApplicationDirections_humberCourtList")
    private HumberCourt generalApplicationDirectionsHumberCourtList;
    @JsonProperty("generalApplicationDirections_kentSurreyCourtList")
    private KentSurreyCourt generalApplicationDirectionsKentSurreyCourtList;
    @JsonProperty("generalApplicationDirections_bedfordshireCourtList")
    private BedfordshireCourt generalApplicationDirectionsBedfordshireCourtList;
    @JsonProperty("generalApplicationDirections_thamesvalleyCourtList")
    private ThamesValleyCourt generalApplicationDirectionsThamesValleyCourtList;
    @JsonProperty("generalApplicationDirections_devonCourtList")
    private DevonCourt generalApplicationDirectionsDevonCourtList;
    @JsonProperty("generalApplicationDirections_dorsetCourtList")
    private DorsetCourt generalApplicationDirectionsDorsetCourtList;
    @JsonProperty("generalApplicationDirections_bristolCourtList")
    private BristolCourt generalApplicationDirectionsBristolCourtList;
    @JsonProperty("generalApplicationDirections_newportCourtList")
    private NewportCourt generalApplicationDirectionsNewportCourtList;
    @JsonProperty("generalApplicationDirections_swanseaCourtList")
    private SwanseaCourt generalApplicationDirectionsSwanseaCourtList;
    @JsonProperty("generalApplicationDirections_northWalesCourtList")
    private NorthWalesCourt generalApplicationDirectionsNorthWalesCourtList;

    @JsonIgnore
    @Override
    public NottinghamCourt getNottinghamCourt() {
        return generalApplicationDirectionsNottinghamCourtList;
    }

    @JsonIgnore
    @Override
    public CfcCourt getCfcCourt() {
        return generalApplicationDirectionsCfcCourtList;
    }

    @JsonIgnore
    @Override
    public BirminghamCourt getBirminghamCourt() {
        return generalApplicationDirectionsBirminghamCourtList;
    }

    @JsonIgnore
    @Override
    public LiverpoolCourt getLiverpoolCourt() {
        return generalApplicationDirectionsLiverpoolCourtList;
    }

    @JsonIgnore
    @Override
    public ManchesterCourt getManchesterCourt() {
        return generalApplicationDirectionsManchesterCourtList;
    }

    @JsonIgnore
    @Override
    public LancashireCourt getLancashireCourt() {
        return generalApplicationDirectionsLancashireCourtList;
    }

    @JsonIgnore
    @Override
    public ClevelandCourt getClevelandCourt(boolean isConsented) {
        return generalApplicationDirectionsClevelandCourtList;
    }

    @JsonIgnore
    @Override
    public NwYorkshireCourt getNwYorkshireCourt() {
        return generalApplicationDirectionsNwYorkshireCourtList;
    }

    @JsonIgnore
    @Override
    public HumberCourt getHumberCourt() {
        return generalApplicationDirectionsHumberCourtList;
    }

    @JsonIgnore
    @Override
    public KentSurreyCourt getKentSurreyCourt() {
        return generalApplicationDirectionsKentSurreyCourtList;
    }

    @JsonIgnore
    @Override
    public BedfordshireCourt getBedfordshireCourt() {
        return generalApplicationDirectionsBedfordshireCourtList;
    }

    @JsonIgnore
    @Override
    public ThamesValleyCourt getThamesValleyCourt() {
        return generalApplicationDirectionsThamesValleyCourtList;
    }

    @JsonIgnore
    @Override
    public DevonCourt getDevonCourt() {
        return generalApplicationDirectionsDevonCourtList;
    }

    @JsonIgnore
    @Override
    public DorsetCourt getDorsetCourt() {
        return generalApplicationDirectionsDorsetCourtList;
    }

    @JsonIgnore
    @Override
    public BristolCourt getBristolCourt() {
        return generalApplicationDirectionsBristolCourtList;
    }

    @JsonIgnore
    @Override
    public NewportCourt getNewportCourt() {
        return generalApplicationDirectionsNewportCourtList;
    }

    @JsonIgnore
    @Override
    public SwanseaCourt getSwanseaCourt() {
        return generalApplicationDirectionsSwanseaCourtList;
    }

    @JsonIgnore
    @Override
    public NorthWalesCourt getNorthWalesCourt() {
        return generalApplicationDirectionsNorthWalesCourtList;
    }
}
