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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
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

@ExtendWith(MockitoExtension.class)
class IntervenersSubmittedHandlerTest {

    public static final String AUTH_TOKEN = "tokien:)";

    @InjectMocks
    private IntervenersSubmittedHandler handler;

    @Mock
    private IntervenerAddedCorresponder intervenerAddedCorresponder;

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
    void givenContestedCase_whenIntervenerOneActionIsAdded_thenSendCorrespondance() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();

        IntervenerChangeDetails intervenerOneChangeDetails = new IntervenerChangeDetails();
        intervenerOneChangeDetails.setIntervenerType(IntervenerType.INTERVENER_ONE);
        intervenerOneChangeDetails.setIntervenerAction(IntervenerAction.ADDED);

        finremCaseData.setCurrentIntervenerChangeDetails(intervenerOneChangeDetails);
        finremCaseData.setIntervenerOptionList(DynamicRadioList.builder().build());

        DynamicRadioListElement operation = DynamicRadioListElement.builder().code(ADD_INTERVENER_ONE_CODE).build();
        finremCaseData.getIntervenerOptionList().setValue(operation);

        when(service.setIntervenerOneAddedChangeDetails(finremCaseData)).thenReturn(intervenerOneChangeDetails);

        handler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(intervenerAddedCorresponder).sendCorrespondence(finremCallbackRequest.getCaseDetails(), AUTH_TOKEN);
    }

    @Test
    void givenContestedCase_whenIntervenerTwoActionIsAdded_thenSendCorrespondance() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();

        IntervenerChangeDetails intervenerTwoChangeDetails = new IntervenerChangeDetails();
        intervenerTwoChangeDetails.setIntervenerType(IntervenerType.INTERVENER_TWO);
        intervenerTwoChangeDetails.setIntervenerAction(IntervenerAction.ADDED);

        finremCaseData.setCurrentIntervenerChangeDetails(intervenerTwoChangeDetails);
        finremCaseData.setIntervenerOptionList(DynamicRadioList.builder().build());

        DynamicRadioListElement operation = DynamicRadioListElement.builder().code(ADD_INTERVENER_TWO_CODE).build();
        finremCaseData.getIntervenerOptionList().setValue(operation);

        when(service.setIntervenerTwoAddedChangeDetails(finremCaseData)).thenReturn(intervenerTwoChangeDetails);

        handler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(intervenerAddedCorresponder).sendCorrespondence(finremCallbackRequest.getCaseDetails(), AUTH_TOKEN);
    }

    @Test
    void givenContestedCase_whenIntervenerThreeActionIsAdded_thenSendCorrespondance() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();

        IntervenerChangeDetails intervenerThreeChangeDetails = new IntervenerChangeDetails();
        intervenerThreeChangeDetails.setIntervenerType(IntervenerType.INTERVENER_THREE);
        intervenerThreeChangeDetails.setIntervenerAction(IntervenerAction.ADDED);

        finremCaseData.setCurrentIntervenerChangeDetails(intervenerThreeChangeDetails);
        finremCaseData.setIntervenerOptionList(DynamicRadioList.builder().build());

        DynamicRadioListElement operation = DynamicRadioListElement.builder().code(ADD_INTERVENER_THREE_CODE).build();
        finremCaseData.getIntervenerOptionList().setValue(operation);

        when(service.setIntervenerThreeAddedChangeDetails(finremCaseData)).thenReturn(intervenerThreeChangeDetails);

        handler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(intervenerAddedCorresponder).sendCorrespondence(finremCallbackRequest.getCaseDetails(), AUTH_TOKEN);
    }

    @Test
    void givenContestedCase_whenIntervenerFourActionIsAdded_thenSendCorrespondance() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();

        IntervenerChangeDetails intervenerFourChangeDetails = new IntervenerChangeDetails();
        intervenerFourChangeDetails.setIntervenerType(IntervenerType.INTERVENER_FOUR);
        intervenerFourChangeDetails.setIntervenerAction(IntervenerAction.ADDED);

        finremCaseData.setCurrentIntervenerChangeDetails(intervenerFourChangeDetails);
        finremCaseData.setIntervenerOptionList(DynamicRadioList.builder().build());

        DynamicRadioListElement operation = DynamicRadioListElement.builder().code(ADD_INTERVENER_FOUR_CODE).build();
        finremCaseData.getIntervenerOptionList().setValue(operation);

        when(service.setIntervenerFourAddedChangeDetails(finremCaseData)).thenReturn(intervenerFourChangeDetails);

        handler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(intervenerAddedCorresponder).sendCorrespondence(finremCallbackRequest.getCaseDetails(), AUTH_TOKEN);
    }

    @Test
    void givenContestedCase_whenIntervenerOneActionIsRemoved_thenSendCorrespondance() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();

        IntervenerChangeDetails intervenerOneChangeDetails = new IntervenerChangeDetails();
        intervenerOneChangeDetails.setIntervenerType(IntervenerType.INTERVENER_ONE);
        intervenerOneChangeDetails.setIntervenerAction(IntervenerAction.REMOVED);

        finremCaseData.setCurrentIntervenerChangeDetails(intervenerOneChangeDetails);
        finremCaseData.setIntervenerOptionList(DynamicRadioList.builder().build());

        DynamicRadioListElement operation = DynamicRadioListElement.builder().code(DEL_INTERVENER_ONE_CODE).build();
        finremCaseData.getIntervenerOptionList().setValue(operation);

        when(service.setIntervenerOneRemovedChangeDetails()).thenReturn(intervenerOneChangeDetails);

        handler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(service).setIntervenerOneRemovedChangeDetails();
    }

    @Test
    void givenContestedCase_whenIntervenerTwoActionIsRemoved_thenSendCorrespondance() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();

        IntervenerChangeDetails intervenerTwoChangeDetails = new IntervenerChangeDetails();
        intervenerTwoChangeDetails.setIntervenerType(IntervenerType.INTERVENER_TWO);
        intervenerTwoChangeDetails.setIntervenerAction(IntervenerAction.REMOVED);

        finremCaseData.setCurrentIntervenerChangeDetails(intervenerTwoChangeDetails);
        finremCaseData.setIntervenerOptionList(DynamicRadioList.builder().build());

        DynamicRadioListElement operation = DynamicRadioListElement.builder().code(DEL_INTERVENER_TWO_CODE).build();
        finremCaseData.getIntervenerOptionList().setValue(operation);

        when(service.setIntervenerTwoRemovedChangeDetails()).thenReturn(intervenerTwoChangeDetails);

        handler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(service).setIntervenerTwoRemovedChangeDetails();
    }

    @Test
    void givenContestedCase_whenIntervenerThreeActionIsRemoved_thenSendCorrespondance() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();

        IntervenerChangeDetails intervenerThreeChangeDetails = new IntervenerChangeDetails();
        intervenerThreeChangeDetails.setIntervenerType(IntervenerType.INTERVENER_THREE);
        intervenerThreeChangeDetails.setIntervenerAction(IntervenerAction.REMOVED);

        finremCaseData.setCurrentIntervenerChangeDetails(intervenerThreeChangeDetails);
        finremCaseData.setIntervenerOptionList(DynamicRadioList.builder().build());

        DynamicRadioListElement operation = DynamicRadioListElement.builder().code(DEL_INTERVENER_THREE_CODE).build();
        finremCaseData.getIntervenerOptionList().setValue(operation);

        when(service.setIntervenerThreeRemovedChangeDetails()).thenReturn(intervenerThreeChangeDetails);

        handler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(service).setIntervenerThreeRemovedChangeDetails();
    }

    @Test
    void givenContestedCase_whenIntervenerFourActionIsRemoved_thenSendCorrespondance() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();

        IntervenerChangeDetails intervenerFourChangeDetails = new IntervenerChangeDetails();
        intervenerFourChangeDetails.setIntervenerType(IntervenerType.INTERVENER_FOUR);
        intervenerFourChangeDetails.setIntervenerAction(IntervenerAction.REMOVED);

        finremCaseData.setCurrentIntervenerChangeDetails(intervenerFourChangeDetails);
        finremCaseData.setIntervenerOptionList(DynamicRadioList.builder().build());

        DynamicRadioListElement operation = DynamicRadioListElement.builder().code(DEL_INTERVENER_FOUR_CODE).build();
        finremCaseData.getIntervenerOptionList().setValue(operation);

        when(service.setIntervenerFourRemovedChangeDetails()).thenReturn(intervenerFourChangeDetails);

        handler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(service).setIntervenerFourRemovedChangeDetails();
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