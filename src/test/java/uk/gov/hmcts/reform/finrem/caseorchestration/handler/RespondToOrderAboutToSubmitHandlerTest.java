package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderService;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;

@ExtendWith(MockitoExtension.class)
class RespondToOrderAboutToSubmitHandlerTest {

    public static final String AUTH_TOKEN = "tokien:)";
    @InjectMocks
    private RespondToOrderAboutToSubmitHandler handler;
    @Mock
    private ConsentOrderService consentOrderService;

    @Test
    void canHandle() {
        assertTrue(handler.canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.RESPOND_TO_ORDER));
    }

    @Test
    void canNotHandleWhenCallbackIsStart() {
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.RESPOND_TO_ORDER));
    }

    @Test
    void canNotHandleWhenCaseTypeIsContested() {
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.RESPOND_TO_ORDER));
    }

    @Test
    void canNotHandleWhenEventIsNotMatchedWithSelectedEvent() {
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.CLOSE));
    }


    @Test
    void givenCase_whenRequestToUpdateLatestConsentOrder_thenHandlerCanHandle() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        when(consentOrderService.getLatestConsentOrderData(any(FinremCallbackRequest.class))).thenReturn(caseDocument());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        Assertions.assertNotNull(response.getData().getLatestConsentOrder());
        verify(consentOrderService).getLatestConsentOrderData(any(FinremCallbackRequest.class));
    }


    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.RESPOND_TO_ORDER)
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONSENTED)
            .data(FinremCaseData.builder().ccdCaseType(CONSENTED).build()).build())
            .build();
    }
}