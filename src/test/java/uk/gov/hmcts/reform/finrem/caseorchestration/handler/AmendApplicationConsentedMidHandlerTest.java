package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AmendApplicationConsentedMidHandlerTest {

    @InjectMocks
    private AmendApplicationConsentedMidHandler handler;
    static final String AUTH_TOKEN = "4d73f8d4-2a8d-48e2-af91-11cbaa642345";

    @Mock
    private ConsentOrderService consentOrderService;
    @Mock
    private InternationalPostalService postalService;

    @Test
    void given_case_whenEvent_type_is_amendApp_thenCanHandle() {
        assertTrue(handler.canHandle(CallbackType.MID_EVENT, CaseType.CONSENTED, EventType.AMEND_APP_DETAILS));
    }

    @Test
    void given_case_when_wrong_callback_then_case_can_not_handle() {
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.SOLICITOR_CREATE));
        assertFalse(handler.canHandle(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.AMEND_APP_DETAILS));
    }

    @Test
    void given_case_when_wrong_casetype_then_case_can_not_handle() {
        assertFalse(handler.canHandle(CallbackType.MID_EVENT, CaseType.CONSENTED, EventType.SOLICITOR_CREATE));
    }

    @Test
    void given_case_when_wrong_eventtype_then_case_can_not_handle() {
        assertFalse(handler.canHandle(CallbackType.MID_EVENT, CaseType.CONSENTED, EventType.CLOSE));
    }

    @Test
    void handle() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        handler.handle(callbackRequest, AUTH_TOKEN);
        verify(consentOrderService).performCheck(callbackRequest, AUTH_TOKEN);
        verify(postalService).validate(callbackRequest.getCaseDetails().getData());
    }

    private CallbackRequest buildCallbackRequest() {
        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().id(123L).build();
        caseDetails.setData(caseData);
        return CallbackRequest.builder().eventId(EventType.SOLICITOR_CREATE.getCcdType())
            .caseDetails(caseDetails).caseDetailsBefore(caseDetails).build();
    }
}