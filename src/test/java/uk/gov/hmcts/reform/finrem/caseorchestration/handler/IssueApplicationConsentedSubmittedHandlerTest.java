package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignPartiesAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge.IssueApplicationConsentCorresponder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
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

    @Test
    void givenCase_whenHandled_thenShouldGrantRespondentSolicitor() {
        FinremCallbackRequest request = FinremCallbackRequestFactory.from();

        handler.handle(request, AUTH_TOKEN);

        verify(assignPartiesAccessService).grantRespondentSolicitor(request.getCaseDetails().getData());
    }

    @Test
    void givenFailToGrantRespondentSolicitor_whenHandled_thenShouldRetryThreeTimes() {
        FinremCallbackRequest request = FinremCallbackRequestFactory.from();

        // always fail
        doThrow(new RuntimeException("boom"))
            .when(assignPartiesAccessService)
            .grantRespondentSolicitor(any());

        handler.handle(request, AUTH_TOKEN);

        verify(assignPartiesAccessService, times(3))
            .grantRespondentSolicitor(request.getCaseDetails().getData());
        verify(issueApplicationConsentCorresponder)
            .sendCorrespondence(request.getCaseDetails(), AUTH_TOKEN);
        verifyNoMoreInteractions(assignPartiesAccessService, issueApplicationConsentCorresponder);
    }

    @Test
    void givenFailToSendCorrespondence_whenHandled_thenShouldRetryThreeTimes() {
        FinremCallbackRequest request = FinremCallbackRequestFactory.from();

        // always fail
        doThrow(new RuntimeException("boom"))
            .when(issueApplicationConsentCorresponder)
            .sendCorrespondence(any(), eq(AUTH_TOKEN));

        handler.handle(request, AUTH_TOKEN);

        verify(assignPartiesAccessService)
            .grantRespondentSolicitor(request.getCaseDetails().getData());
        verify(issueApplicationConsentCorresponder, times(3))
            .sendCorrespondence(request.getCaseDetails(), AUTH_TOKEN);
        verifyNoMoreInteractions(assignPartiesAccessService, issueApplicationConsentCorresponder);
    }
}
