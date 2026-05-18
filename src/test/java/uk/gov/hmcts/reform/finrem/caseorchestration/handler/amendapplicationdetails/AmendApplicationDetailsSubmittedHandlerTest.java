package uk.gov.hmcts.reform.finrem.caseorchestration.handler.amendapplicationdetails;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignPartiesAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryErrorHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.ThrowingRunnable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.assertCondition;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.getThrowingRunnableCaptor;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.mockRunWithRetryWithHandlerInvokesFirstErrorHandler;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.AMEND_APP_DETAILS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.AMEND_CONTESTED_APP_DETAILS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.AMEND_CONTESTED_PAPER_APP_DETAILS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class AmendApplicationDetailsSubmittedHandlerTest {

    static final String FAIL_GRANT_AND_REVOKE = "true,true";
    static final String FAIL_GRANT_ONLY = "true,false";
    static final String FAIL_REVOKE_ONLY = "false,true";

    @Mock
    private RetryExecutor retryExecutor;

    @Mock
    private AssignPartiesAccessService assignPartiesAccessService;

    @InjectMocks
    private AmendApplicationDetailsSubmittedHandler underTest;

    @Test
    void testHandlerCanHandle() {
        assertCanHandle(underTest,
            Arguments.of(SUBMITTED, CONTESTED, AMEND_CONTESTED_PAPER_APP_DETAILS),
            Arguments.of(SUBMITTED, CONTESTED, AMEND_CONTESTED_APP_DETAILS),
            Arguments.of(SUBMITTED, CONSENTED, AMEND_APP_DETAILS)
        );
    }

    @Test
    void givenNoApplicantSolicitorChanged_whenHandled_thenDoNothing() {
        FinremCaseData finremCaseData = spy(FinremCaseData.builder().build());

        FinremCallbackRequest callbackRequest = spy(FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, finremCaseData));
        when(callbackRequest.hasApplicantSolicitorChanged()).thenReturn(false);

        var response = underTest.handle(callbackRequest, AUTH_TOKEN);

        ArgumentCaptor<ThrowingRunnable> captor = getThrowingRunnableCaptor();
        assertAll(
            () -> verify(retryExecutor, never()).runWithRetry(
                captor.capture(),
                eq("granting applicant solicitor"),
                eq(CASE_ID)
            ),
            () -> verify(retryExecutor, never()).runWithRetry(
                any(ThrowingRunnable.class),
                eq("revoking old applicant solicitor"),
                eq(CASE_ID)
            ),
            () -> assertNull(response.getConfirmationBody()),
            () -> assertNull(response.getConfirmationHeader())
        );
    }

    @Test
    void givenApplicantSolicitorChanged_whenAppSolicitorIsNewlyAdded_thenGrantApplicantSolicitorOnly() {
        FinremCaseData finremCaseData = spy(FinremCaseData.builder().build());
        when(finremCaseData.getAppSolicitorEmailIfRepresented()).thenReturn(TEST_SOLICITOR_EMAIL);

        FinremCaseData finremCaseDataBefore = spy(FinremCaseData.builder().build());
        when(finremCaseDataBefore.getAppSolicitorEmailIfRepresented()).thenReturn(null);

        FinremCallbackRequest callbackRequest = spy(FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, finremCaseDataBefore,
            finremCaseData));
        when(callbackRequest.hasApplicantSolicitorChanged()).thenReturn(true);

        var response = underTest.handle(callbackRequest, AUTH_TOKEN);

        ArgumentCaptor<ThrowingRunnable> captor = getThrowingRunnableCaptor();
        assertAll(
            () -> verify(retryExecutor).runWithRetryWithHandler(
                captor.capture(),
                eq("granting applicant solicitor"),
                eq(CASE_ID),
                any(RetryErrorHandler.class)
            ),
            () -> {
                captor.getValue().run();
                verify(assignPartiesAccessService).grantApplicantSolicitor(finremCaseData);
            },
            () -> verify(retryExecutor, never()).runWithRetryWithHandler(
                any(ThrowingRunnable.class),
                eq("revoking old applicant solicitor"),
                eq(CASE_ID),
                any(RetryErrorHandler.class)
            ),
            () -> assertNull(response.getConfirmationBody()),
            () -> assertNull(response.getConfirmationHeader())
        );
    }

    @Test
    void givenApplicantSolicitorChanged_whenAppSolicitorIsRemoved_thenRevokeApplicantSolicitorOnly() {
        FinremCaseData finremCaseData = spy(FinremCaseData.builder().build());
        when(finremCaseData.getAppSolicitorEmailIfRepresented()).thenReturn(null);

        FinremCaseData finremCaseDataBefore = spy(FinremCaseData.builder().build());
        when(finremCaseDataBefore.getAppSolicitorEmailIfRepresented()).thenReturn("old@applicant.email");

        FinremCallbackRequest callbackRequest = spy(FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, finremCaseDataBefore,
            finremCaseData));
        when(callbackRequest.hasApplicantSolicitorChanged()).thenReturn(true);

        var response = underTest.handle(callbackRequest, AUTH_TOKEN);

        ArgumentCaptor<ThrowingRunnable> captor = getThrowingRunnableCaptor();
        assertAll(
            () -> verify(retryExecutor, never()).runWithRetryWithHandler(
                any(ThrowingRunnable.class),
                eq("granting applicant solicitor"),
                eq(CASE_ID),
                any(RetryErrorHandler.class)
            ),
            () -> verify(retryExecutor).runWithRetryWithHandler(
                captor.capture(),
                eq("revoking old applicant solicitor"),
                eq(CASE_ID),
                any(RetryErrorHandler.class)
            ),
            () -> {
                captor.getValue().run();
                verify(assignPartiesAccessService).revokeApplicantSolicitor(finremCaseDataBefore);
            },
            () -> assertNull(response.getConfirmationBody()),
            () -> assertNull(response.getConfirmationHeader())
        );
    }

    @Test
    void givenApplicantSolicitorChanged_whenAppSolicitorEmailIsReplaced_thenGrantAndRevokeApplicantSolicitor() {
        FinremCaseData finremCaseData = spy(FinremCaseData.builder().build());
        when(finremCaseData.getAppSolicitorEmailIfRepresented()).thenReturn("new@applicant.email");

        FinremCaseData finremCaseDataBefore = spy(FinremCaseData.builder().build());
        when(finremCaseDataBefore.getAppSolicitorEmailIfRepresented()).thenReturn("old@applicant.email");

        FinremCallbackRequest callbackRequest = spy(FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, finremCaseDataBefore,
            finremCaseData));
        when(callbackRequest.hasApplicantSolicitorChanged()).thenReturn(true);

        var response = underTest.handle(callbackRequest, AUTH_TOKEN);

        ArgumentCaptor<ThrowingRunnable> captor = getThrowingRunnableCaptor();
        assertAll(
            () -> verify(retryExecutor).runWithRetryWithHandler(
                captor.capture(),
                eq("granting applicant solicitor"),
                eq(CASE_ID),
                any(RetryErrorHandler.class)
            ),
            () -> verify(retryExecutor).runWithRetryWithHandler(
                captor.capture(),
                eq("revoking old applicant solicitor"),
                eq(CASE_ID),
                any(RetryErrorHandler.class)
            ),
            () -> captor.getAllValues().forEach(TestSetUpUtils::runSafely),
            () -> verify(assignPartiesAccessService).grantApplicantSolicitor(finremCaseData),
            () -> verify(assignPartiesAccessService).revokeApplicantSolicitor(finremCaseDataBefore),
            () -> verifyNoMoreInteractions(assignPartiesAccessService),
            () -> assertNull(response.getConfirmationBody()),
            () -> assertNull(response.getConfirmationHeader())
        );
    }

    @ParameterizedTest
    @CsvSource({
        FAIL_GRANT_AND_REVOKE,
        FAIL_GRANT_ONLY,
        FAIL_REVOKE_ONLY
    })
    void givenApplicantSolicitorChanged_whenGrantOrRevokeApplicantSolicitorFailure_thenPopulateConfirmationBody(
        boolean grantFailure, boolean revokeFailure
    ) {
        FinremCaseData finremCaseData = spy(FinremCaseData.builder().build());
        when(finremCaseData.getAppSolicitorEmailIfRepresented()).thenReturn("new@applicant.email");

        FinremCaseData finremCaseDataBefore = spy(FinremCaseData.builder().build());
        when(finremCaseDataBefore.getAppSolicitorEmailIfRepresented()).thenReturn("old@applicant.email");

        FinremCallbackRequest callbackRequest = spy(FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, finremCaseDataBefore,
            finremCaseData));
        when(callbackRequest.hasApplicantSolicitorChanged()).thenReturn(true);

        if (grantFailure) {
            mockRunWithRetryWithHandlerInvokesFirstErrorHandler(retryExecutor, "granting applicant solicitor");
        } else {
            doNothing().when(retryExecutor).runWithRetryWithHandler(
                any(),
                eq("granting applicant solicitor"),
                any(),
                any(RetryErrorHandler.class)
            );
        }
        if (revokeFailure) {
            mockRunWithRetryWithHandlerInvokesFirstErrorHandler(retryExecutor, "revoking old applicant solicitor");
        } else {
            doNothing().when(retryExecutor).runWithRetryWithHandler(
                any(),
                eq("revoking old applicant solicitor"),
                any(),
                any(RetryErrorHandler.class)
            );
        }

        var response = underTest.handle(callbackRequest, AUTH_TOKEN);

        assertAll(
            () -> verify(retryExecutor).runWithRetryWithHandler(
                any(ThrowingRunnable.class),
                eq("granting applicant solicitor"),
                eq(CASE_ID),
                any(RetryErrorHandler.class)
            ),
            () -> verify(retryExecutor).runWithRetryWithHandler(
                any(ThrowingRunnable.class),
                eq("revoking old applicant solicitor"),
                eq(CASE_ID),
                any(RetryErrorHandler.class)
            ),
            () -> assertThat(response.getConfirmationHeader()).contains("Application amended with errors"),
            () -> assertCondition(response.getConfirmationBody(),
                "There was a problem granting access to applicant solicitor (new@applicant.email). Please grant access manually.",
                grantFailure),
            () -> assertCondition(response.getConfirmationBody(),
                "There was a problem revoking access to applicant solicitor (old@applicant.email). Please revoke access manually.",
                revokeFailure)
        );
    }
}
