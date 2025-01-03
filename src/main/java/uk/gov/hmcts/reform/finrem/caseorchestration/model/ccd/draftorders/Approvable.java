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

    /**
     * Retrieves the target document from the available options.
     *
     * <p>
     * It returns the target document. 
     * In some cases, the method checks if the draft order is present and returns it.
     * If the draft order is not available, it checks for the presence of
     * the pension sharing annex and returns it. If neither is available,
     * the method returns {@code null}.
     * </p>
     *
     * @return the {@link CaseDocument} representing the target document,
     *         or {@code null} if no document is available.
     */
    CaseDocument getTargetDocument();

    void setOrderStatus(OrderStatus orderStatus);

    default void setApprovalDate(LocalDateTime approvalDate) {
    }

    default void setApprovalJudge(String approvalJudge) {
    }

    default void setFinalOrder(YesOrNo finalOrder) {
    }

    void replaceDocument(CaseDocument amendedDocument);
}
