package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentInContestedApprovedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentNatureOfApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionDetailsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NatureApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OtherDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionProvider;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UnapprovedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.VariationDocumentTypeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.time.LocalDate;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.DefaultAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesEnaquoAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedySolicitorCudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesGpeopvAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyJudiciaryCrAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesYcfimvAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedySolicitorRAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceSystemupdateRuAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedySolicitorRPlus1RolesQrvrunAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyJudiciaryUAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesZbqgyjAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedySolicitorCrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FRCtUploadOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FRMsNatureApplication;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConsentOrderWrapper implements HasCaseDocument {
    private DraftDirectionOrder latestDraftDirectionOrder;
    private List<DraftDirectionDetailsCollection> draftDirectionDetailsCollection;
    private List<DraftDirectionDetailsCollection> draftDirectionDetailsCollectionRO;
    @CCD(
            label = "The application is for:",
            hint = "The applicant is applying for an order by consent in terms of written agreement (a consent order). Within the draft consent order, the Applicant is applying to Court for;",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "FR_ms_natureApplication",
            typeParameterClass = FRMsNatureApplication.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class}
    )
    private List<NatureApplication> consentNatureOfApplicationChecklist;
    @CCD(
            label = "Address details",
            hint = "If the application includes an application for a Property Adjustment Order in relation to land, please provide the address(es) of the property or properties, if applicable",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class}
    )
    private String consentNatureOfApplicationAddress;
    @CCD(
            label = "Mortgage details",
            hint = "If the application includes an application for a Property Adjustment Order in relation to land, please provide the name(s) and address(es) of any mortgagee(s), if applicable",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class}
    )
    private String consentNatureOfApplicationMortgage;
    @CCD(
            label = "Does the application contain any application for periodical payments, or secured periodical payments for children?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class}
    )
    private YesOrNo consentOrderForChildrenQuestion1;
    @CCD(
            label = "Is there a written agreement?",
            hint = "If the application contains an application for periodical payments or secured periodical payments for children, has a written agreement made about maintenance for the benefit of children?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class}
    )
    private YesOrNo consentNatureOfApplication5;
    @CCD(
            label = "Select what the payments are for:",
            hint = "There is no agreement, but the applicant is applying for payments;",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "FR_ConsentNatureOfApplication6",
            access = {DefaultAccess.class}
    )
    private List<ConsentNatureOfApplication> consentNatureOfApplication6;
    @CCD(
            label = "Other – Please give details",
            hint = "If Other, please provide details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class}
    )
    private String consentNatureOfApplication7;
    @CCD(
            label = "Are you uploading a joint D81?",
            hint = "You can either submit one joint D81 form for both parties or one for each applicant and respondent",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesEnaquoAccess.class, CaseworkerDivorceFinancialremedySolicitorCudAccess.class}
    )
    private YesOrNo consentD81Question;
    @CCD(
            label = "Form D81 Joint Document",
            hint = "Joint D81 form, signed by both parties",
            categoryID = "applicationsConsentOrderToFinaliseProceedings",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class}
    )
    private CaseDocument consentD81Joint;
    @CCD(
            label = "Form D81 Applicant Document",
            hint = "D81 form, signed by Applicant",
            categoryID = "applicationsConsentOrderToFinaliseProceedings",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class}
    )
    private CaseDocument consentD81Applicant;
    @CCD(
            label = "Form D81 Respondent Document",
            hint = "D81 form, signed by Respondent",
            categoryID = "applicationsConsentOrderToFinaliseProceedings",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class}
    )
    private CaseDocument consentD81Respondent;
    @CCD(
            label = "Other Documents",
            hint = "Upload other documentation related to your application",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_DocumentType",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class}
    )
    private List<OtherDocumentCollection> consentOtherCollection;
    @CCD(
            label = "Court",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesGpeopvAccess.class, CaseworkerDivorceFinancialremedyJudiciaryCrAccess.class}
    )
    @JsonProperty("consentOrderFRCName")
    private String consentOrderFrcName;
    @CCD(
            label = "Address",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesGpeopvAccess.class, CaseworkerDivorceFinancialremedyJudiciaryCrAccess.class}
    )
    @JsonProperty("consentOrderFRCAddress")
    private String consentOrderFrcAddress;
    @CCD(
            label = "Email",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesGpeopvAccess.class, CaseworkerDivorceFinancialremedyJudiciaryCrAccess.class}
    )
    @JsonProperty("consentOrderFRCEmail")
    private String consentOrderFrcEmail;
    @CCD(
            label = "Phone",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesGpeopvAccess.class, CaseworkerDivorceFinancialremedyJudiciaryCrAccess.class}
    )
    @JsonProperty("consentOrderFRCPhone")
    private String consentOrderFrcPhone;
    @CCD(
            label = "Subject to Decree Absolute/Final Order?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesYcfimvAccess.class, CaseworkerDivorceFinancialremedySolicitorRAccess.class}
    )
    private YesOrNo consentSubjectToDecreeAbsoluteValue;
    @CCD(
            label = "Does a copy of this order need to be served to the pension provider?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesYcfimvAccess.class, CaseworkerDivorceFinancialremedySolicitorRAccess.class}
    )
    private YesOrNo consentServePensionProvider;
    @CCD(
            label = "Who is responsible for sending a copy of the order to the pension provider?",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesYcfimvAccess.class, CaseworkerDivorceFinancialremedySolicitorRAccess.class}
    )
    private PensionProvider consentServePensionProviderResponsibility;
    @CCD(
            label = "Please Specify",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesYcfimvAccess.class, CaseworkerDivorceFinancialremedySolicitorRAccess.class}
    )
    private String consentServePensionProviderOther;
    @CCD(
            label = "Select Judge",
            hint = "Please select the appropriate judge",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_fl_GeneralOrderJudgeType",
            access = {CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesYcfimvAccess.class, CaseworkerDivorceFinancialremedySolicitorRAccess.class}
    )
    private String consentSelectJudge;
    @CCD(
            label = "Name of Judge",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesYcfimvAccess.class, CaseworkerDivorceFinancialremedySolicitorRAccess.class}
    )
    private String consentJudgeName;
    @CCD(
            label = "Refused Order Collection",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_consentOrder",
            access = {CaseworkerDivorceFinancialremedySolicitorRAccess.class, CaseworkerDivorceSystemupdateRuAccess.class}
    )
    private List<ConsentOrderCollection> consentedNotApprovedOrders;
    @CCD(
            label = "Date of order",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesYcfimvAccess.class, CaseworkerDivorceFinancialremedySolicitorRAccess.class}
    )
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate consentDateOfOrder;
    @CCD(
            label = "Additional comments",
            hint = "Please add any additional comments for  court admin (this comments will not be accesible by applicant's solicitor)",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesYcfimvAccess.class, CaseworkerDivorceFinancialremedySolicitorRAccess.class}
    )
    private String consentAdditionalComments;
    @CCD(
            label = "Online Form A",
            hint = "Online Form A",
            categoryID = "applicationsConsentOrderToFinaliseProceedings",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerDivorceFinancialremedySolicitorRPlus1RolesQrvrunAccess.class, CaseworkerDivorceFinancialremedyJudiciaryUAccess.class}
    )
    private CaseDocument consentMiniFormA;
    @CCD(
            label = "Upload Consented Order",
            categoryID = "applicationsConsentOrderToFinaliseProceedings",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesZbqgyjAccess.class, CaseworkerDivorceFinancialremedySolicitorCrudAccess.class}
    )
    private CaseDocument uploadConsentedOrder;
    @CCD(
            label = "Consent Order Documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_consentOrder",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class, CaseworkerDivorceFinancialremedySolicitorRAccess.class}
    )
    @JsonProperty("Contested_ConsentedApprovedOrders")
    private List<ConsentOrderCollection> contestedConsentedApprovedOrders;
    @CCD(
            label = "Upload Consent Order ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_ct_uploadOrder",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    private List<FRCtUploadOrder> uploadConsentOrder;
    @CCD(ignore = true)
    private String consentVariationOrderLabelC;
    @CCD(ignore = true)
    private String consentVariationOrderLabelL;
    @CCD(ignore = true)
    private String otherDocLabel;
    @CCD(ignore = true)
    private List<VariationDocumentTypeCollection> otherVariationCollection;
    @CCD(ignore = true)
    private CaseDocument uploadApprovedConsentOrder;
    @CCD(
            label = "Consent Order Documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_roleConsentOrder",
            access = {CaseworkerDivorceFinancialremedyCourtadminCudAccess.class}
    )
    private List<ConsentInContestedApprovedOrderCollection> appConsentApprovedOrders;
    @CCD(
            label = "Consent Order Documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_roleConsentOrder",
            access = {CaseworkerDivorceFinancialremedyCourtadminCudAccess.class}
    )
    private List<ConsentInContestedApprovedOrderCollection> respConsentApprovedOrders;
    @CCD(
            label = "Consent Order Documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_roleConsentOrder",
            access = {CaseworkerDivorceFinancialremedyCourtadminCudAccess.class}
    )
    private List<ConsentInContestedApprovedOrderCollection> intv1ConsentApprovedOrders;
    @CCD(
            label = "Consent Order Documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_roleConsentOrder",
            access = {CaseworkerDivorceFinancialremedyCourtadminCudAccess.class}
    )
    private List<ConsentInContestedApprovedOrderCollection> intv2ConsentApprovedOrders;
    @CCD(
            label = "Consent Order Documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_roleConsentOrder",
            access = {CaseworkerDivorceFinancialremedyCourtadminCudAccess.class}
    )
    private List<ConsentInContestedApprovedOrderCollection> intv3ConsentApprovedOrders;
    @CCD(
            label = "Consent Order Documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_roleConsentOrder",
            access = {CaseworkerDivorceFinancialremedyCourtadminCudAccess.class}
    )
    private List<ConsentInContestedApprovedOrderCollection> intv4ConsentApprovedOrders;
    @CCD(
            label = "Refused Order Collection",
            hint = "Refused Order Collection",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_unapprovedOrderCollection",
            access = {CaseworkerDivorceFinancialremedyCourtadminCudAccess.class}
    )
    private List<UnapprovedOrderCollection> appRefusedOrderCollection;
    @CCD(
            label = "Refused Order Collection",
            hint = "Refused Order Collection",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_unapprovedOrderCollection",
            access = {CaseworkerDivorceFinancialremedyCourtadminCudAccess.class}
    )
    private List<UnapprovedOrderCollection> respRefusedOrderCollection;
    @CCD(
            label = "Refused Order Collection",
            hint = "Refused Order Collection",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_unapprovedOrderCollection",
            access = {CaseworkerDivorceFinancialremedyCourtadminCudAccess.class}
    )
    private List<UnapprovedOrderCollection> intv1RefusedOrderCollection;
    @CCD(
            label = "Refused Order Collection",
            hint = "Refused Order Collection",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_unapprovedOrderCollection",
            access = {CaseworkerDivorceFinancialremedyCourtadminCudAccess.class}
    )
    private List<UnapprovedOrderCollection> intv2RefusedOrderCollection;
    @CCD(
            label = "Refused Order Collection",
            hint = "Refused Order Collection",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_unapprovedOrderCollection",
            access = {CaseworkerDivorceFinancialremedyCourtadminCudAccess.class}
    )
    private List<UnapprovedOrderCollection> intv3RefusedOrderCollection;
    @CCD(
            label = "Refused Order Collection",
            hint = "Refused Order Collection",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_unapprovedOrderCollection",
            access = {CaseworkerDivorceFinancialremedyCourtadminCudAccess.class}
    )
    private List<UnapprovedOrderCollection> intv4RefusedOrderCollection;
    @CCD(
            label = "Latest Divorce Order Document",
            hint = "A copy of the latest divorce order is required and must be uploaded for this consent order application. This can either be Decree Absolute/Final Order or Decree Nisi/Conditional Order.",
            categoryID = "applicationsConsentOrderToFinaliseProceedings",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class}
    )
    private CaseDocument latestDivorceOrderUpload;
}
