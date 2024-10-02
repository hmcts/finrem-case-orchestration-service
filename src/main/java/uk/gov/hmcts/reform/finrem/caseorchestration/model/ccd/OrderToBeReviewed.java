package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import java.time.LocalDate;

public interface OrderToBeReviewed {
    public LocalDate getSubmittedDate();

    public LocalDate getReviewedDate();
}
