package uk.gov.hmcts.reform.finrem.caseorchestration.handler.hidecase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class UnHideCaseContestedAboutToSubmitHandlerTest {

    UnhideCaseContestedAboutToSubmitHandler handler;

    @Before
    public void setUpTest() {
        FinremCaseDetailsMapper mapperMock = mock(FinremCaseDetailsMapper.class);
        handler = new UnhideCaseContestedAboutToSubmitHandler(mapperMock);
    }

    @Test
    public void testCanHandle() {

        boolean result = handler.canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.UNHIDE_CASE);
        assertTrue(result);
    }

    @Test
    public void testCannotHandle() {

        boolean result = handler.canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.SEND_CONSENT_IN_CONTESTED_ORDER);

        assertFalse(result);
    }

    @Test
    public void testHandle() {

        FinremCaseDetails caseDetails = new FinremCaseDetails();
        caseDetails.setState(State.APPLICATION_SUBMITTED);
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest();

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, "userAuth");

        assertNotNull(response);
        assertEquals(State.APPLICATION_ISSUED.getStateId(), response.getState());
    }

    private FinremCallbackRequest buildFinremCallbackRequest() {
        FinremCaseData caseData = FinremCaseData.builder().previousState(State.APPLICATION_ISSUED.getStateId())
            .build();
        FinremCaseDetails caseDetails =
            FinremCaseDetails.builder().id(123L).caseType(CaseType.CONTESTED).state(State.CASE_HIDDEN).data(caseData).build();
        return FinremCallbackRequest.builder().eventType(EventType.UNHIDE_CASE).caseDetails(caseDetails).build();
    }

}



