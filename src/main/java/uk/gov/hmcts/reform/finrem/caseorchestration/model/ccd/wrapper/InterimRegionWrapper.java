package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionHighCourtFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionLondonFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionMidlandsFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionNorthEastFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionNorthWestFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionSouthEastFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionSouthWestFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionWalesFrc;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesSfvgopAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FRLondonFRCList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FRNeFrcList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FRSeFrcList;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InterimRegionWrapper {
    @CCD(
            label = "Please state in which Financial Remedies Court Zone the applicant resides",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_region_list",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class}
    )
    @JsonProperty("interim_regionList")
    private Region interimRegionList;
    @CCD(
            label = "Please choose the FRC which covers the area within which the Applicant resides.",
            hint = "This should be the FRC local to the applicant",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_midlands_FRCList",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class}
    )
    @JsonProperty("interim_midlandsFRCList")
    private RegionMidlandsFrc interimMidlandsFrcList;
    @CCD(
            label = "Please choose the FRC which covers the area within which the Applicant resides.",
            hint = "This should be the FRC local to the applicant",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_london_FRCList",
            typeParameterClass = FRLondonFRCList.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class}
    )
    @JsonProperty("interim_londonFRCList")
    private RegionLondonFrc interimLondonFrcList;
    @CCD(
            label = "Please choose the FRC which covers the area within which the Applicant resides.",
            hint = "This should be the FRC local to the applicant",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_nw_frc_list",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class}
    )
    @JsonProperty("interim_northWestFRCList")
    private RegionNorthWestFrc interimNorthWestFrcList;
    @CCD(
            label = "Please choose the FRC which covers the area within which the Applicant resides.",
            hint = "This should be the FRC local to the applicant",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_ne_frc_list",
            typeParameterClass = FRNeFrcList.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class}
    )
    @JsonProperty("interim_northEastFRCList")
    private RegionNorthEastFrc interimNorthEastFrcList;
    @CCD(
            label = "Please choose the FRC which covers the area within which the Applicant resides.",
            hint = "This should be the FRC local to the applicant",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_se_frc_list",
            typeParameterClass = FRSeFrcList.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class}
    )
    @JsonProperty("interim_southEastFRCList")
    private RegionSouthEastFrc interimSouthEastFrcList;
    @CCD(
            label = "Please choose the FRC which covers the area within which the Applicant resides.",
            hint = "This should be the FRC local to the applicant",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_sw_frc_list",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class}
    )
    @JsonProperty("interim_southWestFRCList")
    private RegionSouthWestFrc interimSouthWestFrcList;
    @CCD(
            label = "Please choose the FRC which covers the area within which the Applicant resides.",
            hint = "This should be the FRC local to the applicant",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_wales_frc_list",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesSfvgopAccess.class}
    )
    @JsonProperty("interim_walesFRCList")
    private RegionWalesFrc interimWalesFrcList;
    @CCD(
            label = "Please choose the FRC which covers the area within which the Applicant resides.",
            hint = "This should be the FRC local to the applicant",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_hc_frc_list"
    )
    @JsonProperty("interim_highCourtFRCList")
    private RegionHighCourtFrc interimHighCourtFrcList;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    InterimCourtListWrapper courtListWrapper;

    @JsonIgnore
    public InterimCourtListWrapper getCourtListWrapper() {
        if (courtListWrapper == null) {
            this.courtListWrapper = new InterimCourtListWrapper();
        }
        return courtListWrapper;
    }
}
