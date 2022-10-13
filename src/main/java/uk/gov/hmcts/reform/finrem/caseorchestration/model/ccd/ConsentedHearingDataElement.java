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
public class ConsentedHearingDataElement {
    @JsonProperty("hearingType")
    public String hearingType;
    @JsonProperty("hearingTimeEstimate")
    public String hearingTimeEstimate;
    @JsonProperty("hearingDate")
    public String hearingDate;
    @JsonProperty("hearingTime")
    public String hearingTime;
    @JsonProperty("regionList")
    public String regionList;
    @JsonProperty("midlandsFRCList")
    public String midlandsFRCList;
    @JsonProperty("londonFRCList")
    public String londonFRCList;
    @JsonProperty("northWestFRCList")
    public String northWestFRCList;
    @JsonProperty("northEastFRCList")
    public String northEastFRCList;
    @JsonProperty("southEastFRCList")
    public String southEastFRCList;
    @JsonProperty("southWestFRCList")
    public String southWestFRCList;
    @JsonProperty("walesFRCList")
    public String walesFRCList;
    @JsonProperty("nottinghamCourtList")
    public String nottinghamCourtList;
    @JsonProperty("cfcCourtList")
    public String cfcCourtList;
    @JsonProperty("birminghamCourtList")
    public String birminghamCourtList;
    @JsonProperty("liverpoolCourtList")
    public String liverpoolCourtList;
    @JsonProperty("manchesterCourtList")
    public String manchesterCourtList;
    @JsonProperty("lancashireCourtList")
    public String lancashireCourtList;
    @JsonProperty("cleavelandCourtList")
    public String cleavelandCourtList;
    @JsonProperty("nwyorkshireCourtList")
    public String nwyorkshireCourtList;
    @JsonProperty("humberCourtList")
    public String humberCourtList;
    @JsonProperty("kentSurreyCourtList")
    public String kentSurreyCourtList;
    @JsonProperty("bedfordshireCourtList")
    public String bedfordshireCourtList;
    @JsonProperty("thamesvalleyCourtList")
    public String thamesvalleyCourtList;
    @JsonProperty("devonCourtList")
    public String devonCourtList;
    @JsonProperty("dorsetCourtList")
    public String dorsetCourtList;
    @JsonProperty("bristolCourtList")
    public String bristolCourtList;
    @JsonProperty("newportCourtList")
    public String newportCourtList;
    @JsonProperty("swanseaCourtList")
    public String swanseaCourtList;
    @JsonProperty("northWalesCourtList")
    public String northWalesCourtList;
    @JsonProperty("additionalInformationAboutHearing")
    public String additionalInformationAboutHearing;
    @JsonProperty("promptForAnyDocument")
    public String promptForAnyDocument;
    @JsonProperty("uploadAdditionalDocument")
    public CaseDocument uploadAdditionalDocument;
    @JsonProperty("hearingNotice")
    public CaseDocument hearingNotice;
}
