package uk.gov.hmcts.reform.finrem.caseorchestration.handler.hwfacceptedandissue.consented;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.consented.IssueApplicationConsentedSubmittedHandlerContractTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignPartiesAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge.IssueApplicationConsentCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hwf.HwfCorrespondenceService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryErrorHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.ThrowingRunnable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.getThrowingRunnableCaptor;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.mockRunWithRetryWithHandlerInvokesFirstErrorHandler;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.runSafely;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.HWF_ACCEPTED_AND_ISSUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class HwfAcceptedAndIssueSubmittedHandlerTest extends IssueApplicationConsentedSubmittedHandlerContractTest {

    public HwfAcceptedAndIssueSubmittedHandlerTest() {
        this.expectedConfirmationHeader = "HWF accepted and issued with errors";
    }

    @InjectMocks
    private HwfAcceptedAndIssueSubmittedHandler handler;

    @Mock
    private RetryExecutor retryExecutor;

    @Mock
    private HwfCorrespondenceService hwfNotificationsService;

    @Mock
    private IssueApplicationConsentCorresponder issueApplicationConsentCorresponder;

    @Mock
    private AssignPartiesAccessService assignPartiesAccessService;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, SUBMITTED, CONSENTED, HWF_ACCEPTED_AND_ISSUE);
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

    @Test
    void givenCase_whenHandled_shouldSendHwfCorrespondence() {
        // Arrange
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.fromId(CASE_ID_IN_LONG);

        // Act
        handler().handle(callbackRequest, AUTH_TOKEN);

        ArgumentCaptor<ThrowingRunnable> runnableCaptor = getThrowingRunnableCaptor();
        verify(retryExecutor())
            .runWithRetryWithHandler(
                runnableCaptor.capture(),
                eq("sending HWF correspondence"),
                eq(CASE_ID),
                any(RetryErrorHandler.class)
            );
        runSafely(runnableCaptor.getValue());
        verify(hwfNotificationsService).sendCorrespondence(callbackRequest.getCaseDetails(), AUTH_TOKEN);
    }

    @Test
    void givenCase_whenSendHwfCorrespondenceFailed_thenPopulateErrorToConfirmationBody() {
        // Arrange
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from();

        mockRunWithRetryWithHandlerInvokesFirstErrorHandler(
            retryExecutor,
            "sending HWF correspondence"
        );

        // Act
        var response = handler.handle(callbackRequest, AUTH_TOKEN);

        // then
        assertAll(
            () -> assertThat(response.getConfirmationHeader()).contains(expectedConfirmationHeader),
            () -> assertThat(response.getConfirmationBody())
                .contains("There was a problem sending HWF correspondence. Please send it manually.")
                .doesNotContain("There was a problem sending issue application correspondence. Please send it manually.")
                .doesNotContain("There was a problem granting access to respondent solicitor")
        );
    }

    @Test
    void givenCase_whenSendHwfCorrespondenceFailedAndIssueApplicationCorrespondenceFailed_thenPopulateErrorToConfirmationBody() {
        // Arrange
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from();

        mockRunWithRetryWithHandlerInvokesFirstErrorHandler(
            retryExecutor,
            "sending HWF correspondence"
        );
        mockRunWithRetryWithHandlerInvokesFirstErrorHandler(
            retryExecutor,
            "sending issue application correspondence"
        );

        // Act
        var response = handler.handle(callbackRequest, AUTH_TOKEN);

        // then
        assertAll(
            () -> assertThat(response.getConfirmationHeader()).contains(expectedConfirmationHeader),
            () -> assertThat(response.getConfirmationBody())
                .contains("There was a problem sending HWF correspondence. Please send it manually.")
                .contains("There was a problem sending issue application correspondence. Please send it manually.")
                .doesNotContain("There was a problem granting access to respondent solicitor")
        );
    }
}
