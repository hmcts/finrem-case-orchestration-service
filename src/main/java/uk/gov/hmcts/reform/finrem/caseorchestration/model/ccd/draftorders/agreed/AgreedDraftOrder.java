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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class AgreedDraftOrder implements HasCaseDocument, HasSubmittedInfo, Approvable, WithAttachments {
    private OrderStatus orderStatus;
    private CaseDocument draftOrder;
    private CaseDocument pensionSharingAnnex;
    private CaseDocument coverLetter;
    private String submittedBy;
    private String submittedByEmail;
    private String uploadedOnBehalfOf;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime submittedDate;
    private YesOrNo resubmission;
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

}
