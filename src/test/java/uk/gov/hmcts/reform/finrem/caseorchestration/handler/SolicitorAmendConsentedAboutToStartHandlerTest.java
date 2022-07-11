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
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CONSENT_ORDER_CAMELCASE_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CONSENT_ORDER_LOWERCASE_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CONSENT_OTHER_DOC_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CV_LOWERCASE_LABEL_FIELD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CV_ORDER_CAMELCASE_LABEL_FIELD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CV_OTHER_DOC_LABEL_FIELD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CV_OTHER_DOC_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.VARIATION_ORDER_CAMELCASE_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.VARIATION_ORDER_LOWERCASE_LABEL_VALUE;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorAmendConsentedAboutToStartHandlerTest {

    @InjectMocks
    private SolicitorAmendConsentedAboutToStartHandler handle;
    public static final String AUTH_TOKEN = "4d73f8d4-2a8d-48e2-af91-11cbaa642345";

    @Test
    public void given_case_whenEvent_type_is_amendapp_thenCanHandle() {
        assertThat(handle
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.AMEND_APP_DETAILS),
            is(true));
    }

    @Test
    public void given_case_when_wrong_casetype_then_case_can_not_handle() {
        assertThat(handle
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.AMEND_APP_DETAILS),
            is(false));
    }

    @Test
    public void given_case_when_wrong_eventtype_then_case_can_not_handle() {
        assertThat(handle
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.CLOSE),
            is(false));
    }

    @Test
    public void given_case_when_wrong_callbacktype_then_case_can_not_handle() {
        assertThat(handle
                .canHandle(CallbackType.MID_EVENT, CaseType.CONSENTED, EventType.AMEND_APP_DETAILS),
            is(false));
    }

    @Test
    public void given_case_when_natureOfApplicationIsVariation_thenReturnVariationOrderLabels() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        List<String> orderList  = List.of("Variation Order", "Property Adjustment Order");
        callbackRequest.getCaseDetails().getData().put("natureOfApplication2", orderList);

        AboutToStartOrSubmitCallbackResponse response =
            handle.handle(callbackRequest, AUTH_TOKEN);

        final String camelCaseLabel = (String) response.getData().get(CV_ORDER_CAMELCASE_LABEL_FIELD);
        assertEquals(VARIATION_ORDER_CAMELCASE_LABEL_VALUE, camelCaseLabel);
        final String lowerCaseLabel = (String) response.getData().get(CV_LOWERCASE_LABEL_FIELD);
        assertEquals(VARIATION_ORDER_LOWERCASE_LABEL_VALUE, lowerCaseLabel);
        final String docLabel = (String) response.getData().get(CV_OTHER_DOC_LABEL_FIELD);
        assertEquals(CV_OTHER_DOC_LABEL_VALUE, docLabel);
    }

    @Test
    public void given_case_when_natureOfApplicationDoNotContainsVariation_thenReturnConsentOrderLabels() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        List<String> orderList  = List.of("Property Adjustment Order");
        callbackRequest.getCaseDetails().getData().put("natureOfApplication2", orderList);

        AboutToStartOrSubmitCallbackResponse response =
            handle.handle(callbackRequest, AUTH_TOKEN);

        final String camelCaseLabel = (String) response.getData().get(CV_ORDER_CAMELCASE_LABEL_FIELD);
        assertEquals(CONSENT_ORDER_CAMELCASE_LABEL_VALUE, camelCaseLabel);
        final String lowerCaseLabel = (String) response.getData().get(CV_LOWERCASE_LABEL_FIELD);
        assertEquals(CONSENT_ORDER_LOWERCASE_LABEL_VALUE, lowerCaseLabel);
        final String docLabel = (String) response.getData().get(CV_OTHER_DOC_LABEL_FIELD);
        assertEquals(CONSENT_OTHER_DOC_LABEL_VALUE, docLabel);
    }

    private CallbackRequest buildCallbackRequest() {
        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().id(123L).data(caseData).build();
        return CallbackRequest.builder().eventId("SomeEventId").caseDetails(caseDetails).build();
    }
}