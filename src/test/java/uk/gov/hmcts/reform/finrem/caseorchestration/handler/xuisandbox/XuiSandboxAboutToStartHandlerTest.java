package uk.gov.hmcts.reform.finrem.caseorchestration.handler.xuisandbox;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class XuiSandboxAboutToStartHandlerTest {

    @InjectMocks
    private XuiSandboxAboutToStartHandler handler;

    @Test
    void canHandle() {
        assertCanHandle(handler,
            Arguments.of(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.XUI_SANDBOX),
            Arguments.of(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.XUI_SANDBOX_ONE),
            Arguments.of(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.XUI_SANDBOX_TWO)
        );
    }

    @Test
    void shouldHandle() {
        FinremCallbackRequest callbackRequest = mock(FinremCallbackRequest.class);
        FinremCaseDetails finremCaseDetails = mock(FinremCaseDetails.class);
        when(finremCaseDetails.getCaseType()).thenReturn(CaseType.CONSENTED);
        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        when(finremCaseDetails.getData()).thenReturn(finremCaseData);
        when(callbackRequest.getCaseDetails()).thenReturn(finremCaseDetails);

        FinremCaseData responseData = handler.handle(callbackRequest, AUTH_TOKEN).getData();
        assertThat(responseData.getContactDetailsWrapper().getNocParty()).isNull();
    }
}
