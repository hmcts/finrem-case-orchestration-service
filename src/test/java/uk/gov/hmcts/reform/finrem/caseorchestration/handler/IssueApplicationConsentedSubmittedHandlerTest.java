package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignPartiesAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge.IssueApplicationConsentCorresponder;

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
    private IssueApplicationConsentedSubmittedHandler handlerUnderTest;

    @Mock
    private IssueApplicationConsentCorresponder issueApplicationConsentCorresponder;

    @Mock
    private AssignPartiesAccessService assignPartiesAccessService;

    @Test
    void testCanHandle() {
        assertCanHandle(handlerUnderTest, SUBMITTED, CONSENTED, ISSUE_APPLICATION);
    }

    @Test
    void testHandle() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        handlerUnderTest.handle(callbackRequest, AUTH_TOKEN);
        verify(issueApplicationConsentCorresponder).sendCorrespondence(callbackRequest.getCaseDetails(), AUTH_TOKEN);
    }

    @Test
    void givenCase_whenHandled_thenShouldAssignPartiesAccessBeforeSendCorrespondence() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();

        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = finremCaseDetails.getData();

        // Act
        handlerUnderTest.handle(callbackRequest, AUTH_TOKEN);

        InOrder inOrder = Mockito.inOrder(assignPartiesAccessService, issueApplicationConsentCorresponder);

        inOrder.verify(assignPartiesAccessService).grantApplicantSolicitor(caseData);
        inOrder.verify(assignPartiesAccessService).grantRespondentSolicitor(caseData);
        inOrder.verify(issueApplicationConsentCorresponder).sendCorrespondence(finremCaseDetails, AUTH_TOKEN);
        verifyNoMoreInteractions(assignPartiesAccessService);
        verifyNoMoreInteractions(issueApplicationConsentCorresponder);
    }

    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequestFactory.from(FinremCaseData.builder().ccdCaseType(CONSENTED).build());
    }
}
