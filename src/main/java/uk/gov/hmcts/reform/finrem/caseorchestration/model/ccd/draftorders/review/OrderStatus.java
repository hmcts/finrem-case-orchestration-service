package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_fl_draftOrderOrderStatus", generate = true)
public enum OrderStatus {
    @CCD(label = "To be reviewed")
    TO_BE_REVIEWED,
    @CCD(label = "Approved by judge")
    APPROVED_BY_JUDGE,
    @CCD(label = "Processed")
    PROCESSED,
    @CCD(label = "Refused by judge")
    REFUSED;

    public static boolean isJudgeReviewable(OrderStatus status) {
        return status == TO_BE_REVIEWED;
    }
}
