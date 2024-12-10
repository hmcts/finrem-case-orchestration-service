package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus;

import java.time.LocalDateTime;

public interface Approvable extends DocumentMatcher {

    OrderStatus getOrderStatus();

    LocalDateTime getApprovalDate();

    String getApprovalJudge();

    CaseDocument getReplacedDocument();

    void setOrderStatus(OrderStatus orderStatus);

    void setApprovalDate(LocalDateTime approvalDate);

    void setApprovalJudge(String approvalJudge);

    void replaceDocument(CaseDocument amendedDocument);
}
