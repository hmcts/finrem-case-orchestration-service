package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import java.time.LocalDate;

public interface OrderToBeReviewed {
    LocalDate getSubmittedDate();

    LocalDate getReviewedDate();

    OrderStatus getStatus();

    LocalDate getNotificationSentDate();
}
