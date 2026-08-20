package uk.gov.hmcts.reform.finrem.caseorchestration.handler.feeacctdebitedandissue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.consented.IssueApplicationAboutToStartHandlerContractTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.feeacctdebitedandissue.consented.FeeAccountDebitedAndIssueAboutToStartHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnStartDefaultValueService;

import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.FEE_ACCOUNT_DEBITED_AND_ISSUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class FeeAccountDebitedAndIssueAboutToStartHandlerTest extends IssueApplicationAboutToStartHandlerContractTest {

    @InjectMocks
    private FeeAccountDebitedAndIssueAboutToStartHandler handler;
    @Mock
    private OnStartDefaultValueService onStartDefaultValueService;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, ABOUT_TO_START, CONSENTED, FEE_ACCOUNT_DEBITED_AND_ISSUE);
    }

    @Override
    protected FinremCallbackHandler handler() {
        return handler;
    }

    @Override
    protected OnStartDefaultValueService onStartDefaultValueService() {
        return onStartDefaultValueService;
    }
}
