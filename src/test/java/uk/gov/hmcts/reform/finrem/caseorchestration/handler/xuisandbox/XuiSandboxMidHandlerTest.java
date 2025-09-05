package uk.gov.hmcts.reform.finrem.caseorchestration.handler.xuisandbox;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;

import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class XuiSandboxMidHandlerTest {

    @InjectMocks
    private XuiSandboxMidHandler handler;

    @Test
    void canHandle() {
        assertCanHandle(handler,
            Arguments.of(CallbackType.MID_EVENT, CaseType.CONSENTED, EventType.XUI_SANDBOX),
            Arguments.of(CallbackType.MID_EVENT, CaseType.CONSENTED, EventType.XUI_SANDBOX_ONE),
            Arguments.of(CallbackType.MID_EVENT, CaseType.CONSENTED, EventType.XUI_SANDBOX_TWO)
        );
    }
}
