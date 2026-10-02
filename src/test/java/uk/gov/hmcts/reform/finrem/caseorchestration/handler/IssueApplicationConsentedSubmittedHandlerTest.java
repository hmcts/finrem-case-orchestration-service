package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.consented.IssueApplicationConsentedSubmittedHandlerContractTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignPartiesAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge.IssueApplicationConsentCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.ISSUE_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class IssueApplicationConsentedSubmittedHandlerTest extends IssueApplicationConsentedSubmittedHandlerContractTest {

    @InjectMocks
    private IssueApplicationConsentedSubmittedHandler handler;

    @Mock
    private RetryExecutor retryExecutor;

    @Mock
    private IssueApplicationConsentCorresponder issueApplicationConsentCorresponder;

    @Mock
    private AssignPartiesAccessService assignPartiesAccessService;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, SUBMITTED, CONSENTED, ISSUE_APPLICATION);
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
