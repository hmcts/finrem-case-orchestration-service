package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnStartDefaultValueService;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CIVIL_PARTNERSHIP;

@ExtendWith(MockitoExtension.class)
public class AmendCaseContestedAboutToStartHandlerTest {

    public static final String AUTH_TOKEN = "tokien:)";
    private AmendCaseContestedAboutToStartHandler handler;

    @Mock
    private IdamService idamService;

    @BeforeEach
    public void setup() {
        handler = new AmendCaseContestedAboutToStartHandler(new OnStartDefaultValueService(idamService));
    }

    @Test
    public void givenConsentedCase_whenEventIsAmend_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.AMEND_CASE)).isTrue();
    }

    @Test
    public void givenConsentedCase_whenEventIsIssueApplication_thenHandlerCanNotCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.ISSUE_APPLICATION)).isFalse();
    }

    @Test
    public void givenConsentedCase_whenEventIsAmendAndCallbackIsSubmitted_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.AMEND_CASE)).isFalse();
    }

    @Test
    public void givenConsentedCase_whenEventIsAmendAndCaseTypeIsConsented_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONSENTED, EventType.AMEND_CASE)).isFalse();
    }

    @Test
    public void givenConsentedCase_whenEventIsSolCreate_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.SOLICITOR_CREATE)).isFalse();
    }

    @Test
    public void handle() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> response = handler.handle(callbackRequest, AUTH_TOKEN);
        assertThat(response.getData().get(CIVIL_PARTNERSHIP)).isEqualTo(NO_VALUE);
    }

    private CallbackRequest buildCallbackRequest() {
        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().id(123L).caseTypeId(CaseType.CONTESTED.getCcdType()).data(caseData).build();
        return CallbackRequest.builder()
            .eventId(EventType.AMEND_CASE.getCcdType()).caseDetails(caseDetails).build();
    }
}
