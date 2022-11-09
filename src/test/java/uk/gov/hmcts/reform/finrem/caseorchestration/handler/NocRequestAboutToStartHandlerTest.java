package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamAuthService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.BarristerRepresentationChecker;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class NocRequestAboutToStartHandlerTest {

    private static final String USER_ID = "userId";
    private static final String SOL_EMAIL = "solEmail";
    private static final String BARR_EMAIL = "barrEmail";
    private static final String ERROR = "User has represented litigant as Barrister for case 123456";

    @Mock
    private IdamService idamService;
    @Mock
    private IdamAuthService idamAuthService;
    @Mock
    private BarristerRepresentationChecker barristerRepresentationChecker;

    @InjectMocks
    private NocRequestAboutToStartHandler nocRequestAboutToStartHandler;

    private CallbackRequest callbackRequest;
    private Map<String, Object> caseData;

    @Before
    public void setUp() {
        caseData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().id(123456L).data(caseData).build();
        callbackRequest = CallbackRequest.builder().eventId(EventType.NOC_REQUEST.getCcdType()).caseDetails(caseDetails).build();
    }

    @Test
    public void givenValidCallback_whenCanHandle_thenReturnTrue() {
        assertThat(nocRequestAboutToStartHandler.canHandle(CallbackType.ABOUT_TO_START,
                CaseType.CONTESTED,
                EventType.NOC_REQUEST),
            is(true));
    }

    @Test
    public void givenCallbackWrongCallbackType_whenCanHandle_thenReturnFalse() {
        assertThat(nocRequestAboutToStartHandler.canHandle(CallbackType.ABOUT_TO_SUBMIT,
                CaseType.CONTESTED,
                EventType.NOC_REQUEST),
            is(false));
    }

    @Test
    public void givenCallbackWrongCaseType_whenCanHandle_thenReturnFalse() {
        assertThat(nocRequestAboutToStartHandler.canHandle(CallbackType.ABOUT_TO_START,
                CaseType.CONSENTED,
                EventType.NOC_REQUEST),
            is(false));
    }

    @Test
    public void givenCallbackWrongEventType_whenCanHandle_thenReturnFalse() {
        assertThat(nocRequestAboutToStartHandler.canHandle(CallbackType.ABOUT_TO_START,
                CaseType.CONTESTED,
                EventType.MANAGE_CASE_DOCUMENTS),
            is(false));
    }

    @Test
    public void givenUserWasNotBarrister_whenHandle_thenReturnNoErrors() {
        UserDetails userDetails = UserDetails.builder().email(SOL_EMAIL).build();
        when(idamService.getIdamUserId(AUTH_TOKEN)).thenReturn(USER_ID);
        when(idamAuthService.getUserByUserId(AUTH_TOKEN, USER_ID)).thenReturn(userDetails);
        when(barristerRepresentationChecker.hasUserBeenBarristerOnCase(caseData, userDetails)).thenReturn(false);

        AboutToStartOrSubmitCallbackResponse response = nocRequestAboutToStartHandler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getErrors(), hasSize(0));
    }

    @Test
    public void givenUserWasBarrister_whenHandle_thenReturnError() {
        UserDetails userDetails = UserDetails.builder().email(BARR_EMAIL).build();
        when(idamService.getIdamUserId(AUTH_TOKEN)).thenReturn(USER_ID);
        when(idamAuthService.getUserByUserId(AUTH_TOKEN, USER_ID)).thenReturn(userDetails);
        when(barristerRepresentationChecker.hasUserBeenBarristerOnCase(caseData, userDetails)).thenReturn(true);

        AboutToStartOrSubmitCallbackResponse response = nocRequestAboutToStartHandler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getErrors(), hasSize(1));
        assertThat(response.getErrors().get(0), is(ERROR));
    }
}