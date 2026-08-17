package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Court;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FRLondonFRCList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FRNeFrcList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FRSeFrcList;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AllocatedRegionWrapper {

    @CCD(
            label = "Please state in which Financial Remedies Court Zone the applicant resides",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_region_list",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class}
    )
    @JsonProperty("regionList")
    private Region regionList;
    @CCD(
            label = "Please choose the FRC which covers the area within which the Applicant resides.",
            hint = "This should be the FRC local to the applicant",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_midlands_FRCList",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class}
    )
    @JsonProperty("midlandsFRCList")
    private RegionMidlandsFrc midlandsFrcList;
    @CCD(
            label = "Please choose the FRC which covers the area within which the Applicant resides.",
            hint = "This should be the FRC local to the applicant",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_london_FRCList",
            typeParameterClass = FRLondonFRCList.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class}
    )
    @JsonProperty("londonFRCList")
    private RegionLondonFrc londonFrcList;
    @CCD(
            label = "Please choose the FRC which covers the area within which the Applicant resides.",
            hint = "This should be the FRC local to the applicant",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_nw_frc_list",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class}
    )
    @JsonProperty("northWestFRCList")
    private RegionNorthWestFrc northWestFrcList;
    @CCD(
            label = "Please choose the FRC which covers the area within which the Applicant resides.",
            hint = "This should be the FRC local to the applicant",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_ne_frc_list",
            typeParameterClass = FRNeFrcList.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class}
    )
    @JsonProperty("northEastFRCList")
    private RegionNorthEastFrc northEastFrcList;
    @CCD(
            label = "Please choose the FRC which covers the area within which the Applicant resides.",
            hint = "This should be the FRC local to the applicant",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_se_frc_list",
            typeParameterClass = FRSeFrcList.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class}
    )
    @JsonProperty("southEastFRCList")
    private RegionSouthEastFrc southEastFrcList;
    @CCD(
            label = "Please choose the FRC which covers the area within which the Applicant resides.",
            hint = "This should be the FRC local to the applicant",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_sw_frc_list",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class}
    )
    @JsonProperty("southWestFRCList")
    private RegionSouthWestFrc southWestFrcList;
    @CCD(
            label = "Please choose the FRC which covers the area within which the Applicant resides.",
            hint = "This should be the FRC local to the applicant",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_wales_frc_list",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class}
    )
    @JsonProperty("walesFRCList")
    private RegionWalesFrc walesFrcList;
    @CCD(
            label = "Please choose the FRC which covers the area within which the Applicant resides.",
            hint = "This should be the FRC local to the applicant",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_hc_frc_list",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class}
    )
    @JsonProperty("highCourtFRCList")
    private RegionHighCourtFrc highCourtFrcList;
    @JsonUnwrapped
    DefaultCourtListWrapper courtListWrapper;

    @JsonIgnore
    public DefaultCourtListWrapper getDefaultCourtListWrapper() {
        if (courtListWrapper == null) {
            this.courtListWrapper = new DefaultCourtListWrapper();
        }
        return courtListWrapper;
    }

    public Court toCourt() {
        return Court.builder()
            .region(regionList)
            .midlandsList(midlandsFrcList)
            .londonList(londonFrcList)
            .northWestList(northWestFrcList)
            .northEastList(northEastFrcList)
            .southEastList(southEastFrcList)
            .southWestList(southWestFrcList)
            .walesList(walesFrcList)
            .hcCourtList(highCourtFrcList)
            .courtListWrapper(courtListWrapper)
            .build();
    }
}
