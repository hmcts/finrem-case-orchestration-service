package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.ccd.client.model.Classification;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.AllocatedRegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CourtWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationRegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.InterimRegionWrapper;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class FinremCaseDetails implements CcdCaseDetails<FinremCaseData> {

    private Long id;
    private String jurisdiction;
    private State state;
    private LocalDateTime createdDate;
    private Integer securityLevel;
    private String callbackResponseStatus;
    private LocalDateTime lastModified;
    private Classification securityClassification;
    @JsonProperty("case_data")
    @JsonAlias("data")
    private FinremCaseData data;

    @JsonProperty("case_type_id")
    private CaseType caseType;

    @JsonProperty("locked_by_user_id")
    private Integer lockedBy;

    @JsonIgnore
    public boolean isConsentedApplication() {
        return CaseType.CONSENTED.equals(caseType);
    }

    @JsonIgnore
    public boolean isContestedApplication() {
        return CaseType.CONTESTED.equals(caseType);
    }

    @JsonIgnore
    public boolean isContestedPaperApplication() {
        return isContestedApplication() && data.isPaperCase();
    }

    @JsonIgnore
    public boolean isOrderApprovedCollectionPresent() {
        return isContestedApplication()
            ? isContestedOrderApprovedCollectionPresent()
            : isConsentedOrderApprovedCollectionPresent();
    }

    @JsonIgnore
    public boolean isConsentedOrderApprovedCollectionPresent() {
        return data.getApprovedOrderCollection() != null && !data.getApprovedOrderCollection().isEmpty();
    }

    @JsonIgnore
    public boolean isContestedOrderApprovedCollectionPresent() {
        return data.getConsentOrderWrapper().getContestedConsentedApprovedOrders() != null
            && !data.getConsentOrderWrapper().getContestedConsentedApprovedOrders().isEmpty();
    }

    @JsonIgnore
    public String getAppSolicitorName() {
        return isConsentedApplication()
            ? data.getContactDetailsWrapper().getSolicitorName()
            : data.getContactDetailsWrapper().getApplicantSolicitorName();
    }

    @JsonIgnore
    public Address getAppSolicitorAddress() {
        return isConsentedApplication()
            ? data.getContactDetailsWrapper().getSolicitorAddress()
            : data.getContactDetailsWrapper().getApplicantSolicitorAddress();
    }

    @JsonIgnore
    public String getAppSolicitorEmail() {
        return isConsentedApplication()
            ? data.getContactDetailsWrapper().getSolicitorEmail()
            : data.getContactDetailsWrapper().getApplicantSolicitorEmail();
    }

    @JsonIgnore
    public String getAppSolicitorFirm() {
        return isConsentedApplication()
            ? data.getContactDetailsWrapper().getSolicitorFirm()
            : data.getContactDetailsWrapper().getApplicantSolicitorFirm();
    }

    @JsonIgnore
    public String getSelectedCourt() {
        AllocatedRegionWrapper regionWrapper = data.getRegionWrapper().getAllocatedRegionWrapper();
        CourtWrapper courtList = regionWrapper.getAllocatedCourtWrapper();
        return Map.of(
            Region.MIDLANDS, getMidlandsCourt(regionWrapper.getMidlandsFrcList(), courtList),
            Region.LONDON, getCourtListIdOrDefault(regionWrapper.getAllocatedCourtWrapper().getCfcCourtList())
                .getSelectedCourtId(),
            Region.NORTHEAST, getNorthEastCourt(regionWrapper.getNorthEastFrcList(), courtList),
            Region.NORTHWEST, getNorthWestCourt(regionWrapper.getNorthWestFrcList(), courtList),
            Region.SOUTHWEST, getSouthWestCourt(regionWrapper.getSouthWestFrcList(), courtList),
            Region.SOUTHEAST, getSouthEastCourt(regionWrapper.getSouthEastFrcList(), courtList),
            Region.WALES, getWalesCourt(regionWrapper.getWalesFrcList(), courtList),
            Region.HIGHCOURT, getHighCourt(regionWrapper.getHighCourtFrcList(), courtList)
        ).get(regionWrapper.getRegionList());
    }

    @JsonIgnore
    public String getInterimSelectedCourt() {
        InterimRegionWrapper interimWrapper = data.getRegionWrapper().getInterimRegionWrapper();
        CourtWrapper courtList = data.getRegionWrapper().getInterimCourtList();
        return Map.of(
            Region.MIDLANDS, getMidlandsCourt(interimWrapper.getInterimMidlandsFrcList(), courtList),
            Region.LONDON, getCourtListIdOrDefault(interimWrapper.getCourtListWrapper().getInterimCfcCourtList())
                .getSelectedCourtId(),
            Region.NORTHEAST, getNorthEastCourt(interimWrapper.getInterimNorthEastFrcList(), courtList),
            Region.NORTHWEST, getNorthWestCourt(interimWrapper.getInterimNorthWestFrcList(), courtList),
            Region.SOUTHWEST, getSouthWestCourt(interimWrapper.getInterimSouthWestFrcList(), courtList),
            Region.SOUTHEAST, getSouthEastCourt(interimWrapper.getInterimSouthEastFrcList(), courtList),
            Region.WALES, getWalesCourt(interimWrapper.getInterimWalesFrcList(), courtList),
            Region.HIGHCOURT, getHighCourt(interimWrapper.getInterimHighCourtFrcList(), courtList)
        ).get(interimWrapper.getInterimRegionList());
    }

    @JsonIgnore
    public String getGeneralApplicationSelectedCourt() {
        GeneralApplicationRegionWrapper regionWrapper = data.getRegionWrapper().getGeneralApplicationRegionWrapper();
        CourtWrapper courtList = regionWrapper.getCourtListWrapper();
        return Map.of(
            Region.MIDLANDS, getMidlandsCourt(regionWrapper.getGeneralApplicationDirectionsMidlandsFrcList(),
                courtList),
            Region.LONDON, getCourtListIdOrDefault(regionWrapper.getCourtListWrapper()
                .getGeneralApplicationDirectionsCfcCourtList()).getSelectedCourtId(),
            Region.NORTHEAST, getNorthEastCourt(regionWrapper.getGeneralApplicationDirectionsNorthEastFrcList(),
                courtList),
            Region.NORTHWEST, getNorthWestCourt(regionWrapper.getGeneralApplicationDirectionsNorthWestFrcList(),
                courtList),
            Region.SOUTHWEST, getSouthWestCourt(regionWrapper.getGeneralApplicationDirectionsSouthWestFrcList(),
                courtList),
            Region.SOUTHEAST, getSouthEastCourt(regionWrapper.getGeneralApplicationDirectionsSouthEastFrcList(),
                courtList),
            Region.WALES, getWalesCourt(regionWrapper.getGeneralApplicationDirectionsWalesFrcList(), courtList),
            Region.HIGHCOURT, getHighCourt(regionWrapper.getGeneralApplicationDirectionsHighCourtFrcList(), courtList)
        ).get(regionWrapper.getGeneralApplicationDirectionsRegionList());
    }

    @JsonIgnore
    private String getMidlandsCourt(RegionMidlandsFrc frc, CourtWrapper courtList) {
        return Map.of(
                RegionMidlandsFrc.NOTTINGHAM, getCourtListIdOrDefault(courtList.getNottinghamCourt()),
                RegionMidlandsFrc.BIRMINGHAM, getCourtListIdOrDefault(courtList.getBirminghamCourt()))
            .get(frc).getSelectedCourtId();
    }

    @JsonIgnore
    private String getNorthEastCourt(RegionNorthEastFrc frc, CourtWrapper courtList) {
        return Map.of(
            RegionNorthEastFrc.CLEVELAND, getCourtListIdOrDefault(courtList
                .getClevelandCourt(isConsentedApplication())),
            RegionNorthEastFrc.HS_YORKSHIRE, getCourtListIdOrDefault(courtList.getHumberCourt()),
            RegionNorthEastFrc.NW_YORKSHIRE, getCourtListIdOrDefault(courtList.getNwYorkshireCourt())
        ).get(frc).getSelectedCourtId();
    }

    @JsonIgnore
    private String getNorthWestCourt(RegionNorthWestFrc frc, CourtWrapper courtList) {
        return Map.of(
            RegionNorthWestFrc.MANCHESTER, getCourtListIdOrDefault(courtList.getManchesterCourt()),
            RegionNorthWestFrc.LANCASHIRE, getCourtListIdOrDefault(courtList.getLancashireCourt()),
            RegionNorthWestFrc.LIVERPOOL, getCourtListIdOrDefault(courtList.getLiverpoolCourt())
        ).get(frc).getSelectedCourtId();
    }

    @JsonIgnore
    private String getSouthWestCourt(RegionSouthWestFrc frc, CourtWrapper courtList) {
        return Map.of(
                RegionSouthWestFrc.BRISTOL, getCourtListIdOrDefault(courtList.getBristolCourt()),
                RegionSouthWestFrc.DEVON, getCourtListIdOrDefault(courtList.getDevonCourt()),
                RegionSouthWestFrc.DORSET, getCourtListIdOrDefault(courtList.getDorsetCourt()))
            .get(frc).getSelectedCourtId();
    }

    @JsonIgnore
    private String getSouthEastCourt(RegionSouthEastFrc frc, CourtWrapper courtList) {
        return Map.of(
            RegionSouthEastFrc.BEDFORDSHIRE, getCourtListIdOrDefault(courtList.getBedfordshireCourt()),
            RegionSouthEastFrc.KENT, getCourtListIdOrDefault(courtList.getKentSurreyCourt()),
            RegionSouthEastFrc.THAMES_VALLEY, getCourtListIdOrDefault(courtList.getThamesValleyCourt())
        ).get(frc).getSelectedCourtId();
    }

    @JsonIgnore
    private String getWalesCourt(RegionWalesFrc frc, CourtWrapper courtList) {
        return Map.of(
            RegionWalesFrc.NORTH_WALES, getCourtListIdOrDefault(courtList.getNorthWalesCourt()),
            RegionWalesFrc.NEWPORT, getCourtListIdOrDefault(courtList.getNewportCourt()),
            RegionWalesFrc.SWANSEA, getCourtListIdOrDefault(courtList.getSwanseaCourt())
        ).get(frc).getSelectedCourtId();
    }

    @JsonIgnore
    private String getHighCourt(RegionHighCourtFrc frc, CourtWrapper courtList) {
        return Map.of(
            RegionHighCourtFrc.HIGHCOURT, getCourtListIdOrDefault(courtList.getHighCourt())
        ).get(frc).getSelectedCourtId();
    }

    @JsonIgnore
    private CourtList getCourtListIdOrDefault(CourtList courtList) {
        return Optional.ofNullable(courtList)
            .orElse(new DefaultCourt());
    }

    @JsonIgnore
    public boolean isApplicantSolicitorAgreeToReceiveEmails() {
        return isContestedApplication()
            ? YesOrNo.YES.equals(data.getContactDetailsWrapper().getApplicantSolicitorConsentForEmails())
            : YesOrNo.YES.equals(data.getContactDetailsWrapper().getSolicitorAgreeToReceiveEmails());
    }

    @JsonIgnore
    public boolean isConsentedInContestedCase() {
        return isContestedApplication() && data.getConsentOrderWrapper().getConsentD81Question() != null;
    }

    @JsonIgnore
    public String getRespondentFullName() {
        return isContestedApplication()
            ? data.getFullRespondentNameContested()
            : data.getFullRespondentNameConsented();
    }

}
