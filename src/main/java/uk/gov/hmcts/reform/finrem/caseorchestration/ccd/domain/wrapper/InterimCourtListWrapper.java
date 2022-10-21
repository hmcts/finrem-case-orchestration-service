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
public class InterimCourtListWrapper implements CourtListWrapper {
    @JsonProperty("interim_nottinghamCourtList")
    private NottinghamCourt interimNottinghamCourtList;
    @JsonProperty("interim_cfcCourtList")
    private CfcCourt interimCfcCourtList;
    @JsonProperty("interim_birminghamCourtList")
    private BirminghamCourt interimBirminghamCourtList;
    @JsonProperty("interim_liverpoolCourtList")
    private LiverpoolCourt interimLiverpoolCourtList;
    @JsonProperty("interim_manchesterCourtList")
    private ManchesterCourt interimManchesterCourtList;
    @JsonProperty("interim_lancashireCourtList")
    private LancashireCourt interimLancashireCourtList;
    @JsonProperty("interim_cleavelandCourtList")
    private ClevelandCourt interimClevelandCourtList;
    @JsonProperty("interim_nwyorkshireCourtList")
    private NwYorkshireCourt interimNwYorkshireCourtList;
    @JsonProperty("interim_humberCourtList")
    private HumberCourt interimHumberCourtList;
    @JsonProperty("interim_kentSurreyCourtList")
    private KentSurreyCourt interimKentSurreyCourtList;
    @JsonProperty("interim_bedfordshireCourtList")
    private BedfordshireCourt interimBedfordshireCourtList;
    @JsonProperty("interim_thamesvalleyCourtList")
    private ThamesValleyCourt interimThamesValleyCourtList;
    @JsonProperty("interim_devonCourtList")
    private DevonCourt interimDevonCourtList;
    @JsonProperty("interim_dorsetCourtList")
    private DorsetCourt interimDorsetCourtList;
    @JsonProperty("interim_bristolCourtList")
    private BristolCourt interimBristolCourtList;
    @JsonProperty("interim_newportCourtList")
    private NewportCourt interimNewportCourtList;
    @JsonProperty("interim_swanseaCourtList")
    private SwanseaCourt interimSwanseaCourtList;
    @JsonProperty("interim_northWalesCourtList")
    private NorthWalesCourt interimNorthWalesCourtList;

    @JsonIgnore
    @Override
    public NottinghamCourt getNottinghamCourt() {
        return interimNottinghamCourtList;
    }

    @JsonIgnore
    @Override
    public CfcCourt getCfcCourt() {
        return interimCfcCourtList;
    }

    @JsonIgnore
    @Override
    public BirminghamCourt getBirminghamCourt() {
        return interimBirminghamCourtList;
    }

    @JsonIgnore
    @Override
    public LiverpoolCourt getLiverpoolCourt() {
        return interimLiverpoolCourtList;
    }

    @JsonIgnore
    @Override
    public ManchesterCourt getManchesterCourt() {
        return interimManchesterCourtList;
    }

    @JsonIgnore
    @Override
    public LancashireCourt getLancashireCourt() {
        return interimLancashireCourtList;
    }

    @JsonIgnore
    @Override
    public ClevelandCourt getClevelandCourt(boolean isConsented) {
        return interimClevelandCourtList;
    }

    @JsonIgnore
    @Override
    public NwYorkshireCourt getNwYorkshireCourt() {
        return interimNwYorkshireCourtList;
    }

    @JsonIgnore
    @Override
    public HumberCourt getHumberCourt() {
        return interimHumberCourtList;
    }

    @JsonIgnore
    @Override
    public KentSurreyCourt getKentSurreyCourt() {
        return interimKentSurreyCourtList;
    }

    @JsonIgnore
    @Override
    public BedfordshireCourt getBedfordshireCourt() {
        return interimBedfordshireCourtList;
    }

    @JsonIgnore
    @Override
    public ThamesValleyCourt getThamesValleyCourt() {
        return interimThamesValleyCourtList;
    }

    @JsonIgnore
    @Override
    public DevonCourt getDevonCourt() {
        return interimDevonCourtList;
    }

    @JsonIgnore
    @Override
    public DorsetCourt getDorsetCourt() {
        return interimDorsetCourtList;
    }

    @JsonIgnore
    @Override
    public BristolCourt getBristolCourt() {
        return interimBristolCourtList;
    }

    @JsonIgnore
    @Override
    public NewportCourt getNewportCourt() {
        return interimNewportCourtList;
    }

    @JsonIgnore
    @Override
    public SwanseaCourt getSwanseaCourt() {
        return interimSwanseaCourtList;
    }

    @JsonIgnore
    @Override
    public NorthWalesCourt getNorthWalesCourt() {
        return interimNorthWalesCourtList;
    }
}
