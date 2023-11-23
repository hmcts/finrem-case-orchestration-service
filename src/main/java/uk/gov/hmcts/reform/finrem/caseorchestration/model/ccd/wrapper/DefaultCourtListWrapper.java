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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.LondonCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ManchesterCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NewportCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NorthWalesCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NottinghamCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NwYorkshireCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SwanseaCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ThamesValleyCourt;

import java.util.Arrays;
import java.util.Optional;


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
    @JsonProperty("nwyorkshireCourtList")
    private NwYorkshireCourt nwYorkshireCourtList;
    private HumberCourt humberCourtList;
    private KentSurreyCourt kentSurreyCourtList;
    private BedfordshireCourt bedfordshireCourtList;
    @JsonProperty("thamesvalleyCourtList")
    private ThamesValleyCourt thamesValleyCourtList;
    private DevonCourt devonCourtList;
    private DorsetCourt dorsetCourtList;
    private BristolCourt bristolCourtList;
    private NewportCourt newportCourtList;
    private SwanseaCourt swanseaCourtList;
    private NorthWalesCourt northWalesCourtList;

    private HighCourt highCourtList;

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


    @JsonIgnore
    @Override
    public HighCourt getHighCourt() {
        return highCourtList;
    }


    public void setCourt(String courtId, Boolean isConsented) {
        if (Arrays.stream(NottinghamCourt.values())
            .anyMatch(court -> court.getId().equals(courtId))) {
            this.nottinghamCourtList = NottinghamCourt.getNottinghamCourt(courtId);
        } else if (Arrays.stream(CfcCourt.values())
            .anyMatch(court -> court.getId().equals(courtId))) {
            this.cfcCourtList = CfcCourt.getCfcCourt(courtId);
        } else if (Arrays.stream(BirminghamCourt.values())
            .anyMatch(court -> court.getId().equals(courtId))) {
            this.birminghamCourtList = BirminghamCourt.getBirminghamCourt(courtId);
        } else if (Arrays.stream(LiverpoolCourt.values())
            .anyMatch(court -> court.getId().equals(courtId))) {
            this.liverpoolCourtList = LiverpoolCourt.getLiverpoolCourt(courtId);
        } else if (Arrays.stream(ManchesterCourt.values())
            .anyMatch(court -> court.getId().equals(courtId))) {
            this.manchesterCourtList = ManchesterCourt.getManchesterCourt(courtId);
        } else if (Arrays.stream(LancashireCourt.values())
            .anyMatch(court -> court.getId().equals(courtId))) {
            this.lancashireCourtList = LancashireCourt.getLancashireCourt(courtId);
        } else if (Arrays.stream(ClevelandCourt.values())
            .anyMatch(court -> court.getId().equals(courtId))) {
            if (Optional.ofNullable(isConsented).orElse(false)) {
                this.clevelandCourtList = ClevelandCourt.getCleavelandCourt(courtId);
            } else {
                this.cleavelandCourtList = ClevelandCourt.getCleavelandCourt(courtId);
            }
        } else if (Arrays.stream(NwYorkshireCourt.values())
            .anyMatch(court -> court.getId().equals(courtId))) {
            this.nwYorkshireCourtList = NwYorkshireCourt.getNwYorkshireCourt(courtId);
        } else if (Arrays.stream(HumberCourt.values())
            .anyMatch(court -> court.getId().equals(courtId))) {
            this.humberCourtList = HumberCourt.getHumberCourt(courtId);
        } else if (Arrays.stream(KentSurreyCourt.values())
            .anyMatch(court -> court.getId().equals(courtId))) {
            this.kentSurreyCourtList = KentSurreyCourt.getKentSurreyCourt(courtId);
        } else if (Arrays.stream(BedfordshireCourt.values())
            .anyMatch(court -> court.getId().equals(courtId))) {
            this.bedfordshireCourtList = BedfordshireCourt.getBedfordshireCourt(courtId);
        } else if (Arrays.stream(ThamesValleyCourt.values())
            .anyMatch(court -> court.getId().equals(courtId))) {
            this.thamesValleyCourtList = ThamesValleyCourt.getThamesValleyCourt(courtId);
        } else if (Arrays.stream(DevonCourt.values())
            .anyMatch(court -> court.getId().equals(courtId))) {
            this.devonCourtList = DevonCourt.getDevonCourt(courtId);
        } else if (Arrays.stream(DorsetCourt.values())
            .anyMatch(court -> court.getId().equals(courtId))) {
            this.dorsetCourtList = DorsetCourt.getDorsetCourt(courtId);
        } else if (Arrays.stream(BristolCourt.values())
            .anyMatch(court -> court.getId().equals(courtId))) {
            this.bristolCourtList = BristolCourt.getBristolCourt(courtId);
        } else if (Arrays.stream(NewportCourt.values())
            .anyMatch(court -> court.getId().equals(courtId))) {
            this.newportCourtList = NewportCourt.getNewportCourt(courtId);
        } else if (Arrays.stream(SwanseaCourt.values())
            .anyMatch(court -> court.getId().equals(courtId))) {
            this.swanseaCourtList = SwanseaCourt.getSwanseaCourt(courtId);
        } else if (Arrays.stream(NorthWalesCourt.values())
            .anyMatch(court -> court.getId().equals(courtId))) {
            this.northWalesCourtList = NorthWalesCourt.getNorthWalesCourt(courtId);
        } else if (Arrays.stream(HighCourt.values())
            .anyMatch(court -> court.getId().equals(courtId))) {
            this.highCourtList = HighCourt.getHighCourt(courtId);
        }
    }
}
