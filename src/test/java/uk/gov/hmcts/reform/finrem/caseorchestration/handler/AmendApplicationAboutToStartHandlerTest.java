package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.ccd.callback.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseType;
import uk.gov.hmcts.reform.finrem.ccd.domain.EventType;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.Intention;
import uk.gov.hmcts.reform.finrem.ccd.domain.NatureApplication;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

public class AmendApplicationAboutToStartHandlerTest {

    private static final String AUTH_TOKEN = "4d73f8d4-2a8d-48e2-af91-11cbaa642345";

    private AmendApplicationAboutToStartHandler handler;

    @Before
    public void setup() {
        handler = new AmendApplicationAboutToStartHandler();
    }

    @Test
    public void givenCase_whenEventIsAmendApplication_thenCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.AMEND_APPLICATION_DETAILS),
            is(true));
    }

    @Test
    public void given_case_when_wrong_callback_then_case_can_not_handle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.AMEND_APPLICATION_DETAILS),
            is(false));
    }

    @Test
    public void given_case_when_wrong_casetype_then_case_can_not_handle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.AMEND_APPLICATION_DETAILS),
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

        FinremCaseData data = callbackRequest.getCaseDetails().getCaseData();
        data.getNatureApplicationWrapper().setNatureOfApplication2(Lists.newArrayList(
            NatureApplication.PENSION_SHARING_ORDER,
            NatureApplication.LUMP_SUM_ORDER));
        data.setApplicantIntendsTo(Intention.APPLY_TO_VARY);

        AboutToStartOrSubmitCallbackResponse response = handler.handle(callbackRequest, AUTH_TOKEN);

        final FinremCaseData responseData = response.getData();
        final List<NatureApplication> natureOfApplication2 = responseData.getNatureApplicationWrapper().getNatureOfApplication2();

        assertThat(natureOfApplication2, hasItems(NatureApplication.VARIATION_ORDER));
        assertThat(natureOfApplication2, hasSize(3));
        assertEquals(YesOrNo.NO, responseData.getCivilPartnership());
    }

    @Test
    public void givenCase_whenIntendsToIsNotApplyToVary_thenShouldNotDoAnything() {
        CallbackRequest callbackRequest = callbackRequest();

        FinremCaseData data = callbackRequest.getCaseDetails().getCaseData();
        data.getNatureApplicationWrapper().setNatureOfApplication2(Lists.newArrayList(
            NatureApplication.PENSION_SHARING_ORDER,
            NatureApplication.LUMP_SUM_ORDER));
        data.setApplicantIntendsTo(Intention.APPLY_TO_COURT_FOR);

        AboutToStartOrSubmitCallbackResponse response = handler.handle(callbackRequest, AUTH_TOKEN);

        final FinremCaseData responseData = response.getData();
        final List<NatureApplication> natureOfApplication2 = responseData.getNatureApplicationWrapper().getNatureOfApplication2();

        assertThat(natureOfApplication2, hasItems(NatureApplication.PENSION_SHARING_ORDER,
            NatureApplication.LUMP_SUM_ORDER));
        assertThat(natureOfApplication2, hasSize(2));
        assertEquals(YesOrNo.NO, responseData.getCivilPartnership());
    }

    @Test
    public void givenCase_whenNatureListIsEmptyAndIntendsToIsApplyToVary_thenShouldAddToNatureList() {
        CallbackRequest callbackRequest = callbackRequest();

        FinremCaseData data = callbackRequest.getCaseDetails().getCaseData();
        data.setApplicantIntendsTo(Intention.APPLY_TO_VARY);

        AboutToStartOrSubmitCallbackResponse response = handler.handle(callbackRequest, AUTH_TOKEN);

        final FinremCaseData responseData = response.getData();
        final List<NatureApplication> natureOfApplication2 = responseData.getNatureApplicationWrapper().getNatureOfApplication2();

        assertThat(natureOfApplication2, hasItems(NatureApplication.VARIATION_ORDER));
        assertThat(natureOfApplication2, hasSize(1));
        assertEquals(YesOrNo.NO, responseData.getCivilPartnership());
    }

    private CallbackRequest callbackRequest() {
        return CallbackRequest
            .builder()
            .caseDetails(FinremCaseDetails.builder().id(123L)
                .caseData(new FinremCaseData()).build())
            .build();
    }
}