package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;


class SendOrderConsentAboutToStartHandlerTest {

    private static final String AUTH_TOKEN = "TOKEN-:";
    private SendOrderConsentAboutToStartHandler handler;

    @BeforeEach
    void setup() {
        handler = new SendOrderConsentAboutToStartHandler(new FinremCaseDetailsMapper(new ObjectMapper()
            .registerModule(new JavaTimeModule())));
    }

    @Test
    void givenCase_whenEventIsAmendApplication_thenCanHandle() {
        assertTrue(handler.canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.SEND_ORDER_FOR_APPROVED));
    }

    @Test
    void given_case_when_wrong_callback_then_case_can_not_handle() {
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.SEND_ORDER_FOR_APPROVED));
    }

    @Test
    void given_case_when_wrong_casetype_then_case_can_not_handle() {
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.SEND_ORDER_FOR_APPROVED));
    }

    @Test
    void given_case_when_wrong_eventType_then_case_can_not_handle() {
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.CLOSE));
    }


    @Test
    void handleError() {
        FinremCallbackRequest callbackRequest = callbackRequest();
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);
        assertTrue(response.hasErrors());
        assertEquals("Latest Consent Order Field is empty. Please use the Upload Consent Order Event instead of Send Order",
            response.getErrors().get(0));
    }

    @Test
    void handle() {
        FinremCallbackRequest callbackRequest = callbackRequest();
        FinremCaseData data = callbackRequest.getCaseDetails().getData();
        data.setLatestConsentOrder(caseDocument());
        assertFalse(handler.handle(callbackRequest, AUTH_TOKEN).hasErrors());
    }


    private FinremCallbackRequest callbackRequest() {
        return FinremCallbackRequest
            .builder()
            .caseDetails(FinremCaseDetails.builder().id(123L)
                .data(new FinremCaseData()).build())
            .build();
    }
}