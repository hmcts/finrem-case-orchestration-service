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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.AllocatedRegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.BarristerCollectionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.BulkPrintCoversheetWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CaseFlagsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CfvMigrationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ConsentOrderScannedDocWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ConsentOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftDirectionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.FormAScannedDocWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationRegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralEmailWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralLetterWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.InterimWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFour;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThree;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.MiamWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.NatureApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.OrderWrapper;
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
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class FinremCaseData implements HasCaseDocument {

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
    private CaseDocument additionalDocument;
    private List<DocumentCollection> additionalCicDocuments;
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
    private OrderRefusalHolder orderRefusalOnScreen;
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
    private String bulkPrintLetterIdRes;
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
    private List<DocumentToKeepCollection> documentToKeepCollection;
    private YesOrNo paymentForChildrenDecision;
    private YesOrNo benefitForChildrenDecision;
    private List<BenefitPayment> benefitPaymentChecklist;
    private YesOrNo fastTrackDecision;
    private List<FastTrackReason> fastTrackDecisionReason;
    private Complexity addToComplexityListOfCourts;
    private List<EstimatedAsset> estimatedAssetsChecklist;
    private EstimatedAssetV2 estimatedAssetsChecklistV2;
    private String netValueOfHome;
    private List<PotentialAllegation> potentialAllegationChecklist;
    private String detailPotentialAllegation;
    private YesOrNo otherReasonForComplexity;
    private String otherReasonForComplexityText;
    private String specialAssistanceRequired;
    private String specificArrangementsRequired;
    private YesOrNo isApplicantsHomeCourt;
    private String reasonForLocalCourt;
    private YesOrNo allocatedToBeHeardAtHighCourtJudgeLevel;
    private String allocatedToBeHeardAtHighCourtJudgeLevelText;
    private String mediatorRegistrationNumber;
    private String familyMediatorServiceName;
    private String soleTraderName;
    private CaseDocument uploadMediatorDocument;
    private CaseDocument uploadMediatorDocumentPaperCase;
    private String mediatorRegistrationNumber1;
    private String familyMediatorServiceName1;
    private String soleTraderName1;
    private YesOrNo promptForAnyDocument;
    private List<AdditionalHearingDocumentCollection> additionalHearingDocuments;
    private List<HearingDirectionDetailsCollection> hearingDirectionDetailsCollection;
    private List<DocumentCollection> hearingNoticeDocumentPack;
    private List<DocumentCollection> hearingNoticesDocumentCollection;
    private Map<String, Object> courtDetails;

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
    private List<IntervenerHearingNoticeCollection> intv1HearingNoticesCollection;
    private List<IntervenerHearingNoticeCollection> intv2HearingNoticesCollection;
    private List<IntervenerHearingNoticeCollection> intv3HearingNoticesCollection;
    private List<IntervenerHearingNoticeCollection> intv4HearingNoticesCollection;
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
    private List<HearingUploadBundleCollection> fdrHearingBundleCollections;
    private SendOrderEventPostStateOption sendOrderPostStateOption;
    private DynamicMultiSelectList ordersToShare;
    private DynamicMultiSelectList partiesOnCase;
    private List<ConfidentialUploadedDocumentData> confidentialDocumentsUploaded;
    private ChangeOrganisationRequest changeOrganisationRequestField;
    @JsonProperty("ApplicantOrganisationPolicy")
    private OrganisationPolicy applicantOrganisationPolicy;
    @JsonProperty("RespondentOrganisationPolicy")
    private OrganisationPolicy respondentOrganisationPolicy;
    private CaseRole currentUserCaseRole;
    private String currentUserCaseRoleLabel;
    private String currentUserCaseRoleType;
    private CaseDocument outOfFamilyCourtResolution;

    private DynamicMultiSelectList sourceDocumentList;
    private DynamicMultiSelectList solicitorRoleList;
    private DynamicRadioList intervenersList;
    private DynamicRadioList intervenerOptionList;


    private List<UploadCaseDocumentCollection> manageCaseDocumentCollection;

    @Getter(AccessLevel.NONE)
    @JsonProperty("intervener1")
    private IntervenerOne intervenerOne;

    @Getter(AccessLevel.NONE)
    @JsonProperty("intervener2")
    private IntervenerTwo intervenerTwo;

    @Getter(AccessLevel.NONE)
    @JsonProperty("intervener3")
    private IntervenerThree intervenerThree;

    @Getter(AccessLevel.NONE)
    @JsonProperty("intervener4")
    private IntervenerFour intervenerFour;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private DraftOrdersWrapper draftOrdersWrapper;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private FormAScannedDocWrapper formAScannedDocWrapper;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private ConsentOrderScannedDocWrapper consentOrderScannedDocWrapper;
    private List<ScannedD81Collection> scannedD81Collection;
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
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private OrderWrapper orderWrapper;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private BulkPrintCoversheetWrapper bulkPrintCoversheetWrapper;
    private YesOrNo additionalHearingDocumentsOption;
    private CaseDocument additionalListOfHearingDocuments;

    @JsonProperty("typeOfDocument")
    private ScannedDocumentTypeOption scannedDocsTypeOfDocument;
    private List<ScannedDocumentCollection> applicantScanDocuments;
    private List<ScannedDocumentCollection> respondentScanDocuments;
    private List<ManageScannedDocumentCollection> manageScannedDocumentCollection;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private BarristerCollectionWrapper barristerCollectionWrapper;
    private BarristerParty barristerParty;
    private YesOrNo benefitForChildrenDecisionSchedule;
    private List<BenefitPaymentChecklist> benefitPaymentChecklistSchedule;
    private CaseDocument variationOrderDocument;
    private CaseDocument consentVariationOrderDocument;

    private YesOrNo isNocRejected;

    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private CfvMigrationWrapper cfvMigrationWrapper;

    private YesOrNo isNocFixAppliedFlag;

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
    private List<OrderSentToPartiesCollection> ordersSentToPartiesCollection;

    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private ScheduleOneWrapper scheduleOneWrapper;

    private List<ConsentedHearingDataWrapper> listForHearings;

    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private CaseFlagsWrapper caseFlagsWrapper;

    private String previousState;
    private DynamicList userCaseAccessList;

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
        return List.of(getIntervenerOne(), getIntervenerTwo(), getIntervenerThree(), getIntervenerFour());
    }


    @JsonIgnore
    public IntervenerOne getIntervenerOne() {
        if (intervenerOne == null) {
            this.intervenerOne = IntervenerOne.builder().build();
        }
        return intervenerOne;
    }

    @JsonIgnore
    public IntervenerOne getIntervenerOneWrapperIfPopulated() {
        if (intervenerOne != null) {
            return this.intervenerOne;
        }
        return null;
    }

    @JsonIgnore
    public IntervenerTwo getIntervenerTwo() {
        if (intervenerTwo == null) {
            this.intervenerTwo = IntervenerTwo.builder().build();
        }
        return intervenerTwo;
    }

    @JsonIgnore
    public IntervenerTwo getIntervenerTwoWrapperIfPopulated() {
        if (intervenerTwo != null) {
            return this.intervenerTwo;
        }
        return null;
    }

    @JsonIgnore
    public IntervenerThree getIntervenerThree() {
        if (intervenerThree == null) {
            this.intervenerThree = IntervenerThree.builder().build();
        }
        return intervenerThree;
    }

    @JsonIgnore
    public IntervenerThree getIntervenerThreeWrapperIfPopulated() {
        if (intervenerThree != null) {
            return this.intervenerThree;
        }
        return null;
    }


    @JsonIgnore
    public IntervenerFour getIntervenerFour() {
        if (intervenerFour == null) {
            this.intervenerFour = IntervenerFour.builder().build();
        }
        return intervenerFour;
    }

    @JsonIgnore
    public IntervenerFour getIntervenerFourWrapperIfPopulated() {
        if (intervenerFour != null) {
            return this.intervenerFour;
        }
        return null;
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
    public OrderWrapper getOrderWrapper() {
        if (orderWrapper == null) {
            this.orderWrapper = new OrderWrapper();
        }

        return orderWrapper;
    }

    @JsonIgnore
    public String nullToEmpty(Object o) {
        return Objects.toString(o, "");
    }

    @JsonIgnore
    public String getApplicantLastName() {
        return nullToEmpty(getContactDetailsWrapper().getApplicantLname()).trim();
    }

    @JsonIgnore
    public String getRespondentLastName() {
        return nullToEmpty(getContactDetailsWrapper().getRespondentLname()).trim();
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
    public boolean isPaperCase() {
        return YesOrNo.YES.equals(paperApplication);
    }

    @JsonIgnore
    public boolean isAppAddressConfidential() {
        return YesOrNo.YES.equals(getContactDetailsWrapper().getApplicantAddressHiddenFromRespondent());
    }

    @JsonIgnore
    public String getApplicantSolicitorPostcode() {
        if (isConsentedApplication()) {
            Address solicitorAddress = getContactDetailsWrapper().getSolicitorAddress();
            return solicitorAddress != null ? solicitorAddress.getPostCode() : null;
        } else {
            Address applicantAddress = getContactDetailsWrapper().getApplicantSolicitorAddress();
            return applicantAddress != null ? applicantAddress.getPostCode() : null;
        }
    }

    @JsonIgnore
    public String getRespondentSolicitorPostcode() {
        Address respondentAddress = getContactDetailsWrapper().getRespondentSolicitorAddress();
        return respondentAddress != null ? respondentAddress.getPostCode() : null;
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
            return Map.of(RegionSouthEastFrc.BEDFORDSHIRE, getCourtListIdOrDefault(courtList.getBedfordshireCourt()),
                    RegionSouthEastFrc.KENT_FRC, getCourtListIdOrDefault(courtList.getKentSurreyCourt()),
                    RegionSouthEastFrc.THAMES_VALLEY, getCourtListIdOrDefault(courtList.getThamesValleyCourt()))
                .get(frc).getSelectedCourtId();
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
        return this.getSelectedParties(parties);
    }

    @JsonIgnore
    public List<String> getSelectedParties(DynamicMultiSelectList parties) {
        if (parties == null) {
            return List.of();
        }
        return parties.getValue().stream().map(DynamicMultiSelectListElement::getCode).toList();
    }

    @JsonIgnore
    public List<IntervenerHearingNoticeCollection> getIntervenerCollection(
        IntervenerHearingNoticeCollectionName collectionName) {

        return switch (collectionName) {
            case INTV_1 -> getIntv1HearingNoticesCollection();
            case INTV_2 -> getIntv2HearingNoticesCollection();
            case INTV_3 -> getIntv3HearingNoticesCollection();
            case INTV_4 -> getIntv4HearingNoticesCollection();
        };
    }

    @JsonIgnore
    public FormAScannedDocWrapper getFormAScannedDocWrapper() {
        if (formAScannedDocWrapper == null) {
            this.formAScannedDocWrapper = new FormAScannedDocWrapper();
        }

        return formAScannedDocWrapper;
    }

    @JsonIgnore
    public ConsentOrderScannedDocWrapper getConsentOrderScannedDocWrapper() {
        if (consentOrderScannedDocWrapper == null) {
            this.consentOrderScannedDocWrapper = new ConsentOrderScannedDocWrapper();
        }

        return consentOrderScannedDocWrapper;
    }

    @JsonIgnore
    public CfvMigrationWrapper getCfvMigrationWrapper() {
        if (cfvMigrationWrapper == null) {
            this.cfvMigrationWrapper = new CfvMigrationWrapper();
        }

        return cfvMigrationWrapper;
    }

    @JsonIgnore
    public BulkPrintCoversheetWrapper getBulkPrintCoversheetWrapper() {
        if (bulkPrintCoversheetWrapper == null) {
            this.bulkPrintCoversheetWrapper = new BulkPrintCoversheetWrapper();
        }

        return bulkPrintCoversheetWrapper;
    }

    @JsonIgnore
    public BarristerCollectionWrapper getBarristerCollectionWrapper() {
        if (barristerCollectionWrapper == null) {
            this.barristerCollectionWrapper = new BarristerCollectionWrapper();
        }

        return barristerCollectionWrapper;
    }

    @JsonIgnore
    public DraftOrdersWrapper getDraftOrdersWrapper() {
        if (draftOrdersWrapper == null) {
            this.draftOrdersWrapper = new DraftOrdersWrapper();
        }
        return draftOrdersWrapper;
    }

}
