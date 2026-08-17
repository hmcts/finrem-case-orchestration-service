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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.Bin;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.BulkPrintCoversheetWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CaseDataMetricsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CaseFlagsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CfvMigrationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CitizenDocumentWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ConsentOrderScannedDocWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ConsentOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftDirectionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.EstimatedAssetsChecklistWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ExpressCaseWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.FormAScannedDocWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralEmailWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralLetterWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GenericInputFields;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.InterimWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFour;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThree;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ListForHearingWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageCaseDocumentsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.MhMigrationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.MiamWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.NatureApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.OrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ReferToJudgeWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.RefugeWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.RegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ScheduleOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.SendOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.StopRepresentationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.UploadCaseDocumentWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerChangeDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.PaymentDetailsWrapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.DefaultAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerCaaCudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesGpeopvAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyJudiciaryRuAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesEhdmahAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedySolicitorCrudPlus1RolesOpciwwAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesMhrsdiAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedySolicitorCrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesSvbfxpAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesYcfimvAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyJudiciaryCrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesEnaquoAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesBtwqpnAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesIwxsitAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceBulkscanCrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminRAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceSystemupdateCrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyJudiciaryCrAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCrAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerCaaCAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.APPBARRISTERAPPSOLICITORRESPBARRISTERRESPSOLICITORCrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerApproverCaseworkerCaaCrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedySuperuserCrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesQgxjxfAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedySolicitorRAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceSystemupdateRAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesSfvgopAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminRuAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesPfugmjAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesZbqgyjAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedySuperuserCudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyJudiciaryCudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.APPBARRISTERAPPSOLICITORRESPBARRISTERRAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.RESPSOLICITORRAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedySolicitorRPlus1RolesQrvrunAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.APPBARRISTERCrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.APPSOLICITORCrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerApproverCrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.RESPBARRISTERCrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.RESPSOLICITORCrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.RESPBARRISTERRESPSOLICITORCudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.APPBARRISTERAPPSOLICITORCudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesGjmikxAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.INTVRBARRISTER1CrudPlus7RolesQwwlocAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerApproverCrudCaseworkerCaaCudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerCaaCrudAccess;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class FinremCaseData implements HasCaseDocument {

    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private Bin bin;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private CaseDataMetricsWrapper caseDataMetricsWrapper;
    @CCD(ignore = true)
    @JsonProperty(access = WRITE_ONLY)
    private String ccdCaseId;
    @JsonIgnore
    private CaseType ccdCaseType;
    @CCD(
            label = "Divorce / Dissolution Case Number",
            hint = "Please enter 10 digit alphanumeric case number or 16 digit CCD case number",
            regex = "^([A-Z|a-z][A-Z|a-z])\\d{2}[D|d]\\d{5}$|^([A-Z|a-z][A-Z|a-z])\\d{2}[J|j]\\d{5}$|^([A-Z|a-z][A-Z|a-z])\\d{2}[N|n]\\d{5}$|\\b\\d{4}[ -]\\d{4}[ -]\\d{4}[ -]\\d{4}\\b|\\b\\d{4}\\d{4}\\d{4}\\d{4}\\b",
            access = {DefaultAccess.class, CaseworkerCaaCudAccess.class}
    )
    private String divorceCaseNumber;
    @CCD(
            label = "What stage has the divorce / dissolution case reached ?",
            hint = "To ensure the application is linked to the divorce file without delay, please upload a copy of the decree [nisi] [absolute]",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_fl_StageReached",
            access = {DefaultAccess.class}
    )
    private StageReached divorceStageReached;
    @CCD(
            label = "Upload Decree Nisi / conditional order",
            categoryID = "divorceDocumentsConditionalOrderOrDecreeNisi",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesGpeopvAccess.class, CaseworkerDivorceFinancialremedyJudiciaryRuAccess.class}
    )
    private CaseDocument divorceUploadEvidence1;
    @CCD(ignore = true)
    private CaseDocument d11;
    @CCD(label = "Date order granted", searchable = false, access = {DefaultAccess.class})
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate divorceDecreeNisiDate;
    @CCD(
            label = "Upload Decree Absolute",
            categoryID = "divorceDocumentsFinalOrderOrDecreeAbsolute",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesGpeopvAccess.class, CaseworkerDivorceFinancialremedyJudiciaryRuAccess.class}
    )
    private CaseDocument divorceUploadEvidence2;
    @CCD(label = "Decree Absolute Date", searchable = false, access = {DefaultAccess.class})
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate divorceDecreeAbsoluteDate;
    @CCD(ignore = true)
    private Provision provisionMadeFor;
    @CCD(ignore = true)
    private Intention applicantIntendsTo;
    @CCD(ignore = true)
    private List<PeriodicalPaymentSubstitute> dischargePeriodicalPaymentSubstituteFor;
    @CCD(ignore = true)
    private YesOrNo applyingForConsentOrder;
    @CCD(ignore = true)
    @JsonProperty("ChildSupportAgencyCalculationMade")
    private YesOrNo childSupportAgencyCalculationMade;
    @CCD(ignore = true)
    @JsonProperty("ChildSupportAgencyCalculationReason")
    private String childSupportAgencyCalculationReason;
    @CCD(
            label = "Solicitor Name",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesEhdmahAccess.class, CaseworkerDivorceFinancialremedySolicitorCrudPlus1RolesOpciwwAccess.class}
    )
    private String authorisationName;
    @CCD(ignore = true)
    private String authorisationFirm;
    @CCD(
            label = "Solicitor Position",
            hint = "Please provide position or office held",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesEhdmahAccess.class, CaseworkerDivorceFinancialremedySolicitorCrudPlus1RolesOpciwwAccess.class}
    )
    private String authorisation2b;
    @CCD(
            label = "Date",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesEhdmahAccess.class, CaseworkerDivorceFinancialremedySolicitorCrudPlus1RolesOpciwwAccess.class}
    )
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate authorisation3;
    @CCD(
            label = "Online Form A",
            hint = "Online Form A",
            categoryID = "applicationsMainApplication",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesMhrsdiAccess.class, CaseworkerDivorceFinancialremedySolicitorCrudAccess.class}
    )
    private CaseDocument miniFormA;
    @CCD(
            label = "Draft Consent Order",
            hint = "Please upload a scanned copy of the draft consent order that has been signed by both parties. PLEASE NOTE: Pension documents should be uploaded separately on the pension upload page or they will not be returned with a court seal upon approval of the application. Where possible, documents should be scanned in Black and White.",
            categoryID = "applicationsConsentOrderToFinaliseProceedings",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class}
    )
    private CaseDocument consentOrder;
    @CCD(ignore = true)
    private CaseDocument consentOrderText;
    @CCD(ignore = true)
    private CaseDocument latestConsentOrder;
    @CCD(ignore = true)
    private YesOrNo d81Question;
    @CCD(ignore = true)
    private CaseDocument d81Joint;
    @CCD(ignore = true)
    private CaseDocument d81Applicant;
    @CCD(ignore = true)
    private CaseDocument d81Respondent;
    @CCD(ignore = true)
    private List<PensionTypeCollection> pensionCollection;
    @CCD(
            label = "Pension Documents",
            hint = "If the application contains an application for a pension sharing, pension compensation sharing, pension attachment or pension compensation attachment order, please upload the relevant pension form(s) from the list below",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_PensionType",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class}
    )
    private List<PensionTypeCollection> consentPensionCollection;
    @CCD(
            label = "Copy of Paper Form A application documents",
            hint = "Copy of Paper Form A application documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_c_paymentDocument",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesSvbfxpAccess.class}
    )
    private List<PaymentDocumentCollection> copyOfPaperFormA;
    @CCD(ignore = true)
    @JsonProperty("otherCollection")
    private List<OtherDocumentCollection> otherDocumentsCollection;
    @CCD(ignore = true)
    private OrderDirection orderDirection;
    @CCD(ignore = true)
    private CaseDocument orderDirectionOpt1;
    @CCD(
            label = "Additional document(s)",
            categoryID = "approvedOrders",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Document",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    private List<DocumentCollectionItem> additionalCicDocuments;
    @CCD(ignore = true)
    private String orderDirectionOpt2;
    @CCD(ignore = true)
    private YesOrNo orderDirectionAbsolute;
    @CCD(
            label = "Does a copy of this order need to be served to the pension provider?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesYcfimvAccess.class}
    )
    private YesOrNo servePensionProvider;
    @CCD(
            label = "Who is responsible for sending a copy of the order to the pension provider?",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesYcfimvAccess.class}
    )
    private PensionProvider servePensionProviderResponsibility;
    @CCD(
            label = "Please Specify",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesYcfimvAccess.class}
    )
    private String servePensionProviderOther;
    @CCD(ignore = true)
    private JudgeType orderDirectionJudge;
    @CCD(ignore = true)
    private String orderDirectionJudgeName;
    @CCD(ignore = true)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate orderDirectionDate;
    @CCD(ignore = true)
    private String orderDirectionAddComments;
    @CCD(
            label = "Application Not Approved",
            hint = "Warning: Previous orders will appear at the top of the page. To avoid overwriting a previous order, please ensure you scroll to the bottom of the page and choose ‘Add New’. A blank template will then appear for you to complete.",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_consentOrderNotApproved",
            access = {CaseworkerDivorceFinancialremedyJudiciaryCrudAccess.class}
    )
    private List<OrderRefusalCollection> orderRefusalCollection;
    @CCD(
            label = "Application Not Approved",
            hint = "Please choose ‘Add New’. A blank template will then appear for you to complete.",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_consentOrderNotApproved",
            access = {CaseworkerDivorceFinancialremedyJudiciaryCrudAccess.class}
    )
    private List<OrderRefusalCollection> orderRefusalCollectionNew;
    @CCD(
            label = "Application Not Approved",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyJudiciaryCrudAccess.class}
    )
    private OrderRefusalHolder orderRefusalOnScreen;
    @CCD(
            label = "Preview of Draft Order",
            categoryID = "administrativeDocumentsTransitional",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerDivorceFinancialremedyJudiciaryCrudAccess.class}
    )
    private CaseDocument orderRefusalPreviewDocument;
    @CCD(ignore = true)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dueDate;
    @CCD(
            label = "Issue Date",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesSvbfxpAccess.class, CaseworkerCaaCudAccess.class}
    )
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate issueDate;
    @CCD(ignore = true)
    private AssignToJudgeReason assignedToJudgeReason;
    @CCD(
            label = "The Judge reserved for this case:",
            hint = "Select a Judge",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_fl_AssignToJudge",
            typeParameterClass = FRFlAssignToJudge.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesEnaquoAccess.class}
    )
    private String assignedToJudge;
    @CCD(ignore = true)
    private List<UploadConsentOrderDocumentCollection> uploadConsentOrderDocuments;
    @CCD(
            label = "Upload Order ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_ct_uploadOrder",
            typeParameterClass = FRCtUploadOrder.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesBtwqpnAccess.class}
    )
    private List<UploadOrderCollection> uploadOrder;
    @CCD(ignore = true)
    private List<UploadDocumentCollection> uploadDocuments;
    @CCD(ignore = true)
    private List<SolUploadDocumentCollection> solUploadDocuments;
    @CCD(ignore = true)
    private List<RespondToOrderDocumentCollection> respondToOrderDocuments;
    @CCD(ignore = true)
    private List<AmendedConsentOrderCollection> amendedConsentOrderCollection;
    @CCD(
            label = "Notes",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_caseNotes",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesEnaquoAccess.class}
    )
    private List<CaseNotesCollection> caseNotesCollection;
    @CCD(
            label = "state",
            hint = "state",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesEnaquoAccess.class}
    )
    private String state;
    @CCD(
            label = "Scanned Documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ScannedDocument",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesIwxsitAccess.class}
    )
    private List<ScannedDocumentCollection> scannedDocuments;
    @CCD(
            label = "Supplementary evidence handled",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesIwxsitAccess.class}
    )
    private YesOrNo evidenceHandled;
    @CCD(ignore = true)
    private CaseDocument approvedConsentOrderLetter;
    @CCD(
            label = "Bulk Print LetterId For Respondent / Solicitor",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminCudAccess.class}
    )
    private String bulkPrintLetterIdRes;
    @CCD(
            label = "Bulk Print LetterId For Applicant / Solicitor",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminCudAccess.class}
    )
    private String bulkPrintLetterIdApp;
    @CCD(ignore = true)
    private List<ConsentOrderCollection> approvedOrderCollection;
    @CCD(ignore = true)
    private ApplicantRole divRoleOfFrApplicant;
    @CCD(ignore = true)
    private ApplicantRepresentedPaper applicantRepresentedPaper;
    @CCD(ignore = true)
    private String authorisationSolicitorAddress;
    @CCD(ignore = true)
    private YesOrNo authorisationSigned;
    @CCD(ignore = true)
    private AuthorisationSignedBy authorisationSignedBy;
    @CCD(
            label = "Exception Record Reference",
            searchable = false,
            access = {CaseworkerDivorceBulkscanCrudAccess.class, CaseworkerDivorceFinancialremedyCourtadminRAccess.class, CaseworkerDivorceSystemupdateCrudAccess.class}
    )
    private String bulkScanCaseReference;
    @CCD(ignore = true)
    private List<ChildrenInfoCollection> childrenInfo;
    @CCD(ignore = true)
    private CaseDocument formA;
    @CCD(ignore = true)
    private List<DocumentCollectionItem> scannedD81s;
    @CCD(ignore = true)
    private String transferLocalCourtName;
    @CCD(ignore = true)
    private String transferLocalCourtEmail;
    @CCD(ignore = true)
    private String transferLocalCourtInstructions;
    @CCD(ignore = true)
    private List<TransferCourtEmailCollection> transferLocalCourtEmailCollection;
    @CCD(
            label = "Does this FR case relate to a Dissolution of a Civil Partnership?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesGpeopvAccess.class, CaseworkerCaaCudAccess.class, CaseworkerDivorceFinancialremedyJudiciaryCrAccess.class, CaseworkerDivorceFinancialremedyCrAccess.class}
    )
    private YesOrNo civilPartnership;
    @CCD(
            label = "Is this an urgent case ?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesGpeopvAccess.class, CaseworkerCaaCAccess.class, CaseworkerDivorceFinancialremedyJudiciaryCrAccess.class, CaseworkerDivorceFinancialremedyCrAccess.class}
    )
    private YesOrNo promptForUrgentCaseQuestion;
    @CCD(
            label = "Provide details as to why the case is urgent",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class}
    )
    private String urgentCaseQuestionDetailsTextArea;
    @CCD(
            label = "Change of representatives",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "RepresentationUpdate",
            access = {APPBARRISTERAPPSOLICITORRESPBARRISTERRESPSOLICITORCrudAccess.class, CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesMhrsdiAccess.class, CaseworkerApproverCaseworkerCaaCrudAccess.class, CaseworkerDivorceFinancialremedySolicitorCrudAccess.class, CaseworkerDivorceFinancialremedyCrudAccess.class, CaseworkerDivorceSystemupdateCrudAccess.class}
    )
    @JsonProperty("RepresentationUpdateHistory")
    private List<RepresentationUpdateHistoryCollection> representationUpdateHistory;
    @CCD(ignore = true)
    private YesOrNo paperApplication;
    @CCD(ignore = true)
    @JsonProperty("RespSolNotificationsEmailConsent")
    private YesOrNo respSolNotificationsEmailConsent;
    @CCD(label = "Date of marriage / civil partnership", searchable = false, access = {DefaultAccess.class})
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfMarriage;
    @CCD(label = "Date of separation", searchable = false, access = {DefaultAccess.class})
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfSepration;
    @CCD(
            label = "Name of Court / Divorce Centre where petition issued ",
            searchable = false,
            access = {DefaultAccess.class}
    )
    private String nameOfCourtDivorceCentre;
    @CCD(
            label = "Upload Petition",
            categoryID = "divorceDocumentsApplicationOrPetition",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesGpeopvAccess.class, CaseworkerDivorceFinancialremedyJudiciaryRuAccess.class}
    )
    private CaseDocument divorceUploadPetition;
    @CCD(label = "Application Issued Date", searchable = false, access = {DefaultAccess.class})
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate divorcePetitionIssuedDate;
    @CCD(
            label = "Property address",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class}
    )
    private String propertyAddress;
    @CCD(
            label = "Name(s) and address(es) of any mortgage(s) for property",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class}
    )
    private String mortgageDetail;
    @CCD(label = "    ", searchable = false, typeOverride = FieldType.YesOrNo, access = {DefaultAccess.class})
    private YesOrNo additionalPropertyOrderDecision;
    @CCD(
            label = "Additional Property adjustment order details",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_PropertyAdjustmentOrder",
            access = {DefaultAccess.class}
    )
    @JsonProperty("propertyAdjutmentOrderDetail")
    private List<PropertyAdjustmentOrderCollection> propertyAdjustmentOrderDetail;
    @CCD(
            label = "Documents to remove",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_documentToRemove",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class, CaseworkerDivorceFinancialremedySuperuserCrudAccess.class}
    )
    private List<DocumentToKeepCollection> documentToKeepCollection;
    @CCD(label = "     ", searchable = false, typeOverride = FieldType.YesOrNo, access = {DefaultAccess.class})
    private YesOrNo paymentForChildrenDecision;
    @CCD(label = "     ", searchable = false, typeOverride = FieldType.YesOrNo, access = {DefaultAccess.class})
    private YesOrNo benefitForChildrenDecision;
    @CCD(
            label = "      ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "FR_ms_benefitPaymentChecklist",
            typeParameterClass = FRMsBenefitPaymentChecklist.class,
            access = {DefaultAccess.class}
    )
    private List<BenefitPayment> benefitPaymentChecklist;
    @CCD(label = "     ", searchable = false, typeOverride = FieldType.YesOrNo, access = {DefaultAccess.class})
    private YesOrNo fastTrackDecision;
    @CCD(
            label = "     ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "FR_ms_fast_track_reason",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesGpeopvAccess.class}
    )
    private List<FastTrackReason> fastTrackDecisionReason;
    @CCD(
            label = "  A complex case could be retained for hearing within the Financial Remedy Centre and/or allocated to a higher tier of Judiciary",
            searchable = false,
            access = {DefaultAccess.class}
    )
    private Complexity addToComplexityListOfCourts;
    @CCD(
            label = "Select all that apply",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "FR_ms_estimatedAssetsChecklist",
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesQgxjxfAccess.class}
    )
    private List<EstimatedAsset> estimatedAssetsChecklist;
    @CCD(
            label = "Select one",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class}
    )
    private EstimatedAssetV2 estimatedAssetsChecklistV2;
    @CCD(
            label = "Select the nearest estimate of the case value",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class}
    )
    private EstimatedAssetV3 estimatedAssetsChecklistV3;
    @CCD(label = "  ", searchable = false, access = {DefaultAccess.class})
    private String netValueOfHome;
    @CCD(
            label = "Select all that apply",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "FR_ms_potentialAllegationChecklist",
            access = {DefaultAccess.class}
    )
    private List<PotentialAllegation> potentialAllegationChecklist;
    @CCD(label = "   ", searchable = false, access = {DefaultAccess.class})
    private String detailPotentialAllegation;
    @CCD(label = "  ", searchable = false, typeOverride = FieldType.YesOrNo, access = {DefaultAccess.class})
    private YesOrNo otherReasonForComplexity;
    @CCD(
            label = "If yes – please specify",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class}
    )
    private String otherReasonForComplexityText;
    @CCD(
            label = "  ",
            hint = "For example they need to use British Sign Language, a hearing loop or documents in braille",
            searchable = false,
            access = {DefaultAccess.class}
    )
    private String specialAssistanceRequired;
    @CCD(
            label = "  ",
            hint = "For example you need a separate waiting room to the other person, video link or protective screen due to safety concerns. The court may contact you to discuss your requirements.",
            searchable = false,
            access = {DefaultAccess.class}
    )
    private String specificArrangementsRequired;
    @CCD(
            label = "Are there any reasons why the case should not proceed in the applicant’s Local Court? If yes, please set out what they are.",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesGpeopvAccess.class}
    )
    private YesOrNo isApplicantsHomeCourt;
    @CCD(
            label = "Please specify",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesGpeopvAccess.class}
    )
    private String reasonForLocalCourt;
    @CCD(
            label = "Do you consider that the case should be allocated to be heard at High Court Judge level?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class}
    )
    private YesOrNo allocatedToBeHeardAtHighCourtJudgeLevel;
    @CCD(
            label = "If yes, please set out your reasons for this, with particular reference to the factors set out in the ‘Statement on the Efficient Conduct of Financial Remedy Hearings Allocated to a High Court Judge' (published on 1 February 2016)",
            showCondition = "allocatedToBeHeardAtHighCourtJudgeLevel=\"Yes\"",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class}
    )
    private String allocatedToBeHeardAtHighCourtJudgeLevelText;
    @CCD(label = "Mediator Registration Number (URN)", searchable = false, access = {DefaultAccess.class})
    private String mediatorRegistrationNumber;
    @CCD(label = "Family Mediation Service Name", searchable = false, access = {DefaultAccess.class})
    private String familyMediatorServiceName;
    @CCD(label = "Sole Trader Name", searchable = false, access = {DefaultAccess.class})
    private String soleTraderName;
    @CCD(
            label = "Upload MIAM Document",
            categoryID = "applicationsMainApplication",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {DefaultAccess.class}
    )
    private CaseDocument uploadMediatorDocument;
    @CCD(
            label = "Upload MIAM Document",
            categoryID = "applicationsMainApplication",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {DefaultAccess.class}
    )
    private CaseDocument uploadMediatorDocumentPaperCase;
    @CCD(label = "Mediator Registration Number (URN)", searchable = false, access = {DefaultAccess.class})
    private String mediatorRegistrationNumber1;
    @CCD(label = "Family Mediation Service Name", searchable = false, access = {DefaultAccess.class})
    private String familyMediatorServiceName1;
    @CCD(label = "Sole Trader Name", searchable = false, access = {DefaultAccess.class})
    private String soleTraderName1;
    @CCD(label = "          ", searchable = false, typeOverride = FieldType.YesOrNo, access = {DefaultAccess.class})
    private YesOrNo promptForAnyDocument;
    @CCD(
            label = "Additional Hearing Details",
            hint = "Additional Hearing Details",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_ct_hearingInformationCollection",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesEnaquoAccess.class, CaseworkerDivorceFinancialremedySolicitorRAccess.class, CaseworkerDivorceSystemupdateRAccess.class}
    )
    private List<HearingDirectionDetailsCollection> hearingDirectionDetailsCollection;
    @CCD(
            label = "Hearing notice pack to send",
            hint = "Hearing notice pack to send",
            categoryID = "hearingNotices",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Document",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesSfvgopAccess.class, CaseworkerDivorceSystemupdateRAccess.class}
    )
    private List<DocumentCollectionItem> hearingNoticeDocumentPack;
    @CCD(
            label = "Hearing Notices Documents",
            hint = "Hearing Notices Documents",
            categoryID = "hearingNotices",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Document",
            access = {CaseworkerDivorceFinancialremedyCourtadminRuAccess.class, CaseworkerDivorceFinancialremedyJudiciaryRuAccess.class, CaseworkerDivorceFinancialremedySolicitorRAccess.class}
    )
    private List<DocumentCollectionItem> hearingNoticesDocumentCollection;
    @CCD(ignore = true)
    private Map<String, Object> courtDetails;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private ListForHearingWrapper listForHearingWrapper;
    @CCD(
            label = "  ",
            hint = "Tick all that apply",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "FR_judgeAllocatedList",
            access = {CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesYcfimvAccess.class}
    )
    private List<JudgeAllocated> judgeAllocated;
    @CCD(
            label = "   ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesYcfimvAccess.class}
    )
    private YesOrNo applicationAllocatedTo;
    @CCD(
            label = "    ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesYcfimvAccess.class}
    )
    private YesOrNo caseAllocatedTo;
    @CCD(
            label = "  ",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesYcfimvAccess.class}
    )
    private JudgeTimeEstimate judgeTimeEstimate;
    @CCD(
            label = " Please state ",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesYcfimvAccess.class}
    )
    private String judgeTimeEstimateTextArea;
    @CCD(
            label = "Upload Documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_ct_uploadGeneralDocument",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesBtwqpnAccess.class}
    )
    private List<UploadGeneralDocumentCollection> uploadGeneralDocuments;
    @CCD(
            label = "Assign to Judge Reason",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_fl_AssignToJudgeReason",
            typeParameterClass = FRFlAssignToJudgeReason.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesSvbfxpAccess.class}
    )
    private AssignToJudgeReason assignToJudgeReason;
    @CCD(
            label = "Assign to a Judge Text",
            hint = " A few words describing the purpose of referal",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesSvbfxpAccess.class}
    )
    private String assignToJudgeText;
    @CCD(
            label = "Subject to Decree Absolute/Final Order?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesYcfimvAccess.class}
    )
    private YesOrNo subjectToDecreeAbsoluteValue;
    @CCD(
            label = "Select Judge",
            hint = "Please select the appropriate judge",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_fl_Judge",
            typeParameterClass = FRFlJudge.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesYcfimvAccess.class}
    )
    private String selectJudge;
    @CCD(
            label = "Date of order",
            hint = "Date of order",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesYcfimvAccess.class}
    )
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfOrder;
    @CCD(
            label = "Additional comments",
            hint = "Please add any additional comments for  court admin (this comments will not be accesible by applicant's solicitor)",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesYcfimvAccess.class}
    )
    private String additionalComments;
    @CCD(
            label = "Application Not Approved",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_ct_applicationNotApproved",
            access = {CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesYcfimvAccess.class}
    )
    private List<ApplicationNotApprovedCollection> applicationNotApproved;
    @CCD(
            label = "  ",
            hint = "For example they need to use British Sign Language, a hearing loop or documents in braille",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesYcfimvAccess.class}
    )
    private String attendingCourtWithAssistance;
    @CCD(
            label = "  ",
            hint = "For example you need a separate waiting room to the other person, video link or protective screen due to safety concerns. The court may contact you to discuss your requirements.",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesYcfimvAccess.class}
    )
    private String attendingCourtWithArrangement;
    @CCD(
            label = "Solicitor to Draft Order",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesYcfimvAccess.class}
    )
    private SolicitorToDraftOrder solicitorResponsibleForDraftingOrder;
    @CCD(
            label = "Upload Approved Order ",
            hint = "Upload Approved Order ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_ct_directionOrderCollection",
            typeParameterClass = FRCtDirectionOrderCollection.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesPfugmjAccess.class}
    )
    private List<DirectionOrderCollection> uploadHearingOrder;
    @CCD(
            label = "Unprocessed Hearing Orders",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_ct_directionOrderCollection",
            typeParameterClass = FRCtDirectionOrderCollection.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    private List<DirectionOrderCollection> unprocessedUploadHearingDocuments;
    @CCD(
            label = "Upload Other Documents",
            hint = "Upload Other Documents",
            categoryID = "hearingNotices",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Document",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesSvbfxpAccess.class}
    )
    private List<DocumentCollectionItem> hearingOrderOtherDocuments;
    @CCD(
            label = "Next Hearing Details",
            hint = "Direction Orders Details ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_ct_directionDetailCollection",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesBtwqpnAccess.class}
    )
    private List<DirectionDetailCollection> directionDetailsCollection;
    @CCD(
            label = "Finalised Orders",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "view_finalOrderCollection",
            typeParameterClass = ViewFinalOrderCollection.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesZbqgyjAccess.class}
    )
    private List<DirectionOrderCollection> finalOrderCollection;
    @CCD(
            label = "Hearing Notices",
            hint = "Hearing Notices",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_intervenerHearingNoticeCollection",
            access = {CaseworkerDivorceFinancialremedyCourtadminCudAccess.class}
    )
    private List<IntervenerHearingNoticeCollection> intv1HearingNoticesCollection;
    @CCD(
            label = "Hearing Notices",
            hint = "Hearing Notices",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_intervenerHearingNoticeCollection",
            access = {CaseworkerDivorceFinancialremedyCourtadminCudAccess.class}
    )
    private List<IntervenerHearingNoticeCollection> intv2HearingNoticesCollection;
    @CCD(
            label = "Hearing Notices",
            hint = "Hearing Notices",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_intervenerHearingNoticeCollection",
            access = {CaseworkerDivorceFinancialremedyCourtadminCudAccess.class}
    )
    private List<IntervenerHearingNoticeCollection> intv3HearingNoticesCollection;
    @CCD(
            label = "Hearing Notices",
            hint = "Hearing Notices",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_intervenerHearingNoticeCollection",
            access = {CaseworkerDivorceFinancialremedyCourtadminCudAccess.class}
    )
    private List<IntervenerHearingNoticeCollection> intv4HearingNoticesCollection;
    @CCD(
            label = "Judge Not Approved Reasons",
            hint = "Judge Not Approved Reasons",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_c_judgeNotApprovedReasons",
            access = {CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesYcfimvAccess.class, CaseworkerDivorceFinancialremedySuperuserCudAccess.class}
    )
    private List<JudgeNotApprovedReasonsCollection> judgeNotApprovedReasons;
    @CCD(
            label = "Select Judge",
            hint = "Please select the appropriate Judge",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_fl_RefusalOrderJudgeType",
            typeParameterClass = FRFlRefusalOrderJudgeType.class,
            access = {CaseworkerDivorceFinancialremedyJudiciaryCrudAccess.class}
    )
    private JudgeType refusalOrderJudgeType;
    @CCD(
            label = "Name of Judge",
            hint = "Surname of judge",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerDivorceFinancialremedyJudiciaryCrudAccess.class}
    )
    private String refusalOrderJudgeName;
    @CCD(
            label = "Court order date",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyJudiciaryCrudAccess.class}
    )
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate refusalOrderDate;
    @CCD(
            label = "Preview of Refusal Order",
            categoryID = "administrativeDocumentsTransitional",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerDivorceFinancialremedyJudiciaryCrudAccess.class}
    )
    private CaseDocument refusalOrderPreviewDocument;
    @CCD(
            label = "Refused Order Collection",
            hint = "Refused Order Collection",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_ct_refusedOrderCollection",
            access = {CaseworkerDivorceFinancialremedyJudiciaryCudAccess.class, CaseworkerDivorceFinancialremedySuperuserCudAccess.class}
    )
    private List<RefusalOrderCollection> refusalOrderCollection;
    @CCD(
            label = "Latest Refusal Order Document",
            categoryID = "administrativeDocumentsTransitional",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerDivorceFinancialremedyJudiciaryCudAccess.class, CaseworkerDivorceFinancialremedySolicitorRAccess.class}
    )
    private CaseDocument latestRefusalOrder;
    @CCD(
            label = "Refusal Order Document",
            categoryID = "applicationsConsentOrderToFinaliseProceedings",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerDivorceFinancialremedyJudiciaryCrudAccess.class}
    )
    private CaseDocument refusalOrderAdditionalDocument;
    @CCD(label = " ", searchable = false)
    private String hiddenTabValue;
    @CCD(
            label = "Latest draft hearing order",
            categoryID = "systemDuplicates",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesYcfimvAccess.class, APPBARRISTERAPPSOLICITORRESPBARRISTERRAccess.class, RESPSOLICITORRAccess.class, CaseworkerDivorceSystemupdateCrudAccess.class}
    )
    private CaseDocument latestDraftHearingOrder;
    @CCD(
            label = "Name of Judge",
            hint = "Surname of judge",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesEnaquoAccess.class}
    )
    private String orderApprovedJudgeName;
    @CCD(
            label = "Select Judge",
            hint = "Please select the appropriate Judge",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_fl_RefusalOrderJudgeType",
            typeParameterClass = FRFlRefusalOrderJudgeType.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesEnaquoAccess.class}
    )
    private JudgeType orderApprovedJudgeType;
    @CCD(
            label = "Upload other documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadAdditionalDocument",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesGpeopvAccess.class, CaseworkerDivorceFinancialremedyJudiciaryRuAccess.class}
    )
    private List<UploadAdditionalDocumentCollection> uploadAdditionalDocument;
    @CCD(
            label = "Court order date",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesEnaquoAccess.class}
    )
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate orderApprovedDate;
    @CCD(
            label = "Order approved cover letter",
            categoryID = "administrativeDocumentsTransitional",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerDivorceFinancialremedySolicitorRPlus1RolesQrvrunAccess.class, CaseworkerDivorceFinancialremedyJudiciaryCudAccess.class}
    )
    private CaseDocument orderApprovedCoverLetter;
    @CCD(
            label = "Provide the date of hearing and a description of the document",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String hearingDetails;
    @CCD(
            label = "Share case documents with respondent",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {APPBARRISTERCrudAccess.class, APPSOLICITORCrudAccess.class, CaseworkerApproverCrudAccess.class}
    )
    private YesOrNo applicantShareDocs;
    @CCD(
            label = "Share case documents with applicant",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {RESPBARRISTERCrudAccess.class, RESPSOLICITORCrudAccess.class, CaseworkerApproverCrudAccess.class}
    )
    private YesOrNo respondentShareDocs;
    @CCD(
            label = "Reason for selecting an FRC location",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class}
    )
    @JsonProperty("reasonForFRCLocation")
    private String reasonForFrcLocation;
    @CCD(
            label = "Hearing Bundles ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_hearingUploadBundle",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class, APPBARRISTERAPPSOLICITORRESPBARRISTERRESPSOLICITORCrudAccess.class}
    )
    private List<HearingUploadBundleCollection> hearingUploadBundle;
    @CCD(
            label = "FDR Hearing Bundle",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_hearingUploadBundle",
            access = {RESPBARRISTERRESPSOLICITORCudAccess.class, APPBARRISTERAPPSOLICITORCudAccess.class, CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesMhrsdiAccess.class, CaseworkerDivorceFinancialremedySolicitorCrudAccess.class}
    )
    private List<HearingUploadBundleCollection> fdrHearingBundleCollections;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private SendOrderWrapper sendOrderWrapper;
    @CCD(
            label = "Who should receive this order?",
            searchable = false,
            typeOverride = FieldType.DynamicMultiSelectList,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    private DynamicMultiSelectList partiesOnCase;
    @CCD(
            label = "Confidential documents",
            hint = "Legacy confidential documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_UploadConfidentialDocument",
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesMhrsdiAccess.class}
    )
    private List<ConfidentialUploadedDocumentData> confidentialDocumentsUploaded;
    @CCD(
            label = "Change Organisation Request",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesSfvgopAccess.class, CaseworkerApproverCaseworkerCaaCrudAccess.class}
    )
    private ChangeOrganisationRequest changeOrganisationRequestField;
    @CCD(ignore = true)
    @JsonProperty("ApplicantOrganisationPolicy")
    private OrganisationPolicy applicantOrganisationPolicy;
    @CCD(ignore = true)
    @JsonProperty("RespondentOrganisationPolicy")
    private OrganisationPolicy respondentOrganisationPolicy;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.Text, access = {DefaultAccess.class})
    private CaseRole currentUserCaseRole;
    @CCD(label = " ", searchable = false, access = {DefaultAccess.class})
    private String currentUserCaseRoleLabel;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerDivorceFinancialremedySuperuserCrudAccess.class}
    )
    private String currentUserCaseRoleType;
    @CCD(
            label = "Out Of Family Court Resolution",
            hint = "Out Of Family Court Resolution",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesGjmikxAccess.class}
    )
    private CaseDocument outOfFamilyCourtResolution;
    @CCD(
            label = "List of documents",
            searchable = false,
            typeOverride = FieldType.DynamicMultiSelectList,
            access = {INTVRBARRISTER1CrudPlus7RolesQwwlocAccess.class, APPBARRISTERAPPSOLICITORRESPBARRISTERRESPSOLICITORCrudAccess.class, CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    private DynamicMultiSelectList sourceDocumentList;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.DynamicMultiSelectList,
            access = {INTVRBARRISTER1CrudPlus7RolesQwwlocAccess.class, APPBARRISTERAPPSOLICITORRESPBARRISTERRESPSOLICITORCrudAccess.class, CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    private DynamicMultiSelectList solicitorRoleList;
    @CCD(
            label = "Please select intervener to manage",
            searchable = false,
            typeOverride = FieldType.DynamicRadioList,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesSfvgopAccess.class}
    )
    private DynamicRadioList intervenersList;
    @CCD(
            label = "Please select appropriate option",
            searchable = false,
            typeOverride = FieldType.DynamicRadioList,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesSfvgopAccess.class}
    )
    private DynamicRadioList intervenerOptionList;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private GenericInputFields genericInputFields;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private ManageCaseDocumentsWrapper manageCaseDocumentsWrapper;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private StopRepresentationWrapper stopRepresentationWrapper;

    @CCD(
            label = "Intervener 1",
            searchable = false,
            typeParameterClass = FRIntervener.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesSfvgopAccess.class}
    )
    @Getter(AccessLevel.NONE)
    @JsonProperty("intervener1")
    private IntervenerOne intervenerOne;

    @CCD(
            label = "Intervener 2",
            searchable = false,
            typeParameterClass = FRIntervener.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesSfvgopAccess.class}
    )
    @Getter(AccessLevel.NONE)
    @JsonProperty("intervener2")
    private IntervenerTwo intervenerTwo;

    @CCD(
            label = "Intervener 3",
            searchable = false,
            typeParameterClass = FRIntervener.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesSfvgopAccess.class}
    )
    @Getter(AccessLevel.NONE)
    @JsonProperty("intervener3")
    private IntervenerThree intervenerThree;

    @CCD(
            label = "Intervener 4",
            searchable = false,
            typeParameterClass = FRIntervener.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesSfvgopAccess.class}
    )
    @Getter(AccessLevel.NONE)
    @JsonProperty("intervener4")
    private IntervenerFour intervenerFour;

    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private ManageHearingsWrapper manageHearingsWrapper;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private DraftOrdersWrapper draftOrdersWrapper;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private ExpressCaseWrapper expressCaseWrapper;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private FormAScannedDocWrapper formAScannedDocWrapper;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private ConsentOrderScannedDocWrapper consentOrderScannedDocWrapper;
    @CCD(ignore = true)
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
    @CCD(
            label = "What type of a document is this?",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesIwxsitAccess.class}
    )
    @JsonProperty("typeOfDocument")
    private ScannedDocumentTypeOption scannedDocsTypeOfDocument;
    @CCD(
            label = "Applicant Scanned Documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "applicantScanDocsCollection",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesIwxsitAccess.class}
    )
    private List<ApplicantScanDocsCollection> applicantScanDocuments;
    @CCD(
            label = "Respondent Scanned Documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "respondentScanDocsCollection",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesIwxsitAccess.class}
    )
    private List<RespondentScanDocsCollection> respondentScanDocuments;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_manageCaseDocuments",
            typeParameterClass = FRManageCaseDocuments.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesBtwqpnAccess.class}
    )
    private List<ManageScannedDocumentCollection> manageScannedDocumentCollection;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private BarristerCollectionWrapper barristerCollectionWrapper;
    @CCD(
            label = " ",
            searchable = false,
            typeParameterOverride = "barristerPartyList",
            typeParameterClass = BarristerPartyList.class,
            access = {DefaultAccess.class}
    )
    private BarristerParty barristerParty;
    @CCD(
            label = "     ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerApproverCrudCaseworkerCaaCudAccess.class}
    )
    private YesOrNo benefitForChildrenDecisionSchedule;
    @CCD(
            label = "      ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "FR_ms_benefitPaymentChecklist",
            typeParameterClass = FRMsBenefitPaymentChecklist.class,
            access = {DefaultAccess.class, CaseworkerApproverCrudCaseworkerCaaCudAccess.class}
    )
    private List<BenefitPaymentChecklist> benefitPaymentChecklistSchedule;
    @CCD(
            label = "Original order to be varied",
            categoryID = "applicationsVariationOrder",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class, CaseworkerCaaCrudAccess.class, CaseworkerDivorceFinancialremedyCrudAccess.class}
    )
    private CaseDocument variationOrderDocument;
    @CCD(
            label = "Original order to be varied",
            categoryID = "applicationsVariationOrder",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class, CaseworkerApproverCaseworkerCaaCrudAccess.class, CaseworkerDivorceFinancialremedyCrudAccess.class}
    )
    private CaseDocument consentVariationOrderDocument;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerApproverCaseworkerCaaCrudAccess.class, CaseworkerDivorceSystemupdateCrudAccess.class}
    )
    private YesOrNo isNocRejected;

    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private CfvMigrationWrapper cfvMigrationWrapper;

    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private MhMigrationWrapper mhMigrationWrapper;

    @CCD(label = " ", typeOverride = FieldType.YesOrNo, access = {CaseworkerDivorceSystemupdateCrudAccess.class})
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
    @CCD(
            label = "Orders shared with Parties",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "sendOrderDocuments",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    private List<OrderSentToPartiesCollection> ordersSentToPartiesCollection;

    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private ScheduleOneWrapper scheduleOneWrapper;

    @CCD(ignore = true)
    private List<ConsentedHearingDataWrapper> listForHearings;

    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private CaseFlagsWrapper caseFlagsWrapper;

    @CCD(label = "  ", searchable = false, access = {CaseworkerDivorceFinancialremedySuperuserCrudAccess.class})
    private String previousState;
    @CCD(
            label = "Please select user case access to remove",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerDivorceFinancialremedySuperuserCrudAccess.class}
    )
    private DynamicList userCaseAccessList;

    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private RefugeWrapper refugeWrapper;

    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private PaymentDetailsWrapper paymentDetailsWrapper;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_accessCodeEntry",
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    private List<AccessCodeCollection> applicantAccessCodes;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_accessCodeEntry",
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    private List<AccessCodeCollection> respondentAccessCodes;

    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private CitizenDocumentWrapper citizenDocumentWrapper;

    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private EstimatedAssetsChecklistWrapper estimatedAssetsChecklistWrapper;

    @JsonIgnore
    public CaseDataMetricsWrapper getCaseDataMetricsWrapper() {
        if (caseDataMetricsWrapper == null) {
            this.caseDataMetricsWrapper = new CaseDataMetricsWrapper();
        }
        return caseDataMetricsWrapper;
    }

    @JsonIgnore
    public StopRepresentationWrapper getStopRepresentationWrapper() {
        if (stopRepresentationWrapper == null) {
            this.stopRepresentationWrapper = new StopRepresentationWrapper();
        }
        return stopRepresentationWrapper;
    }

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

    /**
     * Returns the {@link IntervenerWrapper} for the given intervener ID.
     *
     * <p>
     * The supplied {@code id} is 1-based (Intervener 1–4), while the underlying
     * list of interveners is 0-based. This method converts the ID to the correct
     * list index by subtracting one.
     * </p>
     *
     * @param id the 1-based intervener ID
     * @return the matching {@link IntervenerWrapper}
     * @throws IndexOutOfBoundsException if the id is less than 1 or greater than 4
     */
    @JsonIgnore
    public IntervenerWrapper getIntervenerById(int id) {
        return getInterveners().get(id - 1);
    }

    /**
     * Returns all interveners as an ordered, immutable list.
     *
     * <p>
     * The list is ordered as Intervener 1 through Intervener 4 to allow
     * deterministic access by index (for example, via {@link #getIntervenerById(int)}).
     * </p>
     *
     * <p>
     * This method is annotated with {@link JsonIgnore} to prevent the derived list
     * from being serialised, as the interveners are already represented by their
     * individual fields.
     * </p>
     *
     * @return an immutable list containing all intervener wrappers
     */
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
    public boolean isRespondentRepresentedByASolicitor() {
        return YesOrNo.YES.equals(getContactDetailsWrapper().getContestedRespondentRepresented())
            || YesOrNo.YES.equals(getContactDetailsWrapper().getConsentedRespondentRepresented());
    }

    @JsonIgnore
    public boolean isPaperCase() {
        return YesOrNo.YES.equals(paperApplication);
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
    public String getAppSolicitorEmailIfRepresented() {
        if (!isApplicantRepresentedByASolicitor()) {
            return null;
        }

        var contactWrapper = getContactDetailsWrapper();
        if (isConsentedApplication()) {
            return contactWrapper.getSolicitorEmail();
        } else {
            return contactWrapper.getApplicantSolicitorEmail();
        }
    }

    @JsonIgnore
    public String getRespSolicitorEmailIfRepresented() {
        if (!isRespondentRepresentedByASolicitor()) {
            return null;
        }
        return getContactDetailsWrapper().getRespondentSolicitorEmail();
    }

    @JsonIgnore
    public boolean isApplicantSolicitorPopulated() {
        return StringUtils.isNotEmpty(nullToEmpty(getAppSolicitorEmail()));
    }

    /**
     * For consented cases ONLY.
     * Checks if the respondent solicitor email communication is enabled.
     * This is true if:
     * - The case is not a paper case
     * - The respondent is represented by a solicitor
     * - The respondent solicitor email is not null
     * - The respondent solicitor has agreed to receive emails
     * Needs enhancement for contested cases.
     * @return true if the respondent solicitor email communication is enabled, false otherwise.
     */
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

    /*
     * Respondent solicitor email is kept in a consistent field for contested and consented cases.
     */
    @JsonIgnore
    public String getRespondentSolicitorEmail() {
        return nullToEmpty(getContactDetailsWrapper().getRespondentSolicitorEmail());
    }

    /**
     * If caseAllocatedTo is present, then the fastTrackDecision value is not relevant.
     * This suits cases where caseAllocatedTo can be null.
     * caseAllocatedTo may be an older version of an attribute that has been replaced by fastTrackDecision.
     * @return true if the application can be considered a fast track application, false otherwise.
     */
    @JsonIgnore
    public boolean isFastTrackApplication() {
        return Optional.ofNullable(caseAllocatedTo).map(caseAllocatedTo ->
            caseAllocatedTo.isYes()).orElseGet(() -> fastTrackDecision.isYes());
    }

    @JsonIgnore
    public String getSelectedAllocatedCourt() {
        AllocatedRegionWrapper allocatedRegionWrapper = getRegionWrapper().getAllocatedRegionWrapper();
        CourtListWrapper courtList = allocatedRegionWrapper.getDefaultCourtListWrapper();

        if (allocatedRegionWrapper.getRegionList() != null) {
            return Map.of(
                Region.MIDLANDS, getMidlandsCourt(allocatedRegionWrapper.getMidlandsFrcList(), courtList),
                Region.LONDON, getLondonCourt(allocatedRegionWrapper.getLondonFrcList(), courtList),
                Region.NORTHEAST, getNorthEastCourt(allocatedRegionWrapper.getNorthEastFrcList(), courtList),
                Region.NORTHWEST, getNorthWestCourt(allocatedRegionWrapper.getNorthWestFrcList(), courtList),
                Region.SOUTHWEST, getSouthWestCourt(allocatedRegionWrapper.getSouthWestFrcList(), courtList),
                Region.SOUTHEAST, getSouthEastCourt(allocatedRegionWrapper.getSouthEastFrcList(), courtList),
                Region.WALES, getWalesCourt(allocatedRegionWrapper.getWalesFrcList(), courtList),
                Region.HIGHCOURT, getHighCourt(allocatedRegionWrapper.getHighCourtFrcList(), courtList)
            ).get(allocatedRegionWrapper.getRegionList());
        } else {
            return null;
        }
    }

    @JsonIgnore
    public String getSelectedHearingCourt() {
        Court court = getManageHearingsWrapper().getWorkingHearing().getHearingCourtSelection();
        return getSelectedCourtStringFromCourt(court);
    }

    @JsonIgnore
    public String getSelectedCourtStringFromCourt(Court court) {
        CourtListWrapper courtList = court.getDefaultCourtListWrapper();

        return Map.of(
            Region.MIDLANDS, getMidlandsCourt(court.getMidlandsList(), courtList),
            Region.LONDON, getCourtListIdOrDefault(court.getDefaultCourtListWrapper().getCfcCourtList()).getSelectedCourtId(),
            Region.NORTHEAST, getNorthEastCourt(court.getNorthEastList(), courtList),
            Region.NORTHWEST, getNorthWestCourt(court.getNorthWestList(), courtList),
            Region.SOUTHWEST, getSouthWestCourt(court.getSouthWestList(), courtList),
            Region.SOUTHEAST, getSouthEastCourt(court.getSouthEastList(), courtList),
            Region.WALES, getWalesCourt(court.getWalesList(), courtList),
            Region.HIGHCOURT, getHighCourt(court.getHcCourtList(), courtList)
        ).get(court.getRegion());
    }

    @JsonIgnore
    private String getMidlandsCourt(RegionMidlandsFrc frc, CourtListWrapper courtList) {
        if (frc != null) {
            return Map.of(
                RegionMidlandsFrc.NOTTINGHAM, getCourtListIdOrDefault(courtList.getNottinghamCourt()),
                RegionMidlandsFrc.BIRMINGHAM, getCourtListIdOrDefault(courtList.getBirminghamCourt())
            ).get(frc).getSelectedCourtId();
        } else {
            return StringUtils.EMPTY;
        }
    }

    @JsonIgnore
    private String getLondonCourt(RegionLondonFrc frc, CourtListWrapper courtList) {
        if (frc == null) {
            return StringUtils.EMPTY;
        }

        if (frc == RegionLondonFrc.LONDON && courtList.getCfcCourt() != null) {
            return getCourtListIdOrDefault(courtList.getCfcCourt()).getSelectedCourtId();
        }

        if (frc == RegionLondonFrc.LONDON_CONSENTED_COURT && courtList.getLondonCourt() != null) {
            return getCourtListIdOrDefault(courtList.getLondonCourt()).getSelectedCourtId();
        }

        return StringUtils.EMPTY;
    }

    @JsonIgnore
    private String getNorthEastCourt(RegionNorthEastFrc frc, CourtListWrapper courtList) {
        if (frc != null) {
            return Map.of(
                RegionNorthEastFrc.CLEAVELAND, getCourtListIdOrDefault(courtList.getClevelandCourt()),
                RegionNorthEastFrc.CLEVELAND, getCourtListIdOrDefault(courtList.getClevelandCourt()),
                RegionNorthEastFrc.HS_YORKSHIRE, getCourtListIdOrDefault(courtList.getHumberCourt()),
                RegionNorthEastFrc.NW_YORKSHIRE, getCourtListIdOrDefault(courtList.getNwYorkshireCourt())
            ).get(frc).getSelectedCourtId();
        } else {
            return StringUtils.EMPTY;
        }
    }

    @JsonIgnore
    private String getNorthWestCourt(RegionNorthWestFrc frc, CourtListWrapper courtList) {
        if (frc != null) {
            return Map.of(
                RegionNorthWestFrc.MANCHESTER, getCourtListIdOrDefault(courtList.getManchesterCourt()),
                RegionNorthWestFrc.LANCASHIRE, getCourtListIdOrDefault(courtList.getLancashireCourt()),
                RegionNorthWestFrc.LIVERPOOL, getCourtListIdOrDefault(courtList.getLiverpoolCourt())
            ).get(frc).getSelectedCourtId();
        } else {
            return StringUtils.EMPTY;
        }
    }

    @JsonIgnore
    private String getSouthWestCourt(RegionSouthWestFrc frc, CourtListWrapper courtList) {
        if (frc != null) {
            return Map.of(
                RegionSouthWestFrc.BRISTOL, getCourtListIdOrDefault(courtList.getBristolCourt()),
                RegionSouthWestFrc.DEVON, getCourtListIdOrDefault(courtList.getDevonCourt()),
                RegionSouthWestFrc.DORSET, getCourtListIdOrDefault(courtList.getDorsetCourt())
            ).get(frc).getSelectedCourtId();
        } else {
            return StringUtils.EMPTY;
        }
    }

    @JsonIgnore
    private String getSouthEastCourt(RegionSouthEastFrc frc, CourtListWrapper courtList) {
        if (frc != null) {
            return Map.of(
                RegionSouthEastFrc.BEDFORDSHIRE, getCourtListIdOrDefault(courtList.getBedfordshireCourt()),
                // For contested FRCs
                RegionSouthEastFrc.KENT_FRC, getCourtListIdOrDefault(courtList.getKentSurreyCourt()),
                // For consented FRCs
                RegionSouthEastFrc.KENT, getCourtListIdOrDefault(courtList.getKentSurreyCourt()),
                RegionSouthEastFrc.THAMES_VALLEY, getCourtListIdOrDefault(courtList.getThamesValleyCourt())
            ).get(frc).getSelectedCourtId();
        } else {
            return StringUtils.EMPTY;
        }
    }

    @JsonIgnore
    private String getWalesCourt(RegionWalesFrc frc, CourtListWrapper courtList) {
        if (frc != null) {
            return Map.of(
                RegionWalesFrc.NORTH_WALES, getCourtListIdOrDefault(courtList.getNorthWalesCourt()),
                RegionWalesFrc.NEWPORT, getCourtListIdOrDefault(courtList.getNewportCourt()),
                RegionWalesFrc.SWANSEA, getCourtListIdOrDefault(courtList.getSwanseaCourt())
            ).get(frc).getSelectedCourtId();
        } else {
            return StringUtils.EMPTY;
        }
    }

    @JsonIgnore
    private String getHighCourt(RegionHighCourtFrc frc, CourtListWrapper courtList) {
        if (frc != null) {
            return Map.of(
                RegionHighCourtFrc.HIGHCOURT, getCourtListIdOrDefault(courtList.getHighCourt())
            ).get(frc).getSelectedCourtId();
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
    public GenericInputFields getGenericInputFields() {
        if (genericInputFields == null) {
            this.genericInputFields = new GenericInputFields();
        }
        return genericInputFields;
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
    public MhMigrationWrapper getMhMigrationWrapper() {
        if (mhMigrationWrapper == null) {
            this.mhMigrationWrapper = new MhMigrationWrapper();
        }

        return mhMigrationWrapper;
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

    @JsonIgnore
    public ManageHearingsWrapper getManageHearingsWrapper() {
        if (manageHearingsWrapper == null) {
            this.manageHearingsWrapper = new ManageHearingsWrapper();
        }
        return manageHearingsWrapper;
    }

    @JsonIgnore
    public RefugeWrapper getRefugeWrapper() {
        if (refugeWrapper == null) {
            this.refugeWrapper = new RefugeWrapper();
        }
        return refugeWrapper;
    }

    @JsonIgnore
    public ListForHearingWrapper getListForHearingWrapper() {
        if (listForHearingWrapper == null) {
            listForHearingWrapper = new ListForHearingWrapper();
        }
        return listForHearingWrapper;
    }

    @JsonIgnore
    public SendOrderWrapper getSendOrderWrapper() {
        if (sendOrderWrapper == null) {
            this.sendOrderWrapper = new SendOrderWrapper();
        }
        return sendOrderWrapper;
    }

    @JsonIgnore
    public ExpressCaseWrapper getExpressCaseWrapper() {
        if (expressCaseWrapper == null) {
            this.expressCaseWrapper = new ExpressCaseWrapper();
        }
        return expressCaseWrapper;
    }

    @JsonIgnore
    public PaymentDetailsWrapper getPaymentDetailsWrapper() {
        if (paymentDetailsWrapper == null) {
            this.paymentDetailsWrapper = new PaymentDetailsWrapper();
        }
        return paymentDetailsWrapper;
    }

    @JsonIgnore
    public ManageCaseDocumentsWrapper getManageCaseDocumentsWrapper() {
        if (manageCaseDocumentsWrapper == null) {
            this.manageCaseDocumentsWrapper = new ManageCaseDocumentsWrapper();
        }
        return manageCaseDocumentsWrapper;
    }

    @JsonIgnore
    public CitizenDocumentWrapper getCitizenDocumentWrapper() {
        if (citizenDocumentWrapper == null) {
            this.citizenDocumentWrapper = new CitizenDocumentWrapper();
        }
        return citizenDocumentWrapper;
    }

    @JsonIgnore
    public EstimatedAssetsChecklistWrapper getEstimatedAssetsChecklistWrapper() {
        if (estimatedAssetsChecklistWrapper == null) {
            this.estimatedAssetsChecklistWrapper = new EstimatedAssetsChecklistWrapper();
        }
        return estimatedAssetsChecklistWrapper;
    }

    @JsonIgnore
    public Bin getBin() {
        if (bin == null) {
            this.bin = Bin.builder().build();
        }
        return bin;
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @JsonUnwrapped private CaseDataExtra caseDataExtra;
  // ==== end synthesised definition-only fields ====

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @JsonIgnore
  public Object getOrdersToSend() {
    return getSendOrderWrapper().getOrdersToSend();
  }
  @JsonIgnore
  public Object getHearingDocumentsCollection() {
    return getManageHearingsWrapper().getHearingDocumentsCollection();
  }
  @JsonIgnore
  public Object getCitizenApplicantDocument() {
    return getCitizenDocumentWrapper().getCitizenApplicantDocument();
  }
  @JsonIgnore
  public Object getCitizenRespondentDocument() {
    return getCitizenDocumentWrapper().getCitizenRespondentDocument();
  }
  @JsonIgnore
  public Object getCaseFileViewComponentLauncher() {
    return getCaseDataExtra().getCaseFileViewComponentLauncher();
  }
  @JsonIgnore
  public Object getHearingTabItems() {
    return getManageHearingsWrapper().getHearingTabItems();
  }
  @JsonIgnore
  public Object getVacatedOrAdjournedHearingTabItems() {
    return getManageHearingsWrapper().getVacatedOrAdjournedHearingTabItems();
  }
  @JsonIgnore
  public Object getApplicantHearingTabItems() {
    return getManageHearingsWrapper().getApplicantHearingTabItems();
  }
  @JsonIgnore
  public Object getApplicantVacOrAdjHearingTabItems() {
    return getManageHearingsWrapper().getApplicantVacOrAdjHearingTabItems();
  }
  @JsonIgnore
  public Object getRespondentHearingTabItems() {
    return getManageHearingsWrapper().getRespondentHearingTabItems();
  }
  @JsonIgnore
  public Object getRespondentVacOrAdjHearingTabItems() {
    return getManageHearingsWrapper().getRespondentVacOrAdjHearingTabItems();
  }
  @JsonIgnore
  public Object getInt1HearingTabItems() {
    return getManageHearingsWrapper().getInt1HearingTabItems();
  }
  @JsonIgnore
  public Object getInt1VacOrAdjHearingTabItems() {
    return getManageHearingsWrapper().getInt1VacOrAdjHearingTabItems();
  }
  @JsonIgnore
  public Object getInt2HearingTabItems() {
    return getManageHearingsWrapper().getInt2HearingTabItems();
  }
  @JsonIgnore
  public Object getInt2VacOrAdjHearingTabItems() {
    return getManageHearingsWrapper().getInt2VacOrAdjHearingTabItems();
  }
  @JsonIgnore
  public Object getInt3HearingTabItems() {
    return getManageHearingsWrapper().getInt3HearingTabItems();
  }
  @JsonIgnore
  public Object getInt3VacOrAdjHearingTabItems() {
    return getManageHearingsWrapper().getInt3VacOrAdjHearingTabItems();
  }
  @JsonIgnore
  public Object getInt4HearingTabItems() {
    return getManageHearingsWrapper().getInt4HearingTabItems();
  }
  @JsonIgnore
  public Object getInt4VacOrAdjHearingTabItems() {
    return getManageHearingsWrapper().getInt4VacOrAdjHearingTabItems();
  }
  @JsonIgnore
  public Object getGeneralEmailCollection() {
    return getGeneralEmailWrapper().getGeneralEmailCollection();
  }
  @JsonIgnore
  public Object getFlagLauncher() {
    return getCaseDataExtra().getFlagLauncher();
  }
  // ==== end synthesised definition-only fields ====
}
