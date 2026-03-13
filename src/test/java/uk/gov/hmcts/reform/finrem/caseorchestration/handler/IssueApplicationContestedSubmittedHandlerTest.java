package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
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

        verify(retryExecutor).runWithRetry(any(), eq("sending correspondence"), eq(CASE_ID));
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

        verify(retryExecutor).runWithRetry(any(), eq("sending correspondence"), eq(CASE_ID));
        verify(retryExecutor).runWithRetry(any(), eq("granting respondent solicitor"), eq(CASE_ID));
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

        doThrow(new RuntimeException("BOOM"))
            .when(retryExecutor).runWithRetry(any(), eq("granting respondent solicitor"), eq(CASE_ID));
        doAnswer(invocation -> {
            ThrowingRunnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(retryExecutor).runWithRetry(any(), eq("sending correspondence"), eq(CASE_ID));

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

        verify(retryExecutor).runWithRetry(any(), eq("sending correspondence"), eq(CASE_ID));
        verify(retryExecutor).runWithRetry(any(), eq("granting respondent solicitor"), eq(CASE_ID));
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

        doThrow(new RuntimeException("BOOM"))
            .when(retryExecutor).runWithRetry(any(), eq("sending correspondence"), eq(CASE_ID));
        doAnswer(invocation -> {
            ThrowingRunnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(retryExecutor).runWithRetry(any(), eq("granting respondent solicitor"), eq(CASE_ID));

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

        verify(retryExecutor).runWithRetry(any(), eq("sending correspondence"), eq(CASE_ID));
        verify(retryExecutor).runWithRetry(any(), eq("granting respondent solicitor"), eq(CASE_ID));
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

        doThrow(new RuntimeException("BOOM"))
            .when(retryExecutor).runWithRetry(any(), eq("sending correspondence"), eq(CASE_ID));
        doThrow(new RuntimeException("BOOM"))
            .when(retryExecutor).runWithRetry(any(), eq("granting respondent solicitor"), eq(CASE_ID));

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

        verify(retryExecutor).runWithRetry(any(), eq("sending correspondence"), eq(CASE_ID));
        verify(retryExecutor).runWithRetry(any(), eq("granting respondent solicitor"), eq(CASE_ID));
        verifyNoInteractions(corresponder, assignPartiesAccessService);
        verifyNoMoreInteractions(retryExecutor);
    }
}
