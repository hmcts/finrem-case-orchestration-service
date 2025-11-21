package uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitorcreatecase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnStartDefaultValueService;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.SOLICITOR_CREATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class SolicitorCreateConsentedAboutToStartHandlerTest {

    @InjectMocks
    private SolicitorCreateConsentedAboutToStartHandler handler;

    @Mock
    private OnStartDefaultValueService onStartDefaultValueService;


    @Test
    void testHandlerCanHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_START, CaseType.CONSENTED, SOLICITOR_CREATE);
    }

    @Test
    void givenAnyCase_whenHandled_thenDefaultCivilPartnershipFieldSet() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from();
        handler.handle(callbackRequest, AUTH_TOKEN);
        verify(onStartDefaultValueService).defaultCivilPartnershipField(callbackRequest);
    }
}
