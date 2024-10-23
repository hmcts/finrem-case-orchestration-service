package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review;

import java.util.Collection;
import java.util.List;

public enum OrderStatus {
    TO_BE_REVIEWED,
    APPROVED_BY_JUDGE,
    PROCESSED_BY_ADMIN;

    public static Collection<OrderStatus> nonProcessedOrderStatuses() {
        return List.of(TO_BE_REVIEWED);
    }
}
