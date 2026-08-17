package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.Approvable;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.HasSubmittedInfo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.WithAttachments;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FRUploadParty;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_agreedDraftOrder", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class AgreedDraftOrder implements HasCaseDocument, HasSubmittedInfo, Approvable, WithAttachments {
    @CCD(
            label = "Order type",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_fl_draftOrderOrderType"
    )
    private OrderType orderType;
    @CCD(
            label = "Document status",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_fl_draftOrderOrderStatus"
    )
    private OrderStatus orderStatus;
    @CCD(
            label = "Draft order",
            categoryID = "postHearingDraftOrder",
            searchable = false,
            typeOverride = FieldType.Document
    )
    private CaseDocument draftOrder;
    @CCD(
            label = "Pension Sharing Annex",
            categoryID = "postHearingDraftOrder",
            searchable = false,
            typeOverride = FieldType.Document
    )
    private CaseDocument pensionSharingAnnex;
    @CCD(
            label = "Cover Letter",
            categoryID = "postHearingDraftOrder",
            searchable = false,
            typeOverride = FieldType.Document
    )
    private CaseDocument coverLetter;
    @CCD(label = "Submitted by", searchable = false)
    private String submittedBy;
    @CCD(label = "Submitted by (Email address)", searchable = false)
    private String submittedByEmail;
    @CCD(
            label = "Uploaded on behalf of",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "FR_uploadParty",
            typeParameterClass = FRUploadParty.class
    )
    private String uploadedOnBehalfOf;
    @CCD(label = "Date submitted", searchable = false)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime submittedDate;
    @CCD(
            label = "Is this a resubmission?",
            showCondition = "resubmission=\"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private YesOrNo resubmission;
    @CCD(
            label = "Attachments",
            categoryID = "postHearingDraftOrder",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Document"
    )
    private List<DocumentCollectionItem> attachments;

    @Override
    public boolean match(CaseDocument targetDoc) {
        return Optional.ofNullable(targetDoc)
            .map(CaseDocument::getDocumentUrl)
            .filter(documentUrl ->
                documentUrl.equals(Optional.ofNullable(draftOrder).map(CaseDocument::getDocumentUrl).orElse(null))
                    || documentUrl.equals(Optional.ofNullable(pensionSharingAnnex).map(CaseDocument::getDocumentUrl).orElse(null))
            )
            .isPresent();
    }

    @JsonIgnore
    @Override
    public LocalDateTime getApprovalDate() {
        // @JsonIgnore is necessary, as it ensures the property is not visible in AgreedDraftOrder
        return null;
    }

    @JsonIgnore
    @Override
    public String getApprovalJudge() {
        // @JsonIgnore is necessary, as it ensures the property is not visible in AgreedDraftOrder
        return null;
    }

    @JsonIgnore
    @Override
    public YesOrNo getFinalOrder() {
        // @JsonIgnore is necessary, as it ensures the property is not visible in AgreedDraftOrder
        return null;
    }

    @JsonIgnore
    @Override
    public LocalDate getCourtOrderDate() {
        // @JsonIgnore is necessary, as it ensures the property is not visible in AgreedDraftOrder
        return null;
    }

    @JsonIgnore
    @Override
    public CaseDocument getTargetDocument() {
        if (draftOrder != null) {
            return draftOrder;
        } else if (pensionSharingAnnex != null) {
            return pensionSharingAnnex;
        } else {
            return null;
        }
    }

    @Override
    public void replaceDocument(CaseDocument amendedDocument) {
        if (this.draftOrder != null) {
            this.draftOrder = amendedDocument;
        } else if (this.pensionSharingAnnex != null) {
            this.pensionSharingAnnex = amendedDocument;
        } else {
            throw new IllegalArgumentException(
                "Failed to replace the document: No existing draft order or pension sharing annex is available to be replaced. "
                    + "Ensure the document to be amended corresponds to a valid existing document."
            );
        }
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "This is a resubmission. <a href=\"cases/case-details/${[CASE_REFERENCE]}#Orders\" target=\"_blank\">(see previously rejected draft orders)</a>",
          showCondition = "resubmission=\"Yes\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String resubmissionLinkYes;
  @CCD(
          label = "This is not a resubmission.",
          showCondition = "resubmission=\"No\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String resubmissionLinkNo;
  // ==== end synthesised definition-only fields ====
}
