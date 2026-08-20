package uk.gov.hmcts.reform.finrem.caseorchestration.handler.consented;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnStartDefaultValueService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;

public abstract class IssueApplicationAboutToStartHandlerContractTest {

    protected abstract FinremCallbackHandler handler();

    protected abstract OnStartDefaultValueService onStartDefaultValueService();

    @Test
    void shouldPopulateIssueDate_whenHandled() {
        FinremCaseData finremCaseData = FinremCaseData.builder().build();
        FinremCallbackRequest request = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, finremCaseData);

        var response = handler().handle(request, AUTH_TOKEN);

        verify(onStartDefaultValueService()).defaultIssueDate(request);
        assertThat(response.getData()).isEqualTo(finremCaseData);
    }
}