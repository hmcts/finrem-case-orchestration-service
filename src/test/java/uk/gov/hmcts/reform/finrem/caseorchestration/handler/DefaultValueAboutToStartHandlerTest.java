package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CIVIL_PARTNERSHIP;

@RunWith(MockitoJUnitRunner.class)
public class DefaultValueAboutToStartHandlerTest {

    public static final String AUTH_TOKEN = "tokien:)";
    private final DefaultValueAboutToStartHandler handler = new DefaultValueAboutToStartHandler();

    @Test
    public void givenConsentedCase_whenEventIsAmend_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.AMEND_CASE),
            is(true));
    }

    @Test
    public void givenConsentedCase_whenEventIsSolCreate_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.SOLICITOR_CREATE),
            is(true));
    }

    @Test
    public void givenContestedCase_whenEventIsAmend_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.SOLICITOR_CREATE),
            is(true));
    }


    @Test
    public void handle() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> response = handler.handle(callbackRequest, AUTH_TOKEN);
        assertEquals(NO_VALUE, response.getData().get(CIVIL_PARTNERSHIP));
    }

    @Test
    public void givenContestedCase_whenEventIsAmendPaper_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.AMEND_CONTESTED_PAPER_APP_DETAILS),
            is(true));
    }

    private CallbackRequest buildCallbackRequest() {
        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().id(123L).data(caseData).build();
        return CallbackRequest.builder()
            .eventId(EventType.AMEND_CASE.getCcdType()).caseDetails(caseDetails).build();
    }

}