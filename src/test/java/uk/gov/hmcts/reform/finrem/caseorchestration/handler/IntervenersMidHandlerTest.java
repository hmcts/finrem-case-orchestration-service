package uk.gov.hmcts.reform.finrem.caseorchestration.handler;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFourWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThreeWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwoWrapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.ADD_INTERVENER_FOUR_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.ADD_INTERVENER_FOUR_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.ADD_INTERVENER_ONE_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.ADD_INTERVENER_ONE_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.ADD_INTERVENER_THREE_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.ADD_INTERVENER_THREE_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.ADD_INTERVENER_TWO_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.ADD_INTERVENER_TWO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.DEL_INTERVENER_FOUR_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.DEL_INTERVENER_FOUR_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.DEL_INTERVENER_ONE_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.DEL_INTERVENER_ONE_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.DEL_INTERVENER_THREE_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.DEL_INTERVENER_THREE_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.DEL_INTERVENER_TWO_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.DEL_INTERVENER_TWO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_FOUR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_THREE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_TWO;

@RunWith(MockitoJUnitRunner.class)
public class IntervenersMidHandlerTest {

    public static final String AUTH_TOKEN = "tokien:)";

    @InjectMocks
    private IntervenersAboutToStartHandler handler;
    @InjectMocks
    private IntervenersMidHandler midHandler;

    @Test
    public void givenContestedCase_whenICallbackIsSubmitted_thenHandlerCanNotHandle() {
        assertThat(midHandler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.MANAGE_INTERVENERS),
            is(false));
    }

    @Test
    public void givenConsentedCase_whenEventIsClose_thenHandlerCanNotHandle() {
        assertThat(midHandler
                .canHandle(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.CLOSE),
            is(false));
    }

    @Test
    public void givenContestedCase_whenEventIsManageInteveners_thenHandlerCanHandleEvent() {
        assertThat(midHandler
                .canHandle(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.MANAGE_INTERVENERS),
            is(true));
    }

    @Test
    public void givenContestedCase_whenMidEventCalled_thenPrepareOptionListForIntvOneBasedOnIntervenersList() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        IntervenerOneWrapper oneWrapper = IntervenerOneWrapper
            .builder().intervener1Name("One name").intervener1Email("test@test.com").build();

        finremCallbackRequest.getCaseDetails().getData().setIntervenerOneWrapper(oneWrapper);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handleResp = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(4, handleResp.getData().getIntervenersList().getListItems().size());

        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_ONE).build();
        handleResp.getData().getIntervenersList().setValue(option1);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> midHandleResp = midHandler.handle(finremCallbackRequest, AUTH_TOKEN);
        DynamicRadioList intervenerOptionList = midHandleResp.getData().getIntervenerOptionList();


        assertEquals(2, intervenerOptionList.getListItems().size());
        assertEquals(ADD_INTERVENER_ONE_CODE, intervenerOptionList.getValue().getCode());
        assertEquals(ADD_INTERVENER_ONE_VALUE, intervenerOptionList.getValue().getLabel());
        assertEquals(ADD_INTERVENER_ONE_CODE, intervenerOptionList.getListItems().get(0).getCode());
        assertEquals(ADD_INTERVENER_ONE_VALUE, intervenerOptionList.getListItems().get(0).getLabel());
        assertEquals(DEL_INTERVENER_ONE_CODE, intervenerOptionList.getListItems().get(1).getCode());
        assertEquals(DEL_INTERVENER_ONE_VALUE, intervenerOptionList.getListItems().get(1).getLabel());
    }

    @Test
    public void givenContestedCase_whenMidEventCalled_thenPrepareOptionListForIntvTwoBasedOnIntervenersList() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        IntervenerTwoWrapper twoWrapper = IntervenerTwoWrapper
            .builder().intervener2Name("Two name").intervener2Email("test@test.com").build();

        finremCallbackRequest.getCaseDetails().getData().setIntervenerTwoWrapper(twoWrapper);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handleResp = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(4, handleResp.getData().getIntervenersList().getListItems().size());

        DynamicRadioListElement option2 = DynamicRadioListElement.builder().code(INTERVENER_TWO).build();
        handleResp.getData().getIntervenersList().setValue(option2);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> midHandleResp = midHandler.handle(finremCallbackRequest, AUTH_TOKEN);
        DynamicRadioList intervenerOptionList = midHandleResp.getData().getIntervenerOptionList();

        assertEquals(2, intervenerOptionList.getListItems().size());
        assertEquals(ADD_INTERVENER_TWO_CODE, intervenerOptionList.getListItems().get(0).getCode());
        assertEquals(ADD_INTERVENER_TWO_VALUE, intervenerOptionList.getListItems().get(0).getLabel());
        assertEquals(DEL_INTERVENER_TWO_CODE, intervenerOptionList.getListItems().get(1).getCode());
        assertEquals(DEL_INTERVENER_TWO_VALUE, intervenerOptionList.getListItems().get(1).getLabel());
    }


    @Test
    public void givenContestedCase_whenMidEventCalled_thenPrepareOptionListForIntvThreeBasedOnIntervenersList() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        IntervenerThreeWrapper threeWrapper = IntervenerThreeWrapper
            .builder().intervener3Name("Three name").intervener3Email("test@test.com").build();

        finremCallbackRequest.getCaseDetails().getData().setIntervenerThreeWrapper(threeWrapper);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handleResp = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(4, handleResp.getData().getIntervenersList().getListItems().size());

        DynamicRadioListElement option3 = DynamicRadioListElement.builder().code(INTERVENER_THREE).build();
        handleResp.getData().getIntervenersList().setValue(option3);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> midHandleResp = midHandler.handle(finremCallbackRequest, AUTH_TOKEN);
        DynamicRadioList intervenerOptionList = midHandleResp.getData().getIntervenerOptionList();

        assertEquals(2, intervenerOptionList.getListItems().size());
        assertEquals(ADD_INTERVENER_THREE_CODE, intervenerOptionList.getListItems().get(0).getCode());
        assertEquals(ADD_INTERVENER_THREE_VALUE, intervenerOptionList.getListItems().get(0).getLabel());
        assertEquals(DEL_INTERVENER_THREE_CODE, intervenerOptionList.getListItems().get(1).getCode());
        assertEquals(DEL_INTERVENER_THREE_VALUE, intervenerOptionList.getListItems().get(1).getLabel());
    }

    @Test
    public void givenContestedCase_whenMidEventCalled_thenPrepareOptionListForIntvFourBasedOnIntervenersList() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        IntervenerFourWrapper fourWrapper = IntervenerFourWrapper
            .builder().intervener4Name("Four name").intervener4Email("test@test.com").build();

        finremCallbackRequest.getCaseDetails().getData().setIntervenerFourWrapper(fourWrapper);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handleResp = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(4, handleResp.getData().getIntervenersList().getListItems().size());

        DynamicRadioListElement option4 = DynamicRadioListElement.builder().code(INTERVENER_FOUR).build();
        handleResp.getData().getIntervenersList().setValue(option4);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> midHandleResp = midHandler.handle(finremCallbackRequest, AUTH_TOKEN);
        DynamicRadioList intervenerOptionList = midHandleResp.getData().getIntervenerOptionList();

        assertEquals(2, intervenerOptionList.getListItems().size());
        assertEquals(ADD_INTERVENER_FOUR_CODE, intervenerOptionList.getListItems().get(0).getCode());
        assertEquals(ADD_INTERVENER_FOUR_VALUE, intervenerOptionList.getListItems().get(0).getLabel());
        assertEquals(DEL_INTERVENER_FOUR_CODE, intervenerOptionList.getListItems().get(1).getCode());
        assertEquals(DEL_INTERVENER_FOUR_VALUE, intervenerOptionList.getListItems().get(1).getLabel());
    }

    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.MANAGE_INTERVENERS)
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).build())
            .build();
    }
}