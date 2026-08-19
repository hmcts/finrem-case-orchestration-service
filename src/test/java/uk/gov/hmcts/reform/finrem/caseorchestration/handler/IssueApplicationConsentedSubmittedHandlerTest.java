package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignPartiesAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UserNotFoundInOrganisationApiException;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge.IssueApplicationConsentCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryErrorHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.ThrowingRunnable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.getThrowingRunnableCaptor;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.mockRunWithRetryWithHandlerInvokesFirstErrorHandler;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.runSafely;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.ISSUE_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class IssueApplicationConsentedSubmittedHandlerTest {

    private final String expectedConfirmationHeader = "Application Issued with Errors";

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

    @BeforeEach
    void setup() {
        lenient().doNothing().when(retryExecutor).runWithRetryWithHandler(any(), anyString(), any(), any());
    }

    @Test
    void givenCase_whenSendHwfCorrespondenceFailedAndIssueApplicationCorrespondenceFailed_thenPopulateErrorToConfirmationBody() {
        // Arrange
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from();

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
                .contains("There was a problem sending issue application correspondence. Please send it manually.")
                .doesNotContain("There was a problem granting access to respondent solicitor")
        );
    }

    @Test
    void givenCase_whenGrantRespondentSolicitorErrorFailed_thenPopulateErrorToConfirmationBody() {
        // Arrange
        FinremCaseData spiedFinremCaseData = spy(
            FinremCaseData.builder()
                .contactDetailsWrapper(ContactDetailsWrapper.builder()
                    .consentedRespondentRepresented(YesOrNo.YES).build())
                .build()
        );
        when(spiedFinremCaseData.getRespondentSolicitorEmail()).thenReturn(TEST_RESP_SOLICITOR_EMAIL);

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(spiedFinremCaseData);

        mockRunWithRetryWithHandlerInvokesFirstErrorHandler(
            retryExecutor,
            "granting respondent solicitor"
        );

        // Act
        var response = handler.handle(callbackRequest, AUTH_TOKEN);

        // then
        assertAll(
            () -> assertThat(response.getConfirmationHeader()).contains(expectedConfirmationHeader),
            () -> assertThat(response.getConfirmationBody())
                .doesNotContain("There was a problem sending issue application correspondence. Please send it manually.")
                .contains("There was a problem granting access to respondent solicitor: " + TEST_RESP_SOLICITOR_EMAIL)
        );
    }

    @Test
    void givenNoErrorsEvolved_whenHandled_thenDoesNotPopulateErrorToConfirmationBody() {
        FinremCaseData spiedFinremCaseData = spy(
            FinremCaseData.builder()
                .contactDetailsWrapper(ContactDetailsWrapper.builder()
                    .consentedRespondentRepresented(YesOrNo.YES).build())
                .build()
        );
        when(spiedFinremCaseData.getRespondentSolicitorEmail()).thenReturn(TEST_RESP_SOLICITOR_EMAIL);

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(spiedFinremCaseData);

        // Act
        var response = handler.handle(callbackRequest, AUTH_TOKEN);

        // then
        assertAll(
            () -> assertThat(response.getConfirmationHeader()).isNull(),
            () -> assertThat(response.getConfirmationBody()).isNull()
        );
    }

    @Test
    void givenCase_whenHandled_shouldSendIssueApplicationCorrespondence() {
        // Arrange
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.fromId(CASE_ID_IN_LONG);

        // Act
        handler.handle(callbackRequest, AUTH_TOKEN);

        ArgumentCaptor<ThrowingRunnable> runnableCaptor = getThrowingRunnableCaptor();
        verify(retryExecutor)
            .runWithRetryWithHandler(
                runnableCaptor.capture(),
                eq("sending issue application correspondence"),
                eq(CASE_ID),
                any(RetryErrorHandler.class)
            );
        runSafely(runnableCaptor.getValue());
        verify(issueApplicationConsentCorresponder).sendCorrespondence(callbackRequest.getCaseDetails(), AUTH_TOKEN);
    }

    @Test
    void givenCaseWithRepresentedRespondentSolicitor_whenHandled_shouldGrantRespondentSolicitor()
        throws UserNotFoundInOrganisationApiException {
        // Arrange
        FinremCaseData caseData = spy(FinremCaseData.builder()
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .consentedRespondentRepresented(YesOrNo.YES)
                .build())
            .build());
        when(caseData.getRespondentSolicitorEmail()).thenReturn(TEST_RESP_SOLICITOR_EMAIL);

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG,
            caseData);

        // Act
        handler.handle(callbackRequest, AUTH_TOKEN);

        ArgumentCaptor<ThrowingRunnable> runnableCaptor = getThrowingRunnableCaptor();
        verify(retryExecutor)
            .runWithRetryWithHandler(
                runnableCaptor.capture(),
                eq("granting respondent solicitor"),
                eq(CASE_ID),
                any(RetryErrorHandler.class)
            );
        runSafely(runnableCaptor.getValue());
        verify(assignPartiesAccessService).grantRespondentSolicitor(caseData);
    }

    @Test
    void givenCaseWithoutRespondentSolicitorEmail_whenHandled_shouldGrantRespondentSolicitor()
        throws UserNotFoundInOrganisationApiException {
        // Arrange
        FinremCaseData caseData = FinremCaseData.builder()
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .consentedRespondentRepresented(YesOrNo.YES)
                .build())
            .build();

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG,
            caseData);

        // Act
        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(retryExecutor, never())
            .runWithRetryWithHandler(
                any(ThrowingRunnable.class),
                eq("granting respondent solicitor"),
                eq(CASE_ID),
                any(RetryErrorHandler.class)
            );
        verify(assignPartiesAccessService, never()).grantRespondentSolicitor(caseData);
    }

    @Test
    void givenRespondentUnrepresented_whenHandled_shouldGrantRespondentSolicitor()
        throws UserNotFoundInOrganisationApiException {
        // Arrange
        FinremCaseData caseData = FinremCaseData.builder()
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .consentedRespondentRepresented(YesOrNo.NO)
                .build())
            .build();

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG,
            caseData);

        // Act
        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(retryExecutor, never())
            .runWithRetryWithHandler(
                any(ThrowingRunnable.class),
                eq("granting respondent solicitor"),
                eq(CASE_ID),
                any(RetryErrorHandler.class)
            );
        verify(assignPartiesAccessService, never()).grantRespondentSolicitor(caseData);
    }
}
