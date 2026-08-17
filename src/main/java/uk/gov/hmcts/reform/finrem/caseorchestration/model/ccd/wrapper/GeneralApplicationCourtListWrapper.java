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
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudAccess;
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
public class GeneralApplicationCourtListWrapper implements CourtListWrapper {
    @CCD(
            label = "Please give the name of the Court where the Hearing takes place",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_s_NottinghamList",
            typeParameterClass = FRSNottinghamList.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    @JsonProperty("generalApplicationDirections_nottinghamCourtList")
    private NottinghamCourt generalApplicationDirectionsNottinghamCourtList;
    @CCD(
            label = "Please give the name of the Court where the Hearing takes place",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_s_CFCList",
            typeParameterClass = FRSCFCList.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    @JsonProperty("generalApplicationDirections_cfcCourtList")
    private CfcCourt generalApplicationDirectionsCfcCourtList;
    @CCD(
            label = "Please give the name of the Court where the Hearing takes place",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_birmingham_hc_list",
            typeParameterClass = FRBirminghamHcList.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    @JsonProperty("generalApplicationDirections_birminghamCourtList")
    private BirminghamCourt generalApplicationDirectionsBirminghamCourtList;
    @CCD(
            label = "Please give the name of the Court where the Hearing takes place",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_liverpool_hc_list",
            typeParameterClass = FRLiverpoolHcList.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    @JsonProperty("generalApplicationDirections_liverpoolCourtList")
    private LiverpoolCourt generalApplicationDirectionsLiverpoolCourtList;
    @CCD(
            label = "Please give the name of the Court where the Hearing takes place",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_manchester_hc_list",
            typeParameterClass = FRManchesterHcList.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    @JsonProperty("generalApplicationDirections_manchesterCourtList")
    private ManchesterCourt generalApplicationDirectionsManchesterCourtList;
    @CCD(
            label = "Please give the name of the Court where the Hearing takes place",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_lancashireList",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    @JsonProperty("generalApplicationDirections_lancashireCourtList")
    private LancashireCourt generalApplicationDirectionsLancashireCourtList;
    @CCD(
            label = "Please give the name of the Court where the Hearing takes place",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_cleveland_hc_list",
            typeParameterClass = FRClevelandHcList.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    @JsonProperty("generalApplicationDirections_cleavelandCourtList")
    private ClevelandCourt generalApplicationDirectionsClevelandCourtList;
    @CCD(
            label = "Please give the name of the Court where the Hearing takes place",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_nw_yorkshire_hc_list",
            typeParameterClass = FRNwYorkshireHcList.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    @JsonProperty("generalApplicationDirections_nwyorkshireCourtList")
    private NwYorkshireCourt generalApplicationDirectionsNwYorkshireCourtList;
    @CCD(
            label = "Please give the name of the Court where the Hearing takes place",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_humber_hc_list",
            typeParameterClass = FRHumberHcList.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    @JsonProperty("generalApplicationDirections_humberCourtList")
    private HumberCourt generalApplicationDirectionsHumberCourtList;
    @CCD(
            label = "Please give the name of the Court where the Hearing takes place",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_kent_surrey_hc_list",
            typeParameterClass = FRKentSurreyHcList.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    @JsonProperty("generalApplicationDirections_kentSurreyCourtList")
    private KentSurreyCourt generalApplicationDirectionsKentSurreyCourtList;
    @CCD(
            label = "Please give the name of the Court where the Hearing takes place",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_bedfordshireList",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    @JsonProperty("generalApplicationDirections_bedfordshireCourtList")
    private BedfordshireCourt generalApplicationDirectionsBedfordshireCourtList;
    @CCD(
            label = "Please give the name of the Court where the Hearing takes place",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_thamesvalleyList",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    @JsonProperty("generalApplicationDirections_thamesvalleyCourtList")
    private ThamesValleyCourt generalApplicationDirectionsThamesValleyCourtList;
    @CCD(
            label = "Please give the name of the Court where the Hearing takes place",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_devonList",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    @JsonProperty("generalApplicationDirections_devonCourtList")
    private DevonCourt generalApplicationDirectionsDevonCourtList;
    @CCD(
            label = "Please give the name of the Court where the Hearing takes place",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_dorsetList",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    @JsonProperty("generalApplicationDirections_dorsetCourtList")
    private DorsetCourt generalApplicationDirectionsDorsetCourtList;
    @CCD(
            label = "Please give the name of the Court where the Hearing takes place",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_bristolList",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    @JsonProperty("generalApplicationDirections_bristolCourtList")
    private BristolCourt generalApplicationDirectionsBristolCourtList;
    @CCD(
            label = "Please give the name of the Court where the Hearing takes place",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_newport_hc_list",
            typeParameterClass = FRNewportHcList.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    @JsonProperty("generalApplicationDirections_newportCourtList")
    private NewportCourt generalApplicationDirectionsNewportCourtList;
    @CCD(
            label = "Please give the name of the Court where the Hearing takes place",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_swansea_hc_list",
            typeParameterClass = FRSwanseaHcList.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    @JsonProperty("generalApplicationDirections_swanseaCourtList")
    private SwanseaCourt generalApplicationDirectionsSwanseaCourtList;
    @CCD(
            label = "Please give the name of the Court where the Hearing takes place",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_northwalesList",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    @JsonProperty("generalApplicationDirections_northWalesCourtList")
    private NorthWalesCourt generalApplicationDirectionsNorthWalesCourtList;

    @CCD(
            label = "Please give the name of the Court where the Hearing takes place",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_highCourtList",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    @JsonProperty("generalApplicationDirections_highCourtList")
    private HighCourt generalApplicationDirectionsHighCourtList;

    // This is only required for the consented side and should not be used elsewhere.
    @JsonIgnore
    @Override
    public LondonCourt getLondonCourt() {
        return null;
    }

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
    public ClevelandCourt getClevelandCourt() {
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

    @JsonIgnore
    @Override
    public HighCourt getHighCourt() {
        return generalApplicationDirectionsHighCourtList;
    }

    public DefaultCourtListWrapper toDefaultCourtListWrapper() {
        return DefaultCourtListWrapper.builder()
            .nottinghamCourtList(generalApplicationDirectionsNottinghamCourtList)
            .cfcCourtList(generalApplicationDirectionsCfcCourtList)
            .birminghamCourtList(generalApplicationDirectionsBirminghamCourtList)
            .liverpoolCourtList(generalApplicationDirectionsLiverpoolCourtList)
            .manchesterCourtList(generalApplicationDirectionsManchesterCourtList)
            .lancashireCourtList(generalApplicationDirectionsLancashireCourtList)
            .clevelandCourtList(generalApplicationDirectionsClevelandCourtList)
            .nwYorkshireCourtList(generalApplicationDirectionsNwYorkshireCourtList)
            .humberCourtList(generalApplicationDirectionsHumberCourtList)
            .kentSurreyCourtList(generalApplicationDirectionsKentSurreyCourtList)
            .bedfordshireCourtList(generalApplicationDirectionsBedfordshireCourtList)
            .thamesValleyCourtList(generalApplicationDirectionsThamesValleyCourtList)
            .devonCourtList(generalApplicationDirectionsDevonCourtList)
            .dorsetCourtList(generalApplicationDirectionsDorsetCourtList)
            .bristolCourtList(generalApplicationDirectionsBristolCourtList)
            .newportCourtList(generalApplicationDirectionsNewportCourtList)
            .swanseaCourtList(generalApplicationDirectionsSwanseaCourtList)
            .northWalesCourtList(generalApplicationDirectionsNorthWalesCourtList)
            .highCourtList(generalApplicationDirectionsHighCourtList)
            .build();
    }
}
