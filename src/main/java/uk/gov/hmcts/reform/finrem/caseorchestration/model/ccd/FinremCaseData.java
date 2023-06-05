package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CaseFlagsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ConsentOrderWrapper;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ScheduleOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.UploadCaseDocumentWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerChangeDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FinremCaseData {

    @JsonProperty(access = WRITE_ONLY)
    private String ccdCaseId;
    @JsonIgnore
    private CaseType ccdCaseType;

    private String divorceCaseNumber;
    private StageReached divorceStageReached;
    private CaseDocument divorceUploadEvidence1;
    private CaseDocument d11;
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate divorceDecreeNisiDate;
    private CaseDocument divorceUploadEvidence2;
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate divorceDecreeAbsoluteDate;
    private Provision provisionMadeFor;
    private Intention applicantIntendsTo;
    private List<PeriodicalPaymentSubstitute> dischargePeriodicalPaymentSubstituteFor;
    private YesOrNo applyingForConsentOrder;
    @JsonProperty("ChildSupportAgencyCalculationMade")
    private YesOrNo childSupportAgencyCalculationMade;
    @JsonProperty("ChildSupportAgencyCalculationReason")
    private String childSupportAgencyCalculationReason;
    private String authorisationName;
    private String authorisationFirm;
    private String authorisation2b;
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate authorisation3;
    private CaseDocument miniFormA;
    private CaseDocument consentOrder;
    private CaseDocument consentOrderText;
    private CaseDocument latestConsentOrder;
    private YesOrNo d81Question;
    private CaseDocument d81Joint;
    private CaseDocument d81Applicant;
    private CaseDocument d81Respondent;
    private List<PensionTypeCollection> pensionCollection;
    private List<PensionTypeCollection> consentPensionCollection;
    private List<PaymentDocumentCollection> copyOfPaperFormA;
    @JsonProperty("otherCollection")
    private List<OtherDocumentCollection> otherDocumentsCollection;
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
    private JudgeType orderDirectionJudge;
    private String orderDirectionJudgeName;
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate orderDirectionDate;
    private String orderDirectionAddComments;
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
    private YesOrNo paperApplication;
    private CaseDocument bulkPrintCoverSheetAppConfidential;
    private CaseDocument bulkPrintCoverSheetResConfidential;
    @JsonProperty("RespSolNotificationsEmailConsent")
    private YesOrNo respSolNotificationsEmailConsent;
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfMarriage;
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfSepration;
    private String nameOfCourtDivorceCentre;
    private CaseDocument divorceUploadPetition;
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
    private List<UploadConfidentialDocumentCollection> confidentialDocumentsUploaded;
    private ChangeOrganisationRequest changeOrganisationRequestField;
    @JsonProperty("ApplicantOrganisationPolicy")
    private OrganisationPolicy applicantOrganisationPolicy;
    @JsonProperty("RespondentOrganisationPolicy")
    private OrganisationPolicy respondentOrganisationPolicy;
    private CaseRole currentUserCaseRole;
    private String currentUserCaseRoleLabel;
    private CaseDocument outOfFamilyCourtResolution;

    private DynamicRadioList intervenersList;
    private DynamicRadioList intervenerOptionList;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private IntervenerOneWrapper intervenerOneWrapper;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private IntervenerTwoWrapper intervenerTwoWrapper;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private IntervenerThreeWrapper intervenerThreeWrapper;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private IntervenerFourWrapper intervenerFourWrapper;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private RegionWrapper regionWrapper;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private ReferToJudgeWrapper referToJudgeWrapper;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private UploadCaseDocumentWrapper uploadCaseDocumentWrapper;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private ContactDetailsWrapper contactDetailsWrapper;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private GeneralApplicationWrapper generalApplicationWrapper;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private GeneralOrderWrapper generalOrderWrapper;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private InterimWrapper interimWrapper;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private DraftDirectionWrapper draftDirectionWrapper;
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

    @JsonProperty("appBarristerCollection")
    private List<BarristerCollectionItem> applicantBarristers;
    @JsonProperty("respBarristerCollection")
    private List<BarristerCollectionItem> respondentBarristers;
    @JsonProperty("intvr1BarristerCollection")
    private List<BarristerCollectionItem> intvr1Barristers;
    @JsonProperty("intvr2BarristerCollection")
    private List<BarristerCollectionItem> intvr2Barristers;
    @JsonProperty("intvr3BarristerCollection")
    private List<BarristerCollectionItem> intvr3Barristers;
    @JsonProperty("intvr4BarristerCollection")
    private List<BarristerCollectionItem> intvr4Barristers;
    private BarristerParty barristerParty;
    private YesOrNo benefitForChildrenDecisionSchedule;
    private List<BenefitPaymentChecklist> benefitPaymentChecklistSchedule;
    private CaseDocument variationOrderDocument;
    private CaseDocument consentVariationOrderDocument;

    private YesOrNo isNocRejected;

    @JsonIgnore
    private IntervenerChangeDetails currentIntervenerChangeDetails;
    @JsonIgnore
    private Addressee currentAddressee;

    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private ScheduleOneWrapper scheduleOneWrapper;

    private List<ConsentedHearingDataWrapper> listForHearings;

    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private CaseFlagsWrapper caseFlagsWrapper;


    @JsonIgnore
    public CaseFlagsWrapper getCaseFlagsWrapper() {
        if (caseFlagsWrapper == null) {
            this.caseFlagsWrapper = new CaseFlagsWrapper();
        }
        return caseFlagsWrapper;
    }

    @JsonIgnore
    public ScheduleOneWrapper getScheduleOneWrapper() {
        if (scheduleOneWrapper == null) {
            this.scheduleOneWrapper = new ScheduleOneWrapper();
        }
        return scheduleOneWrapper;
    }

    @JsonIgnore
    public MiamWrapper getMiamWrapper() {
        if (miamWrapper == null) {
            this.miamWrapper = new MiamWrapper();
        }
        return miamWrapper;
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
    public DraftDirectionWrapper getDraftDirectionWrapper() {
        if (draftDirectionWrapper == null) {
            this.draftDirectionWrapper = new DraftDirectionWrapper();
        }
        return draftDirectionWrapper;
    }

    @JsonIgnore
    public InterimWrapper getInterimWrapper() {
        if (interimWrapper == null) {
            this.interimWrapper = new InterimWrapper();
        }
        return interimWrapper;
    }

    @JsonIgnore
    public GeneralOrderWrapper getGeneralOrderWrapper() {
        if (generalOrderWrapper == null) {
            this.generalOrderWrapper = new GeneralOrderWrapper();
        }
        return generalOrderWrapper;
    }

    @JsonIgnore
    public GeneralApplicationWrapper getGeneralApplicationWrapper() {
        if (generalApplicationWrapper == null) {
            this.generalApplicationWrapper = new GeneralApplicationWrapper();
        }
        return generalApplicationWrapper;
    }

    @JsonIgnore
    public ContactDetailsWrapper getContactDetailsWrapper() {
        if (contactDetailsWrapper == null) {
            this.contactDetailsWrapper = new ContactDetailsWrapper();
        }
        return contactDetailsWrapper;
    }

    @JsonIgnore
    public UploadCaseDocumentWrapper getUploadCaseDocumentWrapper() {
        if (uploadCaseDocumentWrapper == null) {
            this.uploadCaseDocumentWrapper = new UploadCaseDocumentWrapper();
        }
        return uploadCaseDocumentWrapper;
    }

    @JsonIgnore
    public RegionWrapper getRegionWrapper() {
        if (regionWrapper == null) {
            this.regionWrapper = new RegionWrapper();
        }
        return regionWrapper;
    }

    @JsonIgnore
    public List<IntervenerWrapper> getInterveners() {
        return List.of(getIntervenerOneWrapper(), getIntervenerTwoWrapper(), getIntervenerThreeWrapper(), getIntervenerFourWrapper());
    }


    @JsonIgnore
    public IntervenerOneWrapper getIntervenerOneWrapper() {
        if (intervenerOneWrapper == null) {
            this.intervenerOneWrapper = new IntervenerOneWrapper();
        }
        return intervenerOneWrapper;
    }

    @JsonIgnore
    public IntervenerTwoWrapper getIntervenerTwoWrapper() {
        if (intervenerTwoWrapper == null) {
            this.intervenerTwoWrapper = new IntervenerTwoWrapper();
        }
        return intervenerTwoWrapper;
    }

    @JsonIgnore
    public IntervenerThreeWrapper getIntervenerThreeWrapper() {
        if (intervenerThreeWrapper == null) {
            this.intervenerThreeWrapper = new IntervenerThreeWrapper();
        }
        return intervenerThreeWrapper;
    }

    @JsonIgnore
    public IntervenerFourWrapper getIntervenerFourWrapper() {
        if (intervenerFourWrapper == null) {
            this.intervenerFourWrapper = new IntervenerFourWrapper();
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
    public String getFullRespondentNameContested() {
        return (
            nullToEmpty(getContactDetailsWrapper().getRespondentFmName()).trim()
                + " "
                + nullToEmpty(getContactDetailsWrapper().getRespondentLname()).trim()
        ).trim();
    }

    @JsonIgnore
    public String getFullRespondentNameConsented() {
        return (
            nullToEmpty(getContactDetailsWrapper().getAppRespondentFmName()).trim()
                + " "
                + nullToEmpty(getContactDetailsWrapper().getAppRespondentLName()).trim()
        ).trim();
    }

    @JsonIgnore
    public String getRespondentFullName() {
        return CaseType.CONTESTED.equals(ccdCaseType)
            ? getFullRespondentNameContested()
            : getFullRespondentNameConsented();
    }

    @JsonIgnore
    public boolean isConsentedInContestedCase() {
        return CaseType.CONTESTED.equals(ccdCaseType) && getConsentOrderWrapper().getConsentD81Question() != null;
    }


    @JsonIgnore
    public boolean isApplicantSolicitorResponsibleToDraftOrder() {
        return SolicitorToDraftOrder.APPLICANT_SOLICITOR.equals(solicitorResponsibleForDraftingOrder);
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
    public boolean isContestedPaperApplication() {
        return isContestedApplication() && isPaperCase();
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
    public boolean isRespondentRepresentedByASolicitor() {
        return YesOrNo.YES.equals(getContactDetailsWrapper().getContestedRespondentRepresented())
            || YesOrNo.YES.equals(getContactDetailsWrapper().getConsentedRespondentRepresented());
    }

    @JsonIgnore
    public boolean isIntervenerOneRepresentedByASolicitor() {
        return YesOrNo.YES.equals(getIntervenerOneWrapper().getIntervenerRepresented());
    }

    @JsonIgnore
    public boolean isIntervenerTwoRepresentedByASolicitor() {
        return YesOrNo.YES.equals(getIntervenerTwoWrapper().getIntervenerRepresented());
    }

    @JsonIgnore
    public boolean isIntervenerThreeRepresentedByASolicitor() {
        return YesOrNo.YES.equals(getIntervenerThreeWrapper().getIntervenerRepresented());
    }

    @JsonIgnore
    public boolean isIntervenerFourRepresentedByASolicitor() {
        return YesOrNo.YES.equals(getIntervenerFourWrapper().getIntervenerRepresented());
    }

    @JsonIgnore
    public boolean isPaperCase() {
        return YesOrNo.YES.equals(paperApplication);
    }

    @JsonIgnore
    public boolean isAppAddressConfidential() {
        return YesOrNo.YES.equals(getContactDetailsWrapper().getApplicantAddressHiddenFromRespondent());
    }

    @JsonIgnore
    public boolean isRespAddressConfidential() {
        return YesOrNo.YES.equals(getContactDetailsWrapper().getRespondentAddressHiddenFromApplicant());
    }

    @JsonIgnore
    public boolean isContestedOrderNotApprovedCollectionPresent() {
        return getConsentOrderWrapper().getConsentedNotApprovedOrders() != null
            && !getConsentOrderWrapper().getConsentedNotApprovedOrders().isEmpty();
    }

    @JsonIgnore
    public String getAppSolicitorName() {
        return isConsentedApplication()
            ? getContactDetailsWrapper().getSolicitorName()
            : getContactDetailsWrapper().getApplicantSolicitorName();
    }

    @JsonIgnore
    public Address getAppSolicitorAddress() {
        return isConsentedApplication()
            ? getContactDetailsWrapper().getSolicitorAddress()
            : getContactDetailsWrapper().getApplicantSolicitorAddress();
    }

    @JsonIgnore
    public String getAppSolicitorEmail() {
        return isConsentedApplication()
            ? getContactDetailsWrapper().getSolicitorEmail()
            : getContactDetailsWrapper().getApplicantSolicitorEmail();
    }

    @JsonIgnore
    public boolean isApplicantSolicitorPopulated() {
        return StringUtils.isNotEmpty(nullToEmpty(getAppSolicitorEmail()));
    }

    @JsonIgnore
    public boolean isIntervenerOneEmailPopulated() {
        return StringUtils.isNotEmpty(nullToEmpty(getIntervenerOneWrapper().getIntervener1Email()));
    }

    @JsonIgnore
    public boolean isIntervenerTwoEmailPopulated() {
        return StringUtils.isNotEmpty(nullToEmpty(getIntervenerTwoWrapper().getIntervener2Email()));
    }

    @JsonIgnore
    public boolean isIntervenerThreeEmailPopulated() {
        return StringUtils.isNotEmpty(nullToEmpty(getIntervenerThreeWrapper().getIntervener3Email()));
    }

    @JsonIgnore
    public boolean isIntervenerFourEmailPopulated() {
        return StringUtils.isNotEmpty(nullToEmpty(getIntervenerFourWrapper().getIntervener4Email()));
    }

    @JsonIgnore
    public String getAppSolicitorFirm() {
        return isConsentedApplication()
            ? getContactDetailsWrapper().getSolicitorFirm()
            : getContactDetailsWrapper().getApplicantSolicitorFirm();
    }

    @JsonIgnore
    public boolean isRespondentSolicitorEmailCommunicationEnabled() {
        return !isPaperCase()
            && isRespondentRepresentedByASolicitor()
            && Objects.nonNull(getContactDetailsWrapper().getSolicitorEmail())
            && isRespondentSolicitorAgreeToReceiveEmails();
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
    public boolean isFastTrackApplication() {
        return Optional.ofNullable(caseAllocatedTo).map(caseAllocatedTo -> caseAllocatedTo.isYes()).orElseGet(() -> fastTrackDecision.isYes());
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
    public String getGeneralApplicationSelectedCourt() {
        GeneralApplicationRegionWrapper regionWrapper = getRegionWrapper().getGeneralApplicationRegionWrapper();
        CourtListWrapper courtList = regionWrapper.getCourtListWrapper();
        return Map.of(Region.MIDLANDS, getMidlandsCourt(regionWrapper.getGeneralApplicationDirectionsMidlandsFrcList(), courtList), Region.LONDON,
                getCourtListIdOrDefault(regionWrapper.getCourtListWrapper().getGeneralApplicationDirectionsCfcCourtList()).getSelectedCourtId(),
                Region.NORTHEAST, getNorthEastCourt(regionWrapper.getGeneralApplicationDirectionsNorthEastFrcList(), courtList), Region.NORTHWEST,
                getNorthWestCourt(regionWrapper.getGeneralApplicationDirectionsNorthWestFrcList(), courtList), Region.SOUTHWEST,
                getSouthWestCourt(regionWrapper.getGeneralApplicationDirectionsSouthWestFrcList(), courtList), Region.SOUTHEAST,
                getSouthEastCourt(regionWrapper.getGeneralApplicationDirectionsSouthEastFrcList(), courtList), Region.WALES,
                getWalesCourt(regionWrapper.getGeneralApplicationDirectionsWalesFrcList(), courtList), Region.HIGHCOURT,
                getHighCourt(regionWrapper.getGeneralApplicationDirectionsHighCourtFrcList(), courtList))
            .get(regionWrapper.getGeneralApplicationDirectionsRegionList());
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

