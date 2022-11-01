package uk.gov.hmcts.reform.finrem.caseorchestration.handler;


import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnStartDefaultValueService;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_ORDER_DIRECTION_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_ORDER_DIRECTION_JUDGE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_JUDGE_NAME;

public class ApproveConsentOrderContestedAboutToStartHandlerTest {

    public static final String AUTH_TOKEN = "tokien:)";
    private ApprovedConsentOrderContestedAboutToStartHandler handler;

    @Mock
    private IdamService service;

    @Before
    public void setup() {
        handler =  new ApprovedConsentOrderContestedAboutToStartHandler(new OnStartDefaultValueService());
    }

    @Test
    public void givenContestedCase_whenEventIsApproveAndCallbackIsSubmitted_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.APPROVE_ORDER),
            is(false));
    }

    @Test
    public void givenContestedCase_whenEventIsApproveOrder_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.APPROVE_ORDER),
            is(true));
    }

    @Test
    public void givenContestedCase_whenUseApproveOrder_thenDefaultOrderDateSetToCurrentDate() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        AboutToStartOrSubmitCallbackResponse response = handler.handle(callbackRequest, AUTH_TOKEN);
        assertNotNull(response.getData().get(CONTESTED_ORDER_APPROVED_DATE));
    }

    @Test
    public void givenContestedCase_whenUseApproveOrderAndOrderDateEnteredManually_thenHandle() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        callbackRequest.getCaseDetails().getData().put(CONTESTED_ORDER_APPROVED_DATE,"10-10-2000");
        AboutToStartOrSubmitCallbackResponse response = handler.handle(callbackRequest, AUTH_TOKEN);
        assertEquals("10-10-2000", response.getData().get(CONTESTED_ORDER_APPROVED_DATE));
    }

    @Test
    public void givenContestedCase_whenUseApproveOrder_thenDefaultJudgeNameSetToUser() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        when(service.getIdamFullName(AUTH_TOKEN)).thenReturn("Test Name");
        AboutToStartOrSubmitCallbackResponse response = handler.handle(callbackRequest, AUTH_TOKEN);
        assertNotNull(response.getData().get(CONTESTED_ORDER_APPROVED_JUDGE_NAME));
        assertEquals("Test Name", response.getData().get(CONTESTED_ORDER_APPROVED_JUDGE_NAME));
    }

    @Test
    public void givenContestedCase_whenUseApproveOrderAndJudgeNameEnteredManually_thenHandle() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        callbackRequest.getCaseDetails().getData().put(CONTESTED_ORDER_APPROVED_JUDGE_NAME, "Test Judge");
        AboutToStartOrSubmitCallbackResponse response = handler.handle(callbackRequest, AUTH_TOKEN);
        assertNotNull(response.getData().get(CONTESTED_ORDER_APPROVED_JUDGE_NAME));
        assertEquals("Test Judge", response.getData().get(CONTESTED_ORDER_APPROVED_JUDGE_NAME));
    }

    private CallbackRequest buildCallbackRequest() {
        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().id(123L).caseTypeId(CaseType.CONSENTED.getCcdType()). data(caseData).build();
        return CallbackRequest.builder().eventId(EventType.ISSUE_APPLICATION.getCcdType()).caseDetails(caseDetails).build();
    }
}