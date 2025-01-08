package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentMatcher;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus;

import java.time.LocalDateTime;

public interface Approvable extends DocumentMatcher {

    OrderStatus getOrderStatus();

    LocalDateTime getApprovalDate();

    String getApprovalJudge();

    YesOrNo getFinalOrder();

    CaseDocument getReplacedDocument();

    void setOrderStatus(OrderStatus orderStatus);

    default void setApprovalDate(LocalDateTime approvalDate) {
    }

    default void setApprovalJudge(String approvalJudge) {
    }

    default void setFinalOrder(YesOrNo finalOrder) {
    }

    void replaceDocument(CaseDocument amendedDocument);
}
