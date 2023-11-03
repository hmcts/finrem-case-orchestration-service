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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftDirectionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationRegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralEmailWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralLetterWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.InterimWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFourWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThreeWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwoWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.MiamWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.NatureApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ReferToJudgeWrapper;
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
    visible = true,
    property = "ccdCaseType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = FinremCaseDataConsented.class, name = "FinancialRemedyMVP2"),
    @JsonSubTypes.Type(value = FinremCaseDataContested.class, name = "FinancialRemedyContested")
})
@SuperBuilder
public abstract class FinremCaseData {

    @JsonProperty(access = WRITE_ONLY)
    protected CaseType ccdCaseType;
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
    private OrderDirection orderDirection;
    private CaseDocument orderDirectionOpt1;
    private String orderDirectionOpt2;
    private YesOrNo orderDirectionAbsolute;
    private YesOrNo servePensionProvider;
    private PensionProvider servePensionProviderResponsibility;
    private String servePensionProviderOther;
    private List<OrderRefusalCollection> orderRefusalCollection;
    private List<OrderRefusalCollection> orderRefusalCollectionNew;
    private CaseDocument orderRefusalPreviewDocument;
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dueDate;
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate issueDate;
    private AssignToJudgeReason assignedToJudgeReason;
    private String assignedToJudge;
    private List<UploadConsentOrderDocumentCollection> uploadConsentOrderDocuments;
    private List<UploadOrderCollection> uploadOrder;
    private List<UploadDocumentCollection> uploadDocuments;
    private List<SolUploadDocumentCollection> solUploadDocuments;
    private List<RespondToOrderDocumentCollection> respondToOrderDocuments;
    private List<AmendedConsentOrderCollection> amendedConsentOrderCollection;
    private List<CaseNotesCollection> caseNotesCollection;
    private String state;
    private List<ScannedDocumentCollection> scannedDocuments;
    private YesOrNo evidenceHandled;
    private CaseDocument approvedConsentOrderLetter;
    private CaseDocument bulkPrintCoverSheetRes;
    private String bulkPrintLetterIdRes;
    private CaseDocument bulkPrintCoverSheetApp;
    private CaseDocument bulkPrintCoverSheetIntervener1;
    private CaseDocument bulkPrintCoverSheetIntervener2;
    private CaseDocument bulkPrintCoverSheetIntervener3;
    private CaseDocument bulkPrintCoverSheetIntervener4;
    private String bulkPrintLetterIdApp;
    private List<ConsentOrderCollection> approvedOrderCollection;
    private ApplicantRole divRoleOfFrApplicant;
    private ApplicantRepresentedPaper applicantRepresentedPaper;
    private String authorisationSolicitorAddress;
    private YesOrNo authorisationSigned;
    private AuthorisationSignedBy authorisationSignedBy;
    private String bulkScanCaseReference;
    private List<ChildrenInfoCollection> childrenInfo;
    private CaseDocument formA;
    private List<DocumentCollection> scannedD81s;
    private String transferLocalCourtName;
    private String transferLocalCourtEmail;
    private String transferLocalCourtInstructions;
    private List<TransferCourtEmailCollection> transferLocalCourtEmailCollection;
    private YesOrNo civilPartnership;
    private YesOrNo promptForUrgentCaseQuestion;
    private String urgentCaseQuestionDetailsTextArea;
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
    private LocalDate divorcePetitionIssuedDate;
    private String propertyAddress;
    private String mortgageDetail;
    private YesOrNo additionalPropertyOrderDecision;
    @JsonProperty("propertyAdjutmentOrderDetail")
    private List<PropertyAdjustmentOrderCollection> propertyAdjustmentOrderDetail;
    private YesOrNo paymentForChildrenDecision;
    private YesOrNo benefitForChildrenDecision;
    private List<BenefitPayment> benefitPaymentChecklist;
    private YesOrNo fastTrackDecision;
    private List<FastTrackReason> fastTrackDecisionReason;
    private Complexity addToComplexityListOfCourts;
    private List<EstimatedAsset> estimatedAssetsChecklist;
    private String netValueOfHome;
    private List<PotentialAllegation> potentialAllegationChecklist;
    private String detailPotentialAllegation;
    private YesOrNo otherReasonForComplexity;
    private String otherReasonForComplexityText;
    private String specialAssistanceRequired;
    private String specificArrangementsRequired;
    private YesOrNo isApplicantsHomeCourt;
    private String reasonForLocalCourt;
    private String mediatorRegistrationNumber;
    private String familyMediatorServiceName;
    private String soleTraderName;
    private String mediatorRegistrationNumber1;
    private String familyMediatorServiceName1;
    private String soleTraderName1;
    private YesOrNo promptForAnyDocument;
    private List<AdditionalHearingDocumentCollection> additionalHearingDocuments;
    private List<HearingDirectionDetailsCollection> hearingDirectionDetailsCollection;
    private List<DocumentCollection> hearingNoticeDocumentPack;
    private List<DocumentCollection> hearingNoticesDocumentCollection;
    private HearingTypeDirection hearingType;
    private String timeEstimate;
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate hearingDate;
    private String additionalInformationAboutHearing;
    private String hearingTime;
    private List<JudgeAllocated> judgeAllocated;
    private YesOrNo applicationAllocatedTo;
    private YesOrNo caseAllocatedTo;
    private JudgeTimeEstimate judgeTimeEstimate;
    private String judgeTimeEstimateTextArea;
    private CaseDocument formC;
    private CaseDocument formG;
    private List<UploadGeneralDocumentCollection> uploadGeneralDocuments;
    private AssignToJudgeReason assignToJudgeReason;
    private String assignToJudgeText;
    private YesOrNo subjectToDecreeAbsoluteValue;
    private String selectJudge;
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfOrder;
    private String additionalComments;
    private List<ApplicationNotApprovedCollection> applicationNotApproved;
    private String attendingCourtWithAssistance;
    private String attendingCourtWithArrangement;
    private SolicitorToDraftOrder solicitorResponsibleForDraftingOrder;
    private List<DirectionOrderCollection> uploadHearingOrder;
    private List<DocumentCollection> hearingOrderOtherDocuments;
    private List<DirectionDetailCollection> directionDetailsCollection;
    private List<DirectionOrderCollection> finalOrderCollection;
    private List<JudgeNotApprovedReasonsCollection> judgeNotApprovedReasons;
    private JudgeType refusalOrderJudgeType;
    private String refusalOrderJudgeName;
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate refusalOrderDate;
    private CaseDocument refusalOrderPreviewDocument;
    private List<RefusalOrderCollection> refusalOrderCollection;
    private CaseDocument latestRefusalOrder;
    private CaseDocument refusalOrderAdditionalDocument;
    private String hiddenTabValue;
    private CaseDocument latestDraftHearingOrder;
    private String orderApprovedJudgeName;
    private JudgeType orderApprovedJudgeType;
    private List<UploadAdditionalDocumentCollection> uploadAdditionalDocument;
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate orderApprovedDate;
    private CaseDocument orderApprovedCoverLetter;
    private String hearingDetails;
    private YesOrNo applicantShareDocs;
    private YesOrNo respondentShareDocs;
    @JsonProperty("reasonForFRCLocation")
    private String reasonForFrcLocation;
    private List<HearingUploadBundleCollection> hearingUploadBundle;
    private SendOrderEventPostStateOption sendOrderPostStateOption;
    private List<ConfidentialUploadedDocumentData> confidentialDocumentsUploaded;
    private ChangeOrganisationRequest changeOrganisationRequestField;
    @JsonProperty("ApplicantOrganisationPolicy")
    private OrganisationPolicy applicantOrganisationPolicy;
    @JsonProperty("RespondentOrganisationPolicy")
    private OrganisationPolicy respondentOrganisationPolicy;
    private CaseRole currentUserCaseRole;
    private String currentUserCaseRoleLabel;
    private CaseDocument outOfFamilyCourtResolution;

    private DynamicMultiSelectList sourceDocumentList;
    private DynamicMultiSelectList solicitorRoleList;
    private DynamicRadioList intervenersList;
    private DynamicRadioList intervenerOptionList;

    @Getter(AccessLevel.NONE)
    @JsonProperty("intervener1")
    private IntervenerOneWrapper intervenerOneWrapper;

    @Getter(AccessLevel.NONE)
    @JsonProperty("intervener2")
    private IntervenerTwoWrapper intervenerTwoWrapper;

    @Getter(AccessLevel.NONE)
    @JsonProperty("intervener3")
    private IntervenerThreeWrapper intervenerThreeWrapper;

    private List<UploadCaseDocumentCollection> manageCaseDocumentCollection;

    @Getter(AccessLevel.NONE)
    @JsonProperty("intervener4")
    private IntervenerFourWrapper intervenerFourWrapper;
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
    private MiamWrapper miamWrapper;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private NatureApplicationWrapper natureApplicationWrapper;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private ConsentOrderWrapper consentOrderWrapper;
    private YesOrNo additionalHearingDocumentsOption;
    private CaseDocument additionalListOfHearingDocuments;

    @JsonProperty("typeOfDocument")
    private ScannedDocumentTypeOption scannedDocsTypeOfDocument;
    private List<ScannedDocumentCollection> applicantScanDocuments;
    private List<ScannedDocumentCollection> respondentScanDocuments;


    //TODO: Remove these 2 attributes from this class: currentIntervenerChangeDetails and currentAddressee
    @JsonIgnore
    private IntervenerChangeDetails currentIntervenerChangeDetails;
    @JsonIgnore
    private Addressee currentAddressee;

    @Builder.Default
    @JsonIgnore
    private boolean applicantCorrespondenceEnabled = true;
    @Builder.Default
    @JsonIgnore
    private boolean respondentCorrespondenceEnabled = true;

    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private ScheduleOneWrapper scheduleOneWrapper;

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
    public IntervenerOneWrapper getIntervenerOneWrapperIfPopulated() {
        if (intervenerOneWrapper != null) {
            return this.intervenerOneWrapper;
        }
        return null;
    }

    @JsonIgnore
    public IntervenerTwoWrapper getIntervenerTwoWrapperIfPopulated() {
        if (intervenerTwoWrapper != null) {
            return this.intervenerTwoWrapper;
        }
        return null;
    }

    @JsonIgnore
    public IntervenerThreeWrapper getIntervenerThreeWrapperIfPopulated() {
        if (intervenerThreeWrapper != null) {
            return this.intervenerThreeWrapper;
        }
        return null;
    }

    @JsonIgnore
    public IntervenerFourWrapper getIntervenerFourWrapperIfPopulated() {
        if (intervenerFourWrapper != null) {
            return this.intervenerFourWrapper;
        }
        return null;
    }

    @JsonIgnore
    public List<IntervenerWrapper> getInterveners() {
        return List.of(getIntervenerOneWrapper(), getIntervenerTwoWrapper(), getIntervenerThreeWrapper(), getIntervenerFourWrapper());
    }


    @JsonIgnore
    public IntervenerOneWrapper getIntervenerOneWrapper() {
        if (intervenerOneWrapper == null) {
            this.intervenerOneWrapper = IntervenerOneWrapper.builder().build();
        }
        return intervenerOneWrapper;
    }

    @JsonIgnore
    public IntervenerTwoWrapper getIntervenerTwoWrapper() {
        if (intervenerTwoWrapper == null) {
            this.intervenerTwoWrapper = IntervenerTwoWrapper.builder().build();
        }
        return intervenerTwoWrapper;
    }

    @JsonIgnore
    public IntervenerThreeWrapper getIntervenerThreeWrapper() {
        if (intervenerThreeWrapper == null) {
            this.intervenerThreeWrapper = IntervenerThreeWrapper.builder().build();
        }
        return intervenerThreeWrapper;
    }

    @JsonIgnore
    public IntervenerFourWrapper getIntervenerFourWrapper() {
        if (intervenerFourWrapper == null) {
            this.intervenerFourWrapper = IntervenerFourWrapper.builder().build();
        }
        return intervenerFourWrapper;
    }

    @JsonIgnore
    public ReferToJudgeWrapper getReferToJudgeWrapper() {
        if (referToJudgeWrapper == null) {
            this.referToJudgeWrapper = new ReferToJudgeWrapper();
        }
        return referToJudgeWrapper;
    }

    @JsonIgnore
    public NatureApplicationWrapper getNatureApplicationWrapper() {
        if (natureApplicationWrapper == null) {
            this.natureApplicationWrapper = new NatureApplicationWrapper();
        }

        return natureApplicationWrapper;
    }

    @JsonIgnore
    public ConsentOrderWrapper getConsentOrderWrapper() {
        if (consentOrderWrapper == null) {
            this.consentOrderWrapper = new ConsentOrderWrapper();
        }

        return consentOrderWrapper;
    }

    @JsonIgnore
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
    public String getSelectedAllocatedCourt() {
        AllocatedRegionWrapper allocatedRegionWrapper = getRegionWrapper().getAllocatedRegionWrapper();
        CourtListWrapper courtList = allocatedRegionWrapper.getDefaultCourtListWrapper();
        return Map.of(Region.MIDLANDS, getMidlandsCourt(allocatedRegionWrapper.getMidlandsFrcList(), courtList), Region.LONDON,
                getCourtListIdOrDefault(allocatedRegionWrapper.getDefaultCourtListWrapper().getCfcCourtList()).getSelectedCourtId(), Region.NORTHEAST,
                getNorthEastCourt(allocatedRegionWrapper.getNorthEastFrcList(), courtList), Region.NORTHWEST,
                getNorthWestCourt(allocatedRegionWrapper.getNorthWestFrcList(), courtList), Region.SOUTHWEST,
                getSouthWestCourt(allocatedRegionWrapper.getSouthWestFrcList(), courtList), Region.SOUTHEAST,
                getSouthEastCourt(allocatedRegionWrapper.getSouthEastFrcList(), courtList), Region.WALES,
                getWalesCourt(allocatedRegionWrapper.getWalesFrcList(), courtList), Region.HIGHCOURT,
                getHighCourt(allocatedRegionWrapper.getHighCourtFrcList(),
                    courtList))
            .get(allocatedRegionWrapper.getRegionList());
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

    @JsonIgnore
    public List<String> getSelectedParties() {
        DynamicMultiSelectList parties = this.getPartiesOnCase();
        if (parties == null) {
            return List.of();
        }
        return parties.getValue().stream().map(DynamicMultiSelectListElement::getCode).toList();
    }


}

