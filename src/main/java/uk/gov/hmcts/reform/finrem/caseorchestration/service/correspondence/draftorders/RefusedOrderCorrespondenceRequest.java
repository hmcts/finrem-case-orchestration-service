package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.draftorders;

import lombok.Builder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.RefusedOrder;

import java.util.List;

@Builder
public record RefusedOrderCorrespondenceRequest(FinremCaseDetails caseDetails, String authorisationToken,
                                                List<RefusedOrder> refusedOrders) {
    public String getCaseId() {
        return String.valueOf(caseDetails().getId());
    }
}
