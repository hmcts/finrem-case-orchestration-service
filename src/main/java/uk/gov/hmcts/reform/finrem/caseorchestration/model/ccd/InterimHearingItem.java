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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DefaultCourtListWrapper;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "interimHearingsCollection", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterimHearingItem implements HasCaseDocument {
    @CCD(
            label = "Hearing Date",
            hint = "Fast Track: Date of the Fast Track hearing must be between 6 and 10 weeks.\r\nStandard Track: Date of the hearing must be between 12 and 16 weeks\r\n",
            searchable = false
    )
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public LocalDate interimHearingDate;
    @CCD(label = "Hearing Time", searchable = false)
    public String interimHearingTime;
    @CCD(
            label = "Type of Hearing",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_fl_interimHearingTypeDirections"
    )
    public InterimTypeOfHearing interimHearingType;
    @CCD(label = "Do you want to upload any other documents ?", searchable = false, typeOverride = FieldType.YesOrNo)
    public YesOrNo interimPromptForAnyDocument;
    @CCD(label = "Time Estimate", searchable = false)
    public String interimHearingTimeEstimate;
    @CCD(
            label = "Please upload any additional documents related to your application.",
            showCondition = "interimPromptForAnyDocument=\"Yes\"",
            categoryID = "hearingNotices",
            searchable = false,
            typeOverride = FieldType.Document
    )
    public CaseDocument interimUploadAdditionalDocument;
    @CCD(label = "Additional information about the hearing", searchable = false)
    public String interimAdditionalInformationAboutHearing;

    @CCD(
            label = "Hearing Court: \r\nPlease state in which Financial Remedies Court Zone the applicant resides",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_region_list"
    )
    @JsonProperty("interim_regionList")
    private Region interimRegionList;
    @CCD(
            label = "Please choose the FRC which covers the area within which the Applicant resides - if the applicant does not reside within one of these areas, please choose 'other'",
            showCondition = "interim_regionList=\"midlands\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_midlands_FRCList"
    )
    @JsonProperty("interim_midlandsFRCList")
    private RegionMidlandsFrc interimMidlandsFrcList;
    @CCD(
            label = "Please choose the FRC which covers the area within which the Applicant resides - if the applicant does not reside within one of these areas, please choose 'other'",
            showCondition = "interim_regionList=\"london\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_london_FRCList",
            typeParameterClass = FRLondonFRCList.class
    )
    @JsonProperty("interim_londonFRCList")
    private RegionLondonFrc interimLondonFrcList;
    @CCD(
            label = "Please choose the FRC which covers the area within which the Applicant resides - if the applicant does not reside within one of these areas, please choose 'other'",
            showCondition = "interim_regionList=\"northwest\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_nw_frc_list"
    )
    @JsonProperty("interim_northWestFRCList")
    private RegionNorthWestFrc interimNorthWestFrcList;
    @CCD(
            label = "Please choose the FRC which covers the area within which the Applicant resides - if the applicant does not reside within one of these areas, please choose 'other'",
            showCondition = "interim_regionList=\"northeast\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_ne_frc_list",
            typeParameterClass = FRNeFrcList.class
    )
    @JsonProperty("interim_northEastFRCList")
    private RegionNorthEastFrc interimNorthEastFrcList;
    @CCD(
            label = "Please choose the FRC which covers the area within which the Applicant resides - if the applicant does not reside within one of these areas, please choose 'other'",
            showCondition = "interim_regionList=\"southeast\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_se_frc_list",
            typeParameterClass = FRSeFrcList.class
    )
    @JsonProperty("interim_southEastFRCList")
    private RegionSouthEastFrc interimSouthEastFrcList;
    @CCD(
            label = "Please choose the FRC which covers the area within which the Applicant resides - if the applicant does not reside within one of these areas, please choose 'other'",
            showCondition = "interim_regionList=\"southwest\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_sw_frc_list"
    )
    @JsonProperty("interim_southWestFRCList")
    private RegionSouthWestFrc interimSouthWestFrcList;
    @CCD(
            label = "Please choose the FRC which covers the area within which the Applicant resides - if the applicant does not reside within one of these areas, please choose 'other'",
            showCondition = "interim_regionList=\"wales\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_wales_frc_list"
    )
    @JsonProperty("interim_walesFRCList")
    private RegionWalesFrc interimWalesFrcList;
    @CCD(
            label = "Please choose the FRC which covers the area within which the Applicant resides - if the applicant does not reside within one of these areas, please choose 'other'",
            showCondition = "interim_regionList=\"highcourt\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_hc_frc_list"
    )
    @JsonProperty("interim_highCourtFRCList")
    private RegionHighCourtFrc interimHighCourtFrcList;
    @CCD(
            label = "Please choose the FRC which covers the area within which the Applicant resides - if the applicant does not reside within one of these areas, please choose 'other'",
            showCondition = "interim_regionList=\"midlands\" AND interim_midlandsFRCList=\"nottingham\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_s_NottinghamList",
            typeParameterClass = FRSNottinghamList.class
    )
    @JsonProperty("interim_nottinghamCourtList")
    private NottinghamCourt interimNottinghamCourtList;
    @CCD(
            label = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            showCondition = "interim_regionList=\"london\" AND interim_londonFRCList=\"cfc\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_s_CFCList",
            typeParameterClass = FRSCFCList.class
    )
    @JsonProperty("interim_cfcCourtList")
    private CfcCourt interimCfcCourtList;
    @CCD(
            label = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            showCondition = "interim_regionList=\"midlands\" AND interim_midlandsFRCList=\"birmingham\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_birmingham_hc_list",
            typeParameterClass = FRBirminghamHcList.class
    )
    @JsonProperty("interim_birminghamCourtList")
    private BirminghamCourt interimBirminghamCourtList;
    @CCD(
            label = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            showCondition = "interim_regionList=\"northwest\" AND interim_northWestFRCList=\"liverpool\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_liverpool_hc_list",
            typeParameterClass = FRLiverpoolHcList.class
    )
    @JsonProperty("interim_liverpoolCourtList")
    private LiverpoolCourt interimLiverpoolCourtList;
    @CCD(
            label = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            showCondition = "interim_regionList=\"northwest\" AND interim_northWestFRCList=\"manchester\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_manchester_hc_list",
            typeParameterClass = FRManchesterHcList.class
    )
    @JsonProperty("interim_manchesterCourtList")
    private ManchesterCourt interimManchesterCourtList;
    @CCD(
            label = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            showCondition = "interim_regionList=\"northwest\" AND interim_northWestFRCList=\"lancashire\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_lancashireList"
    )
    @JsonProperty("interim_lancashireCourtList")
    private LancashireCourt interimLancashireCourtList;
    @CCD(
            label = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            showCondition = "interim_regionList=\"northeast\" AND interim_northEastFRCList=\"cleaveland\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_cleveland_hc_list",
            typeParameterClass = FRClevelandHcList.class
    )
    @JsonProperty("interim_cleavelandCourtList")
    private ClevelandCourt interimClevelandCourtList;
    @CCD(
            label = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            showCondition = "interim_regionList=\"northeast\" AND interim_northEastFRCList=\"nwyorkshire\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_nw_yorkshire_hc_list",
            typeParameterClass = FRNwYorkshireHcList.class
    )
    @JsonProperty("interim_nwyorkshireCourtList")
    private NwYorkshireCourt interimNwYorkshireCourtList;
    @CCD(
            label = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            showCondition = "interim_regionList=\"northeast\" AND interim_northEastFRCList=\"hsyorkshire\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_humber_hc_list",
            typeParameterClass = FRHumberHcList.class
    )
    @JsonProperty("interim_humberCourtList")
    private HumberCourt interimHumberCourtList;
    @CCD(
            label = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            showCondition = "interim_regionList=\"southeast\" AND interim_southEastFRCList=\"kentfrc\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_kent_surrey_hc_list",
            typeParameterClass = FRKentSurreyHcList.class
    )
    @JsonProperty("interim_kentSurreyCourtList")
    private KentSurreyCourt interimKentSurreyCourtList;
    @CCD(
            label = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            showCondition = "interim_regionList=\"southeast\" AND interim_southEastFRCList=\"bedfordshire\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_bedfordshireList"
    )
    @JsonProperty("interim_bedfordshireCourtList")
    private BedfordshireCourt interimBedfordshireCourtList;
    @CCD(
            label = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            showCondition = "interim_regionList=\"southeast\" AND interim_southEastFRCList=\"thamesvalley\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_thamesvalleyList"
    )
    @JsonProperty("interim_thamesvalleyCourtList")
    private ThamesValleyCourt interimThamesValleyCourtList;
    @CCD(
            label = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            showCondition = "interim_regionList=\"southwest\" AND interim_southWestFRCList=\"devon\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_devonList"
    )
    @JsonProperty("interim_devonCourtList")
    private DevonCourt interimDevonCourtList;
    @CCD(
            label = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            showCondition = "interim_regionList=\"southwest\" AND interim_southWestFRCList=\"dorset\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_dorsetList"
    )
    @JsonProperty("interim_dorsetCourtList")
    private DorsetCourt interimDorsetCourtList;
    @CCD(
            label = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            showCondition = "interim_regionList=\"southwest\" AND interim_southWestFRCList=\"bristol\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_bristolList"
    )
    @JsonProperty("interim_bristolCourtList")
    private BristolCourt interimBristolCourtList;
    @CCD(
            label = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            showCondition = "interim_regionList=\"wales\" AND interim_walesFRCList=\"newport\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_newport_hc_list",
            typeParameterClass = FRNewportHcList.class
    )
    @JsonProperty("interim_newportCourtList")
    private NewportCourt interimNewportCourtList;
    @CCD(
            label = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            showCondition = "interim_regionList=\"wales\" AND interim_walesFRCList=\"swansea\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_swansea_hc_list",
            typeParameterClass = FRSwanseaHcList.class
    )
    @JsonProperty("interim_swanseaCourtList")
    private SwanseaCourt interimSwanseaCourtList;
    @CCD(
            label = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            showCondition = "interim_regionList=\"wales\" AND interim_walesFRCList=\"northwales\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_northwalesList"
    )
    @JsonProperty("interim_northWalesCourtList")
    private NorthWalesCourt interimNorthWalesCourtList;
    @CCD(
            label = "Please give the name of the Court which is closest to the Applicants home postcode. If you are unsure, please check on http://courttribunalfinder.service.gov.uk",
            showCondition = "interim_regionList=\"highcourt\" AND interim_highCourtFRCList=\"highcourt\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_highCourtList"
    )
    @JsonProperty("interim_highCourtList")
    private HighCourt interimHighCourtList;

    public Court toCourt() {
        return Court.builder()
            .region(interimRegionList)
            .midlandsList(interimMidlandsFrcList)
            .londonList(interimLondonFrcList)
            .northWestList(interimNorthWestFrcList)
            .northEastList(interimNorthEastFrcList)
            .southEastList(interimSouthEastFrcList)
            .southWestList(interimSouthWestFrcList)
            .walesList(interimWalesFrcList)
            .hcCourtList(interimHighCourtFrcList)
            .courtListWrapper(DefaultCourtListWrapper.builder()
                .nottinghamCourtList(interimNottinghamCourtList)
                .cfcCourtList(interimCfcCourtList)
                .birminghamCourtList(interimBirminghamCourtList)
                .liverpoolCourtList(interimLiverpoolCourtList)
                .manchesterCourtList(interimManchesterCourtList)
                .lancashireCourtList(interimLancashireCourtList)
                .clevelandCourtList(interimClevelandCourtList)
                .nwYorkshireCourtList(interimNwYorkshireCourtList)
                .humberCourtList(interimHumberCourtList)
                .kentSurreyCourtList(interimKentSurreyCourtList)
                .bedfordshireCourtList(interimBedfordshireCourtList)
                .thamesValleyCourtList(interimThamesValleyCourtList)
                .devonCourtList(interimDevonCourtList)
                .dorsetCourtList(interimDorsetCourtList)
                .bristolCourtList(interimBristolCourtList)
                .newportCourtList(interimNewportCourtList)
                .swanseaCourtList(interimSwanseaCourtList)
                .northWalesCourtList(interimNorthWalesCourtList)
                .highCourtList(interimHighCourtList)
                .build())
            .build();
    }
}
