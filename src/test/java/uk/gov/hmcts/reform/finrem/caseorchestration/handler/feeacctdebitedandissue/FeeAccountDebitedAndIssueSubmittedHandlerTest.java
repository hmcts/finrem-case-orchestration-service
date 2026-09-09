package uk.gov.hmcts.reform.finrem.caseorchestration.handler.feeacctdebitedandissue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.consented.IssueApplicationConsentedSubmittedHandlerContractTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.feeacctdebitedandissue.consented.FeeAccountDebitedAndIssueSubmittedHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignPartiesAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge.IssueApplicationConsentCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.FEE_ACCOUNT_DEBITED_AND_ISSUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class FeeAccountDebitedAndIssueSubmittedHandlerTest extends IssueApplicationConsentedSubmittedHandlerContractTest {

    public FeeAccountDebitedAndIssueSubmittedHandlerTest() {
        this.expectedConfirmationHeader = "Fee account debited and issued with errors";
    }

    @InjectMocks
    private FeeAccountDebitedAndIssueSubmittedHandler handler;

    @Mock
    private RetryExecutor retryExecutor;

    @Mock
    private IssueApplicationConsentCorresponder issueApplicationConsentCorresponder;

    @Mock
    private AssignPartiesAccessService assignPartiesAccessService;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, SUBMITTED, CONSENTED, FEE_ACCOUNT_DEBITED_AND_ISSUE);
    }

    @Override
    protected FinremCallbackHandler handler() {
        return handler;
    }

    @Override
    protected RetryExecutor retryExecutor() {
        return retryExecutor;
    }

    @Override
    protected IssueApplicationConsentCorresponder issueApplicationConsentCorresponder() {
        return issueApplicationConsentCorresponder;
    }

    @Override
    protected AssignPartiesAccessService assignPartiesAccessService() {
        return assignPartiesAccessService;
    }

}
