package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_fl_draftOrderOrderType", generate = true)
public enum OrderType {
    @CCD(label = "An agreed order following a hearing")
    AGREED_ORDER,
    @CCD(label = "Accelerated Procedure Order")
    ACCELERATED_PROCEDURE_ORDER,
    @CCD(label = "A suggested draft order")
    SUGGESTED_ORDER
}
