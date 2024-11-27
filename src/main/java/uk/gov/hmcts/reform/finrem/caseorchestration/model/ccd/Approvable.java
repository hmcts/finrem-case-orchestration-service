package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus;

import java.time.LocalDate;

public interface Approvable {

    boolean match(CaseDocument targetDoc);

    void setOrderStatus(OrderStatus orderStatus);

    void setApprovalDate(LocalDate localDate);

    void setApprovalJudge(String approvalJudge);

    void replaceDocument(CaseDocument amendedDocument);
}
