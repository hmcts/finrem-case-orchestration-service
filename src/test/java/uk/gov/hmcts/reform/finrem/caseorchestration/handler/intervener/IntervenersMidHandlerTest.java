package uk.gov.hmcts.reform.finrem.caseorchestration.handler.intervener;

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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFour;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThree;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwo;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_INVALID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_THREE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_TWO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.UPDATE_INTERVENER_FOUR_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.UPDATE_INTERVENER_ONE_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.UPDATE_INTERVENER_THREE_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.UPDATE_INTERVENER_TWO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class IntervenersMidHandlerTest {

    @InjectMocks
    private IntervenersAboutToStartHandler aboutToStartHandler;

    @InjectMocks
    private IntervenersMidHandler midHandler;

    @Test
    void testCanHandle() {
        assertCanHandle(midHandler, CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.MANAGE_INTERVENERS);
    }

    @Test
    void givenContestedCase_whenMidEventCalledAndInterv1SelectedToOperate_thenPrepareOptionListForIntvBasedOnIntervenersList() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handleResp = aboutToStartHandler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(4, handleResp.getData().getIntervenersList().getListItems().size());

        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_ONE).build();
        handleResp.getData().getIntervenersList().setValue(option1);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> midHandleResp = midHandler.handle(finremCallbackRequest, AUTH_TOKEN);
        DynamicRadioList intervenerOptionList = midHandleResp.getData().getIntervenerOptionList();


        assertEquals(1, intervenerOptionList.getListItems().size());
        assertEquals(ADD_INTERVENER_ONE_CODE, intervenerOptionList.getValue().getCode());
        assertEquals(ADD_INTERVENER_ONE_VALUE, intervenerOptionList.getValue().getLabel());
        assertEquals(ADD_INTERVENER_ONE_CODE, intervenerOptionList.getListItems().getFirst().getCode());
        assertEquals(ADD_INTERVENER_ONE_VALUE, intervenerOptionList.getListItems().getFirst().getLabel());
    }

    @Test
    public void givenContestedCase_whenMidEventCalledAndIntervSelectedToOperateNotValid_thenThrowException() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handleResp = aboutToStartHandler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(4, handleResp.getData().getIntervenersList().getListItems().size());

        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_INVALID).build();
        handleResp.getData().getIntervenersList().setValue(option1);

        assertThatThrownBy(() ->
            midHandler.handle(finremCallbackRequest, AUTH_TOKEN)
        ).isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid intervener selected for caseId 1234567890");

    }

    @Test
    public void givenContestedCase_whenMidEventCalledAndInterv2SelectedToOperate_thenPrepareOptionListForIntvBasedOnIntervenersList() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handleResp = aboutToStartHandler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(4, handleResp.getData().getIntervenersList().getListItems().size());

        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_TWO).build();
        handleResp.getData().getIntervenersList().setValue(option1);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> midHandleResp = midHandler.handle(finremCallbackRequest, AUTH_TOKEN);
        DynamicRadioList intervenerOptionList = midHandleResp.getData().getIntervenerOptionList();

        assertEquals(1, intervenerOptionList.getListItems().size());
        assertEquals(ADD_INTERVENER_TWO_CODE, intervenerOptionList.getValue().getCode());
        assertEquals(ADD_INTERVENER_TWO_VALUE, intervenerOptionList.getValue().getLabel());
        assertEquals(ADD_INTERVENER_TWO_CODE, intervenerOptionList.getListItems().getFirst().getCode());
        assertEquals(ADD_INTERVENER_TWO_VALUE, intervenerOptionList.getListItems().getFirst().getLabel());
    }

    @Test
    public void givenContestedCase_whenMidEventCalledAndInterv3SelectedToOperate_thenPrepareOptionListForIntvBasedOnIntervenersList() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handleResp = aboutToStartHandler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(4, handleResp.getData().getIntervenersList().getListItems().size());

        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_THREE).build();
        handleResp.getData().getIntervenersList().setValue(option1);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> midHandleResp = midHandler.handle(finremCallbackRequest, AUTH_TOKEN);
        DynamicRadioList intervenerOptionList = midHandleResp.getData().getIntervenerOptionList();


        assertEquals(1, intervenerOptionList.getListItems().size());
        assertEquals(ADD_INTERVENER_THREE_CODE, intervenerOptionList.getValue().getCode());
        assertEquals(ADD_INTERVENER_THREE_VALUE, intervenerOptionList.getValue().getLabel());
        assertEquals(ADD_INTERVENER_THREE_CODE, intervenerOptionList.getListItems().getFirst().getCode());
        assertEquals(ADD_INTERVENER_THREE_VALUE, intervenerOptionList.getListItems().getFirst().getLabel());
    }

    @Test
    public void givenContestedCase_whenMidEventCalledAndInterv4SelectedToOperate_thenPrepareOptionListForIntvBasedOnIntervenersList() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handleResp = aboutToStartHandler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(4, handleResp.getData().getIntervenersList().getListItems().size());

        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_FOUR).build();
        handleResp.getData().getIntervenersList().setValue(option1);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> midHandleResp = midHandler.handle(finremCallbackRequest, AUTH_TOKEN);
        DynamicRadioList intervenerOptionList = midHandleResp.getData().getIntervenerOptionList();

        assertEquals(1, intervenerOptionList.getListItems().size());
        assertEquals(ADD_INTERVENER_FOUR_CODE, intervenerOptionList.getValue().getCode());
        assertEquals(ADD_INTERVENER_FOUR_VALUE, intervenerOptionList.getValue().getLabel());
        assertEquals(ADD_INTERVENER_FOUR_CODE, intervenerOptionList.getListItems().getFirst().getCode());
        assertEquals(ADD_INTERVENER_FOUR_VALUE, intervenerOptionList.getListItems().getFirst().getLabel());
    }

    @Test
    public void givenContestedCase_whenMidEventCalled_thenPrepareOptionListForIntvOneBasedOnIntervenersList() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        IntervenerOne oneWrapper = IntervenerOne
            .builder().intervenerName("One name").intervenerEmail("test@test.com").build();

        finremCallbackRequest.getCaseDetails().getData().setIntervenerOne(oneWrapper);
        finremCallbackRequest.getCaseDetailsBefore().getData().setIntervenerOne(oneWrapper);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handleResp = aboutToStartHandler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(4, handleResp.getData().getIntervenersList().getListItems().size());

        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_ONE).build();
        handleResp.getData().getIntervenersList().setValue(option1);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> midHandleResp = midHandler.handle(finremCallbackRequest, AUTH_TOKEN);
        DynamicRadioList intervenerOptionList = midHandleResp.getData().getIntervenerOptionList();

        assertEquals(2, intervenerOptionList.getListItems().size());
        assertEquals(ADD_INTERVENER_ONE_CODE, intervenerOptionList.getValue().getCode());
        assertEquals(UPDATE_INTERVENER_ONE_VALUE, intervenerOptionList.getValue().getLabel());
        assertEquals(ADD_INTERVENER_ONE_CODE, intervenerOptionList.getListItems().get(0).getCode());
        assertEquals(UPDATE_INTERVENER_ONE_VALUE, intervenerOptionList.getListItems().get(0).getLabel());
        assertEquals(DEL_INTERVENER_ONE_CODE, intervenerOptionList.getListItems().get(1).getCode());
        assertEquals(DEL_INTERVENER_ONE_VALUE, intervenerOptionList.getListItems().get(1).getLabel());
    }

    @Test
    public void givenContestedCase_whenMidEventCalled_thenPrepareOptionListForIntvTwoBasedOnIntervenersList() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        IntervenerTwo twoWrapper = IntervenerTwo
            .builder().intervenerName("Two name").intervenerEmail("test@test.com").build();

        finremCallbackRequest.getCaseDetails().getData().setIntervenerTwo(twoWrapper);
        finremCallbackRequest.getCaseDetailsBefore().getData().setIntervenerTwo(twoWrapper);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handleResp = aboutToStartHandler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(4, handleResp.getData().getIntervenersList().getListItems().size());

        DynamicRadioListElement option2 = DynamicRadioListElement.builder().code(INTERVENER_TWO).build();
        handleResp.getData().getIntervenersList().setValue(option2);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> midHandleResp = midHandler.handle(finremCallbackRequest, AUTH_TOKEN);
        DynamicRadioList intervenerOptionList = midHandleResp.getData().getIntervenerOptionList();

        assertEquals(2, intervenerOptionList.getListItems().size());
        assertEquals(ADD_INTERVENER_TWO_CODE, intervenerOptionList.getListItems().get(0).getCode());
        assertEquals(UPDATE_INTERVENER_TWO_VALUE, intervenerOptionList.getListItems().get(0).getLabel());
        assertEquals(DEL_INTERVENER_TWO_CODE, intervenerOptionList.getListItems().get(1).getCode());
        assertEquals(DEL_INTERVENER_TWO_VALUE, intervenerOptionList.getListItems().get(1).getLabel());
    }

    @Test
    public void givenContestedCase_whenMidEventCalled_thenPrepareOptionListForIntvThreeBasedOnIntervenersList() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        IntervenerThree threeWrapper = IntervenerThree
            .builder().intervenerName("Three name").intervenerEmail("test@test.com").build();

        finremCallbackRequest.getCaseDetails().getData().setIntervenerThree(threeWrapper);
        finremCallbackRequest.getCaseDetailsBefore().getData().setIntervenerThree(threeWrapper);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handleResp = aboutToStartHandler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(4, handleResp.getData().getIntervenersList().getListItems().size());

        DynamicRadioListElement option3 = DynamicRadioListElement.builder().code(INTERVENER_THREE).build();
        handleResp.getData().getIntervenersList().setValue(option3);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> midHandleResp = midHandler.handle(finremCallbackRequest, AUTH_TOKEN);
        DynamicRadioList intervenerOptionList = midHandleResp.getData().getIntervenerOptionList();

        assertEquals(2, intervenerOptionList.getListItems().size());
        assertEquals(ADD_INTERVENER_THREE_CODE, intervenerOptionList.getListItems().get(0).getCode());
        assertEquals(UPDATE_INTERVENER_THREE_VALUE, intervenerOptionList.getListItems().get(0).getLabel());
        assertEquals(DEL_INTERVENER_THREE_CODE, intervenerOptionList.getListItems().get(1).getCode());
        assertEquals(DEL_INTERVENER_THREE_VALUE, intervenerOptionList.getListItems().get(1).getLabel());
    }

    @Test
    public void givenContestedCase_whenMidEventCalled_thenPrepareOptionListForIntvFourBasedOnIntervenersList() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        IntervenerFour fourWrapper = IntervenerFour
            .builder().intervenerName("Four name").intervenerEmail("test@test.com").build();

        finremCallbackRequest.getCaseDetails().getData().setIntervenerFour(fourWrapper);
        finremCallbackRequest.getCaseDetailsBefore().getData().setIntervenerFour(fourWrapper);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handleResp = aboutToStartHandler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(4, handleResp.getData().getIntervenersList().getListItems().size());

        DynamicRadioListElement option4 = DynamicRadioListElement.builder().code(INTERVENER_FOUR).build();
        handleResp.getData().getIntervenersList().setValue(option4);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> midHandleResp = midHandler.handle(finremCallbackRequest, AUTH_TOKEN);
        DynamicRadioList intervenerOptionList = midHandleResp.getData().getIntervenerOptionList();

        assertEquals(2, intervenerOptionList.getListItems().size());
        assertEquals(ADD_INTERVENER_FOUR_CODE, intervenerOptionList.getListItems().get(0).getCode());
        assertEquals(UPDATE_INTERVENER_FOUR_VALUE, intervenerOptionList.getListItems().get(0).getLabel());
        assertEquals(DEL_INTERVENER_FOUR_CODE, intervenerOptionList.getListItems().get(1).getCode());
        assertEquals(DEL_INTERVENER_FOUR_VALUE, intervenerOptionList.getListItems().get(1).getLabel());
    }

    @Test
    public void givenContestedCase_whenMidEventCalledWithInvalidOption_thenHandlerThrowError() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        IntervenerFour fourWrapper = IntervenerFour
            .builder().intervenerName("Four name").intervenerEmail("test@test.com").build();

        finremCallbackRequest.getCaseDetails().getData().setIntervenerFour(fourWrapper);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handleResp = aboutToStartHandler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(4, handleResp.getData().getIntervenersList().getListItems().size());

        DynamicRadioListElement option4 = DynamicRadioListElement.builder().code(INTERVENER_INVALID).build();
        handleResp.getData().getIntervenersList().setValue(option4);

        assertThatThrownBy(() ->
            midHandler.handle(finremCallbackRequest, AUTH_TOKEN)
        ).isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid intervener selected for caseId 1234567890");
    }

    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), FinremCaseData.builder().build());
    }
}
