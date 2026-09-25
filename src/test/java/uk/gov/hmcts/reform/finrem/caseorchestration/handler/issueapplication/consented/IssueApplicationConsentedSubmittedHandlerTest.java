package uk.gov.hmcts.reform.finrem.caseorchestration.handler.issueapplication.consented;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.consented.IssueApplicationConsentedSubmittedHandlerContractTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignPartiesAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationAuditService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge.consented.AssignToJudgeCorresponder;
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
    private AssignToJudgeCorresponder assignToJudgeCorresponder;

    @Mock
    private AssignPartiesAccessService assignPartiesAccessService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private NotificationAuditService notificationAuditService;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, SUBMITTED, CONSENTED, ISSUE_APPLICATION);
    }

    @Override
    protected ApplicationEventPublisher applicationEventPublisher() {
        return applicationEventPublisher;
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
    protected AssignToJudgeCorresponder assignToJudgeCorresponder() {
        return assignToJudgeCorresponder;
    }

    @Override
    protected AssignPartiesAccessService assignPartiesAccessService() {
        return assignPartiesAccessService;
    }

    @Override
    protected NotificationAuditService notificationAuditService() {
        return notificationAuditService;
    }

    @Override
    protected CoreCaseDataService coreCaseDataService() {
        return coreCaseDataService;
    }
}
