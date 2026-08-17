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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnStartDefaultValueService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class AmendCaseConsentedAboutToStartHandlerTest {

    @InjectMocks
    private AmendCaseConsentedAboutToStartHandler handler;

    @Mock
    private OnStartDefaultValueService onStartDefaultValueService;

    @Test
    void testHandlerCanHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.AMEND_CASE);
    }

    @Test
    void shouldPopulateDefaultCivilPartnership() {
        FinremCallbackRequest finremCallbackRequest = FinremCallbackRequestFactory.from();

        var response = handler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(onStartDefaultValueService).defaultCivilPartnershipField(finremCallbackRequest);
        assertThat(response.getData()).isEqualTo(finremCallbackRequest.getFinremCaseData());
    }
}
