package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingInstructionProcessable;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Reviewable;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.OrderFiledBy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.RefusalOrderConvertible;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FRUploadParty;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_draftOrderDocumentReview", generate = true)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DraftOrderDocumentReview implements HasCaseDocument, Reviewable, RefusalOrderConvertible, HearingInstructionProcessable {
    @CCD(label = "Draft order", searchable = false, typeOverride = FieldType.Document)
    private CaseDocument draftOrderDocument;
    @CCD(
            label = "Order Type",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_fl_draftOrderOrderType"
    )
    private OrderType orderType;
    @CCD(
            label = "Order status",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_fl_draftOrderOrderStatus"
    )
    private OrderStatus orderStatus;
    @CCD(label = "Date submitted")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime submittedDate;
    @CCD(label = "Is this a resubmission?", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo resubmission;
    @CCD(label = "Submitted by", searchable = false)
    private String submittedBy;
    @CCD(label = "Submitted by (Email address)", searchable = false)
    private String submittedByEmail;
    @CCD(label = "Order filed by", searchable = false, typeOverride = FieldType.Text)
    private OrderFiledBy orderFiledBy;
    @CCD(
            label = "Uploaded on behalf of",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "FR_uploadParty",
            typeParameterClass = FRUploadParty.class
    )
    private String uploadedOnBehalfOf;
    @CCD(
            label = "Attachments",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Document"
    )
    private List<DocumentCollectionItem> attachments;
    @CCD(label = "Judge name", searchable = false)
    private String approvalJudge;
    @CCD(label = "Approval date", searchable = false)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime approvalDate;
    @CCD(label = "Is this a final order?", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo finalOrder;
    @CCD(label = "Court order date", searchable = false)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate courtOrderDate;
    @CCD(label = "Cover letter", searchable = false, typeOverride = FieldType.Document)
    private CaseDocument coverLetter;
    @CCD(label = "Is there another hearing to be listed?", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo anotherHearingToBeListed;
    @CCD(label = "Type of hearing", searchable = false)
    private String hearingType;
    @CCD(
            label = "Time estimate",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "FR_fl_hearingTimeDirections"
    )
    private String hearingTimeEstimate;
    @CCD(label = "Additional time", searchable = false)
    private String additionalTime;
    @CCD(label = "Any other listing instructions?", searchable = false)
    private String otherListingInstructions;
    @CCD(label = "Date refused", searchable = false)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime refusedDate;
    @CCD(label = "Notification Sent Date", searchable = false)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime notificationSentDate;

    @JsonIgnore
    @Override
    public CaseDocument getTargetDocument() {
        return draftOrderDocument;
    }

    @Override
    public void replaceDocument(CaseDocument amendedDocument) {
        this.setDraftOrderDocument(amendedDocument);
    }

    @Override
    public boolean match(CaseDocument targetDoc) {
        return Optional.ofNullable(targetDoc).map(CaseDocument::getDocumentUrl).equals(Optional.ofNullable(draftOrderDocument)
            .map(CaseDocument::getDocumentUrl));
    }

    @Override
    @JsonIgnore
    public CaseDocument getRefusedDocument() {
        return getDraftOrderDocument();
    }
}
