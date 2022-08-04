package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.ccd.callback.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseType;
import uk.gov.hmcts.reform.finrem.ccd.domain.EventType;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;

@RunWith(MockitoJUnitRunner.class)
public class DefaultValueAboutToStartHandlerTest {

    public static final String AUTH_TOKEN = "tokien:)";
    private final DefaultValueAboutToStartHandler handler =  new DefaultValueAboutToStartHandler();

    @Test
    public void givenConsentedCase_whenEventIsAmend_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.AMEND_CASE),
            is(true));
    }

    @Test
    public void givenConsentedCase_whenEventIsSolCreate_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.SOLICITOR_CREATE),
            is(true));
    }

    @Test
    public void givenContestedCase_whenEventIsAmend_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.SOLICITOR_CREATE),
            is(true));
    }


    @Test
    public void handle() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        AboutToStartOrSubmitCallbackResponse response = handler.handle(callbackRequest, AUTH_TOKEN);
        assertEquals(YesOrNo.NO, response.getData().getCivilPartnership());
    }

    private CallbackRequest buildCallbackRequest() {
        FinremCaseData caseData = new FinremCaseData();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123L).caseData(caseData).build();
        return CallbackRequest.builder().eventType(EventType.ISSUE_APPLICATION).caseDetails(caseDetails).build();
    }

}