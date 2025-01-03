package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.draftorders;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import static org.assertj.core.api.Assertions.assertThat;

class RefusedOrderCorrespondenceRequestTest {
    @Test
    void testGetCaseId() {
        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .id(12345L)
            .build();
        RefusedOrderCorrespondenceRequest request = RefusedOrderCorrespondenceRequest.builder()
            .caseDetails(caseDetails)
            .build();

        assertThat(request.getCaseId()).isEqualTo("12345");
    }
}
