package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignPartiesAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenerateCoverSheetService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.issueapplication.IssueApplicationContestedEmailCorresponder;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.getThrowingRunnableCaptor;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class IssueApplicationContestedSubmittedHandlerTest {

    @InjectMocks
    private IssueApplicationContestedSubmittedHandler handler;
    @Mock
    private IssueApplicationContestedEmailCorresponder corresponder;
    @Mock
    private AssignPartiesAccessService assignPartiesAccessService;
    @Mock
    private GenerateCoverSheetService generateCoverSheetService;
    @Mock
    private RetryExecutor retryExecutor;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.ISSUE_APPLICATION);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldExecuteSendCorrespondenceOnly_whenRespondentRepresentedAndEmailIsBlankOrEmpty(String email) throws Exception {

        FinremCaseDetails caseDetails = mock(FinremCaseDetails.class);
        when(caseDetails.getCaseIdAsString()).thenReturn(CASE_ID);
        FinremCaseData caseData = mock(FinremCaseData.class);

        when(caseDetails.getData()).thenReturn(caseData);
        ContactDetailsWrapper contactDetailsWrapper = mock(ContactDetailsWrapper.class);
        when(contactDetailsWrapper.getContestedRespondentRepresented()).thenReturn(YesOrNo.YES);
        when(caseData.getContactDetailsWrapper()).thenReturn(contactDetailsWrapper);
        when(caseData.getRespondentSolicitorEmail()).thenReturn(email);

        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder()
            .caseDetails(caseDetails).build();
        handler.handle(callbackRequest, AUTH_TOKEN);

        ArgumentCaptor<ThrowingRunnable> captor = getThrowingRunnableCaptor();
        verify(retryExecutor).runWithRetry(captor.capture(), eq("sending correspondence"), eq(CASE_ID));

        verifyCoversheetGenerationRun(caseDetails);

        verifySendCorrespondenceRun(captor, caseDetails);
        verifyNoMoreInteractions(retryExecutor);
    }

    @Test
    void shouldGrantRespondentSolicitor_whenRespondentRepresentedAndEmailIProvided() throws Exception {

        FinremCaseDetails caseDetails = mock(FinremCaseDetails.class);
        when(caseDetails.getCaseIdAsString()).thenReturn(CASE_ID);
        FinremCaseData caseData = mock(FinremCaseData.class);

        when(caseDetails.getData()).thenReturn(caseData);
        ContactDetailsWrapper contactDetailsWrapper = mock(ContactDetailsWrapper.class);
        when(contactDetailsWrapper.getContestedRespondentRepresented()).thenReturn(YesOrNo.YES);
        when(caseData.getContactDetailsWrapper()).thenReturn(contactDetailsWrapper);
        when(caseData.getRespondentSolicitorEmail()).thenReturn(TEST_SOLICITOR_EMAIL);
        when(caseData.getCcdCaseId()).thenReturn(CASE_ID);

        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder()
            .caseDetails(caseDetails).build();
        handler.handle(callbackRequest, AUTH_TOKEN);

        ArgumentCaptor<ThrowingRunnable> sendingCorrespondenceCaptor = getThrowingRunnableCaptor();
        verify(retryExecutor).runWithRetry(sendingCorrespondenceCaptor.capture(), eq("sending correspondence"), eq(CASE_ID));
        verifySendCorrespondenceRun(sendingCorrespondenceCaptor, caseDetails);

        verifyCoversheetGenerationRun(caseDetails);

        ArgumentCaptor<ThrowingRunnable> grantingRespondentCaptor = getThrowingRunnableCaptor();
        verify(retryExecutor).runWithRetry(grantingRespondentCaptor.capture(), eq("granting respondent solicitor"), eq(CASE_ID));
        verifyGrantRespondentSolicitorRun(grantingRespondentCaptor, caseData);
        verifyNoMoreInteractions(retryExecutor);
    }

    @Test
    void shouldReportError_whenGrantingRespondentThrowsAnyException() throws Exception {

        FinremCaseDetails caseDetails = mock(FinremCaseDetails.class);
        when(caseDetails.getCaseIdAsString()).thenReturn(CASE_ID);
        FinremCaseData caseData = mock(FinremCaseData.class);

        when(caseDetails.getData()).thenReturn(caseData);
        ContactDetailsWrapper contactDetailsWrapper = mock(ContactDetailsWrapper.class);
        when(contactDetailsWrapper.getContestedRespondentRepresented()).thenReturn(YesOrNo.YES);
        when(caseData.getContactDetailsWrapper()).thenReturn(contactDetailsWrapper);
        when(caseData.getRespondentSolicitorEmail()).thenReturn(TEST_SOLICITOR_EMAIL);
        when(caseData.getCcdCaseId()).thenReturn(CASE_ID);

        doAnswer(invocation -> {
            ThrowingRunnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(retryExecutor).runWithRetry(any(ThrowingRunnable.class), eq("Case Issued - generating applicant cover sheet"), eq(CASE_ID));

        doAnswer(invocation -> {
            ThrowingRunnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(retryExecutor).runWithRetry(any(ThrowingRunnable.class), eq("Case Issued - generating respondent cover sheet"), eq(CASE_ID));

        doThrow(new RuntimeException("BOOM"))
            .when(retryExecutor).runWithRetry(any(ThrowingRunnable.class), eq("granting respondent solicitor"), eq(CASE_ID));

        doAnswer(invocation -> {
            ThrowingRunnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(retryExecutor).runWithRetry(any(ThrowingRunnable.class), eq("sending correspondence"), eq(CASE_ID));

        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder()
            .caseDetails(caseDetails).build();
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);
        assertThat(response).extracting(
            GenericAboutToStartOrSubmitCallbackResponse::getConfirmationHeader,
            GenericAboutToStartOrSubmitCallbackResponse::getConfirmationBody
        ).containsExactly(
            "# Application Issued with Errors",
            "<ul><li><h2>There was a problem granting access to respondent solicitor: testSolicitor@email.com</h2></li></ul>"
        );

        verify(generateCoverSheetService).generateAndSetApplicantCoverSheet(caseDetails, AUTH_TOKEN);
        verify(generateCoverSheetService).generateAndSetRespondentCoverSheet(caseDetails, AUTH_TOKEN);
        verify(retryExecutor).runWithRetry(any(ThrowingRunnable.class), eq("sending correspondence"), eq(CASE_ID));
        verify(retryExecutor).runWithRetry(any(ThrowingRunnable.class), eq("granting respondent solicitor"), eq(CASE_ID));
        verify(corresponder).sendCorrespondence(caseDetails);
        verifyNoInteractions(assignPartiesAccessService);
        verifyNoMoreInteractions(retryExecutor);
    }

    @Test
    void shouldReportError_whenSendingCorrespondenceThrowsAnyException() throws Exception {

        FinremCaseDetails caseDetails = mock(FinremCaseDetails.class);
        when(caseDetails.getCaseIdAsString()).thenReturn(CASE_ID);
        FinremCaseData caseData = mock(FinremCaseData.class);

        when(caseDetails.getData()).thenReturn(caseData);
        ContactDetailsWrapper contactDetailsWrapper = mock(ContactDetailsWrapper.class);
        when(contactDetailsWrapper.getContestedRespondentRepresented()).thenReturn(YesOrNo.YES);
        when(caseData.getContactDetailsWrapper()).thenReturn(contactDetailsWrapper);
        when(caseData.getRespondentSolicitorEmail()).thenReturn(TEST_SOLICITOR_EMAIL);
        when(caseData.getCcdCaseId()).thenReturn(CASE_ID);

        doAnswer(invocation -> {
            ThrowingRunnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(retryExecutor).runWithRetry(any(ThrowingRunnable.class), eq("Case Issued - generating applicant cover sheet"), eq(CASE_ID));

        doAnswer(invocation -> {
            ThrowingRunnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(retryExecutor).runWithRetry(any(ThrowingRunnable.class), eq("Case Issued - generating respondent cover sheet"), eq(CASE_ID));

        doThrow(new RuntimeException("BOOM"))
            .when(retryExecutor).runWithRetry(any(ThrowingRunnable.class), eq("sending correspondence"), eq(CASE_ID));

        doAnswer(invocation -> {
            ThrowingRunnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(retryExecutor).runWithRetry(any(ThrowingRunnable.class), eq("granting respondent solicitor"), eq(CASE_ID));

        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder()
            .caseDetails(caseDetails).build();
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);
        assertThat(response).extracting(
            GenericAboutToStartOrSubmitCallbackResponse::getConfirmationHeader,
            GenericAboutToStartOrSubmitCallbackResponse::getConfirmationBody
        ).containsExactly(
            "# Application Issued with Errors",
            "<ul><li><h2>There was a problem sending correspondence.</h2></li></ul>"
        );

        verify(generateCoverSheetService).generateAndSetApplicantCoverSheet(caseDetails, AUTH_TOKEN);
        verify(generateCoverSheetService).generateAndSetRespondentCoverSheet(caseDetails, AUTH_TOKEN);
        verify(retryExecutor).runWithRetry(any(ThrowingRunnable.class), eq("sending correspondence"), eq(CASE_ID));
        verify(retryExecutor).runWithRetry(any(ThrowingRunnable.class), eq("granting respondent solicitor"), eq(CASE_ID));
        verifyNoInteractions(corresponder);
        verify(assignPartiesAccessService).grantRespondentSolicitor(caseData);
        verifyNoMoreInteractions(retryExecutor);
    }

    @Test
    void shouldReportError_whenMultipleExceptionThrown() throws Exception {

        FinremCaseDetails caseDetails = mock(FinremCaseDetails.class);
        when(caseDetails.getCaseIdAsString()).thenReturn(CASE_ID);
        FinremCaseData caseData = mock(FinremCaseData.class);

        when(caseDetails.getData()).thenReturn(caseData);
        ContactDetailsWrapper contactDetailsWrapper = mock(ContactDetailsWrapper.class);
        when(contactDetailsWrapper.getContestedRespondentRepresented()).thenReturn(YesOrNo.YES);
        when(caseData.getContactDetailsWrapper()).thenReturn(contactDetailsWrapper);
        when(caseData.getRespondentSolicitorEmail()).thenReturn(TEST_SOLICITOR_EMAIL);
        when(caseData.getCcdCaseId()).thenReturn(CASE_ID);

        doAnswer(invocation -> {
            ThrowingRunnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(retryExecutor).runWithRetry(any(ThrowingRunnable.class), eq("Case Issued - generating applicant cover sheet"), eq(CASE_ID));

        doAnswer(invocation -> {
            ThrowingRunnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(retryExecutor).runWithRetry(any(ThrowingRunnable.class), eq("Case Issued - generating respondent cover sheet"), eq(CASE_ID));

        doThrow(new RuntimeException("BOOM"))
            .when(retryExecutor).runWithRetry(any(ThrowingRunnable.class), eq("sending correspondence"), eq(CASE_ID));

        doThrow(new RuntimeException("BOOM"))
            .when(retryExecutor).runWithRetry(any(ThrowingRunnable.class), eq("granting respondent solicitor"), eq(CASE_ID));

        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder()
            .caseDetails(caseDetails).build();
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);
        assertThat(response).extracting(
            GenericAboutToStartOrSubmitCallbackResponse::getConfirmationHeader,
            GenericAboutToStartOrSubmitCallbackResponse::getConfirmationBody
        ).containsExactly(
            "# Application Issued with Errors",
            "<ul>"
                + "<li><h2>There was a problem granting access to respondent solicitor: testSolicitor@email.com</h2></li>"
                + "<li><h2>There was a problem sending correspondence.</h2></li>"
                + "</ul>"
        );

        verify(generateCoverSheetService).generateAndSetApplicantCoverSheet(caseDetails, AUTH_TOKEN);
        verify(generateCoverSheetService).generateAndSetRespondentCoverSheet(caseDetails, AUTH_TOKEN);
        verify(retryExecutor).runWithRetry(any(ThrowingRunnable.class), eq("sending correspondence"), eq(CASE_ID));
        verify(retryExecutor).runWithRetry(any(ThrowingRunnable.class), eq("granting respondent solicitor"), eq(CASE_ID));
        verifyNoInteractions(corresponder, assignPartiesAccessService);
        verifyNoMoreInteractions(retryExecutor);
    }

    private void verifyCoversheetGenerationRun(FinremCaseDetails finremCaseDetails)
        throws Exception {

        ArgumentCaptor<ThrowingRunnable> applicantCaptor = getThrowingRunnableCaptor();
        ArgumentCaptor<ThrowingRunnable> respondentCaptor = getThrowingRunnableCaptor();

        verify(retryExecutor).runWithRetry(applicantCaptor.capture(),
            eq("Case Issued - generating applicant cover sheet"), eq(CASE_ID));
        verify(retryExecutor).runWithRetry(respondentCaptor.capture(),
            eq("Case Issued - generating respondent cover sheet"), eq(CASE_ID));

        applicantCaptor.getValue().run();
        respondentCaptor.getValue().run();
        verify(generateCoverSheetService).generateAndSetApplicantCoverSheet(finremCaseDetails, AUTH_TOKEN);
        verify(generateCoverSheetService).generateAndSetRespondentCoverSheet(finremCaseDetails, AUTH_TOKEN);
        verifyNoMoreInteractions(generateCoverSheetService);
    }

    private void verifySendCorrespondenceRun(ArgumentCaptor<ThrowingRunnable> captor, FinremCaseDetails finremCaseDetails)
        throws Exception {
        captor.getValue().run();
        verify(corresponder).sendCorrespondence(finremCaseDetails);
        verifyNoMoreInteractions(corresponder);
    }

    private void verifyGrantRespondentSolicitorRun(ArgumentCaptor<ThrowingRunnable> captor, FinremCaseData finremCaseData)
        throws Exception {
        captor.getValue().run();
        verify(assignPartiesAccessService).grantRespondentSolicitor(finremCaseData);
        verifyNoMoreInteractions(assignPartiesAccessService);
    }
}
