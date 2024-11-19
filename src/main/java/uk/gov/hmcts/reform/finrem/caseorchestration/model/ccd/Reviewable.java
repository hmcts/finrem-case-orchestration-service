package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus;

import java.time.LocalDateTime;

public interface Reviewable {
    LocalDateTime getSubmittedDate();

    OrderStatus getOrderStatus();
}