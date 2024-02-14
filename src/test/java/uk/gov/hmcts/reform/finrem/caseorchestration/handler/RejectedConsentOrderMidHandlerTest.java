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
class RejectedConsentOrderMidHandlerTest {

    @InjectMocks
    private RejectedConsentOrderMidHandler handler;
    @Mock
    private RefusalOrderDocumentService refusalOrderDocumentService;

    private static final String AUTH_TOKEN = "Token-:";

    @Test
    void given_case_whenEventRejectedOrder_thenCanHandle() {
        assertTrue(handler.canHandle(CallbackType.MID_EVENT, CaseType.CONSENTED, EventType.REJECT_ORDER));
    }


    @Test
    void given_case_when_wrong_callback_then_case_can_not_handle() {
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.REJECT_ORDER));
    }

    @Test
    void given_case_when_wrong_event_type_then_case_can_not_handle() {
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.CLOSE));
    }

    @Test
    void given_case_when_all_wrong_then_case_can_not_handle() {
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.CLOSE));
    }


    @Test
    void given_case_when_order_not_approved_then_reject_order() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        handler.handle(callbackRequest, AUTH_TOKEN);
        verify(refusalOrderDocumentService).previewConsentOrderNotApproved(AUTH_TOKEN, callbackRequest.getCaseDetails());
    }

    private FinremCallbackRequest buildCallbackRequest() {
        FinremCaseData caseData = new FinremCaseData();
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.REJECT_ORDER)
            .caseDetailsBefore(FinremCaseDetails.builder().id(123L).caseType(CONSENTED)
                .data(caseData).build())
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONSENTED)
                .data(caseData).build())
            .build();
    }
}