package uk.gov.hmcts.reform.finrem.caseorchestration.handler;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnStartDefaultValueService;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_ORDER_DIRECTION_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_ORDER_DIRECTION_JUDGE_NAME;

@RunWith(MockitoJUnitRunner.class)
public class ApproveConsentOrderAboutToStartHandlerTest {

    public static final String AUTH_TOKEN = "tokien:)";
    @InjectMocks
    private ApprovedConsentOrderAboutToStartHandler handler;
    @Mock
    private OnStartDefaultValueService onStartDefaultValueService;

    @Test
    public void givenConsentedCase_whenEventIsApproveAndCallbackIsSubmitted_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONSENTED, EventType.APPROVE_ORDER),
            is(false));
    }

    @Test
    public void givenConsentedCase_whenEventIsApproveOrder_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.APPROVE_ORDER),
            is(true));
    }

    @Test
    public void givenContestedCase_whenUseApproveOrder_thenDefaultOrderDateSetToCurrentDate() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> response = handler.handle(callbackRequest, AUTH_TOKEN);
        verify(onStartDefaultValueService).defaultConsentedOrderDate(callbackRequest);
    }

    @Test
    public void givenContestedCase_whenUseApproveOrderAndOrderDateEnteredManually_thenHandle() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        callbackRequest.getCaseDetails().getData().put(CONSENTED_ORDER_DIRECTION_DATE, "10-10-2000");
        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> response = handler.handle(callbackRequest, AUTH_TOKEN);
        assertEquals("10-10-2000", response.getData().get(CONSENTED_ORDER_DIRECTION_DATE));
    }

    @Test
    public void givenContestedCase_whenUseApproveOrder_thenDefaultJudgeNameSetToUser() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> response = handler.handle(callbackRequest, AUTH_TOKEN);
        verify(onStartDefaultValueService).defaultConsentedOrderJudgeName(callbackRequest, AUTH_TOKEN);
    }

    @Test
    public void givenContestedCase_whenUseApproveOrderAndJudgeNameEnteredManually_thenHandle() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        callbackRequest.getCaseDetails().getData().put(CONSENTED_ORDER_DIRECTION_JUDGE_NAME, "Test Judge");
        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> response = handler.handle(callbackRequest, AUTH_TOKEN);
        assertNotNull(response.getData().get(CONSENTED_ORDER_DIRECTION_JUDGE_NAME));
        assertEquals("Test Judge", response.getData().get(CONSENTED_ORDER_DIRECTION_JUDGE_NAME));
    }

    private CallbackRequest buildCallbackRequest() {
        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().id(123L).caseTypeId(CaseType.CONSENTED.getCcdType()).data(caseData).build();
        return CallbackRequest.builder().eventId(EventType.APPROVE_ORDER.getCcdType()).caseDetails(caseDetails).build();
    }
}