package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consented.AssignedToJudgeSkipRespIntlPostCorresponder;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.ISSUE_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class IssueApplicationConsentedSubmittedHandlerTest {

    @InjectMocks
    private IssueApplicationConsentedSubmittedHandler handlerUnderTest;

    @Mock
    private AssignedToJudgeSkipRespIntlPostCorresponder assignedToJudgeSkipRespIntlPostCorresponder;

    @Test
    void testCanHandle() {
        assertCanHandle(handlerUnderTest, SUBMITTED, CONSENTED, ISSUE_APPLICATION);
    }

    @Test
    void testHandle() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        handlerUnderTest.handle(callbackRequest, AUTH_TOKEN);
        verify(assignedToJudgeSkipRespIntlPostCorresponder).sendCorrespondence(callbackRequest.getCaseDetails(), AUTH_TOKEN);
    }

    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequestFactory.from(FinremCaseData.builder().ccdCaseType(CONSENTED).build());
    }
}
