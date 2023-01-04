package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Intention;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedNatureApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

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
        handler = new AmendApplicationAboutToStartHandler(new FinremCaseDetailsMapper(new ObjectMapper().registerModule(new JavaTimeModule())));
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
        FinremCallbackRequest callbackRequest = callbackRequest();

        FinremCaseData data = callbackRequest.getCaseDetails().getData();
        data.getNatureApplicationWrapper().setNatureOfApplication2(Lists.newArrayList(
            ContestedNatureApplication.PENSION_SHARING_ORDER,
            ContestedNatureApplication.LUMP_SUM_ORDER));
        data.setApplicantIntendsTo(Intention.APPLY_TO_VARY);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        final FinremCaseData responseData = response.getData();
        final List<ContestedNatureApplication> natureOfApplication2 = responseData.getNatureApplicationWrapper().getNatureOfApplication2();

        assertThat(natureOfApplication2, hasItems(ContestedNatureApplication.VARIATION_ORDER));
        assertThat(natureOfApplication2, hasSize(3));
        assertEquals(YesOrNo.NO, responseData.getCivilPartnership());
    }

    @Test
    public void givenCase_whenIntendsToIsNotApplyToVary_thenShouldNotDoAnything() {
        FinremCallbackRequest callbackRequest = callbackRequest();

        FinremCaseData data = callbackRequest.getCaseDetails().getData();
        data.getNatureApplicationWrapper().setNatureOfApplication2(Lists.newArrayList(
            ContestedNatureApplication.PENSION_SHARING_ORDER,
            ContestedNatureApplication.LUMP_SUM_ORDER));
        data.setApplicantIntendsTo(Intention.APPLY_TO_COURT_FOR);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        final FinremCaseData responseData = response.getData();
        final List<ContestedNatureApplication> natureOfApplication2 = responseData.getNatureApplicationWrapper().getNatureOfApplication2();

        assertThat(natureOfApplication2, hasItems(ContestedNatureApplication.PENSION_SHARING_ORDER,
            ContestedNatureApplication.LUMP_SUM_ORDER));
        assertThat(natureOfApplication2, hasSize(2));
        assertEquals(YesOrNo.NO, responseData.getCivilPartnership());
    }

    @Test
    public void givenCase_whenNatureListIsEmptyAndIntendsToIsApplyToVary_thenShouldAddToNatureList() {
        FinremCallbackRequest callbackRequest = callbackRequest();

        FinremCaseData data = callbackRequest.getCaseDetails().getData();
        data.setApplicantIntendsTo(Intention.APPLY_TO_VARY);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        final FinremCaseData responseData = response.getData();
        final List<ContestedNatureApplication> natureOfApplication2 = responseData.getNatureApplicationWrapper().getNatureOfApplication2();

        assertThat(natureOfApplication2, hasItems(ContestedNatureApplication.VARIATION_ORDER));
        assertThat(natureOfApplication2, hasSize(1));
        assertEquals(YesOrNo.NO, responseData.getCivilPartnership());
    }

    private FinremCallbackRequest callbackRequest() {
        return FinremCallbackRequest
            .<FinremCaseDetails>builder()
            .caseDetails(FinremCaseDetails.builder().id(123L)
                .data(new FinremCaseData()).build())
            .build();
    }
}