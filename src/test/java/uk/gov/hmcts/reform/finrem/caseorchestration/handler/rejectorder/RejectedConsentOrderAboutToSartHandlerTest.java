package uk.gov.hmcts.reform.finrem.caseorchestration.handler.rejectorder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.RefusalOrderDocumentService;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class RejectedConsentOrderAboutToSartHandlerTest {

    @InjectMocks
    private RejectedConsentOrderAboutToSartHandler handler;
    @Mock
    private RefusalOrderDocumentService refusalOrderDocumentService;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.REJECT_ORDER);
    }

    @Test
    void given_case_when_order_not_approved_then_reject_order() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from();
        handler.handle(callbackRequest, AUTH_TOKEN);
        verify(refusalOrderDocumentService).setDefaults(callbackRequest.getCaseDetails().getData(), AUTH_TOKEN);
    }
}
