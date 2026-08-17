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
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCruPlus2RolesAblzopAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCruPlus2RolesGasysbAccess;
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
public class HearingCourtWrapper implements CourtListWrapper {

    @CCD(
            label = "Where is the Applicant’s Local Court? ",
            hint = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_s_NottinghamList",
            typeParameterClass = FRSNottinghamList.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCruPlus2RolesAblzopAccess.class}
    )
    @JsonProperty("hearing_nottinghamCourtList")
    private NottinghamCourt hearingNottinghamCourtList;
    @CCD(
            label = "Where is the Applicant’s Local Court? ",
            hint = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_s_CFCList",
            typeParameterClass = FRSCFCList.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCruPlus2RolesAblzopAccess.class}
    )
    @JsonProperty("hearing_cfcCourtList")
    private CfcCourt hearingCfcCourtList;
    @CCD(
            label = "Where is the Applicant’s Local Court? ",
            hint = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_birmingham_hc_list",
            typeParameterClass = FRBirminghamHcList.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCruPlus2RolesAblzopAccess.class}
    )
    @JsonProperty("hearing_birminghamCourtList")
    private BirminghamCourt hearingBirminghamCourtList;
    @CCD(
            label = "Where is the Applicant’s Local Court? ",
            hint = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_liverpool_hc_list",
            typeParameterClass = FRLiverpoolHcList.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCruPlus2RolesAblzopAccess.class}
    )
    @JsonProperty("hearing_liverpoolCourtList")
    private LiverpoolCourt hearingLiverpoolCourtList;
    @CCD(
            label = "Where is the Applicant’s Local Court? ",
            hint = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_manchester_hc_list",
            typeParameterClass = FRManchesterHcList.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCruPlus2RolesAblzopAccess.class}
    )
    @JsonProperty("hearing_manchesterCourtList")
    private ManchesterCourt hearingManchesterCourtList;
    @CCD(
            label = "Where is the Applicant’s Local Court? ",
            hint = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_lancashireList",
            access = {CaseworkerDivorceFinancialremedyCourtadminCruPlus2RolesAblzopAccess.class}
    )
    @JsonProperty("hearing_lancashireCourtList")
    private LancashireCourt hearingLancashireCourtList;
    @CCD(
            label = "Where is the Applicant’s Local Court? ",
            hint = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_cleveland_hc_list",
            typeParameterClass = FRClevelandHcList.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCruPlus2RolesAblzopAccess.class}
    )
    @JsonProperty("hearing_cleavelandCourtList")
    private ClevelandCourt hearingClevelandCourtList;
    @CCD(
            label = "Where is the Applicant’s Local Court? ",
            hint = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_nw_yorkshire_hc_list",
            typeParameterClass = FRNwYorkshireHcList.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCruPlus2RolesAblzopAccess.class}
    )
    @JsonProperty("hearing_nwyorkshireCourtList")
    private NwYorkshireCourt hearingNwYorkshireCourtList;
    @CCD(
            label = "Where is the Applicant’s Local Court? ",
            hint = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_humber_hc_list",
            typeParameterClass = FRHumberHcList.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCruPlus2RolesAblzopAccess.class}
    )
    @JsonProperty("hearing_humberCourtList")
    private HumberCourt hearingHumberCourtList;
    @CCD(
            label = "Where is the Applicant’s Local Court? ",
            hint = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_kent_surrey_hc_list",
            typeParameterClass = FRKentSurreyHcList.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCruPlus2RolesAblzopAccess.class}
    )
    @JsonProperty("hearing_kentSurreyCourtList")
    private KentSurreyCourt hearingKentSurreyCourtList;
    @CCD(
            label = "Where is the Applicant’s Local Court? ",
            hint = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_bedfordshireList",
            access = {CaseworkerDivorceFinancialremedyCourtadminCruPlus2RolesGasysbAccess.class}
    )
    @JsonProperty("hearing_bedfordshireCourtList")
    private BedfordshireCourt hearingBedfordshireCourtList;
    @CCD(
            label = "Where is the Applicant’s Local Court? ",
            hint = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_thamesvalleyList",
            access = {CaseworkerDivorceFinancialremedyCourtadminCruPlus2RolesAblzopAccess.class}
    )
    @JsonProperty("hearing_thamesvalleyCourtList")
    private ThamesValleyCourt hearingThamesValleyCourtList;
    @CCD(
            label = "Where is the Applicant’s Local Court? ",
            hint = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_devonList",
            access = {CaseworkerDivorceFinancialremedyCourtadminCruPlus2RolesAblzopAccess.class}
    )
    @JsonProperty("hearing_devonCourtList")
    private DevonCourt hearingDevonCourtList;
    @CCD(
            label = "Where is the Applicant’s Local Court? ",
            hint = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_dorsetList",
            access = {CaseworkerDivorceFinancialremedyCourtadminCruPlus2RolesAblzopAccess.class}
    )
    @JsonProperty("hearing_dorsetCourtList")
    private DorsetCourt hearingDorsetCourtList;
    @CCD(
            label = "Where is the Applicant’s Local Court? ",
            hint = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_bristolList",
            access = {CaseworkerDivorceFinancialremedyCourtadminCruPlus2RolesAblzopAccess.class}
    )
    @JsonProperty("hearing_bristolCourtList")
    private BristolCourt hearingBristolCourtList;
    @CCD(
            label = "Where is the Applicant’s Local Court? ",
            hint = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_newport_hc_list",
            typeParameterClass = FRNewportHcList.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCruPlus2RolesAblzopAccess.class}
    )
    @JsonProperty("hearing_newportCourtList")
    private NewportCourt hearingNewportCourtList;
    @CCD(
            label = "Where is the Applicant’s Local Court? ",
            hint = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_swansea_hc_list",
            typeParameterClass = FRSwanseaHcList.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCruPlus2RolesGasysbAccess.class}
    )
    @JsonProperty("hearing_swanseaCourtList")
    private SwanseaCourt hearingSwanseaCourtList;
    @CCD(
            label = "Where is the Applicant’s Local Court? ",
            hint = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_northwalesList",
            access = {CaseworkerDivorceFinancialremedyCourtadminCruPlus2RolesGasysbAccess.class}
    )
    @JsonProperty("hearing_northWalesCourtList")
    private NorthWalesCourt hearingNorthWalesCourtList;
    @CCD(
            label = "Where is the Applicant’s Local Court? ",
            hint = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_highCourtList",
            access = {CaseworkerDivorceFinancialremedyCourtadminCruPlus2RolesGasysbAccess.class}
    )
    @JsonProperty("hearing_highCourtList")
    private HighCourt hearingHighCourtList;

    //This is only required for the consented side and should not be used elsewhere.
    @JsonIgnore
    @Override
    public LondonCourt getLondonCourt() {
        return null;
    }

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
    public ClevelandCourt getClevelandCourt() {
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

    public DefaultCourtListWrapper toDefaultCourtListWrapper() {
        return DefaultCourtListWrapper.builder()
            .nottinghamCourtList(hearingNottinghamCourtList)
            .cfcCourtList(hearingCfcCourtList)
            .birminghamCourtList(hearingBirminghamCourtList)
            .liverpoolCourtList(hearingLiverpoolCourtList)
            .manchesterCourtList(hearingManchesterCourtList)
            .lancashireCourtList(hearingLancashireCourtList)
            .clevelandCourtList(hearingClevelandCourtList)
            .nwYorkshireCourtList(hearingNwYorkshireCourtList)
            .humberCourtList(hearingHumberCourtList)
            .kentSurreyCourtList(hearingKentSurreyCourtList)
            .bedfordshireCourtList(hearingBedfordshireCourtList)
            .thamesValleyCourtList(hearingThamesValleyCourtList)
            .devonCourtList(hearingDevonCourtList)
            .dorsetCourtList(hearingDorsetCourtList)
            .bristolCourtList(hearingBristolCourtList)
            .newportCourtList(hearingNewportCourtList)
            .swanseaCourtList(hearingSwanseaCourtList)
            .northWalesCourtList(hearingNorthWalesCourtList)
            .highCourtList(hearingHighCourtList)
            .build();
    }
}
