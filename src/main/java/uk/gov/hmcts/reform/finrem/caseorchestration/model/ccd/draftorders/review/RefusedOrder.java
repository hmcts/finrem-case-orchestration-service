package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.JudgeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.OrderFiledBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FRFlRefusalOrderJudgeType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_refusedOrderOrPsa", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RefusedOrder implements HasCaseDocument {

    @CCD(
            label = "Refused document",
            categoryID = "postHearingDraftOrder",
            searchable = false,
            typeOverride = FieldType.Document
    )
    private CaseDocument refusedDocument;

    @CCD(label = "Refusal order", categoryID = "approvedOrders", searchable = false, typeOverride = FieldType.Document)
    private CaseDocument refusalOrder;

    @CCD(label = "Date refused", searchable = false)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime refusedDate;

    @CCD(label = "Date submitted", searchable = false)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime submittedDate;

    @CCD(label = "Submitted by", searchable = false)
    private String submittedBy;

    @CCD(label = "Submitted by", searchable = false)
    private String submittedByEmail;

    @CCD(label = "Order filed by", searchable = false, typeOverride = FieldType.Text)
    private OrderFiledBy orderFiledBy;

    @CCD(
            label = "Attachments",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Document"
    )
    private List<DocumentCollectionItem> attachments;

    @CCD(label = "Judge name", searchable = false)
    private String refusalJudge;

    @CCD(label = "Judge feedback", searchable = false)
    private String judgeFeedback;

    @CCD(label = "Hearing Date", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate hearingDate;

    @CCD(
            label = "Judge Type",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_fl_RefusalOrderJudgeType",
            typeParameterClass = FRFlRefusalOrderJudgeType.class
    )
    private JudgeType judgeType;

}
