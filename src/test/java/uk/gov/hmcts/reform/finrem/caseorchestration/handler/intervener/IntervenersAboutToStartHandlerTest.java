package uk.gov.hmcts.reform.finrem.caseorchestration.handler.intervener;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataContested;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFourWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThreeWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwoWrapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_FOUR_LABEL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_ONE_LABEL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_THREE_LABEL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_TWO_LABEL;

@RunWith(MockitoJUnitRunner.class)
public class IntervenersAboutToStartHandlerTest {

    public static final String AUTH_TOKEN = "tokien:)";
    @InjectMocks
    private IntervenersAboutToStartHandler handler;

    @Test
    public void givenContestedCase_whenICallbackIsSubmitted_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.MANAGE_INTERVENERS),
            is(false));
    }

    @Test
    public void givenConsentedCase_whenEventIsClose_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.CLOSE),
            is(false));
    }

    @Test
    public void givenConsentedCase_whenCallbackIsConsented_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.CLOSE),
            is(false));
    }

    @Test
    public void givenContestedCase_whenEventIsManageInteveners_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.MANAGE_INTERVENERS),
            is(true));
    }

    @Test
    public void givenContestedCase_whenUseManageInterveners_thenPrepareIntervenersList() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseDataContested> handle =
            handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(4, handle.getData().getIntervenersList().getListItems().size());
    }

    @Test
    public void givenContestedCase_whenUseManageIntervenersAndThereIsOneIntervnerAlready_thenPrepareIntervenersList() {
        FinremCallbackRequest<FinremCaseDataContested> finremCallbackRequest = buildCallbackRequest();
        IntervenerOneWrapper oneWrapper = IntervenerOneWrapper
            .builder().intervenerName("One name").intervenerEmail("test@test.com").build();
        IntervenerTwoWrapper twoWrapper = IntervenerTwoWrapper
            .builder().intervenerName("Two name").intervenerEmail("test@test.com").build();
        IntervenerThreeWrapper threeWrapper = IntervenerThreeWrapper
            .builder().intervenerName("Three name").intervenerEmail("test@test.com").build();
        IntervenerFourWrapper fourWrapper = IntervenerFourWrapper
            .builder().intervenerName("Four name").intervenerEmail("test@test.com").build();

        finremCallbackRequest.getCaseDetails().getData().setIntervenerOneWrapper(oneWrapper);
        finremCallbackRequest.getCaseDetails().getData().setIntervenerTwoWrapper(twoWrapper);
        finremCallbackRequest.getCaseDetails().getData().setIntervenerThreeWrapper(threeWrapper);
        finremCallbackRequest.getCaseDetails().getData().setIntervenerFourWrapper(fourWrapper);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseDataContested> handle =
            handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(4, handle.getData().getIntervenersList().getListItems().size());
        assertEquals(INTERVENER_ONE, handle.getData().getIntervenersList().getValueCode());
        assertEquals(INTERVENER_ONE_LABEL + ": " + "One name",
            handle.getData().getIntervenersList().getListItems().get(0).getLabel());
        assertEquals(INTERVENER_TWO_LABEL + ": " + "Two name",
            handle.getData().getIntervenersList().getListItems().get(1).getLabel());
        assertEquals(INTERVENER_THREE_LABEL + ": " + "Three name",
            handle.getData().getIntervenersList().getListItems().get(2).getLabel());
        assertEquals(INTERVENER_FOUR_LABEL + ": " + "Four name",
            handle.getData().getIntervenersList().getListItems().get(3).getLabel());
    }

    private FinremCallbackRequest<FinremCaseDataContested> buildCallbackRequest() {
        return FinremCallbackRequest
            .<FinremCaseDataContested>builder()
            .eventType(EventType.MANAGE_INTERVENERS)
            .caseDetails(FinremCaseDetails.<FinremCaseDataContested>builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseDataContested()).build())
            .build();
    }
}
