package uk.gov.hmcts.reform.finrem.caseorchestration.handler;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnStartDefaultValueService;

import java.time.LocalDate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;

@ExtendWith(MockitoExtension.class)
class IssueApplicationConsentedAboutToStartHandlerTest {

    public static final String AUTH_TOKEN = "tokien:)";
    @InjectMocks
    private IssueApplicationConsentedAboutToStartHandler handler;

    @Mock
    private OnStartDefaultValueService onStartDefaultValueService;

    @Test
    void givenConsentedCase_whenEventIsAmendAndCallbackIsSubmitted_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONSENTED, EventType.ISSUE_APPLICATION),
            is(false));
    }

    @Test
    void givenConsentedCase_whenEventIsAmend_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.ISSUE_APPLICATION),
            is(true));
    }

    @Test
    void givenConsentedCase_whenUseIssueApplication_thenDefaultIssueDateSetToCurrentDate() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        handler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(onStartDefaultValueService).defaultIssueDate(finremCallbackRequest);
    }

    @Test
    void givenConsentedCase_whenUseIssueApplicationAndIssueDateEnteredManually_thenHandle() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        finremCallbackRequest.getCaseDetails().getData().setIssueDate(LocalDate.of(2000,10,10));
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);
        assertEquals(LocalDate.of(2000,10,10), response.getData().getIssueDate());
    }

    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.ISSUE_APPLICATION)
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONSENTED)
                .data(new FinremCaseData()).build())
            .build();
    }
}