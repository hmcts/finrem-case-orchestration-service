package uk.gov.hmcts.reform.finrem.caseorchestration.handler.intervener;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerChangeDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IntervenerService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.intervener.IntervenerAddedCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.intervener.IntervenerRemovedCorresponder;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_THREE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_TWO;

@ExtendWith(MockitoExtension.class)
class IntervenersSubmittedHandlerTest {

    public static final String AUTH_TOKEN = "tokien:)";

    @InjectMocks
    private IntervenersSubmittedHandler handler;

    @Mock
    private IntervenerAddedCorresponder intervenerAddedCorresponder;

    @Mock
    private IntervenerRemovedCorresponder intervenerRemovedCorresponder;

    @Mock
    private IntervenerService service;

    @Test
    void givenContestedCase_whenICallbackIsAboutToSubmit_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.MANAGE_INTERVENERS),
            is(false));
    }

    @Test
    void givenConsentedCase_whenEventIsClose_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.CLOSE),
            is(false));
    }

    @Test
    void givenContestedCase_whenEventIsManageInteveners_thenHandlerCanHandleEvent() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.MANAGE_INTERVENERS),
            is(true));
    }

    @Test
    void givenContestedCase_whenIntervenerOneActionIsAdded_thenSendCorrespondence() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData caseData = finremCallbackRequest.getCaseDetails().getData();

        IntervenerChangeDetails intervenerOneChangeDetails = new IntervenerChangeDetails();
        intervenerOneChangeDetails.setIntervenerType(IntervenerType.INTERVENER_ONE);
        intervenerOneChangeDetails.setIntervenerAction(IntervenerAction.ADDED);

        caseData.setCurrentIntervenerChangeDetails(intervenerOneChangeDetails);
        caseData.setIntervenerOptionList(DynamicRadioList.builder().build());
        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_ONE).build();
        DynamicRadioListElement operation = DynamicRadioListElement.builder().code(ADD_INTERVENER_ONE_CODE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        caseData.setIntervenersList(dynamicRadioList);
        caseData.getIntervenersList().setValue(option);
        caseData.getIntervenerOptionList().setValue(operation);

        when(service.setIntervenerOneAddedChangeDetails(caseData)).thenReturn(intervenerOneChangeDetails);
        when(service.checkIfAnyIntervenerSolicitorRemoved(any(), any())).thenReturn(false);
        handler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(intervenerAddedCorresponder).sendCorrespondence(
            finremCallbackRequest.getCaseDetails(), finremCallbackRequest.getCaseDetailsBefore(), AUTH_TOKEN);
    }

    @Test
    void givenContestedCase_whenIntervenerTwoActionIsAdded_thenSendCorrespondence() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData caseData = finremCallbackRequest.getCaseDetails().getData();

        IntervenerChangeDetails intervenerTwoChangeDetails = new IntervenerChangeDetails();
        intervenerTwoChangeDetails.setIntervenerType(IntervenerType.INTERVENER_TWO);
        intervenerTwoChangeDetails.setIntervenerAction(IntervenerAction.ADDED);

        caseData.setCurrentIntervenerChangeDetails(intervenerTwoChangeDetails);
        caseData.setIntervenerOptionList(DynamicRadioList.builder().build());
        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_TWO).build();
        DynamicRadioListElement operation = DynamicRadioListElement.builder().code(ADD_INTERVENER_TWO_CODE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        caseData.setIntervenersList(dynamicRadioList);
        caseData.getIntervenersList().setValue(option);
        caseData.getIntervenerOptionList().setValue(operation);

        when(service.setIntervenerTwoAddedChangeDetails(caseData)).thenReturn(intervenerTwoChangeDetails);
        when(service.checkIfAnyIntervenerSolicitorRemoved(any(), any())).thenReturn(false);
        handler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(intervenerAddedCorresponder).sendCorrespondence(
            finremCallbackRequest.getCaseDetails(), finremCallbackRequest.getCaseDetailsBefore(), AUTH_TOKEN);
    }

    @Test
    void givenContestedCase_whenIntervenerThreeActionIsAdded_thenSendCorrespondence() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData caseData = finremCallbackRequest.getCaseDetails().getData();

        IntervenerChangeDetails intervenerThreeChangeDetails = new IntervenerChangeDetails();
        intervenerThreeChangeDetails.setIntervenerType(IntervenerType.INTERVENER_THREE);
        intervenerThreeChangeDetails.setIntervenerAction(IntervenerAction.ADDED);

        caseData.setCurrentIntervenerChangeDetails(intervenerThreeChangeDetails);
        caseData.setIntervenerOptionList(DynamicRadioList.builder().build());
        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_THREE).build();
        DynamicRadioListElement operation = DynamicRadioListElement.builder().code(ADD_INTERVENER_THREE_CODE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        caseData.setIntervenersList(dynamicRadioList);
        caseData.getIntervenersList().setValue(option);
        caseData.getIntervenerOptionList().setValue(operation);

        when(service.setIntervenerThreeAddedChangeDetails(caseData)).thenReturn(intervenerThreeChangeDetails);
        when(service.checkIfAnyIntervenerSolicitorRemoved(any(), any())).thenReturn(false);
        handler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(intervenerAddedCorresponder).sendCorrespondence(
            finremCallbackRequest.getCaseDetails(), finremCallbackRequest.getCaseDetailsBefore(), AUTH_TOKEN);
    }

    @Test
    void givenContestedCase_whenIntervenerFourActionIsAdded_thenSendCorrespondence() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData caseData = finremCallbackRequest.getCaseDetails().getData();

        IntervenerChangeDetails intervenerFourChangeDetails = new IntervenerChangeDetails();
        intervenerFourChangeDetails.setIntervenerType(IntervenerType.INTERVENER_FOUR);
        intervenerFourChangeDetails.setIntervenerAction(IntervenerAction.ADDED);

        caseData.setCurrentIntervenerChangeDetails(intervenerFourChangeDetails);
        caseData.setIntervenerOptionList(DynamicRadioList.builder().build());
        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_FOUR).build();
        DynamicRadioListElement operation = DynamicRadioListElement.builder().code(ADD_INTERVENER_FOUR_CODE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        caseData.setIntervenersList(dynamicRadioList);
        caseData.getIntervenersList().setValue(option);
        caseData.getIntervenerOptionList().setValue(operation);

        when(service.setIntervenerFourAddedChangeDetails(caseData)).thenReturn(intervenerFourChangeDetails);
        when(service.checkIfAnyIntervenerSolicitorRemoved(any(), any())).thenReturn(false);
        handler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(intervenerAddedCorresponder).sendCorrespondence(
            finremCallbackRequest.getCaseDetails(), finremCallbackRequest.getCaseDetailsBefore(), AUTH_TOKEN);
    }

    @Test
    void givenContestedCase_whenIntervenerOneActionIsRemoved_thenSendCorrespondence() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData caseData = finremCallbackRequest.getCaseDetails().getData();

        IntervenerChangeDetails intervenerOneChangeDetails = new IntervenerChangeDetails();
        intervenerOneChangeDetails.setIntervenerType(IntervenerType.INTERVENER_ONE);
        intervenerOneChangeDetails.setIntervenerAction(IntervenerAction.REMOVED);

        caseData.setCurrentIntervenerChangeDetails(intervenerOneChangeDetails);
        caseData.setIntervenerOptionList(DynamicRadioList.builder().build());
        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_ONE).build();
        DynamicRadioListElement operation = DynamicRadioListElement.builder().code(DEL_INTERVENER_ONE_CODE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        caseData.setIntervenersList(dynamicRadioList);
        caseData.getIntervenersList().setValue(option);
        caseData.getIntervenerOptionList().setValue(operation);

        when(service.setIntervenerOneRemovedChangeDetails(caseData)).thenReturn(intervenerOneChangeDetails);
        when(service.checkIfAnyIntervenerSolicitorRemoved(any(), any())).thenReturn(false);
        handler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(intervenerRemovedCorresponder).sendCorrespondence(
            finremCallbackRequest.getCaseDetails(), finremCallbackRequest.getCaseDetailsBefore(), AUTH_TOKEN);
    }

    @Test
    void givenContestedCase_whenIntervenerTwoActionIsRemoved_thenSendCorrespondence() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData caseData = finremCallbackRequest.getCaseDetails().getData();

        IntervenerChangeDetails intervenerTwoChangeDetails = new IntervenerChangeDetails();
        intervenerTwoChangeDetails.setIntervenerType(IntervenerType.INTERVENER_TWO);
        intervenerTwoChangeDetails.setIntervenerAction(IntervenerAction.REMOVED);

        caseData.setCurrentIntervenerChangeDetails(intervenerTwoChangeDetails);
        caseData.setIntervenerOptionList(DynamicRadioList.builder().build());
        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_TWO).build();
        DynamicRadioListElement operation = DynamicRadioListElement.builder().code(DEL_INTERVENER_TWO_CODE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        caseData.setIntervenersList(dynamicRadioList);
        caseData.getIntervenersList().setValue(option);
        caseData.getIntervenerOptionList().setValue(operation);

        when(service.setIntervenerTwoRemovedChangeDetails(caseData)).thenReturn(intervenerTwoChangeDetails);
        when(service.checkIfAnyIntervenerSolicitorRemoved(any(), any())).thenReturn(false);
        handler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(intervenerRemovedCorresponder).sendCorrespondence(
            finremCallbackRequest.getCaseDetails(), finremCallbackRequest.getCaseDetailsBefore(), AUTH_TOKEN);
    }

    @Test
    void givenContestedCase_whenIntervenerThreeActionIsRemoved_thenSendCorrespondence() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData caseData = finremCallbackRequest.getCaseDetails().getData();

        IntervenerChangeDetails intervenerThreeChangeDetails = new IntervenerChangeDetails();
        intervenerThreeChangeDetails.setIntervenerType(IntervenerType.INTERVENER_THREE);
        intervenerThreeChangeDetails.setIntervenerAction(IntervenerAction.REMOVED);

        caseData.setCurrentIntervenerChangeDetails(intervenerThreeChangeDetails);
        caseData.setIntervenerOptionList(DynamicRadioList.builder().build());
        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_THREE).build();
        DynamicRadioListElement operation = DynamicRadioListElement.builder().code(DEL_INTERVENER_THREE_CODE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        caseData.setIntervenersList(dynamicRadioList);
        caseData.getIntervenersList().setValue(option);
        caseData.getIntervenerOptionList().setValue(operation);

        when(service.setIntervenerThreeRemovedChangeDetails(caseData)).thenReturn(intervenerThreeChangeDetails);
        when(service.checkIfAnyIntervenerSolicitorRemoved(any(), any())).thenReturn(false);
        handler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(intervenerRemovedCorresponder).sendCorrespondence(
            finremCallbackRequest.getCaseDetails(), finremCallbackRequest.getCaseDetailsBefore(), AUTH_TOKEN);
    }

    @Test
    void givenContestedCase_whenIntervenerFourActionIsRemoved_thenSendCorrespondence() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData caseData = finremCallbackRequest.getCaseDetails().getData();

        IntervenerChangeDetails intervenerFourChangeDetails = new IntervenerChangeDetails();
        intervenerFourChangeDetails.setIntervenerType(IntervenerType.INTERVENER_FOUR);
        intervenerFourChangeDetails.setIntervenerAction(IntervenerAction.REMOVED);

        caseData.setCurrentIntervenerChangeDetails(intervenerFourChangeDetails);
        caseData.setIntervenerOptionList(DynamicRadioList.builder().build());
        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_FOUR).build();
        DynamicRadioListElement operation = DynamicRadioListElement.builder().code(DEL_INTERVENER_FOUR_CODE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        caseData.setIntervenersList(dynamicRadioList);
        caseData.getIntervenersList().setValue(option);
        caseData.getIntervenerOptionList().setValue(operation);

        when(service.setIntervenerFourRemovedChangeDetails(caseData)).thenReturn(intervenerFourChangeDetails);
        when(service.checkIfAnyIntervenerSolicitorRemoved(any(), any())).thenReturn(false);
        handler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(intervenerRemovedCorresponder).sendCorrespondence(
            finremCallbackRequest.getCaseDetails(), finremCallbackRequest.getCaseDetailsBefore(), AUTH_TOKEN);
    }

    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.MANAGE_INTERVENERS)
            .caseDetailsBefore(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).build())
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).build())
            .build();
    }
}