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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderPrintService;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class SendOrderConsentForNotApprovedOrderAboutToSubmitHandlerTest {

    private static final String AUTH_TOKEN = "TOKEN-:";
    @InjectMocks
    private SendOrderConsentForNotApprovedOrderAboutToSubmitHandler handler;

    @Mock
    private ConsentOrderPrintService service;


    @Test
    void givenCase_whenEventIsAmendApplication_thenCanHandle() {
        assertTrue(handler.canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.SEND_ORDER));
    }

    @Test
    void given_case_when_wrong_callback_then_case_can_not_handle() {
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.SEND_ORDER));
    }

    @Test
    void given_case_when_wrong_casetype_then_case_can_not_handle() {
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.SEND_ORDER));
    }

    @Test
    void given_case_when_wrong_eventType_then_case_can_not_handle() {
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.CLOSE));
    }


    @Test
    void handle() {
        FinremCallbackRequest callbackRequest = callbackRequest();
        handler.handle(callbackRequest, AUTH_TOKEN).hasErrors();
        verify(service).sendConsentOrderToBulkPrint(any(FinremCaseDetails.class),
            any(FinremCaseDetails.class),
            any(EventType.class),
            anyString());
    }


    private FinremCallbackRequest callbackRequest() {
        return FinremCallbackRequest
            .builder()
            .caseDetails(FinremCaseDetails.builder().id(123L)
                .data(new FinremCaseData()).build())
            .caseDetailsBefore(FinremCaseDetails.builder().id(123L)
                .data(new FinremCaseData()).build())
            .build();
    }
}