package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterimHearingItem {
    @JsonProperty("interimHearingDate")
    public String interimHearingDate;
    @JsonProperty("interimHearingTime")
    public String interimHearingTime;
    @JsonProperty("interimHearingType")
    public String interimHearingType;
    @JsonProperty("interim_regionList")
    public String interimRegionList;
    @JsonProperty("interim_cfcCourtList")
    public String interimCfcCourtList;
    @JsonProperty("interim_walesFRCList")
    public String interimWalesFRCList;
    @JsonProperty("interim_londonFRCList")
    public String interimLondonFRCList;
    @JsonProperty("interim_devonCourtList")
    public String interimDevonCourtList;
    @JsonProperty("interim_dorsetCourtList")
    public String interimDorsetCourtList;
    @JsonProperty("interim_humberCourtList")
    public String interimHumberCourtList;
    @JsonProperty("interim_midlandsFRCList")
    public String interimMidlandsFRCList;
    @JsonProperty("interim_bristolCourtList")
    public String interimBristolCourtList;
    @JsonProperty("interim_newportCourtList")
    public String interimNewportCourtList;
    @JsonProperty("interim_northEastFRCList")
    public String interimNorthEastFRCList;
    @JsonProperty("interim_northWestFRCList")
    public String interimNorthWestFRCList;
    @JsonProperty("interim_southEastFRCList")
    public String interimSouthEastFRCList;
    @JsonProperty("interim_southWestFRCList")
    public String interimSouthWestFRCList;
    @JsonProperty("interim_swanseaCourtList")
    public String interimSwanseaCourtList;
    @JsonProperty("interimHearingTimeEstimate")
    public String interimHearingTimeEstimate;
    @JsonProperty("interim_liverpoolCourtList")
    public String interimLiverpoolCourtList;
    @JsonProperty("interimPromptForAnyDocument")
    public String interimPromptForAnyDocument;
    @JsonProperty("interim_birminghamCourtList")
    public String interimBirminghamCourtList;
    @JsonProperty("interim_cleavelandCourtList")
    public String interimCleavelandCourtList;
    @JsonProperty("interim_kentSurreyCourtList")
    public String interimKentSurreyCourtList;
    @JsonProperty("interim_lancashireCourtList")
    public String interimLancashireCourtList;
    @JsonProperty("interim_manchesterCourtList")
    public String interimManchesterCourtList;
    @JsonProperty("interim_northWalesCourtList")
    public String interimNorthWalesCourtList;
    @JsonProperty("interim_nottinghamCourtList")
    public String interimNottinghamCourtList;
    @JsonProperty("interim_nwyorkshireCourtList")
    public String interimNwyorkshireCourtList;
    @JsonProperty("interim_bedfordshireCourtList")
    public String interimBedfordshireCourtList;
    @JsonProperty("interim_thamesvalleyCourtList")
    public String interimThamesvalleyCourtList;
    @JsonProperty("interimUploadAdditionalDocument")
    public CaseDocument interimUploadAdditionalDocument;
    @JsonProperty("interimAdditionalInformationAboutHearing")
    public String interimAdditionalInformationAboutHearing;
}
