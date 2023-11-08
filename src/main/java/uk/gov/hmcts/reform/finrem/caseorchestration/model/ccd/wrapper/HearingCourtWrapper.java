package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BedfordshireCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BirminghamCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BristolCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CfcCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ClevelandCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DevonCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DorsetCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HighCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HumberCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.KentSurreyCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.LancashireCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.LiverpoolCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ManchesterCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NewportCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NorthWalesCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NottinghamCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NwYorkshireCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SwanseaCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ThamesValleyCourt;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingCourtWrapper implements CourtListWrapper {
    @JsonProperty("hearing_nottinghamCourtList")
    private NottinghamCourt hearingNottinghamCourtList;
    @JsonProperty("hearing_cfcCourtList")
    private CfcCourt hearingCfcCourtList;
    @JsonProperty("hearing_birminghamCourtList")
    private BirminghamCourt hearingBirminghamCourtList;
    @JsonProperty("hearing_liverpoolCourtList")
    private LiverpoolCourt hearingLiverpoolCourtList;
    @JsonProperty("hearing_manchesterCourtList")
    private ManchesterCourt hearingManchesterCourtList;
    @JsonProperty("hearing_lancashireCourtList")
    private LancashireCourt hearingLancashireCourtList;
    @JsonProperty("hearing_cleavelandCourtList")
    private ClevelandCourt hearingClevelandCourtList;
    @JsonProperty("hearing_nwyorkshireCourtList")
    private NwYorkshireCourt hearingNwYorkshireCourtList;
    @JsonProperty("hearing_humberCourtList")
    private HumberCourt hearingHumberCourtList;
    @JsonProperty("hearing_kentSurreyCourtList")
    private KentSurreyCourt hearingKentSurreyCourtList;
    @JsonProperty("hearing_bedfordshireCourtList")
    private BedfordshireCourt hearingBedfordshireCourtList;
    @JsonProperty("hearing_thamesvalleyCourtList")
    private ThamesValleyCourt hearingThamesValleyCourtList;
    @JsonProperty("hearing_devonCourtList")
    private DevonCourt hearingDevonCourtList;
    @JsonProperty("hearing_dorsetCourtList")
    private DorsetCourt hearingDorsetCourtList;
    @JsonProperty("hearing_bristolCourtList")
    private BristolCourt hearingBristolCourtList;
    @JsonProperty("hearing_newportCourtList")
    private NewportCourt hearingNewportCourtList;
    @JsonProperty("hearing_swanseaCourtList")
    private SwanseaCourt hearingSwanseaCourtList;
    @JsonProperty("hearing_northWalesCourtList")
    private NorthWalesCourt hearingNorthWalesCourtList;

    @JsonProperty("hearing_highCourtList")
    private HighCourt hearingHighCourtList;

    @JsonIgnore
    @Override
    public NottinghamCourt getNottinghamCourt() {
        return hearingNottinghamCourtList;
    }

    @JsonIgnore
    @Override
    public CfcCourt getCfcCourt() {
        return hearingCfcCourtList;
    }

    @JsonIgnore
    @Override
    public BirminghamCourt getBirminghamCourt() {
        return hearingBirminghamCourtList;
    }

    @JsonIgnore
    @Override
    public LiverpoolCourt getLiverpoolCourt() {
        return hearingLiverpoolCourtList;
    }

    @JsonIgnore
    @Override
    public ManchesterCourt getManchesterCourt() {
        return hearingManchesterCourtList;
    }

    @JsonIgnore
    @Override
    public LancashireCourt getLancashireCourt() {
        return hearingLancashireCourtList;
    }

    @JsonIgnore
    @Override
    public ClevelandCourt getClevelandCourt(boolean isConsented) {
        return hearingClevelandCourtList;
    }

    @JsonIgnore
    @Override
    public NwYorkshireCourt getNwYorkshireCourt() {
        return hearingNwYorkshireCourtList;
    }

    @JsonIgnore
    @Override
    public HumberCourt getHumberCourt() {
        return hearingHumberCourtList;
    }

    @JsonIgnore
    @Override
    public KentSurreyCourt getKentSurreyCourt() {
        return hearingKentSurreyCourtList;
    }

    @JsonIgnore
    @Override
    public BedfordshireCourt getBedfordshireCourt() {
        return hearingBedfordshireCourtList;
    }

    @JsonIgnore
    @Override
    public ThamesValleyCourt getThamesValleyCourt() {
        return hearingThamesValleyCourtList;
    }

    @JsonIgnore
    @Override
    public DevonCourt getDevonCourt() {
        return hearingDevonCourtList;
    }

    @JsonIgnore
    @Override
    public DorsetCourt getDorsetCourt() {
        return hearingDorsetCourtList;
    }

    @JsonIgnore
    @Override
    public BristolCourt getBristolCourt() {
        return hearingBristolCourtList;
    }

    @JsonIgnore
    @Override
    public NewportCourt getNewportCourt() {
        return hearingNewportCourtList;
    }

    @JsonIgnore
    @Override
    public SwanseaCourt getSwanseaCourt() {
        return hearingSwanseaCourtList;
    }

    @JsonIgnore
    @Override
    public NorthWalesCourt getNorthWalesCourt() {
        return hearingNorthWalesCourtList;
    }

    @JsonIgnore
    @Override
    public HighCourt getHighCourt() {
        return hearingHighCourtList;
    }
}