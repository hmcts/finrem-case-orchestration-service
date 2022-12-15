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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_DIRECTION_JUDGE_NAME;

@RunWith(MockitoJUnitRunner.class)
public class ConsentApplicationApprovedInContestedAboutToStartHandlerTest {

    @InjectMocks
    private ConsentApplicationApprovedInContestedAboutToStartHandler handler;
    @Mock
    private IdamService service;
    public static final String AUTH_TOKEN = "tokien:)";

    @Test
    public void canHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.CONSENT_APPLICATION_APPROVED_IN_CONTESTED),
            is(true));
    }

    @Test
    public void canNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.CONSENT_APPLICATION_APPROVED_IN_CONTESTED),
            is(false));
    }

    @Test
    public void canNotHandleWrongEventType() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.CLOSE),
            is(false));
    }

    @Test
    public void canNotHandleWrongCallbackType() {
        assertThat(handler
                .canHandle(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.CONSENT_APPLICATION_APPROVED_IN_CONTESTED),
            is(false));
    }

    @Test
    public void givenContestedCase_whenEventExecuted_thenSetTheLoggedInUserIfNull() {
        when(service.getIdamFullName(AUTH_TOKEN)).thenReturn("Moj Judge");
        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> response = handler.handle(buildCallbackRequest(null), AUTH_TOKEN);
        assertEquals("Moj Judge", response.getData().get(CONTESTED_ORDER_DIRECTION_JUDGE_NAME));
    }

    @Test
    public void givenContestedCase_whenEventExecuted_thenDoNotSetIfUserIsAlreadyThere() {
        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> response = handler.handle(buildCallbackRequest("HM Moj Judge"), AUTH_TOKEN);
        assertEquals("HM Moj Judge", response.getData().get(CONTESTED_ORDER_DIRECTION_JUDGE_NAME));
    }

    private CallbackRequest buildCallbackRequest(String judgeName) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CONTESTED_ORDER_DIRECTION_JUDGE_NAME, judgeName);
        CaseDetails caseDetails = CaseDetails.builder().caseTypeId(CaseType.CONTESTED.getCcdType())
            .id(123L).data(caseData).build();
        return CallbackRequest.builder()
            .eventId(EventType.CONSENT_APPLICATION_APPROVED_IN_CONTESTED.getCcdType()).caseDetails(caseDetails).build();
    }
}