package uk.gov.hmcts.reform.finrem.caseorchestration.handler.intervener;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFour;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThree;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwo;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_FOUR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_FOUR_LABEL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_ONE_LABEL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_THREE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_THREE_LABEL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_TWO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_TWO_LABEL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class IntervenersAboutToStartHandlerTest {

    @InjectMocks
    private IntervenersAboutToStartHandler handler;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.MANAGE_INTERVENERS);
    }

    @Test
    void givenAnyCase_whenHandled_thenPrepareEmptyIntervenersList() {
        FinremCallbackRequest finremCallbackRequest = FinremCallbackRequestFactory.from();
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertThat(response.getData().getIntervenersList().getListItems())
            .extracting(DynamicRadioListElement::getCode, DynamicRadioListElement::getLabel)
            .containsExactly(
                Tuple.tuple(INTERVENER_ONE, INTERVENER_ONE_LABEL + ": not added to case yet."),
                Tuple.tuple(INTERVENER_TWO, INTERVENER_TWO_LABEL + ": not added to case yet."),
                Tuple.tuple(INTERVENER_THREE, INTERVENER_THREE_LABEL + ": not added to case yet."),
                Tuple.tuple(INTERVENER_FOUR, INTERVENER_FOUR_LABEL + ": not added to case yet."));
    }

    @Test
    void givenAnyCase_whenOneToFourIntervenersSet_thenPrepareIntervenersList() {
        FinremCallbackRequest finremCallbackRequest = FinremCallbackRequestFactory.from(FinremCaseData.builder()
            .intervenerOne(IntervenerOne.builder().intervenerName("One name").build())
            .intervenerTwo(IntervenerTwo.builder().intervenerName("Two name").build())
            .intervenerThree(IntervenerThree.builder().intervenerName("Three name").build())
            .intervenerFour(IntervenerFour.builder().intervenerName("Four name").build())
            .build());
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertThat(response.getData().getIntervenersList().getListItems())
            .extracting(DynamicRadioListElement::getCode, DynamicRadioListElement::getLabel)
            .containsExactly(
                Tuple.tuple(INTERVENER_ONE, INTERVENER_ONE_LABEL + ": One name"),
                Tuple.tuple(INTERVENER_TWO, INTERVENER_TWO_LABEL + ": Two name"),
                Tuple.tuple(INTERVENER_THREE, INTERVENER_THREE_LABEL + ": Three name"),
                Tuple.tuple(INTERVENER_FOUR, INTERVENER_FOUR_LABEL + ": Four name"));
    }
}
