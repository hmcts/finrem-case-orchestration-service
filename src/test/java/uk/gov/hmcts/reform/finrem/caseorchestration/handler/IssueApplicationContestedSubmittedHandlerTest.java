package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignPartiesAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.issueapplication.IssueApplicationContestedEmailCorresponder;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class IssueApplicationContestedSubmittedHandlerTest {

    @InjectMocks
    private IssueApplicationContestedSubmittedHandler handler;
    @Mock
    private IssueApplicationContestedEmailCorresponder corresponder;
    @Mock
    private AssignPartiesAccessService assignPartiesAccessService;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.ISSUE_APPLICATION);
    }

    @Test
    void givenCase_whenHandled_thenShouldSendCorrespondence() {
        FinremCallbackRequest request = FinremCallbackRequestFactory.from();

        handler.handle(request, AUTH_TOKEN);

        verify(corresponder).sendCorrespondence(request.getCaseDetails());
    }

    @Test
    void givenCase_whenHandled_thenShouldGrantRespondentSolicitor() {
        FinremCaseData caseData = mock(FinremCaseData.class);
        FinremCallbackRequest request = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, caseData);

        handler.handle(request, AUTH_TOKEN);

        verify(assignPartiesAccessService).grantRespondentSolicitor(caseData);
    }
}
