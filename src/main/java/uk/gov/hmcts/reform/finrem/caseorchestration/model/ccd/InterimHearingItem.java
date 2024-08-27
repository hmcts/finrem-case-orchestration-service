package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterimHearingItem implements HasCaseDocument {
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public LocalDate interimHearingDate;
    public String interimHearingTime;
    public InterimTypeOfHearing interimHearingType;
    public YesOrNo interimPromptForAnyDocument;
    public String interimHearingTimeEstimate;
    public CaseDocument interimUploadAdditionalDocument;
    public String interimAdditionalInformationAboutHearing;

    @JsonProperty("interim_regionList")
    private Region interimRegionList;
    @JsonProperty("interim_midlandsFRCList")
    private RegionMidlandsFrc interimMidlandsFrcList;
    @JsonProperty("interim_londonFRCList")
    private RegionLondonFrc interimLondonFrcList;
    @JsonProperty("interim_northWestFRCList")
    private RegionNorthWestFrc interimNorthWestFrcList;
    @JsonProperty("interim_northEastFRCList")
    private RegionNorthEastFrc interimNorthEastFrcList;
    @JsonProperty("interim_southEastFRCList")
    private RegionSouthEastFrc interimSouthEastFrcList;
    @JsonProperty("interim_southWestFRCList")
    private RegionSouthWestFrc interimSouthWestFrcList;
    @JsonProperty("interim_walesFRCList")
    private RegionWalesFrc interimWalesFrcList;
    @JsonProperty("interim_highCourtFRCList")
    private RegionHighCourtFrc interimHighCourtFrcList;
    @JsonProperty("interim_nottinghamCourtList")
    private NottinghamCourt interimNottinghamCourtList;
    @JsonProperty("interim_cfcCourtList")
    private CfcCourt interimCfcCourtList;
    @JsonProperty("interim_birminghamCourtList")
    private BirminghamCourt interimBirminghamCourtList;
    @JsonProperty("interim_liverpoolCourtList")
    private LiverpoolCourt interimLiverpoolCourtList;
    @JsonProperty("interim_manchesterCourtList")
    private ManchesterCourt interimManchesterCourtList;
    @JsonProperty("interim_lancashireCourtList")
    private LancashireCourt interimLancashireCourtList;
    @JsonProperty("interim_cleavelandCourtList")
    private ClevelandCourt interimClevelandCourtList;
    @JsonProperty("interim_nwyorkshireCourtList")
    private NwYorkshireCourt interimNwYorkshireCourtList;
    @JsonProperty("interim_humberCourtList")
    private HumberCourt interimHumberCourtList;
    @JsonProperty("interim_kentSurreyCourtList")
    private KentSurreyCourt interimKentSurreyCourtList;
    @JsonProperty("interim_bedfordshireCourtList")
    private BedfordshireCourt interimBedfordshireCourtList;
    @JsonProperty("interim_thamesvalleyCourtList")
    private ThamesValleyCourt interimThamesValleyCourtList;
    @JsonProperty("interim_devonCourtList")
    private DevonCourt interimDevonCourtList;
    @JsonProperty("interim_dorsetCourtList")
    private DorsetCourt interimDorsetCourtList;
    @JsonProperty("interim_bristolCourtList")
    private BristolCourt interimBristolCourtList;
    @JsonProperty("interim_newportCourtList")
    private NewportCourt interimNewportCourtList;
    @JsonProperty("interim_swanseaCourtList")
    private SwanseaCourt interimSwanseaCourtList;
    @JsonProperty("interim_northWalesCourtList")
    private NorthWalesCourt interimNorthWalesCourtList;
    @JsonProperty("interim_highCourtList")
    private HighCourt interimHighCourtList;
}
