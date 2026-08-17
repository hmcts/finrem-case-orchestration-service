package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

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

import java.time.LocalDate;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_consentOrderNotApproved", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderRefusalHolder implements HasCaseDocument {
    @CCD(
            label = "AND AFTER",
            hint = "The order will already contain the wording ‘AFTER reading the consent order signed by both parties’. Please complete the box below if you want to add an addition line, this will appear on the order as ‘AND AFTER [insert text here]’.",
            typeOverride = FieldType.TextArea
    )
    private String orderRefusalAfterText;
    @CCD(
            label = "Reason for Refusal",
            hint = "Please tick all boxes that apply",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "FR_ms_OrderRefusal",
            typeParameterClass = FRMsOrderRefusal.class
    )
    private List<OrderRefusalOption> orderRefusal;
    @CCD(
            label = "Other (please specify)",
            hint = "If Other was ticked please provide details",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String orderRefusalOther;
    @CCD(
            label = "Upload additional documents",
            hint = "Please upload any additional documents",
            searchable = false,
            typeOverride = FieldType.Document
    )
    private CaseDocument orderRefusalDocs;
    @CCD(
            label = "Select Judge",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_fl_GeneralOrderJudgeType"
    )
    @JsonProperty("orderRefusalJudge")
    private JudgeType orderRefusalJudge;
    @CCD(label = "Name of Judge", searchable = false, typeOverride = FieldType.TextArea)
    private String orderRefusalJudgeName;
    @CCD(label = "Date of order", searchable = false)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate orderRefusalDate;
    @CCD(
            label = "Additional comments",
            hint = "Please add any additional comments for Court Admin (these comments will not be accessible by the Applicant’s solicitor)",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String orderRefusalAddComments;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "********************", searchable = false, typeOverride = FieldType.Label)
  private String orderRefusalSpacing;
  // ==== end synthesised definition-only fields ====
}
