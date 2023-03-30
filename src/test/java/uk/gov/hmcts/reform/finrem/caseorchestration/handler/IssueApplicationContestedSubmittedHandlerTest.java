package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.issueapplication.IssueApplicationContestedEmailCorresponder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class IssueApplicationContestedSubmittedHandlerTest {

    public static final String AUTH_TOKEN = "tokien:)";

    @InjectMocks
    private IssueApplicationContestedSubmittedHandler handler;
    @Mock
    private IssueApplicationContestedEmailCorresponder corresponder;

    @Test
    void givenContestedCase_whenCaseTypeIsConsented_thenHandlerWillNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONSENTED, EventType.ISSUE_APPLICATION),
            is(false));
    }

    @Test
    void givenContestedCase_whenCallbackIsAboutToSubmit_thenHandlerWillNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.ISSUE_APPLICATION),
            is(false));
    }

    @Test
    void givenContestedCase_whenWrongEvent_thenHandlerWillNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.CLOSE),
            is(false));
    }

    @Test
    void givenContestedCase_whenAllCorrect_thenHandlerWillHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.ISSUE_APPLICATION),
            is(true));
    }

    @Test
    void givenCase_whenIssueApplication_thenSendAppropriateCorrespondenceToApplicationSolicitor() {
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123L).data(FinremCaseData.builder().build()).build();
        FinremCallbackRequest callbackRequest
            = FinremCallbackRequest.builder().eventType(EventType.ISSUE_APPLICATION).caseDetails(caseDetails).build();

        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(corresponder, times(1)).sendCorrespondence(callbackRequest.getCaseDetails());
    }
}