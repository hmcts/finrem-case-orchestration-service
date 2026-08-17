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
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCruPlus2RolesAblzopAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCruPlus2RolesGasysbAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FRLondonFRCList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FRNeFrcList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FRSeFrcList;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingRegionWrapper {
    @CCD(
            label = "Please state in which Financial Remedies Court Zone the applicant resides",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_region_list",
            access = {CaseworkerDivorceFinancialremedyCourtadminCruPlus2RolesAblzopAccess.class}
    )
    @JsonProperty("hearing_regionList")
    private Region hearingRegionList;
    @CCD(
            label = "Please choose the FRC which covers the area within which the Applicant resides.",
            hint = "This should be the FRC local to the applicant",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_midlands_FRCList",
            access = {CaseworkerDivorceFinancialremedyCourtadminCruPlus2RolesAblzopAccess.class}
    )
    @JsonProperty("hearing_midlandsFRCList")
    private RegionMidlandsFrc hearingMidlandsFrcList;
    @CCD(
            label = "Please choose the FRC which covers the area within which the Applicant resides.",
            hint = "This should be the FRC local to the applicant",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_london_FRCList",
            typeParameterClass = FRLondonFRCList.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCruPlus2RolesAblzopAccess.class}
    )
    @JsonProperty("hearing_londonFRCList")
    private RegionLondonFrc hearingLondonFrcList;
    @CCD(
            label = "Please choose the FRC which covers the area within which the Applicant resides.",
            hint = "This should be the FRC local to the applicant",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_nw_frc_list",
            access = {CaseworkerDivorceFinancialremedyCourtadminCruPlus2RolesAblzopAccess.class}
    )
    @JsonProperty("hearing_northWestFRCList")
    private RegionNorthWestFrc hearingNorthWestFrcList;
    @CCD(
            label = "Please choose the FRC which covers the area within which the Applicant resides.",
            hint = "This should be the FRC local to the applicant",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_ne_frc_list",
            typeParameterClass = FRNeFrcList.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCruPlus2RolesAblzopAccess.class}
    )
    @JsonProperty("hearing_northEastFRCList")
    private RegionNorthEastFrc hearingNorthEastFrcList;
    @CCD(
            label = "Please choose the FRC which covers the area within which the Applicant resides.",
            hint = "This should be the FRC local to the applicant",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_se_frc_list",
            typeParameterClass = FRSeFrcList.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCruPlus2RolesAblzopAccess.class}
    )
    @JsonProperty("hearing_southEastFRCList")
    private RegionSouthEastFrc hearingSouthEastFrcList;
    @CCD(
            label = "Please choose the FRC which covers the area within which the Applicant resides.",
            hint = "This should be the FRC local to the applicant",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_sw_frc_list",
            access = {CaseworkerDivorceFinancialremedyCourtadminCruPlus2RolesAblzopAccess.class}
    )
    @JsonProperty("hearing_southWestFRCList")
    private RegionSouthWestFrc hearingSouthWestFrcList;
    @CCD(
            label = "Please choose the FRC which covers the area within which the Applicant resides.",
            hint = "This should be the FRC local to the applicant",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_wales_frc_list",
            access = {CaseworkerDivorceFinancialremedyCourtadminCruPlus2RolesGasysbAccess.class}
    )
    @JsonProperty("hearing_walesFRCList")
    private RegionWalesFrc hearingWalesFrcList;
    @CCD(
            label = "Please choose the FRC which covers the area within which the Applicant resides.",
            hint = "This should be the FRC local to the applicant",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_hc_frc_list",
            access = {CaseworkerDivorceFinancialremedyCourtadminCruPlus2RolesGasysbAccess.class}
    )
    @JsonProperty("hearing_highCourtFRCList")
    private RegionHighCourtFrc hearingHighCourtFrcList;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    HearingCourtWrapper courtListWrapper;

    @JsonIgnore
    public HearingCourtWrapper getCourtListWrapper() {
        if (courtListWrapper == null) {
            this.courtListWrapper = new HearingCourtWrapper();
        }
        return courtListWrapper;
    }

    public Court toCourt() {
        return Court.builder()
            .region(hearingRegionList)
            .midlandsList(hearingMidlandsFrcList)
            .londonList(hearingLondonFrcList)
            .northWestList(hearingNorthWestFrcList)
            .northEastList(hearingNorthEastFrcList)
            .southEastList(hearingSouthEastFrcList)
            .southWestList(hearingSouthWestFrcList)
            .walesList(hearingWalesFrcList)
            .hcCourtList(hearingHighCourtFrcList)
            .courtListWrapper(courtListWrapper.toDefaultCourtListWrapper())
            .build();
    }

    @JsonIgnore
    public boolean isEmpty() {
        return hearingRegionList == null
            && hearingMidlandsFrcList == null
            && hearingLondonFrcList == null
            && hearingNorthWestFrcList == null
            && hearingNorthEastFrcList == null
            && hearingSouthEastFrcList == null
            && hearingSouthWestFrcList == null
            && hearingWalesFrcList == null
            && hearingHighCourtFrcList == null;
    }
}
