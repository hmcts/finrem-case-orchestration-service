package uk.gov.hmcts.reform.finrem.caseorchestration.handler.hwfacceptedandissue.consented;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.consented.IssueApplicationAboutToSubmitHandlerContractTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.issueapplication.IssueApplicationService;

import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class HwfAcceptedAndIssueAboutToSubmitHandlerTest extends IssueApplicationAboutToSubmitHandlerContractTest {

    @InjectMocks
    private HwfAcceptedAndIssueAboutToSubmitHandler handler;
    @Mock
    private OnlineFormDocumentService onlineFormDocumentService;
    @Mock
    private IssueApplicationService issueApplicationService;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.HWF_ACCEPTED_AND_ISSUE);
    }

    @Override
    protected FinremCallbackHandler handler() {
        return handler;
    }

    @Override
    protected OnlineFormDocumentService onlineFormDocumentService() {
        return onlineFormDocumentService;
    }

    @Override
    protected IssueApplicationService issueApplicationService() {
        return issueApplicationService;
    }
}
