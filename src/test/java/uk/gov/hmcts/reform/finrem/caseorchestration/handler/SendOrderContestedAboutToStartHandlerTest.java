package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.SendOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PartyService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class SendOrderContestedAboutToStartHandlerTest {

    @Mock
    private GeneralOrderService generalOrderService;

    @Mock
    private PartyService partyService;

    @InjectMocks
    private SendOrderContestedAboutToStartHandler handler;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.SEND_ORDER);
    }

    @Test
    void givenCallbackRequest_whenInvoke_thenPopulateValues() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();

        DynamicMultiSelectList expectedDynamicMultiSelectList = DynamicMultiSelectList.builder().build();
        when(partyService.getAllActivePartyList(caseDetails)).thenReturn(expectedDynamicMultiSelectList);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =  handler.handle(finremCallbackRequest, AUTH_TOKEN);

        verify(generalOrderService).setOrderList(caseDetails);
        verify(partyService).getAllActivePartyList(caseDetails);

        assertThat(response)
            .extracting(GenericAboutToStartOrSubmitCallbackResponse::getData)
            .extracting(FinremCaseData::getPartiesOnCase)
            .isEqualTo(expectedDynamicMultiSelectList);
        assertThat(response)
            .extracting(GenericAboutToStartOrSubmitCallbackResponse::getData)
            .extracting(FinremCaseData::getSendOrderWrapper)
            .extracting(SendOrderWrapper::getSendOrderPostStateOption)
            .isNull();
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
