package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ADDITIONAL_HEARING_DOCUMENTS_OPTION;

@RunWith(MockitoJUnitRunner.class)
public class ListForHearingAboutToStartHandlerTest {

    public static final String AUTH_TOKEN = "tokien:)";
    private final ListForHearingAboutToStartHandler handler =  new ListForHearingAboutToStartHandler();

    @Test
    public void givenContestedCase_whenEventIsListForHearing_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.LIST_FOR_HEARING),
            is(true));
    }

    @Test
    public void givenContestedCase_whenEventIsNotListForHearing_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.CLOSE),
            is(false));
    }

    @Test
    public void givenContestedCase_whenCallbackIsSubmit_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.LIST_FOR_HEARING),
            is(false));
    }

    @Test
    public void givenConsentCase_whenEventIsListForHearing_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.LIST_FOR_HEARING),
            is(false));
    }


    @Test
    public void givenCase_whenEventStart_thenSetDefaultOptionToNo() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        AboutToStartOrSubmitCallbackResponse response = handler.handle(callbackRequest, AUTH_TOKEN);
        assertEquals(NO_VALUE, response.getData().get(ADDITIONAL_HEARING_DOCUMENTS_OPTION));
    }

    private CallbackRequest buildCallbackRequest() {
        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().id(123L).data(caseData).build();
        return CallbackRequest.builder().eventId("SomeEventId").caseDetails(caseDetails).build();
    }

}