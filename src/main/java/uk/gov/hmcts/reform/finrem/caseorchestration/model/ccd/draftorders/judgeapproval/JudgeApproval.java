package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.time.LocalDate;
import java.util.List;

import static java.util.Optional.ofNullable;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FRFlAssignToJudge;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_judgeApproval", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JudgeApproval {

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_judgeApprovalDocType"
    )
    private JudgeApprovalDocType docType;

    @CCD(label = " ", searchable = false)
    private String title;

    @CCD(label = " ", searchable = false)
    private String inlineDocType;

    @CCD(label = " ", searchable = false)
    private String hearingInfo;

    @CCD(label = "Hearing Date", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate hearingDate;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_fl_AssignToJudge",
            typeParameterClass = FRFlAssignToJudge.class
    )
    private String hearingJudge;

    @CCD(label = "Has Attachment?", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo hasAttachment;

    @CCD(label = " ", searchable = false, typeOverride = FieldType.Document)
    @JsonProperty("document")
    private CaseDocument document;

    @CCD(
            label = "You must upload Microsoft Word documents. Document names should clearly reflect the party name, the type of hearing and the date of the hearing. For example “JonesFDA11Jul24”",
            regex = ".doc,.docx",
            searchable = false,
            typeOverride = FieldType.Document
    )
    @JsonProperty("amendedDocument")
    private CaseDocument amendedDocument;

    @CCD(label = "Is this order ready to be sealed and issued?", searchable = false)
    @JsonProperty("judgeDecision")
    private JudgeDecision judgeDecision;

    @CCD(label = " ", searchable = false, typeOverride = FieldType.Collection, typeParameterOverride = "Document")
    @JsonProperty("attachments")
    private List<DocumentCollectionItem> attachments;

    @CCD(label = "Is this a final order?", searchable = false, typeOverride = FieldType.DynamicMultiSelectList)
    @JsonProperty("isFinalOrder")
    private DynamicMultiSelectList isFinalOrder;

    @CCD(label = "Court order date", searchable = false)
    @JsonProperty("courtOrderDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate courtOrderDate;

    @CCD(
            label = "Provide your feedback. Your comments will go directly to the legal representative:",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("changesRequestedByJudge")
    private String changesRequestedByJudge;

    @JsonIgnore
    private SortKey sortKey;

    public YesOrNo getHasAttachment() {
        return YesOrNo.forValue(!ofNullable(attachments).orElse(List.of()).isEmpty());
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
  private String enhancedTitle;
  @CCD(label = "*Attachments*", searchable = false, typeOverride = FieldType.Label)
  private String attachmentsLabel;
  // ==== end synthesised definition-only fields ====
}
