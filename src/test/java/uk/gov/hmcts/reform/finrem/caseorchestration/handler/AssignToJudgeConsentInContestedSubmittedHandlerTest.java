package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudgeconsentincontested.FinremAssignToJudgeConsentInContestedCorresponder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class AssignToJudgeConsentInContestedSubmittedHandlerTest {

    private AssignToJudgeConsentInContestedSubmittedHandler handler;

    @Mock
    private FinremAssignToJudgeConsentInContestedCorresponder corresponder;
    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Before
    public void setup() {
        handler =  new AssignToJudgeConsentInContestedSubmittedHandler(finremCaseDetailsMapper, corresponder);
    }

    @Test
    public void givenACcdCallbackAssignToJudgeConsentInConsented_WhenCanHandleCalled_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.ASSIGN_TO_JUDGE_CONSENT),
            is(true));
    }

    @Test
    public void givenACcdCallbackAboutToSubmit_WhenCanHandleCalled_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.ASSIGN_TO_JUDGE_CONSENT),
            is(false));
    }

    @Test
    public void givenACcdCallbackSubmittedConsented_WhenCanHandleCalled_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONSENTED, EventType.ASSIGN_TO_JUDGE_CONSENT),
            is(false));
    }

    @Test
    public void givenACcdCallbackSubmittedSolicitorCreate_WhenCanHandleCalled_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.SOLICITOR_CREATE),
            is(false));
    }

    @Test
    public void givenACcdCallbackAssignToJudgeConsentInConsented_WhenHandle_thenSendCorresponder() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest();
        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(corresponder, times(1)).sendCorrespondence(eq(callbackRequest.getCaseDetails()), any());
    }

    private FinremCallbackRequest buildFinremCallbackRequest() {
        FinremCaseData caseData = FinremCaseData.builder().build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123L).data(caseData).build();
        return FinremCallbackRequest.builder().eventType(EventType.ASSIGN_TO_JUDGE_CONSENT).caseDetails(caseDetails).build();
    }

}
