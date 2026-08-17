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
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FRSNottinghamList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FRSCFCList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FRBirminghamHcList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FRLiverpoolHcList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FRManchesterHcList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FRClevelandHcList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FRNwYorkshireHcList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FRHumberHcList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FRKentSurreyHcList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FRNewportHcList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FRSwanseaHcList;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DefaultCourtListWrapper implements CourtListWrapper {

    @CCD(
            label = "Please choose the FRC which covers the area within which the Applicant resides - if the applicant does not reside within one of these areas, please choose 'other'",
            showCondition = "region=\"midlands\" AND midlandsList=\"nottingham\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_s_NottinghamList",
            typeParameterClass = FRSNottinghamList.class
    )
    private NottinghamCourt nottinghamCourtList;
    @CCD(
            label = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            showCondition = "region=\"london\" AND londonList=\"cfc\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_s_CFCList",
            typeParameterClass = FRSCFCList.class
    )
    private CfcCourt cfcCourtList;
    @CCD(
            label = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            showCondition = "region=\"midlands\" AND midlandsList=\"birmingham\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_birmingham_hc_list",
            typeParameterClass = FRBirminghamHcList.class
    )
    private BirminghamCourt birminghamCourtList;
    @CCD(ignore = true)
    private LondonCourt londonCourtList;
    @CCD(
            label = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            showCondition = "region=\"northwest\" AND northWestList=\"liverpool\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_liverpool_hc_list",
            typeParameterClass = FRLiverpoolHcList.class
    )
    private LiverpoolCourt liverpoolCourtList;
    @CCD(
            label = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            showCondition = "region=\"northwest\" AND northWestList=\"manchester\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_manchester_hc_list",
            typeParameterClass = FRManchesterHcList.class
    )
    private ManchesterCourt manchesterCourtList;
    @CCD(
            label = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            showCondition = "region=\"northwest\" AND northWestList=\"lancashire\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_lancashireList"
    )
    private LancashireCourt lancashireCourtList;
    @CCD(
            label = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            showCondition = "region=\"northeast\" AND northEastList=\"cleaveland\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_cleveland_hc_list",
            typeParameterClass = FRClevelandHcList.class
    )
    private ClevelandCourt cleavelandCourtList;
    @CCD(
            label = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            showCondition = "region=\"northeast\" AND northEastList=\"cleaveland\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_cleveland_hc_list",
            typeParameterClass = FRClevelandHcList.class
    )
    private ClevelandCourt clevelandCourtList;
    @CCD(
            label = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            showCondition = "region=\"northeast\" AND northEastList=\"nwyorkshire\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_nw_yorkshire_hc_list",
            typeParameterClass = FRNwYorkshireHcList.class
    )
    @JsonProperty("nwyorkshireCourtList")
    private NwYorkshireCourt nwYorkshireCourtList;
    @CCD(
            label = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            showCondition = "region=\"northeast\" AND northEastList=\"hsyorkshire\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_humber_hc_list",
            typeParameterClass = FRHumberHcList.class
    )
    private HumberCourt humberCourtList;
    @CCD(
            label = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            showCondition = "region=\"southeast\" AND southEastList=\"kentfrc\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_kent_surrey_hc_list",
            typeParameterClass = FRKentSurreyHcList.class
    )
    private KentSurreyCourt kentSurreyCourtList;
    @CCD(
            label = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            showCondition = "region=\"southeast\" AND southEastList=\"bedfordshire\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_bedfordshireList"
    )
    private BedfordshireCourt bedfordshireCourtList;
    @CCD(
            label = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            showCondition = "region=\"southeast\" AND southEastList=\"thamesvalley\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_thamesvalleyList"
    )
    @JsonProperty("thamesvalleyCourtList")
    private ThamesValleyCourt thamesValleyCourtList;
    @CCD(
            label = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            showCondition = "region=\"southwest\" AND southWestList=\"devon\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_devonList"
    )
    private DevonCourt devonCourtList;
    @CCD(
            label = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            showCondition = "region=\"southwest\" AND southWestList=\"dorset\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_dorsetList"
    )
    private DorsetCourt dorsetCourtList;
    @CCD(
            label = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            showCondition = "region=\"southwest\" AND southWestList=\"bristol\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_bristolList"
    )
    private BristolCourt bristolCourtList;
    @CCD(
            label = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            showCondition = "region=\"wales\" AND walesList=\"newport\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_newport_hc_list",
            typeParameterClass = FRNewportHcList.class
    )
    private NewportCourt newportCourtList;
    @CCD(
            label = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            showCondition = "region=\"wales\" AND walesList=\"swansea\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_swansea_hc_list",
            typeParameterClass = FRSwanseaHcList.class
    )
    private SwanseaCourt swanseaCourtList;
    @CCD(
            label = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            showCondition = "region=\"wales\" AND walesList=\"northwales\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_northwalesList"
    )
    private NorthWalesCourt northWalesCourtList;
    @CCD(
            label = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            showCondition = "region=\"highcourt\" AND hcCourtList=\"highcourt\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_highCourtList"
    )
    private HighCourt highCourtList;

    @JsonIgnore
    @Override
    public NottinghamCourt getNottinghamCourt() {
        return nottinghamCourtList;
    }

    @JsonIgnore
    @Override
    public LondonCourt getLondonCourt() {
        return londonCourtList;
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
    public ClevelandCourt getClevelandCourt() {
        ClevelandCourt clevelandCourtListFallback = cleavelandCourtList;
        return clevelandCourtList != null ? clevelandCourtList : clevelandCourtListFallback;
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
        } else if (Arrays.stream(LondonCourt.values())
            .anyMatch(court -> court.getId().equals(courtId))) {
            this.londonCourtList = LondonCourt.getLondonCourt(courtId);
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
