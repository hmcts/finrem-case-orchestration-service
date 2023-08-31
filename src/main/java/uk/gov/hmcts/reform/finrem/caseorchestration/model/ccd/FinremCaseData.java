package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CaseFlagsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DefaultRegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralEmailWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralLetterWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.RegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerChangeDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "ccdCaseType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = FinremCaseDataConsented.class, name = "FinancialRemedyMVP2"),
    @JsonSubTypes.Type(value = FinremCaseDataContested.class, name = "FinancialRemedyContested")
})
@SuperBuilder
public abstract class FinremCaseData {

    @JsonProperty(access = WRITE_ONLY)
    private CaseType ccdCaseType;
    @JsonProperty(access = WRITE_ONLY)
    private String ccdCaseId;
    private String assignedToJudge;
    private List<UploadOrderCollection> uploadOrder;
    private List<CaseNotesCollection> caseNotesCollection;
    private String state;
    private List<ScannedDocumentCollection> scannedDocuments;
    private YesOrNo evidenceHandled;
    private CaseDocument bulkPrintCoverSheetRes;
    private String bulkPrintLetterIdRes;
    private CaseDocument bulkPrintCoverSheetApp;
    private String bulkPrintLetterIdApp;
    private String bulkScanCaseReference;
    private YesOrNo civilPartnership;
    private ChangeOrganisationRequest changeOrganisationRequestField;
    private CaseRole currentUserCaseRole;
    private String currentUserCaseRoleLabel;
    private String divorceCaseNumber;
    private StageReached divorceStageReached;
    private CaseDocument divorceUploadEvidence1;
    private CaseDocument divorceUploadEvidence2;
    private YesOrNo paperApplication;
    private CaseDocument bulkPrintCoverSheetAppConfidential;
    private CaseDocument bulkPrintCoverSheetResConfidential;
    private CaseDocument miniFormA;
    private CaseDocument consentOrder;
    private List<PaymentDocumentCollection> copyOfPaperFormA;
    private YesOrNo helpWithFeesQuestion;
    private String authorisationName;
    private String authorisation2b;
    private YesOrNo servePensionProvider;
    private PensionProvider servePensionProviderResponsibility;
    private String servePensionProviderOther;
    private List<OrderRefusalCollection> orderRefusalCollection;
    private List<OrderRefusalCollection> orderRefusalCollectionNew;
    private CaseDocument orderRefusalPreviewDocument;
    @JsonProperty("RepresentationUpdateHistory")
    private List<RepresentationUpdateHistoryCollection> representationUpdateHistory;
    @JsonProperty("RespSolNotificationsEmailConsent")
    private YesOrNo respSolNotificationsEmailConsent;
    @JsonProperty("ApplicantOrganisationPolicy")
    private OrganisationPolicy applicantOrganisationPolicy;
    @JsonProperty("RespondentOrganisationPolicy")
    private OrganisationPolicy respondentOrganisationPolicy;
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate divorceDecreeNisiDate;
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate divorceDecreeAbsoluteDate;
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate authorisation3;
    @JsonProperty("HWFNumber")
    private String hwfNumber;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal amountToPay;
    @JsonProperty("PBANumber")
    private String pbaNumber;
    @JsonProperty("PBAreference")
    private String pbaReference;
    @JsonProperty("PBAPaymentReference")
    private String pbaPaymentReference;
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate issueDate;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private CaseFlagsWrapper caseFlagsWrapper;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private GeneralOrderWrapper generalOrderWrapper;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private GeneralLetterWrapper generalLetterWrapper;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private GeneralEmailWrapper generalEmailWrapper;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private RegionWrapper regionWrapper;


    //TODO: Remove these 2 attributes from this class: currentIntervenerChangeDetails and currentAddressee
    @JsonIgnore
    private IntervenerChangeDetails currentIntervenerChangeDetails;
    @JsonIgnore
    private Addressee currentAddressee;


    @JsonIgnore
    public abstract String getAppSolicitorName();

    @JsonIgnore
    public abstract Address getAppSolicitorAddress();

    @JsonIgnore
    public abstract String getAppSolicitorEmail();

    @JsonIgnore
    public abstract boolean isConsentedInContestedCase();

    @JsonIgnore
    public abstract boolean isApplicantSolicitorAgreeToReceiveEmails();

    @JsonIgnore
    public abstract boolean isRespondentRepresentedByASolicitor();

    @JsonIgnore
    public abstract String getRespondentFullName();

    @JsonIgnore
    public abstract ContactDetailsWrapper getContactDetailsWrapper();

    @JsonIgnore
    public boolean isRespondentSolicitorEmailCommunicationEnabled() {
        return !isPaperCase()
            && isRespondentRepresentedByASolicitor()
            && Objects.nonNull(getContactDetailsWrapper().getRespondentSolicitorEmail())
            && isRespondentSolicitorAgreeToReceiveEmails();
    }

    @JsonIgnore
    public GeneralLetterWrapper getGeneralLetterWrapper() {
        if (generalLetterWrapper == null) {
            this.generalLetterWrapper = new GeneralLetterWrapper();
        }
        return generalLetterWrapper;
    }

    @JsonIgnore
    public GeneralEmailWrapper getGeneralEmailWrapper() {
        if (generalEmailWrapper == null) {
            this.generalEmailWrapper = new GeneralEmailWrapper();
        }
        return generalEmailWrapper;
    }

    @JsonIgnore
    public GeneralOrderWrapper getGeneralOrderWrapper() {
        if (generalOrderWrapper == null) {
            this.generalOrderWrapper = new GeneralOrderWrapper();
        }
        return generalOrderWrapper;
    }


    @JsonIgnore
    public RegionWrapper getRegionWrapper() {
        if (regionWrapper == null) {
            this.regionWrapper = new RegionWrapper();
        }
        return regionWrapper;
    }

    @JsonIgnore
    //TODO: use Apache StringUtils
    public String nullToEmpty(Object o) {
        return Objects.toString(o, "");
    }

    @JsonIgnore
    public String getFullApplicantName() {
        return (
            nullToEmpty(getContactDetailsWrapper().getApplicantFmName()).trim()
                + " "
                + nullToEmpty(getContactDetailsWrapper().getApplicantLname()).trim()
        ).trim();
    }


    @JsonIgnore
    public boolean isConsentedApplication() {
        return CaseType.CONSENTED.equals(ccdCaseType);
    }

    @JsonIgnore
    public boolean isContestedApplication() {
        return CaseType.CONTESTED.equals(ccdCaseType);
    }

    @JsonIgnore
    public boolean isApplicantRepresentedByASolicitor() {
        return YesOrNo.YES.equals(getContactDetailsWrapper().getApplicantRepresented());
    }

    @JsonIgnore
    public boolean isRespondentSolicitorAgreeToReceiveEmails() {
        return YesOrNo.YES.equals(respSolNotificationsEmailConsent);
    }


    @JsonIgnore
    public boolean isPaperCase() {
        return YesOrNo.YES.equals(paperApplication);
    }


    @JsonIgnore
    public boolean isApplicantSolicitorPopulated() {
        return StringUtils.isNotEmpty(nullToEmpty(getAppSolicitorEmail()));
    }

    @JsonIgnore
    public String getRespondentSolicitorName() {
        return getContactDetailsWrapper().getRespondentSolicitorName();
    }


    @JsonIgnore
    public boolean isRespondentSolicitorPopulated() {
        return StringUtils.isNotEmpty(nullToEmpty(getContactDetailsWrapper().getRespondentSolicitorEmail()));
    }

    @JsonIgnore
    public String getSelectedCourt() {
        DefaultRegionWrapper regionWrapper = getRegionWrapper().getDefaultRegionWrapper();
        CourtListWrapper courtList = regionWrapper.getDefaultCourtListWrapper();
        return Map.of(Region.MIDLANDS, getMidlandsCourt(regionWrapper.getMidlandsFrcList(), courtList), Region.LONDON,
                getCourtListIdOrDefault(regionWrapper.getDefaultCourtListWrapper().getCfcCourtList()).getSelectedCourtId(), Region.NORTHEAST,
                getNorthEastCourt(regionWrapper.getNorthEastFrcList(), courtList), Region.NORTHWEST,
                getNorthWestCourt(regionWrapper.getNorthWestFrcList(), courtList), Region.SOUTHWEST,
                getSouthWestCourt(regionWrapper.getSouthWestFrcList(), courtList), Region.SOUTHEAST,
                getSouthEastCourt(regionWrapper.getSouthEastFrcList(), courtList), Region.WALES,
                getWalesCourt(regionWrapper.getWalesFrcList(), courtList), Region.HIGHCOURT, getHighCourt(regionWrapper.getHighCourtFrcList(),
                    courtList))
            .get(regionWrapper.getRegionList());
    }

    @JsonIgnore
    private String getMidlandsCourt(RegionMidlandsFrc frc, CourtListWrapper courtList) {
        if (frc != null) {
            return Map.of(RegionMidlandsFrc.NOTTINGHAM, getCourtListIdOrDefault(courtList.getNottinghamCourt()), RegionMidlandsFrc.BIRMINGHAM,
                getCourtListIdOrDefault(courtList.getBirminghamCourt())).get(frc).getSelectedCourtId();
        } else {
            return StringUtils.EMPTY;
        }
    }

    @JsonIgnore
    private String getNorthEastCourt(RegionNorthEastFrc frc, CourtListWrapper courtList) {
        if (frc != null) {
            return Map.of(RegionNorthEastFrc.CLEVELAND, getCourtListIdOrDefault(courtList.getClevelandCourt(isConsentedApplication())),
                RegionNorthEastFrc.HS_YORKSHIRE, getCourtListIdOrDefault(courtList.getHumberCourt()), RegionNorthEastFrc.NW_YORKSHIRE,
                getCourtListIdOrDefault(courtList.getNwYorkshireCourt())).get(frc).getSelectedCourtId();
        } else {
            return StringUtils.EMPTY;
        }
    }

    @JsonIgnore
    private String getNorthWestCourt(RegionNorthWestFrc frc, CourtListWrapper courtList) {
        if (frc != null) {
            return Map.of(RegionNorthWestFrc.MANCHESTER, getCourtListIdOrDefault(courtList.getManchesterCourt()), RegionNorthWestFrc.LANCASHIRE,
                getCourtListIdOrDefault(courtList.getLancashireCourt()), RegionNorthWestFrc.LIVERPOOL,
                getCourtListIdOrDefault(courtList.getLiverpoolCourt())).get(frc).getSelectedCourtId();
        } else {
            return StringUtils.EMPTY;
        }
    }

    @JsonIgnore
    private String getSouthWestCourt(RegionSouthWestFrc frc, CourtListWrapper courtList) {
        if (frc != null) {
            return Map.of(RegionSouthWestFrc.BRISTOL, getCourtListIdOrDefault(courtList.getBristolCourt()), RegionSouthWestFrc.DEVON,
                    getCourtListIdOrDefault(courtList.getDevonCourt()), RegionSouthWestFrc.DORSET,
                    getCourtListIdOrDefault(courtList.getDorsetCourt()))
                .get(frc).getSelectedCourtId();
        } else {
            return StringUtils.EMPTY;
        }
    }

    @JsonIgnore
    private String getSouthEastCourt(RegionSouthEastFrc frc, CourtListWrapper courtList) {
        if (frc != null) {
            return Map.of(RegionSouthEastFrc.BEDFORDSHIRE, getCourtListIdOrDefault(courtList.getBedfordshireCourt()), RegionSouthEastFrc.KENT,
                getCourtListIdOrDefault(courtList.getKentSurreyCourt()), RegionSouthEastFrc.THAMES_VALLEY,
                getCourtListIdOrDefault(courtList.getThamesValleyCourt())).get(frc).getSelectedCourtId();
        } else {
            return StringUtils.EMPTY;
        }
    }

    @JsonIgnore
    private String getWalesCourt(RegionWalesFrc frc, CourtListWrapper courtList) {
        if (frc != null) {
            return Map.of(RegionWalesFrc.NORTH_WALES, getCourtListIdOrDefault(courtList.getNorthWalesCourt()), RegionWalesFrc.NEWPORT,
                    getCourtListIdOrDefault(courtList.getNewportCourt()), RegionWalesFrc.SWANSEA,
                    getCourtListIdOrDefault(courtList.getSwanseaCourt()))
                .get(frc).getSelectedCourtId();
        } else {
            return StringUtils.EMPTY;
        }
    }

    @JsonIgnore
    private String getHighCourt(RegionHighCourtFrc frc, CourtListWrapper courtList) {
        if (frc != null) {
            return Map.of(RegionHighCourtFrc.HIGHCOURT, getCourtListIdOrDefault(courtList.getHighCourt())).get(frc).getSelectedCourtId();
        } else {
            return StringUtils.EMPTY;
        }
    }

    @JsonIgnore
    private CourtList getCourtListIdOrDefault(CourtList courtList) {
        return Optional.ofNullable(courtList).orElse(new DefaultCourt());
    }
}

