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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ConsentOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContestedContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftDirectionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.InterimWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFourWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThreeWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwoWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.MiamWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ReferToJudgeWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ScheduleOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.UploadCaseDocumentWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;

import java.time.LocalDate;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FinremCaseDataContested extends FinremCaseData {


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
    private YesOrNo promptForUrgentCaseQuestion;
    private String urgentCaseQuestionDetailsTextArea;
    private List<NatureApplication> natureOfApplicationChecklist;
    private List<PensionTypeCollection> consentPensionCollection;
    private CaseDocument bulkPrintCoverSheetIntervener1;
    private CaseDocument bulkPrintCoverSheetIntervener2;
    private CaseDocument bulkPrintCoverSheetIntervener3;
    private CaseDocument bulkPrintCoverSheetIntervener4;
    private Document d11;
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
    private CaseDocument outOfFamilyCourtResolution;
    private DynamicMultiSelectList sourceDocumentList;
    private DynamicMultiSelectList solicitorRoleList;
    private DynamicRadioList intervenersList;
    private DynamicRadioList intervenerOptionList;
    private List<UploadCaseDocumentCollection> manageCaseDocumentCollection;
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
    private List<ContestedGeneralOrderCollection> generalOrders;
    private List<ContestedGeneralOrderCollection> generalOrdersConsent;

    @Getter(AccessLevel.NONE)
    @JsonProperty("intervener1")
    private IntervenerOneWrapper intervenerOneWrapper;

    @Getter(AccessLevel.NONE)
    @JsonProperty("intervener2")
    private IntervenerTwoWrapper intervenerTwoWrapper;

    @Getter(AccessLevel.NONE)
    @JsonProperty("intervener3")
    private IntervenerThreeWrapper intervenerThreeWrapper;

    @Getter(AccessLevel.NONE)
    @JsonProperty("intervener4")
    private IntervenerFourWrapper intervenerFourWrapper;

    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private ReferToJudgeWrapper referToJudgeWrapper;

    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private UploadCaseDocumentWrapper uploadCaseDocumentWrapper;

    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private ContestedContactDetailsWrapper contactDetailsWrapper;

    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private ScheduleOneWrapper scheduleOneWrapper;

    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private ConsentOrderWrapper consentOrderWrapper;

    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private GeneralApplicationWrapper generalApplicationWrapper;

    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private InterimWrapper interimWrapper;

    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private DraftDirectionWrapper draftDirectionWrapper;

    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private MiamWrapper miamWrapper;


    @JsonIgnore
    public MiamWrapper getMiamWrapper() {
        if (miamWrapper == null) {
            this.miamWrapper = new MiamWrapper();
        }
        return miamWrapper;
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
    public GeneralApplicationWrapper getGeneralApplicationWrapper() {
        if (generalApplicationWrapper == null) {
            this.generalApplicationWrapper = new GeneralApplicationWrapper();
        }
        return generalApplicationWrapper;
    }

    @JsonIgnore
    public ConsentOrderWrapper getConsentOrderWrapper() {
        if (consentOrderWrapper == null) {
            this.consentOrderWrapper = new ConsentOrderWrapper();
        }

        return consentOrderWrapper;
    }

    @JsonIgnore
    public ScheduleOneWrapper getScheduleOneWrapper() {
        if (scheduleOneWrapper == null) {
            this.scheduleOneWrapper = new ScheduleOneWrapper();
        }
        return scheduleOneWrapper;
    }

    @JsonIgnore
    public ContestedContactDetailsWrapper getContactDetailsWrapper() {
        if (contactDetailsWrapper == null) {
            this.contactDetailsWrapper = new ContestedContactDetailsWrapper();
        }
        return contactDetailsWrapper;
    }


    @JsonIgnore
    public String getAppSolicitorName() {
        return getContactDetailsWrapper().getApplicantSolicitorName();
    }

    @Override
    public Address getAppSolicitorAddress() {
        return getContactDetailsWrapper().getApplicantSolicitorAddress();
    }

    @JsonIgnore
    public String getAppSolicitorEmail() {
        return getContactDetailsWrapper().getApplicantSolicitorEmail();
    }

    @JsonIgnore
    public boolean isRespondentRepresentedByASolicitor() {
        return YesOrNo.YES.equals(getContactDetailsWrapper().getContestedRespondentRepresented());
    }

    @JsonIgnore
    public boolean isApplicantSolicitorAgreeToReceiveEmails() {
        return YesOrNo.YES.equals(getContactDetailsWrapper().getApplicantSolicitorConsentForEmails());
    }

    @JsonIgnore
    public String getRespondentFullName() {
        return (
            nullToEmpty(getContactDetailsWrapper().getRespondentFmName()).trim()
                + " "
                + nullToEmpty(getContactDetailsWrapper().getRespondentLname()).trim()
        ).trim();
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
    public UploadCaseDocumentWrapper getUploadCaseDocumentWrapper() {
        if (uploadCaseDocumentWrapper == null) {
            this.uploadCaseDocumentWrapper = new UploadCaseDocumentWrapper();
        }
        return uploadCaseDocumentWrapper;
    }

    @JsonIgnore
    public boolean isConsentedInContestedCase() {
        return getConsentOrderWrapper().getConsentD81Question() != null;
    }
}


