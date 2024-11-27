package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Approvable;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasSubmittedInfo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.CaseDocumentCollection;
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
public class AgreedDraftOrder implements HasCaseDocument, HasSubmittedInfo, Approvable {
    private OrderStatus orderStatus;
    private CaseDocument draftOrder;
    private CaseDocument pensionSharingAnnex;
    private String submittedBy;
    private String uploadedOnBehalfOf;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime submittedDate;
    private YesOrNo resubmission;
    private List<CaseDocumentCollection> attachments;

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

    @Override
    public void setApprovalDate(LocalDate localDate) {
        //  no approval date; Ignore it.
    }

    @Override
    public void setApprovalJudge(String approvalJudge) {
        //  no approval judge; Ignore it.
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
