package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Schedule1OrMatrimonialAndCpList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@ExtendWith(MockitoExtension.class)
class AmendCaseContestedAboutToSubmitHandlerTest {

    public static final String AUTH_TOKEN = "tokien:)";

    @InjectMocks
    private AmendCaseContestedAboutToSubmitHandler handler;

    @Test
    void givenContestedCase_whenEventIsAmendCase_thenHandlerCanHandle() {
        assertTrue(handler.canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.AMEND_CASE));
    }

    @Test
    void givenContestedCase_whenEventIsClose_thenHandlerCanNotHandle() {
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.CLOSE));
    }

    @Test
    void givenContestedCase_whenCallBackIsStart_thenHandlerCanNotHandle() {
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.AMEND_CASE));
    }

    @Test
    void givenContestedCase_whenCaseTypeIsConsented_thenHandlerCanNotHandle() {
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.AMEND_CASE));
    }

    @Test
    void givenContestedCase_whenNoneMatches_thenHandlerCanNotHandle() {
        assertFalse(handler.canHandle(CallbackType.SUBMITTED, CaseType.CONSENTED, EventType.CLOSE));
    }

    @Test
    void givenContestedCase_whenTypeOfApplicationFieldMissing_thenDefaultsToMatrimoninalType() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> callbackResponse
            = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(callbackResponse.getData().getScheduleOneWrapper().getTypeOfApplication().getValue(),
            Schedule1OrMatrimonialAndCpList.MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS.getValue());
    }

    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.AMEND_CASE)
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).build())
            .build();
    }
}