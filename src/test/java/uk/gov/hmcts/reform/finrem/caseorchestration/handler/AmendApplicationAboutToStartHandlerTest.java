package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.APPLICANT_INTENDS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.VARIATION_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.URGENT_CASE_QUESTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_NATURE_OF_APPLICATION;

public class AmendApplicationAboutToStartHandlerTest {

    private static final String AUTH_TOKEN = "4d73f8d4-2a8d-48e2-af91-11cbaa642345";

    private AmendApplicationAboutToStartHandler handler;
    private ConsentedApplicationHelper helper;

    @Before
    public void setup() {
        helper = new ConsentedApplicationHelper();
        handler = new AmendApplicationAboutToStartHandler(helper);
    }

    @Test
    public void givenCase_whenEventIsAmendApplication_thenCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.AMEND_APP_DETAILS),
            is(true));
    }

    @Test
    public void given_case_when_wrong_callback_then_case_can_not_handle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.AMEND_APP_DETAILS),
            is(false));
    }

    @Test
    public void given_case_when_wrong_casetype_then_case_can_not_handle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.AMEND_APP_DETAILS),
            is(false));
    }

    @Test
    public void given_case_when_wrong_eventType_then_case_can_not_handle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.CLOSE),
            is(false));
    }


    @Test
    public void givenCase_whenIntendsToIsApplyToVary_thenShouldAddToNatureList() {
        CallbackRequest callbackRequest = callbackRequest();

        Map<String, Object> data = callbackRequest.getCaseDetails().getData();
        data.put(CONSENTED_NATURE_OF_APPLICATION, List.of("Pension document","Lump sum"));
        data.put(APPLICANT_INTENDS, "ApplyToVary");

        AboutToStartOrSubmitCallbackResponse response = handler.handle(callbackRequest, AUTH_TOKEN);

        final Map<String, Object> responseData = response.getData();
        final List<String> list = helper.convertToList(responseData.get(CONSENTED_NATURE_OF_APPLICATION));

        assertThat(list, hasItems(VARIATION_ORDER));
        assertThat(list, hasSize(3));
        assertEquals(NO_VALUE, responseData.get(CIVIL_PARTNERSHIP));
    }

    @Test
    public void givenCase_whenIntendsToIsNotApplyToVary_thenShouldNotDoAnything() {
        CallbackRequest callbackRequest = callbackRequest();

        Map<String, Object> data = callbackRequest.getCaseDetails().getData();
        data.put(CONSENTED_NATURE_OF_APPLICATION, List.of("Pension document","Lump sum"));
        data.put(APPLICANT_INTENDS, "ApplyToCourtFor");

        AboutToStartOrSubmitCallbackResponse response = handler.handle(callbackRequest, AUTH_TOKEN);

        final Map<String, Object> responseData = response.getData();
        final List<String> list = helper.convertToList(responseData.get(CONSENTED_NATURE_OF_APPLICATION));

        assertThat(list, hasItems("Pension document","Lump sum"));
        assertThat(list, hasSize(2));
        assertEquals(NO_VALUE, responseData.get(CIVIL_PARTNERSHIP));
    }

    @Test
    public void givenCase_whenNatureListIsEmptyAndIntendsToIsApplyToVary_thenShouldAddToNatureList() {
        CallbackRequest callbackRequest = callbackRequest();

        Map<String, Object> data = callbackRequest.getCaseDetails().getData();
        data.put(APPLICANT_INTENDS, "ApplyToVary");

        AboutToStartOrSubmitCallbackResponse response = handler.handle(callbackRequest, AUTH_TOKEN);

        final Map<String, Object> responseData = response.getData();
        final List<String> list = helper.convertToList(responseData.get(CONSENTED_NATURE_OF_APPLICATION));

        assertThat(list, hasItems(VARIATION_ORDER));
        assertThat(list, hasSize(1));
        assertEquals(NO_VALUE, responseData.get(CIVIL_PARTNERSHIP));
    }

    private CallbackRequest callbackRequest() {
        return CallbackRequest
            .builder()
            .caseDetails(CaseDetails.builder().id(123L)
                .data(new HashMap<>()).build())
            .build();
    }
}