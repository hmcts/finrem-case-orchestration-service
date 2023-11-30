package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.RefusalOrderDocumentService;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RejectedConsentOrderInContestedAboutToSubmitHandlerTest {

    @InjectMocks
    private RejectedConsentOrderInContestedAboutToSubmitHandler handler;
    @Mock
    private RefusalOrderDocumentService refusalOrderDocumentService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String AUTH_TOKEN = "4d73f8d4-2a8d-48e2-af91-11cbaa642345";
    private static final String REJECT_ORDER_VALID_JSON = "/fixtures/fee-lookup.json";

    @Test
    void given_case_whenEventRejectedOrder_thenCanHandle() {
        assertTrue(handler.canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.CONSENT_ORDER_NOT_APPROVED));
    }


    @Test
    void given_case_when_wrong_callback_then_case_can_not_handle() {
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.CONSENT_ORDER_NOT_APPROVED));
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
        CallbackRequest callbackRequest = doValidCaseDataSetUp();

        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(refusalOrderDocumentService).generateConsentOrderNotApproved(AUTH_TOKEN, callbackRequest.getCaseDetails());
    }

    private CallbackRequest doValidCaseDataSetUp() {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(REJECT_ORDER_VALID_JSON)) {
            return objectMapper.readValue(resourceAsStream, CallbackRequest.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}