package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
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

@RunWith(MockitoJUnitRunner.class)
public class SolicitorCreateConsentedMidHandlerTest {

    @InjectMocks
    private SolicitorCreateConsentedMidHandler solicitorCreateConsentedMidHandler;
    public static final String AUTH_TOKEN = "tokien:)";

    @Test
    public void canHandle() {
        assertThat(solicitorCreateConsentedMidHandler
                .canHandle(CallbackType.MID_EVENT, CaseType.CONSENTED, EventType.SOLICITOR_CREATE),
            is(true));
    }

    @Test
    public void given_case_when_wrong_callback_then_return_false() {
        assertThat(solicitorCreateConsentedMidHandler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.SOLICITOR_CREATE),
            is(false));
    }

    @Test
    public void given_case_when_wrong_casetype_then_return_false() {
        assertThat(solicitorCreateConsentedMidHandler
                .canHandle(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.SOLICITOR_CREATE),
            is(false));
    }

    @Test
    public void given_case_when_wrong_eventtype_then_return_false() {
        assertThat(solicitorCreateConsentedMidHandler
                .canHandle(CallbackType.MID_EVENT, CaseType.CONSENTED, EventType.CLOSE),
            is(false));
    }

    @Test
    public void give_casedata_when_asked_customlebel_then_return_custom_label() {
        CallbackRequest callbackRequest = buildCallbackRequest();

        AboutToStartOrSubmitCallbackResponse response = solicitorCreateConsentedMidHandler.handle(callbackRequest, AUTH_TOKEN);
        final String customLabel = (String) response.getData().get("customLabel");
        assertEquals("consent/variation order", customLabel);
    }

    private CallbackRequest buildCallbackRequest() {
        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().id(123L).data(caseData).build();
        return CallbackRequest.builder().eventId("SomeEventId").caseDetails(caseDetails).build();
    }
}