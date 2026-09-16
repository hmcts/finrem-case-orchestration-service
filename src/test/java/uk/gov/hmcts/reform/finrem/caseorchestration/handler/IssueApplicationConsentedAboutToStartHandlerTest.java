package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.consented.IssueApplicationAboutToStartHandlerContractTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnStartDefaultValueService;

import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.ISSUE_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class IssueApplicationConsentedAboutToStartHandlerTest extends IssueApplicationAboutToStartHandlerContractTest {

    @InjectMocks
    private IssueApplicationConsentedAboutToStartHandler handler;

    @Mock
    private OnStartDefaultValueService onStartDefaultValueService;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, ABOUT_TO_START, CONSENTED, ISSUE_APPLICATION);
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
