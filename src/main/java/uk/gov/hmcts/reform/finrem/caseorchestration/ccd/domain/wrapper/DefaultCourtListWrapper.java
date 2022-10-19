package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.LondonCourt;
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
public class DefaultCourtListWrapper implements CourtListWrapper {
    private NottinghamCourt nottinghamCourtList;
    private CfcCourt cfcCourtList;
    private BirminghamCourt birminghamCourtList;
    private LondonCourt londonCourtList;
    private LiverpoolCourt liverpoolCourtList;
    private ManchesterCourt manchesterCourtList;
    private LancashireCourt lancashireCourtList;
    private ClevelandCourt cleavelandCourtList;
    private ClevelandCourt clevelandCourtList;
    private NwYorkshireCourt nwYorkshireCourtList;
    private HumberCourt humberCourtList;
    private KentSurreyCourt kentSurreyCourtList;
    private BedfordshireCourt bedfordshireCourtList;
    private ThamesValleyCourt thamesValleyCourtList;
    private DevonCourt devonCourtList;
    private DorsetCourt dorsetCourtList;
    private BristolCourt bristolCourtList;
    private NewportCourt newportCourtList;
    private SwanseaCourt swanseaCourtList;
    private NorthWalesCourt northWalesCourtList;

    @JsonIgnore
    @Override
    public NottinghamCourt getNottinghamCourt() {
        return nottinghamCourtList;
    }

    @JsonIgnore
    @Override
    public CfcCourt getCfcCourt() {
        return cfcCourtList;
    }

    @JsonIgnore
    @Override
    public BirminghamCourt getBirminghamCourt() {
        return birminghamCourtList;
    }

    @JsonIgnore
    @Override
    public LiverpoolCourt getLiverpoolCourt() {
        return liverpoolCourtList;
    }

    @JsonIgnore
    @Override
    public ManchesterCourt getManchesterCourt() {
        return manchesterCourtList;
    }

    @JsonIgnore
    @Override
    public LancashireCourt getLancashireCourt() {
        return lancashireCourtList;
    }

    @JsonIgnore
    @Override
    public ClevelandCourt getClevelandCourt(boolean isConsented) {
        return isConsented ? clevelandCourtList : cleavelandCourtList;
    }

    @JsonIgnore
    @Override
    public NwYorkshireCourt getNwYorkshireCourt() {
        return nwYorkshireCourtList;
    }

    @JsonIgnore
    @Override
    public HumberCourt getHumberCourt() {
        return humberCourtList;
    }

    @JsonIgnore
    @Override
    public KentSurreyCourt getKentSurreyCourt() {
        return kentSurreyCourtList;
    }

    @JsonIgnore
    @Override
    public BedfordshireCourt getBedfordshireCourt() {
        return bedfordshireCourtList;
    }

    @JsonIgnore
    @Override
    public ThamesValleyCourt getThamesValleyCourt() {
        return thamesValleyCourtList;
    }

    @JsonIgnore
    @Override
    public DevonCourt getDevonCourt() {
        return devonCourtList;
    }

    @JsonIgnore
    @Override
    public DorsetCourt getDorsetCourt() {
        return dorsetCourtList;
    }

    @JsonIgnore
    @Override
    public BristolCourt getBristolCourt() {
        return bristolCourtList;
    }

    @JsonIgnore
    @Override
    public NewportCourt getNewportCourt() {
        return newportCourtList;
    }

    @JsonIgnore
    @Override
    public SwanseaCourt getSwanseaCourt() {
        return swanseaCourtList;
    }

    @JsonIgnore
    @Override
    public NorthWalesCourt getNorthWalesCourt() {
        return northWalesCourtList;
    }
}
