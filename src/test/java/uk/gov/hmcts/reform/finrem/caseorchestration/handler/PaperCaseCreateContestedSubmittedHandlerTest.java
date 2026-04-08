package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignPartiesAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CreateCaseService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.ThrowingRunnable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.NEW_PAPER_CASE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class PaperCaseCreateContestedSubmittedHandlerTest {

    @InjectMocks
    private PaperCaseCreateContestedSubmittedHandler handler;
    @Mock
    private CreateCaseService createCaseService;
    @Mock
    private AssignPartiesAccessService assignPartiesAccessService;
    @Mock
    private RetryExecutor retryExecutor;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, SUBMITTED, CONTESTED, NEW_PAPER_CASE);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldExecuteSetSupplementaryOnly_whenApplicantSolicitorEmailIsBlankOrEmpty(String email) throws Exception {

        FinremCaseDetails caseDetails = mock(FinremCaseDetails.class);
        when(caseDetails.getCaseIdAsString()).thenReturn(CASE_ID);
        FinremCaseData caseData = mock(FinremCaseData.class);
        when(caseData.getAppSolicitorEmailIfRepresented()).thenReturn(email);
        when(caseDetails.getData()).thenReturn(caseData);

        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder()
            .caseDetails(caseDetails).build();
        handler.handle(callbackRequest, AUTH_TOKEN);

        ArgumentCaptor<ThrowingRunnable> captor = TestSetUpUtils.getThrowingRunnableCaptor();
        verify(retryExecutor).runWithRetry(captor.capture(), eq("setting supplementary data"), eq(CASE_ID));
        verifySettingSupplementaryDataRun(captor, callbackRequest);
        verifyNoMoreInteractions(retryExecutor);
    }

    @Test
    void shouldExecuteSetSupplementaryAndGrantApplicantSolicitor_whenApplicantSolicitorEmailProvided() throws Exception {

        FinremCaseDetails caseDetails = mock(FinremCaseDetails.class);
        when(caseDetails.getCaseIdAsString()).thenReturn(CASE_ID);
        FinremCaseData caseData = mock(FinremCaseData.class);
        when(caseData.getAppSolicitorEmailIfRepresented()).thenReturn(TEST_SOLICITOR_EMAIL);
        when(caseDetails.getData()).thenReturn(caseData);

        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder()
            .caseDetails(caseDetails).build();
        handler.handle(callbackRequest, AUTH_TOKEN);

        ArgumentCaptor<ThrowingRunnable> settingSupplementaryDataCaptor = TestSetUpUtils.getThrowingRunnableCaptor();
        verify(retryExecutor).runWithRetry(settingSupplementaryDataCaptor.capture(), eq("setting supplementary data"), eq(CASE_ID));
        verifySettingSupplementaryDataRun(settingSupplementaryDataCaptor, callbackRequest);

        ArgumentCaptor<ThrowingRunnable> grantingApplicantSolicitorCaptor = TestSetUpUtils.getThrowingRunnableCaptor();
        verify(retryExecutor).runWithRetry(grantingApplicantSolicitorCaptor.capture(), eq("granting applicant solicitor"), eq(CASE_ID));
        verifyGrantingApplicantSolicitorRun(grantingApplicantSolicitorCaptor, caseData);

        verifyNoMoreInteractions(retryExecutor);
    }

    @Test
    void shouldReportError_whenSetSupplementaryDataThrowsAnyException() throws Exception {

        FinremCaseDetails caseDetails = mock(FinremCaseDetails.class);
        when(caseDetails.getCaseIdAsString()).thenReturn(CASE_ID);
        FinremCaseData caseData = mock(FinremCaseData.class);
        when(caseData.getAppSolicitorEmailIfRepresented()).thenReturn(TEST_SOLICITOR_EMAIL);
        when(caseDetails.getData()).thenReturn(caseData);

        doThrow(new RuntimeException("BOOM"))
            .when(retryExecutor).runWithRetry(any(ThrowingRunnable.class), eq("setting supplementary data"), eq(CASE_ID));
        doAnswer(invocation -> {
            ThrowingRunnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(retryExecutor).runWithRetry(any(ThrowingRunnable.class), eq("granting applicant solicitor"), eq(CASE_ID));

        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder()
            .caseDetails(caseDetails).build();
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);
        assertThat(response).extracting(
            GenericAboutToStartOrSubmitCallbackResponse::getConfirmationHeader,
            GenericAboutToStartOrSubmitCallbackResponse::getConfirmationBody
        ).containsExactly(
            "# Paper Case Created with Errors",
            "<ul><li><h2>There was a problem setting supplementary data.</h2></li></ul>"
        );

        verify(retryExecutor).runWithRetry(any(ThrowingRunnable.class), eq("setting supplementary data"), eq(CASE_ID));
        verify(retryExecutor).runWithRetry(any(ThrowingRunnable.class), eq("granting applicant solicitor"), eq(CASE_ID));
        verifyNoMoreInteractions(retryExecutor);

        verifyNoInteractions(createCaseService);
        verify(assignPartiesAccessService).grantApplicantSolicitor(caseData);
    }

    @Test
    void shouldReportError_whenGrantApplicantSolicitorThrowsAnyException() throws Exception {

        FinremCaseDetails caseDetails = mock(FinremCaseDetails.class);
        when(caseDetails.getCaseIdAsString()).thenReturn(CASE_ID);
        FinremCaseData caseData = mock(FinremCaseData.class);
        when(caseData.getAppSolicitorEmailIfRepresented()).thenReturn(TEST_SOLICITOR_EMAIL);
        when(caseDetails.getData()).thenReturn(caseData);

        doThrow(new RuntimeException("BOOM"))
            .when(retryExecutor).runWithRetry(any(ThrowingRunnable.class), eq("granting applicant solicitor"), eq(CASE_ID));
        doAnswer(invocation -> {
            ThrowingRunnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(retryExecutor).runWithRetry(any(ThrowingRunnable.class), eq("setting supplementary data"), eq(CASE_ID));

        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder()
            .caseDetails(caseDetails).build();
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);
        assertThat(response).extracting(
            GenericAboutToStartOrSubmitCallbackResponse::getConfirmationHeader,
            GenericAboutToStartOrSubmitCallbackResponse::getConfirmationBody
        ).containsExactly(
            "# Paper Case Created with Errors",
            "<ul><li><h2>There was a problem granting access to applicant solicitor: %s</h2></li></ul>".formatted(TEST_SOLICITOR_EMAIL)
        );

        verify(retryExecutor).runWithRetry(any(ThrowingRunnable.class), eq("setting supplementary data"), eq(CASE_ID));
        verify(retryExecutor).runWithRetry(any(ThrowingRunnable.class), eq("granting applicant solicitor"), eq(CASE_ID));
        verifyNoMoreInteractions(retryExecutor);

        verify(createCaseService).setSupplementaryData(any(FinremCallbackRequest.class), eq(AUTH_TOKEN));
        verifyNoInteractions(assignPartiesAccessService);
    }

    private void verifySettingSupplementaryDataRun(ArgumentCaptor<ThrowingRunnable> captor, FinremCallbackRequest request)
        throws Exception {
        captor.getValue().run();
        verify(createCaseService).setSupplementaryData(request, AUTH_TOKEN);
        verifyNoMoreInteractions(createCaseService);
    }

    private void verifyGrantingApplicantSolicitorRun(ArgumentCaptor<ThrowingRunnable> captor, FinremCaseData finremCaseData)
        throws Exception {
        captor.getValue().run();
        verify(assignPartiesAccessService).grantApplicantSolicitor(finremCaseData);
        verifyNoMoreInteractions(assignPartiesAccessService);
    }
}
