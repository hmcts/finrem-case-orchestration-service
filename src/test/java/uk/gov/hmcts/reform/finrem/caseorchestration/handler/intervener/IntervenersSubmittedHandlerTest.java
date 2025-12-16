package uk.gov.hmcts.reform.finrem.caseorchestration.handler.intervener;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerChangeDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IntervenerService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.intervener.IntervenerAddedCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.intervener.IntervenerRemovedCorresponder;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.ADD_INTERVENER_FOUR_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.ADD_INTERVENER_ONE_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.ADD_INTERVENER_THREE_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.ADD_INTERVENER_TWO_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.DEL_INTERVENER_FOUR_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.DEL_INTERVENER_ONE_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.DEL_INTERVENER_THREE_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.DEL_INTERVENER_TWO_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_FOUR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_THREE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_TWO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class IntervenersSubmittedHandlerTest {

    @InjectMocks
    private IntervenersSubmittedHandler submittedHandler;

    @Mock
    private IntervenerAddedCorresponder intervenerAddedCorresponder;

    @Mock
    private IntervenerRemovedCorresponder intervenerRemovedCorresponder;

    @Mock
    private IntervenerService intervenerService;

    @Test
    void testCanHandle() {
        assertCanHandle(submittedHandler, CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.MANAGE_INTERVENERS);
    }

    @Test
    void givenContestedCase_whenIntervenerOneActionIsAdded_thenSendCorrespondence() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();

        IntervenerChangeDetails intervenerOneChangeDetails = new IntervenerChangeDetails();
        intervenerOneChangeDetails.setIntervenerType(IntervenerType.INTERVENER_ONE);
        intervenerOneChangeDetails.setIntervenerAction(IntervenerAction.ADDED);

        finremCaseData.setCurrentIntervenerChangeDetails(intervenerOneChangeDetails);
        finremCaseData.setIntervenerOptionList(DynamicRadioList.builder().build());
        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_ONE).build();

        DynamicRadioListElement operation = DynamicRadioListElement.builder().code(ADD_INTERVENER_ONE_CODE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        finremCaseData.getIntervenersList().setValue(option);
        finremCaseData.getIntervenerOptionList().setValue(operation);
        IntervenerOne oneWrapper = IntervenerOne
            .builder().intervenerName("One name").intervenerEmail("test@test.com").build();
        finremCaseData.setIntervenerOne(oneWrapper);
        when(intervenerService.setIntervenerAddedChangeDetails(oneWrapper)).thenReturn(intervenerOneChangeDetails);

        submittedHandler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(intervenerAddedCorresponder).sendCorrespondence(finremCallbackRequest.getCaseDetails(), AUTH_TOKEN);
    }

    @Test
    void givenContestedCase_whenIntervenerTwoActionIsAdded_thenSendCorrespondence() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();

        IntervenerChangeDetails intervenerTwoChangeDetails = new IntervenerChangeDetails();
        intervenerTwoChangeDetails.setIntervenerType(IntervenerType.INTERVENER_TWO);
        intervenerTwoChangeDetails.setIntervenerAction(IntervenerAction.ADDED);

        finremCaseData.setCurrentIntervenerChangeDetails(intervenerTwoChangeDetails);
        finremCaseData.setIntervenerOptionList(DynamicRadioList.builder().build());
        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_TWO).build();

        DynamicRadioListElement operation = DynamicRadioListElement.builder().code(ADD_INTERVENER_TWO_CODE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        finremCaseData.getIntervenersList().setValue(option);
        finremCaseData.getIntervenerOptionList().setValue(operation);
        IntervenerTwo twoWrapper = IntervenerTwo
            .builder().intervenerName("Two name").intervenerEmail("test@test.com").build();
        finremCaseData.setIntervenerTwo(twoWrapper);

        when(intervenerService.setIntervenerAddedChangeDetails(twoWrapper)).thenReturn(intervenerTwoChangeDetails);

        submittedHandler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(intervenerAddedCorresponder).sendCorrespondence(finremCallbackRequest.getCaseDetails(), AUTH_TOKEN);
    }

    @Test
    void givenContestedCase_whenIntervenerThreeActionIsAdded_thenSendCorrespondence() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();

        IntervenerChangeDetails intervenerThreeChangeDetails = new IntervenerChangeDetails();
        intervenerThreeChangeDetails.setIntervenerType(IntervenerType.INTERVENER_THREE);
        intervenerThreeChangeDetails.setIntervenerAction(IntervenerAction.ADDED);

        finremCaseData.setCurrentIntervenerChangeDetails(intervenerThreeChangeDetails);
        finremCaseData.setIntervenerOptionList(DynamicRadioList.builder().build());
        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_THREE).build();

        DynamicRadioListElement operation = DynamicRadioListElement.builder().code(ADD_INTERVENER_THREE_CODE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        finremCaseData.getIntervenersList().setValue(option);
        finremCaseData.getIntervenerOptionList().setValue(operation);

        IntervenerThree threeWrapper = IntervenerThree
            .builder().intervenerName("Three name").intervenerEmail("test@test.com").build();
        finremCaseData.setIntervenerThree(threeWrapper);

        when(intervenerService.setIntervenerAddedChangeDetails(threeWrapper)).thenReturn(intervenerThreeChangeDetails);

        submittedHandler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(intervenerAddedCorresponder).sendCorrespondence(finremCallbackRequest.getCaseDetails(), AUTH_TOKEN);
    }

    @Test
    void givenContestedCase_whenIntervenerFourActionIsAdded_thenSendCorrespondence() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();

        IntervenerChangeDetails intervenerFourChangeDetails = new IntervenerChangeDetails();
        intervenerFourChangeDetails.setIntervenerType(IntervenerType.INTERVENER_FOUR);
        intervenerFourChangeDetails.setIntervenerAction(IntervenerAction.ADDED);

        finremCaseData.setCurrentIntervenerChangeDetails(intervenerFourChangeDetails);
        finremCaseData.setIntervenerOptionList(DynamicRadioList.builder().build());
        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_FOUR).build();

        DynamicRadioListElement operation = DynamicRadioListElement.builder().code(ADD_INTERVENER_FOUR_CODE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        finremCaseData.getIntervenersList().setValue(option);
        finremCaseData.getIntervenerOptionList().setValue(operation);


        IntervenerFour fourWrapper = IntervenerFour
            .builder().intervenerName("Four name").intervenerEmail("test@test.com").build();
        finremCaseData.setIntervenerFour(fourWrapper);

        when(intervenerService.setIntervenerAddedChangeDetails(fourWrapper)).thenReturn(intervenerFourChangeDetails);

        submittedHandler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(intervenerAddedCorresponder).sendCorrespondence(finremCallbackRequest.getCaseDetails(), AUTH_TOKEN);
    }

    @Test
    void givenContestedCase_whenIntervenerOneActionIsRemoved_thenSendCorrespondence() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();

        IntervenerChangeDetails intervenerOneChangeDetails = new IntervenerChangeDetails();
        intervenerOneChangeDetails.setIntervenerType(IntervenerType.INTERVENER_THREE);
        intervenerOneChangeDetails.setIntervenerAction(IntervenerAction.REMOVED);

        finremCaseData.setCurrentIntervenerChangeDetails(intervenerOneChangeDetails);
        finremCaseData.setIntervenerOptionList(DynamicRadioList.builder().build());
        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_ONE).build();

        DynamicRadioListElement operation = DynamicRadioListElement.builder().code(DEL_INTERVENER_ONE_CODE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        finremCaseData.getIntervenersList().setValue(option);
        finremCaseData.getIntervenerOptionList().setValue(operation);

        FinremCaseData finremCaseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();
        IntervenerOne oneWrapper = IntervenerOne
            .builder().intervenerName("One name").intervenerEmail("test@test.com").build();
        finremCaseDataBefore.setIntervenerOne(oneWrapper);

        when(intervenerService.setIntervenerRemovedChangeDetails(oneWrapper)).thenReturn(intervenerOneChangeDetails);
        submittedHandler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(intervenerRemovedCorresponder).sendCorrespondence(finremCallbackRequest.getCaseDetails(), AUTH_TOKEN);
    }

    @Test
    void givenContestedCase_whenIntervenerTwoActionIsRemoved_thenSendCorrespondence() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();

        IntervenerChangeDetails intervenerTwoChangeDetails = new IntervenerChangeDetails();
        intervenerTwoChangeDetails.setIntervenerType(IntervenerType.INTERVENER_TWO);
        intervenerTwoChangeDetails.setIntervenerAction(IntervenerAction.REMOVED);

        finremCaseData.setCurrentIntervenerChangeDetails(intervenerTwoChangeDetails);
        finremCaseData.setIntervenerOptionList(DynamicRadioList.builder().build());
        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_TWO).build();

        DynamicRadioListElement operation = DynamicRadioListElement.builder().code(DEL_INTERVENER_TWO_CODE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        finremCaseData.getIntervenersList().setValue(option);
        finremCaseData.getIntervenerOptionList().setValue(operation);

        FinremCaseData finremCaseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();
        IntervenerTwo twoWrapper = IntervenerTwo
            .builder().intervenerName("Two name").intervenerEmail("test@test.com").build();
        finremCaseDataBefore.setIntervenerTwo(twoWrapper);

        when(intervenerService.setIntervenerRemovedChangeDetails(twoWrapper)).thenReturn(intervenerTwoChangeDetails);
        submittedHandler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(intervenerRemovedCorresponder).sendCorrespondence(finremCallbackRequest.getCaseDetails(), AUTH_TOKEN);
    }

    @Test
    void givenContestedCase_whenIntervenerThreeActionIsRemoved_thenSendCorrespondence() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();

        IntervenerChangeDetails intervenerThreeChangeDetails = new IntervenerChangeDetails();
        intervenerThreeChangeDetails.setIntervenerType(IntervenerType.INTERVENER_THREE);
        intervenerThreeChangeDetails.setIntervenerAction(IntervenerAction.REMOVED);

        finremCaseData.setCurrentIntervenerChangeDetails(intervenerThreeChangeDetails);
        finremCaseData.setIntervenerOptionList(DynamicRadioList.builder().build());
        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_THREE).build();

        DynamicRadioListElement operation = DynamicRadioListElement.builder().code(DEL_INTERVENER_THREE_CODE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        finremCaseData.getIntervenersList().setValue(option);
        finremCaseData.getIntervenerOptionList().setValue(operation);

        FinremCaseData finremCaseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();
        IntervenerThree threeWrapper = IntervenerThree
            .builder().intervenerName("Three name").intervenerEmail("test@test.com").build();
        finremCaseDataBefore.setIntervenerThree(threeWrapper);

        when(intervenerService.setIntervenerRemovedChangeDetails(threeWrapper)).thenReturn(intervenerThreeChangeDetails);
        submittedHandler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(intervenerRemovedCorresponder).sendCorrespondence(finremCallbackRequest.getCaseDetails(), AUTH_TOKEN);
    }

    @Test
    void givenContestedCase_whenIntervenerFourActionIsRemoved_thenSendCorrespondence() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();

        IntervenerChangeDetails intervenerFourChangeDetails = new IntervenerChangeDetails();
        intervenerFourChangeDetails.setIntervenerType(IntervenerType.INTERVENER_FOUR);
        intervenerFourChangeDetails.setIntervenerAction(IntervenerAction.REMOVED);

        finremCaseData.setCurrentIntervenerChangeDetails(intervenerFourChangeDetails);
        finremCaseData.setIntervenerOptionList(DynamicRadioList.builder().build());
        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_FOUR).build();

        DynamicRadioListElement operation = DynamicRadioListElement.builder().code(DEL_INTERVENER_FOUR_CODE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        finremCaseData.getIntervenersList().setValue(option);
        finremCaseData.getIntervenerOptionList().setValue(operation);

        FinremCaseData finremCaseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();
        IntervenerFour fourWrapper = IntervenerFour
            .builder().intervenerName("Four name").intervenerEmail("test@test.com").build();
        finremCaseDataBefore.setIntervenerFour(fourWrapper);

        when(intervenerService.setIntervenerRemovedChangeDetails(fourWrapper)).thenReturn(intervenerFourChangeDetails);
        submittedHandler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(intervenerRemovedCorresponder).sendCorrespondence(finremCallbackRequest.getCaseDetails(), AUTH_TOKEN);
    }

    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequestFactory.from();
    }
}
