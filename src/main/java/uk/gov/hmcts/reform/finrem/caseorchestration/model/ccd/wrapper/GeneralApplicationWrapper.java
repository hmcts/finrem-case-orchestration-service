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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.TemporaryField;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApplicantAndRespondentEvidenceParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationOutcome;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.JudgeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.time.LocalDate;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.DefaultAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.APPBARRISTERAPPSOLICITORRESPBARRISTERRESPSOLICITORCrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesMhrsdiAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedySolicitorCrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedySolicitorRAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesIwxsitAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesZbqgyjAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesEnaquoAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedySolicitorCudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedySuperuserCrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.RESPBARRISTERRESPSOLICITORCudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.APPBARRISTERAPPSOLICITORCudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FRCtGeneralApplicationCollection;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeneralApplicationWrapper implements HasCaseDocument {
    @CCD(
            label = "Is a Hearing required?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    private YesOrNo generalApplicationDirectionsHearingRequired;
    @CCD(
            label = "Application received from",
            searchable = false,
            typeOverride = FieldType.DynamicRadioList,
            access = {DefaultAccess.class}
    )
    private String generalApplicationReceivedFrom;
    @CCD(
            label = "Application received from",
            searchable = false,
            access = {APPBARRISTERAPPSOLICITORRESPBARRISTERRESPSOLICITORCrudAccess.class}
    )
    private ApplicantAndRespondentEvidenceParty appRespGeneralApplicationReceivedFrom;
    @CCD(label = "  ", searchable = false, access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class})
    private String generalApplicationDirectionsHearingTime;
    @CCD(label = "  ", searchable = false, access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class})
    private String generalApplicationDirectionsHearingTimeEstimate;
    @CCD(
            label = "  ",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    private String generalApplicationDirectionsAdditionalInformation;
    @CCD(
            label = "Recitals",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    private String generalApplicationDirectionsRecitals;
    @CCD(
            label = "Court order date",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate generalApplicationDirectionsCourtOrderDate;
    @CCD(
            label = "Directions from the Judge",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    private String generalApplicationDirectionsTextFromJudge;
    @CCD(
            label = "Preview of General Applications Directions",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    @TemporaryField
    private CaseDocument generalApplicationDirectionsPreview;
    @CCD(
            label = "Hearing / no hearing document",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesMhrsdiAccess.class, CaseworkerDivorceFinancialremedySolicitorCrudAccess.class}
    )
    @Deprecated
    private CaseDocument generalApplicationDirectionsDocument;
    @CCD(
            label = "Intervener General Applications",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "generalApplicationCollection",
            access = {CaseworkerDivorceFinancialremedySolicitorRAccess.class}
    )
    private List<GeneralApplicationsCollection> generalApplicationIntvrOrders;
    @CCD(
            label = "General application not approved reason",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesIwxsitAccess.class, CaseworkerDivorceFinancialremedySolicitorRAccess.class}
    )
    private String generalApplicationNotApprovedReason;
    @CCD(label = "  ", searchable = false, access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class})
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate generalApplicationDirectionsHearingDate;
    @CCD(
            label = "Select judge",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_fl_GeneralOrderJudgeType",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    private JudgeType generalApplicationDirectionsJudgeType;
    @CCD(
            label = "Name of Judge",
            hint = "Surname of judge",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    private String generalApplicationDirectionsJudgeName;
    @CCD(
            label = "General Application Collection",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_ct_generalApplicationCollection",
            typeParameterClass = FRCtGeneralApplicationCollection.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesZbqgyjAccess.class, CaseworkerDivorceFinancialremedySolicitorCrudAccess.class}
    )
    @JsonProperty("generalApplicationCollection")
    private List<GeneralApplicationCollection> generalApplicationDocumentCollection;
    @CCD(label = "Application created by", searchable = false, access = {DefaultAccess.class})
    private String generalApplicationCreatedBy;
    @CCD(
            label = "Is a hearing required?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class}
    )
    private YesOrNo generalApplicationHearingRequired;
    @CCD(label = "Time estimate", searchable = false, access = {DefaultAccess.class})
    private String generalApplicationTimeEstimate;
    @CCD(label = "Special measures", searchable = false, access = {DefaultAccess.class})
    private String generalApplicationSpecialMeasures;
    @CCD(
            label = "Upload General Application",
            hint = "Please upload a copy of the application as a word or PDF document (word documents will be converted to PDF after submission).",
            categoryID = "administrativeDocumentsTransitional",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {DefaultAccess.class}
    )
    private CaseDocument generalApplicationDocument;
    @CCD(
            label = "General Application",
            categoryID = "administrativeDocumentsTransitional",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerDivorceFinancialremedyCourtadminCudAccess.class, CaseworkerDivorceFinancialremedySolicitorCrudAccess.class}
    )
    private CaseDocument generalApplicationLatestDocument;
    @CCD(
            label = "Upload Draft Order",
            hint = "Please upload a copy of the draft order as a word or PDF document (word documents will be converted to PDF after submission)",
            categoryID = "administrativeDocumentsTransitional",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {DefaultAccess.class}
    )
    private CaseDocument generalApplicationDraftOrder;
    @CCD(
            label = "General Application Date",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminCudAccess.class, CaseworkerDivorceFinancialremedySolicitorCrudAccess.class}
    )
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate generalApplicationLatestDocumentDate;
    @CCD(label = "General Application Pre State", searchable = false, access = {DefaultAccess.class})
    private String generalApplicationPreState;
    @CCD(
            label = "Judge's email address",
            searchable = false,
            typeOverride = FieldType.Email,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    private String generalApplicationReferToJudgeEmail;
    @CCD(
            label = "Please Specify",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesEnaquoAccess.class, CaseworkerDivorceFinancialremedySolicitorRAccess.class}
    )
    private String generalApplicationOutcomeOther;
    @CCD(
            label = "General Application Outcome",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesEnaquoAccess.class, CaseworkerDivorceFinancialremedySolicitorRAccess.class}
    )
    private GeneralApplicationOutcome generalApplicationOutcome;
    @CCD(
            label = "General Applications",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "generalApplicationCollection",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesEnaquoAccess.class, CaseworkerDivorceFinancialremedySolicitorCudAccess.class, CaseworkerDivorceFinancialremedySuperuserCrudAccess.class}
    )
    private List<GeneralApplicationsCollection> generalApplications;
    @CCD(
            label = "General Applications",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "generalApplicationCollection",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesEnaquoAccess.class, RESPBARRISTERRESPSOLICITORCudAccess.class, APPBARRISTERAPPSOLICITORCudAccess.class}
    )
    private List<GeneralApplicationsCollection> appRespGeneralApplications;
    @CCD(
            label = "General Applications",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "generalApplicationCollection",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class}
    )
    private List<GeneralApplicationsCollection> intervener1GeneralApplications;
    @CCD(
            label = "General Applications",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "generalApplicationCollection",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class}
    )
    private List<GeneralApplicationsCollection> intervener2GeneralApplications;
    @CCD(
            label = "General Applications",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "generalApplicationCollection",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class}
    )
    private List<GeneralApplicationsCollection> intervener3GeneralApplications;
    @CCD(
            label = "General Applications",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "generalApplicationCollection",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class, CaseworkerDivorceFinancialremedySuperuserCrudAccess.class}
    )
    private List<GeneralApplicationsCollection> intervener4GeneralApplications;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerDivorceFinancialremedySuperuserCrudAccess.class}
    )
    private String generalApplicationTracking;
    @CCD(
            label = "Rejection Reason",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class}
    )
    private String generalApplicationRejectReason;
    @CCD(
            label = "Please select from general application",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {DefaultAccess.class}
    )
    private DynamicList generalApplicationList;
    @CCD(
            label = "Please select from general application",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    private DynamicList generalApplicationReferList;
    @CCD(
            label = "General application refer detail",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    private String generalApplicationReferDetail;
    @CCD(
            label = "Please select from general application",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesEnaquoAccess.class, CaseworkerDivorceFinancialremedySolicitorRAccess.class}
    )
    private DynamicList generalApplicationOutcomeList;
    @CCD(
            label = "Please select from general application",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    private DynamicList generalApplicationDirectionsList;
}
