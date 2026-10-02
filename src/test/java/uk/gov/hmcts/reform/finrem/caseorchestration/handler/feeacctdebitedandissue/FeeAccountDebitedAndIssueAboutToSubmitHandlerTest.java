package uk.gov.hmcts.reform.finrem.caseorchestration.handler.feeacctdebitedandissue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.consented.IssueApplicationAboutToSubmitHandlerContractTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.feeacctdebitedandissue.consented.FeeAccountDebitedAndIssueAboutToSubmitHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.issueapplication.IssueApplicationService;

import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class FeeAccountDebitedAndIssueAboutToSubmitHandlerTest extends IssueApplicationAboutToSubmitHandlerContractTest {

    @InjectMocks
    private FeeAccountDebitedAndIssueAboutToSubmitHandler handler;
    @Mock
    private OnlineFormDocumentService onlineFormDocumentService;
    @Mock
    private IssueApplicationService issueApplicationService;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.FEE_ACCOUNT_DEBITED_AND_ISSUE);
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
