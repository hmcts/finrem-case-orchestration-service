package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;

import static org.assertj.core.api.Assertions.assertThat;

class FinremCallbackRequestTest {

    @Test
    void testFrom() {
        FinremCaseData caseData = new FinremCaseData();
        FinremCallbackRequest request = FinremCallbackRequest.from(caseData);

        assertThat(request.getCaseDetails().getData()).isEqualTo(caseData);
    }
}
