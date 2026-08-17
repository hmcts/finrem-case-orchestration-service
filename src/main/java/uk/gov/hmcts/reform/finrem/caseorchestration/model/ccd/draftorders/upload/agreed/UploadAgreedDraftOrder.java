package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.OrderFiledBy;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FRFlAssignToJudge;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FRUploadOrdersOrPsas;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_uploadAgreedDraftOrder", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UploadAgreedDraftOrder {
    @CCD(
            label = "Confirm the uploaded documents are for the case",
            searchable = false,
            typeOverride = FieldType.DynamicMultiSelectList
    )
    @JsonProperty("confirmUploadedDocuments")
    private DynamicMultiSelectList confirmUploadedDocuments;

    @CCD(label = "Which hearing was this?", searchable = false, typeOverride = FieldType.DynamicList)
    @JsonProperty("hearingDetails")
    private DynamicList hearingDetails;

    @CCD(label = "Do you know who was the judge at this hearing?", searchable = false, typeOverride = FieldType.YesOrNo)
    @JsonProperty("judgeKnownAtHearing")
    private YesOrNo judgeKnownAtHearing;

    @CCD(
            label = "Who is the judge that heard this hearing?",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_fl_AssignToJudge",
            typeParameterClass = FRFlAssignToJudge.class
    )
    @JsonProperty("judge")
    private String judge;

    @CCD(
            label = "Who are you uploading this on behalf of?",
            searchable = false,
            typeOverride = FieldType.DynamicRadioList
    )
    @JsonProperty("uploadParty")
    private DynamicRadioList uploadParty;

    @CCD(
            label = "What are you uploading?",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "FR_UploadOrdersOrPsas",
            typeParameterClass = FRUploadOrdersOrPsas.class
    )
    @JsonProperty("uploadOrdersOrPsas")
    private List<String> uploadOrdersOrPsas;

    @CCD(
            label = "Upload draft order",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_AgreedDraftOrders"
    )
    @JsonProperty("agreedDraftOrderCollection")
    private List<UploadAgreedDraftOrderCollection> uploadAgreedDraftOrderCollection;

    @CCD(
            label = "Pension Sharing Annexes",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_AgreedPensionSharingAnnexes"
    )
    @JsonProperty("agreedPsaCollection")
    private List<AgreedPensionSharingAnnexCollection> agreedPsaCollection;

    @JsonIgnore
    private OrderFiledBy orderFiledBy;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "The hearing judge's name should be added to all uploaded draft orders.",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String judgeNameGuidance;
  @CCD(label = "This may delay the judge approving your order", searchable = false, typeOverride = FieldType.Label)
  private String judgeUnknownWarning;
  // ==== end synthesised definition-only fields ====
}
