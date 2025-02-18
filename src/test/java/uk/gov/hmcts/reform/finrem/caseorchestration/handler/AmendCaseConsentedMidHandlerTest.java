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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.AMEND_CASE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class AmendCaseConsentedMidHandlerTest {

    @InjectMocks
    private AmendApplicationConsentedMidHandler handler;
    @Mock
    private ConsentOrderService consentOrderService;
    @Mock
    private InternationalPostalService postalService;
    @Mock
    private ObjectMapper objectMapper;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.MID_EVENT, CONSENTED, EventType.AMEND_CASE);
    }

    @Test
    void testHandle() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        handler.handle(callbackRequest, AUTH_TOKEN);
        verify(postalService).validate(callbackRequest.getCaseDetails().getData());
    }

    @Test
    void handle() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        handler.handle(callbackRequest, AUTH_TOKEN);
        verify(consentOrderService).performCheck(objectMapper.convertValue(callbackRequest, CallbackRequest.class), AUTH_TOKEN);
    }

    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequest
            .builder()
            .eventType(AMEND_CASE)
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONSENTED)
                .data(FinremCaseData.builder().ccdCaseType(CONSENTED).build()).build())
            .caseDetailsBefore(FinremCaseDetails.builder().id(123L).caseType(CONSENTED)
                .data(FinremCaseData.builder().ccdCaseType(CONSENTED).build()).build())
            .build();
    }
}
