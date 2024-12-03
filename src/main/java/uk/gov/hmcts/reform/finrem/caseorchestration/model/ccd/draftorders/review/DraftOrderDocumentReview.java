package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Approvable;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingInstructionProcessable;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Reviewable;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.CaseDocumentCollection;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DraftOrderDocumentReview implements HasCaseDocument, Reviewable, Approvable, HearingInstructionProcessable {
    private CaseDocument draftOrderDocument;
    private OrderStatus orderStatus;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime submittedDate;
    private YesOrNo resubmission;
    private String submittedBy;
    private String uploadedOnBehalfOf;
    private List<CaseDocumentCollection> attachments;
    private String approvalJudge;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime approvalDate;
    private YesOrNo finalOrder;
    private YesOrNo anotherHearingToBeListed;
    private String hearingType;
    private String hearingTimeEstimate;
    private String additionalTime;
    private String otherListingInstructions;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime refusedDate;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime notificationSentDate;

    @Override
    public void replaceDocument(CaseDocument amendedDocument) {
        this.setDraftOrderDocument(amendedDocument);
    }

    @Override
    public boolean match(CaseDocument targetDoc) {
        return Optional.ofNullable(targetDoc).map(CaseDocument::getDocumentUrl).equals(Optional.ofNullable(draftOrderDocument)
            .map(CaseDocument::getDocumentUrl));
    }
}
