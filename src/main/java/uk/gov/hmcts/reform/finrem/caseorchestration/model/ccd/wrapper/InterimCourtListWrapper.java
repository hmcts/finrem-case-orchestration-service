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
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesSfvgopAccess;
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
public class InterimCourtListWrapper implements CourtListWrapper {
    @CCD(
            label = "Where is the Applicant’s Local Court? ",
            hint = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_s_NottinghamList",
            typeParameterClass = FRSNottinghamList.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class}
    )
    @JsonProperty("interim_nottinghamCourtList")
    private NottinghamCourt interimNottinghamCourtList;
    @CCD(
            label = "Where is the Applicant’s Local Court? ",
            hint = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_s_CFCList",
            typeParameterClass = FRSCFCList.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class}
    )
    @JsonProperty("interim_cfcCourtList")
    private CfcCourt interimCfcCourtList;
    @CCD(
            label = "Where is the Applicant’s Local Court? ",
            hint = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_birmingham_hc_list",
            typeParameterClass = FRBirminghamHcList.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class}
    )
    @JsonProperty("interim_birminghamCourtList")
    private BirminghamCourt interimBirminghamCourtList;
    @CCD(
            label = "Where is the Applicant’s Local Court? ",
            hint = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_liverpool_hc_list",
            typeParameterClass = FRLiverpoolHcList.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class}
    )
    @JsonProperty("interim_liverpoolCourtList")
    private LiverpoolCourt interimLiverpoolCourtList;
    @CCD(
            label = "Where is the Applicant’s Local Court? ",
            hint = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_manchester_hc_list",
            typeParameterClass = FRManchesterHcList.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class}
    )
    @JsonProperty("interim_manchesterCourtList")
    private ManchesterCourt interimManchesterCourtList;
    @CCD(
            label = "Where is the Applicant’s Local Court? ",
            hint = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_lancashireList",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class}
    )
    @JsonProperty("interim_lancashireCourtList")
    private LancashireCourt interimLancashireCourtList;
    @CCD(
            label = "Where is the Applicant’s Local Court? ",
            hint = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_cleveland_hc_list",
            typeParameterClass = FRClevelandHcList.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class}
    )
    @JsonProperty("interim_cleavelandCourtList")
    private ClevelandCourt interimClevelandCourtList;
    @CCD(
            label = "Where is the Applicant’s Local Court? ",
            hint = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_nw_yorkshire_hc_list",
            typeParameterClass = FRNwYorkshireHcList.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class}
    )
    @JsonProperty("interim_nwyorkshireCourtList")
    private NwYorkshireCourt interimNwYorkshireCourtList;
    @CCD(
            label = "Where is the Applicant’s Local Court? ",
            hint = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_humber_hc_list",
            typeParameterClass = FRHumberHcList.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class}
    )
    @JsonProperty("interim_humberCourtList")
    private HumberCourt interimHumberCourtList;
    @CCD(
            label = "Where is the Applicant’s Local Court? ",
            hint = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_kent_surrey_hc_list",
            typeParameterClass = FRKentSurreyHcList.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class}
    )
    @JsonProperty("interim_kentSurreyCourtList")
    private KentSurreyCourt interimKentSurreyCourtList;
    @CCD(
            label = "Where is the Applicant’s Local Court? ",
            hint = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_bedfordshireList",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesSfvgopAccess.class}
    )
    @JsonProperty("interim_bedfordshireCourtList")
    private BedfordshireCourt interimBedfordshireCourtList;
    @CCD(
            label = "Where is the Applicant’s Local Court? ",
            hint = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_thamesvalleyList",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class}
    )
    @JsonProperty("interim_thamesvalleyCourtList")
    private ThamesValleyCourt interimThamesValleyCourtList;
    @CCD(
            label = "Where is the Applicant’s Local Court? ",
            hint = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_devonList",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class}
    )
    @JsonProperty("interim_devonCourtList")
    private DevonCourt interimDevonCourtList;
    @CCD(
            label = "Where is the Applicant’s Local Court? ",
            hint = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_dorsetList",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class}
    )
    @JsonProperty("interim_dorsetCourtList")
    private DorsetCourt interimDorsetCourtList;
    @CCD(
            label = "Where is the Applicant’s Local Court? ",
            hint = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_bristolList",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class}
    )
    @JsonProperty("interim_bristolCourtList")
    private BristolCourt interimBristolCourtList;
    @CCD(
            label = "Where is the Applicant’s Local Court? ",
            hint = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_newport_hc_list",
            typeParameterClass = FRNewportHcList.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class}
    )
    @JsonProperty("interim_newportCourtList")
    private NewportCourt interimNewportCourtList;
    @CCD(
            label = "Where is the Applicant’s Local Court? ",
            hint = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_swansea_hc_list",
            typeParameterClass = FRSwanseaHcList.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesSfvgopAccess.class}
    )
    @JsonProperty("interim_swanseaCourtList")
    private SwanseaCourt interimSwanseaCourtList;
    @CCD(
            label = "Where is the Applicant’s Local Court? ",
            hint = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_northwalesList",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesSfvgopAccess.class}
    )
    @JsonProperty("interim_northWalesCourtList")
    private NorthWalesCourt interimNorthWalesCourtList;

    @CCD(
            label = "Where is the Applicant’s Local Court? ",
            hint = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_highCourtList",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesSfvgopAccess.class}
    )
    @JsonProperty("interim_highCourtList")
    private HighCourt interimHighCourtList;

    @JsonIgnore
    @Override
    public NottinghamCourt getNottinghamCourt() {
        return interimNottinghamCourtList;
    }

    //This is only required for the consented side and should not be used elsewhere.
    @Override
    public LondonCourt getLondonCourt() {
        return null;
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
    public ClevelandCourt getClevelandCourt() {
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

    @JsonIgnore
    @Override
    public HighCourt getHighCourt() {
        return interimHighCourtList;
    }
}
