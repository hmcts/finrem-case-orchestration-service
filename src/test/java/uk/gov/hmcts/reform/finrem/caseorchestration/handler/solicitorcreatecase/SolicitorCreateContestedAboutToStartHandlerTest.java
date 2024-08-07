package uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitorcreatecase;


import org.junit.Before;
import org.junit.Test;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TYPE_OF_APPLICATION_DEFAULT_TO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.URGENT_CASE_QUESTION;

public class SolicitorCreateContestedAboutToStartHandlerTest {

    public static final String AUTH_TOKEN = "tokien:)";
    private SolicitorCreateContestedAboutToStartHandler handler;

    @Before
    public void setup() {
        handler =  new SolicitorCreateContestedAboutToStartHandler(new OnStartDefaultValueService());
    }

    @Test
    public void givenContestedCase_whenEventIsAmendAndCallbackIsSubmitted_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.SOLICITOR_CREATE),
            is(false));
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
        assertEquals(TYPE_OF_APPLICATION_DEFAULT_TO, response.getData().get(TYPE_OF_APPLICATION));
        assertEquals(NO_VALUE, response.getData().get(URGENT_CASE_QUESTION));
    }

    private CallbackRequest buildCallbackRequest() {
        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().id(123L).data(caseData).build();
        return CallbackRequest.builder().eventId("SomeEventId").caseDetails(caseDetails).build();
    }
}