package uk.gov.hmcts.reform.finrem.caseorchestration.handler.intervener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.ADD_INTERVENER_ONE_CODE;

@RunWith(MockitoJUnitRunner.class)
public class IntervenersSubmittedHandlerTest {

    public static final String AUTH_TOKEN = "tokien:)";

    @InjectMocks
    private IntervenersSubmittedHandler handler;

    @Mock
    private IntervenerAddedCorresponder intervenerAddedCorresponder;

    @Mock
    private IntervenerService service;

    @Test
    public void givenContestedCase_whenICallbackIsAboutToSubmit_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.MANAGE_INTERVENERS),
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
                .canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.MANAGE_INTERVENERS),
            is(true));
    }

    @Test
    public void givenContestedCase_whenIntervenerActionIsAdded_thenSendCorrespondance() {
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