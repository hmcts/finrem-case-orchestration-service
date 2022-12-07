package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConsentApplicationApprovedInContestedAboutToStartHandlerTest {

    @InjectMocks
    private ConsentApplicationApprovedInContestedAboutToStartHandler handler;
    @Mock
    private IdamService service;
    public static final String AUTH_TOKEN = "tokien:)";

    @Test
    public void canHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.CONSENT_APPLICATION_APPROVED_IN_CONTESTED),
            is(true));
    }

    @Test
    public void canNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.CONSENT_APPLICATION_APPROVED_IN_CONTESTED),
            is(false));
    }

    @Test
    public void canNotHandleWrongEventType() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.CLOSE),
            is(false));
    }

    @Test
    public void canNotHandleWrongCallbackType() {
        assertThat(handler
                .canHandle(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.CONSENT_APPLICATION_APPROVED_IN_CONTESTED),
            is(false));
    }

    @Test
    public void givenContestedCase_whenEventExecuted_thenSetTheLoggedInUserIfNull() {
        when(service.getIdamFullName(AUTH_TOKEN)).thenReturn("Moj Judge");
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest(null), AUTH_TOKEN);
        assertEquals("Moj Judge", response.getData().getConsentJudgeName());
    }

    @Test
    public void givenContestedCase_whenEventExecuted_thenDoNotSetIfUserIsAlreadyThere() {
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest("HM Moj Judge"), AUTH_TOKEN);
        assertEquals("HM Moj Judge", response.getData().getConsentJudgeName());
    }

    private FinremCallbackRequest callbackRequest(String judgeName) {
        FinremCaseData finremCaseData = new FinremCaseData();
        finremCaseData.setConsentJudgeName(judgeName);
        return FinremCallbackRequest
            .<FinremCaseDetails>builder()
            .eventType(EventType.CONSENT_APPLICATION_APPROVED_IN_CONTESTED)
            .caseDetails(FinremCaseDetails.builder().id(123L)
                .data(finremCaseData).build())
            .build();
    }
}