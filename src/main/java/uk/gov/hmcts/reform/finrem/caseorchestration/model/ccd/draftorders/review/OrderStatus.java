package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review;

public enum OrderStatus {
    TO_BE_REVIEWED,
    APPROVED_BY_JUDGE,
    PROCESSED,
    REFUSED;

    public static boolean isJudgeReviewable(OrderStatus status) {
        return status == TO_BE_REVIEWED;
    }
}
