package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PsaDocumentReview implements HasCaseDocument, Reviewable, Approvable, HearingInstructionProcessable {
    private CaseDocument psaDocument;
    private OrderStatus orderStatus;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime submittedDate;
    private YesOrNo resubmission;
    private String submittedBy;
    private String uploadedOnBehalfOf;
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
    private LocalDateTime reviewedDate;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime notificationSentDate;

    @Override
    public boolean match(CaseDocument targetDoc) {
        return Optional.ofNullable(targetDoc).map(CaseDocument::getDocumentUrl).equals(Optional.ofNullable(psaDocument)
            .map(CaseDocument::getDocumentUrl));
    }

    @Override
    public void replaceDocument(CaseDocument amendedDocument) {
        this.setPsaDocument(amendedDocument);
    }
}
