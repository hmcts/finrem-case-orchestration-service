package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConsentedHearingDataElement {
    @JsonProperty("hearingType")
    public String hearingType;
    @JsonProperty("hearingTimeEstimate")
    public String hearingTimeEstimate;
    @JsonProperty("hearingDate")
    public String hearingDate;
    @JsonProperty("hearingTime")
    public String hearingTime;
    @JsonProperty("hearing_regionList")
    public String regionList;
    @JsonProperty("hearing_midlandsFRCList")
    public String midlandsFRCList;
    @JsonProperty("hearing_londonFRCList")
    public String londonFRCList;
    @JsonProperty("hearing_northWestFRCList")
    public String northWestFRCList;
    @JsonProperty("hearing_northEastFRCList")
    public String northEastFRCList;
    @JsonProperty("hearing_southEastFRCList")
    public String southEastFRCList;
    @JsonProperty("hearing_southWestFRCList")
    public String southWestFRCList;
    @JsonProperty("hearing_walesFRCList")
    public String walesFRCList;
    @JsonProperty("hearing_highCourtFRCList")
    public String highCourtFRCList;
    @JsonProperty("hearing_nottinghamCourtList")
    public String nottinghamCourtList;
    @JsonProperty("hearing_cfcCourtList")
    public String cfcCourtList;
    @JsonProperty("hearing_birminghamCourtList")
    public String birminghamCourtList;
    @JsonProperty("hearing_liverpoolCourtList")
    public String liverpoolCourtList;
    @JsonProperty("hearing_manchesterCourtList")
    public String manchesterCourtList;
    @JsonProperty("hearing_lancashireCourtList")
    public String lancashireCourtList;
    @JsonProperty("hearing_cleavelandCourtList")
    public String cleavelandCourtList;
    @JsonProperty("hearing_nwyorkshireCourtList")
    public String nwyorkshireCourtList;
    @JsonProperty("hearing_humberCourtList")
    public String humberCourtList;
    @JsonProperty("hearing_kentSurreyCourtList")
    public String kentSurreyCourtList;
    @JsonProperty("hearing_bedfordshireCourtList")
    public String bedfordshireCourtList;
    @JsonProperty("hearing_thamesvalleyCourtList")
    public String thamesvalleyCourtList;
    @JsonProperty("hearing_devonCourtList")
    public String devonCourtList;
    @JsonProperty("hearing_dorsetCourtList")
    public String dorsetCourtList;
    @JsonProperty("hearing_bristolCourtList")
    public String bristolCourtList;
    @JsonProperty("hearing_newportCourtList")
    public String newportCourtList;
    @JsonProperty("hearing_swanseaCourtList")
    public String swanseaCourtList;
    @JsonProperty("hearing_northWalesCourtList")
    public String northWalesCourtList;
    @JsonProperty("hearing_highCourtList")
    public String highCourtList;
    @JsonProperty("additionalInformationAboutHearing")
    public String additionalInformationAboutHearing;
    @JsonProperty("promptForAnyDocument")
    public String promptForAnyDocument;
    @JsonProperty("uploadAdditionalDocument")
    public CaseDocument uploadAdditionalDocument;
    @JsonProperty("hearingNotice")
    public CaseDocument hearingNotice;
}
