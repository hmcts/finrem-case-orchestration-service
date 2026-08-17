package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DefaultCourtListWrapper;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_ct_courtList", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Court implements CourtListWrapper {
    @CCD(
            label = "Please state in which Financial Remedies Court Zone the applicant resides",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_region_list"
    )
    private Region region;
    @CCD(
            label = "Please choose the FRC which covers the area within which the Applicant resides - if the applicant does not reside within one of these areas, please choose 'other'",
            showCondition = "region=\"midlands\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_midlands_FRCList"
    )
    private RegionMidlandsFrc midlandsList;
    @CCD(
            label = "Please choose the FRC which covers the area within which the Applicant resides - if the applicant does not reside within one of these areas, please choose 'other'",
            showCondition = "region=\"london\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_london_FRCList",
            typeParameterClass = FRLondonFRCList.class
    )
    private RegionLondonFrc londonList;
    @CCD(
            label = "Please choose the FRC which covers the area within which the Applicant resides - if the applicant does not reside within one of these areas, please choose 'other'",
            showCondition = "region=\"northwest\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_nw_frc_list"
    )
    private RegionNorthWestFrc northWestList;
    @CCD(
            label = "Please choose the FRC which covers the area within which the Applicant resides - if the applicant does not reside within one of these areas, please choose 'other'",
            showCondition = "region=\"northeast\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_ne_frc_list",
            typeParameterClass = FRNeFrcList.class
    )
    private RegionNorthEastFrc northEastList;
    @CCD(
            label = "Please choose the FRC which covers the area within which the Applicant resides - if the applicant does not reside within one of these areas, please choose 'other'",
            showCondition = "region=\"southeast\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_se_frc_list",
            typeParameterClass = FRSeFrcList.class
    )
    private RegionSouthEastFrc southEastList;
    @CCD(
            label = "Please choose the FRC which covers the area within which the Applicant resides - if the applicant does not reside within one of these areas, please choose 'other'",
            showCondition = "region=\"southwest\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_sw_frc_list"
    )
    private RegionSouthWestFrc southWestList;
    @CCD(
            label = "Please choose the FRC which covers the area within which the Applicant resides - if the applicant does not reside within one of these areas, please choose 'other'",
            showCondition = "region=\"wales\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_wales_frc_list"
    )
    private RegionWalesFrc walesList;

    @CCD(
            label = "Please choose the FRC which covers the area within which the Applicant resides - if the applicant does not reside within one of these areas, please choose 'other'",
            showCondition = "region=\"highcourt\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_hc_frc_list"
    )
    private RegionHighCourtFrc hcCourtList;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    DefaultCourtListWrapper courtListWrapper;

    @JsonIgnore
    public DefaultCourtListWrapper getDefaultCourtListWrapper() {
        if (courtListWrapper == null) {
            this.courtListWrapper = new DefaultCourtListWrapper();
        }
        return courtListWrapper;
    }

    @JsonIgnore
    @Override
    public NottinghamCourt getNottinghamCourt() {
        return getDefaultCourtListWrapper().getNottinghamCourtList();
    }

    @JsonIgnore
    @Override
    public CfcCourt getCfcCourt() {
        return getDefaultCourtListWrapper().getCfcCourtList();
    }

    @JsonIgnore
    @Override
    public LondonCourt getLondonCourt() {
        return getDefaultCourtListWrapper().getLondonCourtList();
    }

    @JsonIgnore
    @Override
    public BirminghamCourt getBirminghamCourt() {
        return getDefaultCourtListWrapper().getBirminghamCourtList();
    }

    @JsonIgnore
    @Override
    public LiverpoolCourt getLiverpoolCourt() {
        return getDefaultCourtListWrapper().getLiverpoolCourtList();
    }

    @JsonIgnore
    @Override
    public ManchesterCourt getManchesterCourt() {
        return getDefaultCourtListWrapper().getManchesterCourtList();
    }

    @JsonIgnore
    @Override
    public LancashireCourt getLancashireCourt() {
        return getDefaultCourtListWrapper().getLancashireCourtList();
    }

    @JsonIgnore
    @Override
    public ClevelandCourt getClevelandCourt() {
        return getDefaultCourtListWrapper().getClevelandCourt();
    }

    @JsonIgnore
    @Override
    public NwYorkshireCourt getNwYorkshireCourt() {
        return getDefaultCourtListWrapper().getNwYorkshireCourtList();
    }

    @JsonIgnore
    @Override
    public HumberCourt getHumberCourt() {
        return getDefaultCourtListWrapper().getHumberCourtList();
    }

    @JsonIgnore
    @Override
    public KentSurreyCourt getKentSurreyCourt() {
        return getDefaultCourtListWrapper().getKentSurreyCourtList();
    }

    @JsonIgnore
    @Override
    public BedfordshireCourt getBedfordshireCourt() {
        return getDefaultCourtListWrapper().getBedfordshireCourtList();
    }

    @JsonIgnore
    @Override
    public ThamesValleyCourt getThamesValleyCourt() {
        return getDefaultCourtListWrapper().getThamesValleyCourtList();
    }

    @JsonIgnore
    @Override
    public DevonCourt getDevonCourt() {
        return getDefaultCourtListWrapper().getDevonCourtList();
    }

    @JsonIgnore
    @Override
    public DorsetCourt getDorsetCourt() {
        return getDefaultCourtListWrapper().getDorsetCourtList();
    }

    @JsonIgnore
    @Override
    public BristolCourt getBristolCourt() {
        return getDefaultCourtListWrapper().getBristolCourtList();
    }

    @JsonIgnore
    @Override
    public NewportCourt getNewportCourt() {
        return getDefaultCourtListWrapper().getNewportCourtList();
    }

    @JsonIgnore
    @Override
    public SwanseaCourt getSwanseaCourt() {
        return getDefaultCourtListWrapper().getSwanseaCourtList();
    }

    @JsonIgnore
    @Override
    public NorthWalesCourt getNorthWalesCourt() {
        return getDefaultCourtListWrapper().getNorthWalesCourtList();
    }

    @JsonIgnore
    @Override
    public HighCourt getHighCourt() {
        return getDefaultCourtListWrapper().getHighCourt();
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "This would usually be the applicants local Court", searchable = false, typeOverride = FieldType.Label)
  private String localCourtLbl;
  // ==== end synthesised definition-only fields ====
}
