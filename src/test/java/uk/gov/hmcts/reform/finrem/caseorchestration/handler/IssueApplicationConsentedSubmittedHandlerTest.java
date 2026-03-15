package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignPartiesAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge.IssueApplicationConsentCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.ISSUE_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class IssueApplicationConsentedSubmittedHandlerTest {

    @InjectMocks
    private IssueApplicationConsentedSubmittedHandler handler;
    @Mock
    private IssueApplicationConsentCorresponder issueApplicationConsentCorresponder;
    @Mock
    private AssignPartiesAccessService assignPartiesAccessService;
    @Mock
    private RetryExecutor retryExecutor;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, SUBMITTED, CONSENTED, ISSUE_APPLICATION);
    }

    @Test
    void givenCase_whenHandled_thenShouldSendCorrespondence() {
        FinremCallbackRequest request = FinremCallbackRequestFactory.from();

        handler.handle(request, AUTH_TOKEN);

        verify(issueApplicationConsentCorresponder).sendCorrespondence(request.getCaseDetails(), AUTH_TOKEN);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldExecuteSendCorrespondenceOnly_whenRespondentRepresentedAndEmailIsBlankOrEmpty(String email) throws Exception {

        FinremCaseDetails caseDetails = mock(FinremCaseDetails.class);
        when(caseDetails.getCaseIdAsString()).thenReturn(CASE_ID);
        FinremCaseData caseData = mock(FinremCaseData.class);

        when(caseDetails.getData()).thenReturn(caseData);
        ContactDetailsWrapper contactDetailsWrapper = mock(ContactDetailsWrapper.class);
        when(contactDetailsWrapper.getConsentedRespondentRepresented()).thenReturn(YesOrNo.YES);
        when(caseData.getContactDetailsWrapper()).thenReturn(contactDetailsWrapper);
        when(caseData.getRespondentSolicitorEmail()).thenReturn(email);

        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder()
            .caseDetails(caseDetails).build();
        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(retryExecutor).runWithRetry(any(), eq("sending correspondence"), eq(CASE_ID));
        verifyNoMoreInteractions(retryExecutor);
    }
}
