package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingInstructionProcessable;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Reviewable;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.RefusalOrderConvertible;

import java.time.LocalDateTime;
import java.util.Optional;

@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PsaDocumentReview implements HasCaseDocument, Reviewable, RefusalOrderConvertible, HearingInstructionProcessable {
    private CaseDocument psaDocument;
    private OrderStatus orderStatus;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime submittedDate;
    private YesOrNo resubmission;
    private String submittedBy;
    private String submittedByEmail;
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
    private LocalDateTime refusedDate;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime notificationSentDate;

    @JsonIgnore
    @Override
    public CaseDocument getReplacedDocument() {
        return psaDocument;
    }

    @Override
    public boolean match(CaseDocument targetDoc) {
        return Optional.ofNullable(targetDoc).map(CaseDocument::getDocumentUrl).equals(Optional.ofNullable(psaDocument)
            .map(CaseDocument::getDocumentUrl));
    }

    @Override
    public void replaceDocument(CaseDocument amendedDocument) {
        this.setPsaDocument(amendedDocument);
    }
    
    @Override
    @JsonIgnore
    public CaseDocument getRefusedDocument() {
        return getPsaDocument();
    }
}
