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
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FRLondonFRCList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FRNeFrcList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FRSeFrcList;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeneralApplicationRegionWrapper {
    @CCD(
            label = "Please choose the Region in which the Hearing takes place",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_region_list",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    @JsonProperty("generalApplicationDirections_regionList")
    private Region generalApplicationDirectionsRegionList;
    @CCD(
            label = "Please choose the FRC which covers the area within which the Hearing takes place",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_midlands_FRCList",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    @JsonProperty("generalApplicationDirections_midlandsFRCList")
    private RegionMidlandsFrc generalApplicationDirectionsMidlandsFrcList;
    @CCD(
            label = "Please choose the FRC which covers the area within which the Hearing takes place",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_london_FRCList",
            typeParameterClass = FRLondonFRCList.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    @JsonProperty("generalApplicationDirections_londonFRCList")
    private RegionLondonFrc generalApplicationDirectionsLondonFrcList;
    @CCD(
            label = "Please choose the FRC which covers the area within which the Hearing takes place",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_nw_frc_list",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    @JsonProperty("generalApplicationDirections_northWestFRCList")
    private RegionNorthWestFrc generalApplicationDirectionsNorthWestFrcList;
    @CCD(
            label = "Please choose the FRC which covers the area within which the Hearing takes place",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_ne_frc_list",
            typeParameterClass = FRNeFrcList.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    @JsonProperty("generalApplicationDirections_northEastFRCList")
    private RegionNorthEastFrc generalApplicationDirectionsNorthEastFrcList;
    @CCD(
            label = "Please choose the FRC which covers the area within which the Hearing takes place",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_se_frc_list",
            typeParameterClass = FRSeFrcList.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    @JsonProperty("generalApplicationDirections_southEastFRCList")
    private RegionSouthEastFrc generalApplicationDirectionsSouthEastFrcList;
    @CCD(
            label = "Please choose the FRC which covers the area within which the Hearing takes place",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_sw_frc_list",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    @JsonProperty("generalApplicationDirections_southWestFRCList")
    private RegionSouthWestFrc generalApplicationDirectionsSouthWestFrcList;
    @CCD(
            label = "Please choose the FRC which covers the area within which the Hearing takes place",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_wales_frc_list",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    @JsonProperty("generalApplicationDirections_walesFRCList")
    private RegionWalesFrc generalApplicationDirectionsWalesFrcList;
    @CCD(
            label = "Please choose the FRC which covers the area within which the Hearing takes place",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_hc_frc_list",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    @JsonProperty("generalApplicationDirections_highCourtFRCList")
    private RegionHighCourtFrc generalApplicationDirectionsHighCourtFrcList;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    GeneralApplicationCourtListWrapper courtListWrapper;

    @JsonIgnore
    public GeneralApplicationCourtListWrapper getCourtListWrapper() {
        if (courtListWrapper == null) {
            this.courtListWrapper = new GeneralApplicationCourtListWrapper();
        }
        return courtListWrapper;
    }

    @JsonIgnore
    public void clearRegions() {
        this.generalApplicationDirectionsRegionList = null;
        this.generalApplicationDirectionsMidlandsFrcList = null;
        this.generalApplicationDirectionsLondonFrcList = null;
        this.generalApplicationDirectionsNorthWestFrcList = null;
        this.generalApplicationDirectionsNorthEastFrcList = null;
        this.generalApplicationDirectionsSouthEastFrcList = null;
        this.generalApplicationDirectionsSouthWestFrcList = null;
        this.generalApplicationDirectionsWalesFrcList = null;
        this.generalApplicationDirectionsHighCourtFrcList = null;
    }

    public Court toCourt() {
        return Court.builder()
            .region(generalApplicationDirectionsRegionList)
            .midlandsList(generalApplicationDirectionsMidlandsFrcList)
            .londonList(generalApplicationDirectionsLondonFrcList)
            .northWestList(generalApplicationDirectionsNorthWestFrcList)
            .northEastList(generalApplicationDirectionsNorthEastFrcList)
            .southEastList(generalApplicationDirectionsSouthEastFrcList)
            .southWestList(generalApplicationDirectionsSouthWestFrcList)
            .walesList(generalApplicationDirectionsWalesFrcList)
            .hcCourtList(generalApplicationDirectionsHighCourtFrcList)
            .courtListWrapper(getCourtListWrapper().toDefaultCourtListWrapper())
            .build();
    }
}
