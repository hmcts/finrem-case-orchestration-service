package uk.gov.hmcts.reform.finrem.caseorchestration.handler.fee;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.feeacctdebitedandissue.consented.FeeAccountDebitedAndIssueAboutToStartHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnStartDefaultValueService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.FEE_ACCOUNT_DEBITED_AND_ISSUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class FeeAccountDebitedAndIssueAboutToStartHandlerTest {

    @InjectMocks
    private FeeAccountDebitedAndIssueAboutToStartHandler handler;
    @Mock
    private OnStartDefaultValueService onStartDefaultValueService;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, ABOUT_TO_START, CONSENTED, FEE_ACCOUNT_DEBITED_AND_ISSUE);
    }

    @Test
    void shouldPopulateIssueDate_whenHandled() {
        FinremCaseData finremCaseData = FinremCaseData.builder().build();
        FinremCallbackRequest request = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, finremCaseData);

        var response = handler.handle(request, AUTH_TOKEN);

        verify(onStartDefaultValueService).defaultIssueDate(request);
        assertThat(response.getData()).isEqualTo(finremCaseData);
    }
}
