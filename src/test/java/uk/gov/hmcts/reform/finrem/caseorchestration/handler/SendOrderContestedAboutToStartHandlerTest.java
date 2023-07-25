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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PartyService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@ExtendWith(MockitoExtension.class)
class SendOrderContestedAboutToStartHandlerTest {
    public static final String AUTH_TOKEN = "tokien:)";
    @Mock
    private GeneralOrderService generalOrderService;
    @Mock
    private PartyService partyService;

    @InjectMocks
    private SendOrderContestedAboutToStartHandler handler;

    @Test
    void givenACcdCallbackContestedCase_WhenAnAboutToStartEventSendOrder_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.SEND_ORDER),
            is(true));
    }

    @Test
    void givenACcdCallbackConsentedCase_WhenAnAboutToStartEventSendOrder_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONSENTED, EventType.SEND_ORDER),
            is(false));
    }


    @Test
    void handle() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();

        handler.handle(finremCallbackRequest, AUTH_TOKEN);

        verify(generalOrderService).setOrderList(caseDetails);
        verify(partyService).getAllActivePartyList(caseDetails);
    }


    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.SEND_ORDER)
            .caseDetailsBefore(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).build())
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).build())
            .build();
    }
}