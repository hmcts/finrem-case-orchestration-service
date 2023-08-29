package uk.gov.hmcts.reform.finrem.caseorchestration.handler.intervener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataContested;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFourWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThreeWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwoWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IntervenerService;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.ADD_INTERVENER_FOUR_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.ADD_INTERVENER_ONE_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.ADD_INTERVENER_THREE_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.ADD_INTERVENER_TWO_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.DEL_INTERVENER_FOUR_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.DEL_INTERVENER_ONE_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.DEL_INTERVENER_THREE_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.DEL_INTERVENER_TWO_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_FOUR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_INVALID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_THREE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_TWO;

@RunWith(MockitoJUnitRunner.class)
public class IntervenersAboutToSubmitHandlerTest {

    public static final String AUTH_TOKEN = "tokien:)";

    @InjectMocks
    private IntervenersAboutToSubmitHandler handler;

    @InjectMocks
    private IntervenersAboutToStartHandler startHandler;
    @InjectMocks
    private IntervenersMidHandler midHandler;

    @Mock
    private IntervenerService service;

    @Test
    public void givenContestedCase_whenICallbackIsSubmitted_thenHandlerCanNotHandle() {
        assertThat(midHandler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.MANAGE_INTERVENERS),
            is(false));
    }

    @Test
    public void givenConsentedCase_whenEventIsClose_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.CLOSE),
            is(false));
    }

    @Test
    public void givenContestedCase_whenEventIsManageInteveners_thenHandlerCanHandleEvent() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.MANAGE_INTERVENERS),
            is(true));
    }

    @Test
    public void givenContestedCase_whenSelectionMadeToAddIntervener1_thenHandle() {
        FinremCallbackRequest<FinremCaseDataContested> finremCallbackRequest = buildCallbackRequest();
        FinremCaseDataContested finremCaseData = finremCallbackRequest.getCaseDetails().getData();

        IntervenerOneWrapper oneWrapper = IntervenerOneWrapper
            .builder().intervenerName("One name").intervenerEmail("test@test.com").build();

        finremCaseData.setIntervenerOneWrapper(oneWrapper);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseDataContested> handleResp =
            startHandler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(4, handleResp.getData().getIntervenersList().getListItems().size());

        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_ONE).build();
        finremCaseData.getIntervenersList().setValue(option1);

        midHandler.handle(finremCallbackRequest, AUTH_TOKEN);

        DynamicRadioListElement operation = DynamicRadioListElement.builder().code(ADD_INTERVENER_ONE_CODE).build();
        finremCaseData.getIntervenerOptionList().setValue(operation);

        handler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(service).updateIntervenerDetails(oneWrapper, finremCallbackRequest);
    }

    @Test
    public void givenContestedCase_whenSelectionMadeToRemoveIntervener1_thenHandle() {
        FinremCallbackRequest<FinremCaseDataContested> finremCallbackRequest = buildCallbackRequest();
        FinremCaseDataContested finremCaseData = finremCallbackRequest.getCaseDetails().getData();

        IntervenerOneWrapper oneWrapper = IntervenerOneWrapper
            .builder().intervenerName("One name").intervenerEmail("test@test.com").build();

        finremCaseData.setIntervenerOneWrapper(oneWrapper);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseDataContested> handleResp =
            startHandler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(4, handleResp.getData().getIntervenersList().getListItems().size());

        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_ONE).build();
        finremCaseData.getIntervenersList().setValue(option1);

        midHandler.handle(finremCallbackRequest, AUTH_TOKEN);

        DynamicRadioListElement operation = DynamicRadioListElement.builder().code(DEL_INTERVENER_ONE_CODE).build();
        finremCaseData.getIntervenerOptionList().setValue(operation);

        handler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(service).removeIntervenerDetails(oneWrapper, finremCaseData, finremCallbackRequest.getCaseDetails().getId());
    }

    @Test
    public void givenContestedCase_whenSelectionMadeToAddIntervener2_thenHandle() {
        FinremCallbackRequest<FinremCaseDataContested> finremCallbackRequest = buildCallbackRequest();
        FinremCaseDataContested finremCaseData = finremCallbackRequest.getCaseDetails().getData();

        IntervenerTwoWrapper twoWrapper = IntervenerTwoWrapper
            .builder().intervenerName("Two name").intervenerEmail("test@test.com").build();

        finremCaseData.setIntervenerTwoWrapper(twoWrapper);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseDataContested> handleResp =
            startHandler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(4, handleResp.getData().getIntervenersList().getListItems().size());

        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_TWO).build();
        finremCaseData.getIntervenersList().setValue(option1);

        midHandler.handle(finremCallbackRequest, AUTH_TOKEN);

        DynamicRadioListElement operation = DynamicRadioListElement.builder().code(ADD_INTERVENER_TWO_CODE).build();
        finremCaseData.getIntervenerOptionList().setValue(operation);

        handler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(service).updateIntervenerDetails(twoWrapper, finremCallbackRequest);
    }

    @Test
    public void givenContestedCase_whenSelectionMadeToRemoveIntervener2_thenHandle() {
        FinremCallbackRequest<FinremCaseDataContested> finremCallbackRequest = buildCallbackRequest();
        FinremCaseDataContested finremCaseData = finremCallbackRequest.getCaseDetails().getData();

        IntervenerTwoWrapper twoWrapper = IntervenerTwoWrapper
            .builder().intervenerName("Two name").intervenerEmail("test@test.com").build();

        finremCaseData.setIntervenerTwoWrapper(twoWrapper);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseDataContested> handleResp =
            startHandler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(4, handleResp.getData().getIntervenersList().getListItems().size());

        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_TWO).build();
        finremCaseData.getIntervenersList().setValue(option1);

        midHandler.handle(finremCallbackRequest, AUTH_TOKEN);

        DynamicRadioListElement operation = DynamicRadioListElement.builder().code(DEL_INTERVENER_TWO_CODE).build();
        finremCaseData.getIntervenerOptionList().setValue(operation);

        handler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(service).removeIntervenerDetails(twoWrapper, finremCaseData, finremCallbackRequest.getCaseDetails().getId());
    }


    @Test
    public void givenContestedCase_whenSelectionMadeToAddIntervener3_thenHandle() {
        FinremCallbackRequest<FinremCaseDataContested> finremCallbackRequest = buildCallbackRequest();
        FinremCaseDataContested finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        IntervenerThreeWrapper threeWrapper = IntervenerThreeWrapper
            .builder().intervenerName("Three name").intervenerEmail("test@test.com").build();

        finremCaseData.setIntervenerThreeWrapper(threeWrapper);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseDataContested> handleResp =
            startHandler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(4, handleResp.getData().getIntervenersList().getListItems().size());

        DynamicRadioListElement option3 = DynamicRadioListElement.builder().code(INTERVENER_THREE).build();
        handleResp.getData().getIntervenersList().setValue(option3);
        midHandler.handle(finremCallbackRequest, AUTH_TOKEN);

        DynamicRadioListElement operation = DynamicRadioListElement.builder().code(ADD_INTERVENER_THREE_CODE).build();
        finremCaseData.getIntervenerOptionList().setValue(operation);

        handler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(service).updateIntervenerDetails(threeWrapper, finremCallbackRequest);
    }

    @Test
    public void givenContestedCase_whenSelectionMadeToRemoveIntervener3_thenHandle() {
        FinremCallbackRequest<FinremCaseDataContested> finremCallbackRequest = buildCallbackRequest();
        FinremCaseDataContested finremCaseData = finremCallbackRequest.getCaseDetails().getData();

        IntervenerThreeWrapper threeWrapper = IntervenerThreeWrapper
            .builder().intervenerName("Three name").intervenerEmail("test@test.com").build();

        finremCaseData.setIntervenerThreeWrapper(threeWrapper);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseDataContested> handleResp = startHandler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(4, handleResp.getData().getIntervenersList().getListItems().size());

        DynamicRadioListElement option3 = DynamicRadioListElement.builder().code(INTERVENER_THREE).build();
        handleResp.getData().getIntervenersList().setValue(option3);
        midHandler.handle(finremCallbackRequest, AUTH_TOKEN);

        DynamicRadioListElement operation = DynamicRadioListElement.builder().code(DEL_INTERVENER_THREE_CODE).build();
        finremCaseData.getIntervenerOptionList().setValue(operation);

        handler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(service).removeIntervenerDetails(threeWrapper, finremCaseData, finremCallbackRequest.getCaseDetails().getId());
    }

    @Test
    public void givenContestedCase_whenSelectionMadeToAddIntervener4_thenHandle() {
        FinremCallbackRequest<FinremCaseDataContested> finremCallbackRequest = buildCallbackRequest();
        FinremCaseDataContested finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        IntervenerFourWrapper fourWrapper = IntervenerFourWrapper
            .builder().intervenerName("Four name").intervenerEmail("test@test.com").build();

        finremCaseData.setIntervenerFourWrapper(fourWrapper);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseDataContested> handleResp =
            startHandler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(4, handleResp.getData().getIntervenersList().getListItems().size());

        DynamicRadioListElement option4 = DynamicRadioListElement.builder().code(INTERVENER_FOUR).build();
        handleResp.getData().getIntervenersList().setValue(option4);

        midHandler.handle(finremCallbackRequest, AUTH_TOKEN);

        DynamicRadioListElement operation = DynamicRadioListElement.builder().code(ADD_INTERVENER_FOUR_CODE).build();
        finremCaseData.getIntervenerOptionList().setValue(operation);

        handler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(service).updateIntervenerDetails(fourWrapper, finremCallbackRequest);
    }

    @Test
    public void givenContestedCase_whenSelectionMadeToRemoveIntervener4_thenHandle() {
        FinremCallbackRequest<FinremCaseDataContested> finremCallbackRequest = buildCallbackRequest();
        FinremCaseDataContested finremCaseData = finremCallbackRequest.getCaseDetails().getData();


        IntervenerFourWrapper fourWrapper = IntervenerFourWrapper
            .builder().intervenerName("Four name").intervenerEmail("test@test.com").build();

        finremCaseData.setIntervenerFourWrapper(fourWrapper);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseDataContested> handleResp =
            startHandler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(4, handleResp.getData().getIntervenersList().getListItems().size());

        DynamicRadioListElement option4 = DynamicRadioListElement.builder().code(INTERVENER_FOUR).build();
        handleResp.getData().getIntervenersList().setValue(option4);

        midHandler.handle(finremCallbackRequest, AUTH_TOKEN);

        DynamicRadioListElement operation = DynamicRadioListElement.builder().code(DEL_INTERVENER_FOUR_CODE).build();
        finremCaseData.getIntervenerOptionList().setValue(operation);

        handler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(service).removeIntervenerDetails(fourWrapper, finremCaseData, finremCallbackRequest.getCaseDetails().getId());
    }

    @Test
    public void givenContestedCase_whenInvalidOptionReceived_thenHandlerThrowError() {
        FinremCallbackRequest<FinremCaseDataContested> finremCallbackRequest = buildCallbackRequest();
        FinremCaseDataContested finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        IntervenerFourWrapper fourWrapper = IntervenerFourWrapper
            .builder().intervenerName("Four name").intervenerEmail("test@test.com").build();

        finremCaseData.setIntervenerFourWrapper(fourWrapper);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseDataContested> handleResp = startHandler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(4, handleResp.getData().getIntervenersList().getListItems().size());

        DynamicRadioListElement option4 = DynamicRadioListElement.builder().code(INTERVENER_FOUR).build();
        handleResp.getData().getIntervenersList().setValue(option4);

        midHandler.handle(finremCallbackRequest, AUTH_TOKEN);


        DynamicRadioListElement operation = DynamicRadioListElement.builder().code(INTERVENER_INVALID).build();
        finremCaseData.getIntervenerOptionList().setValue(operation);

        assertThatThrownBy(() ->
            handler.handle(finremCallbackRequest, AUTH_TOKEN)
        ).isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid option received for case " + 123L);
    }

    private FinremCallbackRequest<FinremCaseDataContested> buildCallbackRequest() {
        return FinremCallbackRequest
            .<FinremCaseDataContested>builder()
            .eventType(EventType.MANAGE_INTERVENERS)
            .caseDetailsBefore(FinremCaseDetails.<FinremCaseDataContested>builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseDataContested()).build())
            .caseDetails(FinremCaseDetails.<FinremCaseDataContested>builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseDataContested()).build())
            .build();
    }
}
