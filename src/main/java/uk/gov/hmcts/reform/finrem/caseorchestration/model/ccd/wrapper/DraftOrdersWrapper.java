package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.TemporaryField;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.JudgeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UuidCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.FinalisedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.ExtraReportFieldsInput;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.HearingInstruction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeApproval;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.RefusedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.suggested.SuggestedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadAgreedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadSuggestedDraftOrder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.isJudgeReviewable;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesGpeopvAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCruPlus1RolesChccdnAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesMhrsdiAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedySolicitorRAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.RESPBARRISTERRESPSOLICITORCudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.APPBARRISTERAPPSOLICITORCudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedySolicitorCrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.INTVRBARRISTER1CrudPlus7RolesQwwlocAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyJudiciaryCrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyJudiciaryCruAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesPfugmjAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCuPlus1RolesOsjhrjAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FRTypeOfDraftOrderList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FRFlRefusalOrderJudgeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FRCtDirectionOrderCollection;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DraftOrdersWrapper implements HasCaseDocument {

    @CCD(
            label = "What kind of draft order do you need to upload?",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "FR_typeOfDraftOrderList",
            typeParameterClass = FRTypeOfDraftOrderList.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesGpeopvAccess.class}
    )
    private String typeOfDraftOrder;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesGpeopvAccess.class}
    )
    private YesOrNo showUploadPartyQuestion;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminCruPlus1RolesChccdnAccess.class}
    )
    private String consentApplicationGuidanceText;

    @CCD(
            label = "Upload draft orders",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesGpeopvAccess.class}
    )
    @TemporaryField
    private UploadSuggestedDraftOrder uploadSuggestedDraftOrder;

    @CCD(
            label = "Upload agreed draft orders",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesGpeopvAccess.class}
    )
    @TemporaryField
    private UploadAgreedDraftOrder uploadAgreedDraftOrder;

    @CCD(
            label = "Draft Orders",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_draftOrdersReview",
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesMhrsdiAccess.class}
    )
    @JsonProperty("draftOrdersReviewCollection")
    private List<DraftOrdersReviewCollection> draftOrdersReviewCollection;
    @CCD(
            label = "Refused Orders",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_refusedOrderOrPsa",
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesMhrsdiAccess.class, CaseworkerDivorceFinancialremedySolicitorRAccess.class}
    )
    @JsonProperty("refusedOrdersCollection")
    private List<RefusedOrderCollection> refusedOrdersCollection;
    @CCD(
            label = "Agreed draft orders following a hearing",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_agreedDraftOrder",
            access = {RESPBARRISTERRESPSOLICITORCudAccess.class, APPBARRISTERAPPSOLICITORCudAccess.class, CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesMhrsdiAccess.class}
    )
    @JsonProperty("agreedDraftOrderCollection")
    private List<AgreedDraftOrderCollection> agreedDraftOrderCollection;
    @CCD(
            label = "Suggested draft orders prior to a hearing",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_suggestedDraftOrder",
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesMhrsdiAccess.class, CaseworkerDivorceFinancialremedySolicitorCrudAccess.class}
    )
    @JsonProperty("suggestedDraftOrderCollection")
    private List<SuggestedDraftOrderCollection> suggestedDraftOrderCollection;
    @CCD(
            label = "Agreed draft orders following a hearing",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_agreedDraftOrder",
            access = {INTVRBARRISTER1CrudPlus7RolesQwwlocAccess.class}
    )
    @JsonProperty("intvAgreedDraftOrderCollection")
    private List<AgreedDraftOrderCollection> intvAgreedDraftOrderCollection;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceFinancialremedyJudiciaryCrudAccess.class}
    )
    private YesOrNo showWarningMessageToJudge;

    @CCD(label = " ", searchable = false, access = {CaseworkerDivorceFinancialremedyJudiciaryCrudAccess.class})
    @JsonProperty("judgeApproval1")
    private JudgeApproval judgeApproval1;

    @CCD(label = " ", searchable = false, access = {CaseworkerDivorceFinancialremedyJudiciaryCrudAccess.class})
    @JsonProperty("judgeApproval2")
    private JudgeApproval judgeApproval2;

    @CCD(label = " ", searchable = false, access = {CaseworkerDivorceFinancialremedyJudiciaryCrudAccess.class})
    @JsonProperty("judgeApproval3")
    private JudgeApproval judgeApproval3;

    @CCD(label = " ", searchable = false, access = {CaseworkerDivorceFinancialremedyJudiciaryCrudAccess.class})
    @JsonProperty("judgeApproval4")
    private JudgeApproval judgeApproval4;

    @CCD(label = " ", searchable = false, access = {CaseworkerDivorceFinancialremedyJudiciaryCrudAccess.class})
    @JsonProperty("judgeApproval5")
    private JudgeApproval judgeApproval5;

    @CCD(label = " ", searchable = false, access = {CaseworkerDivorceFinancialremedyJudiciaryCruAccess.class})
    @JsonProperty("hearingInstruction")
    private HearingInstruction hearingInstruction;

    @CCD(
            label = "The confirmation body for the approve orders event",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyJudiciaryCrudAccess.class}
    )
    @JsonProperty("approveOrdersConfirmationBody")
    private String approveOrdersConfirmationBody;
  
    @CCD(label = " ", searchable = false, access = {CaseworkerDivorceFinancialremedyJudiciaryCrudAccess.class})
    @JsonProperty("extraReportFieldsInput")
    private ExtraReportFieldsInput extraReportFieldsInput;

    @CCD(label = "Generated order reason", searchable = false)
    private String generatedOrderReason;

    @CCD(label = "Generated order date refused", searchable = false)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime generatedOrderRefusedDate;

    @CCD(
            label = "Generated order judge type",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_fl_RefusalOrderJudgeType",
            typeParameterClass = FRFlRefusalOrderJudgeType.class
    )
    private JudgeType generatedOrderJudgeType;

    @CCD(label = "Generated order judge name", searchable = false, typeOverride = FieldType.DateTime)
    private String generatedOrderJudgeName;

    @CCD(
            label = "Refusal order IDs to be sent",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Text",
            access = {CaseworkerDivorceFinancialremedyJudiciaryCrudAccess.class}
    )
    private List<UuidCollection> refusalOrderIdsToBeSent;

    @CCD(
            label = "Unprocessed Approved Orders",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_ct_directionOrderCollection",
            typeParameterClass = FRCtDirectionOrderCollection.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesPfugmjAccess.class}
    )
    private List<DirectionOrderCollection> unprocessedApprovedDocuments;

    @CCD(
            label = "isLegacyApprovedOrderPresent",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesPfugmjAccess.class}
    )
    private YesOrNo isLegacyApprovedOrderPresent;

    @CCD(
            label = "isUnprocessedApprovedDocumentPresent",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesPfugmjAccess.class}
    )
    private YesOrNo isUnprocessedApprovedDocumentPresent;

    @CCD(
            label = "isUnreviewedDocumentPresent",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceFinancialremedyCourtadminCuPlus1RolesOsjhrjAccess.class}
    )
    private YesOrNo isUnreviewedDocumentPresent;

    @CCD(
            label = "Finalised Orders",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_finalisedOrder",
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesMhrsdiAccess.class}
    )
    @JsonProperty("finalisedOrdersCollection")
    private List<FinalisedOrderCollection> finalisedOrdersCollection;

    public void appendAgreedDraftOrderCollection(List<AgreedDraftOrderCollection> newAgreedDraftOrderCollection) {
        if (agreedDraftOrderCollection == null) {
            agreedDraftOrderCollection = new ArrayList<>();
        }
        agreedDraftOrderCollection.addAll(newAgreedDraftOrderCollection);
    }

    public void appendIntvAgreedDraftOrderCollection(List<AgreedDraftOrderCollection> newAgreedDraftOrderCollection) {
        if (intvAgreedDraftOrderCollection == null) {
            intvAgreedDraftOrderCollection = new ArrayList<>();
        }
        intvAgreedDraftOrderCollection.addAll(newAgreedDraftOrderCollection);
    }

    public void appendDraftOrdersReviewCollection(List<DraftOrdersReviewCollection> newDraftOrdersReviewCollection) {
        if (draftOrdersReviewCollection == null) {
            draftOrdersReviewCollection = new ArrayList<>();
        }
        draftOrdersReviewCollection.addAll(newDraftOrdersReviewCollection);
    }

    @JsonIgnore
    public List<DraftOrdersReviewCollection> getOutstandingDraftOrdersReviewCollection() {
        Stream<DraftOrdersReviewCollection> draftOrdersStream = ofNullable(draftOrdersReviewCollection)
            .orElse(List.of())
            .stream()
            .filter(a -> a != null && a.getValue() != null)
            .filter(a -> a.getValue().getDraftOrderDocReviewCollection().stream()
                .anyMatch(draftOrderDoc -> isJudgeReviewable(draftOrderDoc.getValue().getOrderStatus()))
                || a.getValue().getPsaDocReviewCollection().stream()
                    .anyMatch(psa -> isJudgeReviewable(psa.getValue().getOrderStatus())));
        return draftOrdersStream.toList();
    }

}
