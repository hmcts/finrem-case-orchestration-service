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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.RefusalOrderDocumentService;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;

@ExtendWith(MockitoExtension.class)
class RejectedConsentOrderAboutToSubmitHandlerTest {

    @InjectMocks
    private RejectedConsentOrderAboutToSubmitHandler handler;
    @Mock
    private RefusalOrderDocumentService refusalOrderDocumentService;

    private static final String AUTH_TOKEN = "4d73f8d4-2a8d-48e2-af91-11cbaa642345";

    @Test
    void given_case_whenEventRejectedOrder_thenCanHandle() {
        assertTrue(handler.canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.REJECT_ORDER));
    }


    @Test
    void given_case_when_wrong_callback_then_case_can_not_handle() {
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.REJECT_ORDER));
    }

    @Test
    void given_case_when_wrong_event_type_then_case_can_not_handle() {
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.CLOSE));
    }

    @Test
    void given_case_when_all_wrong_then_case_can_not_handle() {
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.CLOSE));
    }

    @Test
    void given_case_when_order_not_approved_then_reject_order() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();

        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(refusalOrderDocumentService).processConsentOrderNotApproved(callbackRequest.getCaseDetails(), AUTH_TOKEN);
    }

    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.REJECT_ORDER)
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONSENTED)
                .data(FinremCaseData.builder().ccdCaseType(CONSENTED).build()).build())
            .build();
    }
}