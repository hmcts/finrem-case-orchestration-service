package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandler;
import uk.gov.hmcts.reform.finrem.ccd.callback.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseType;
import uk.gov.hmcts.reform.finrem.ccd.domain.EventType;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CallbackDispatchServiceTest extends BaseServiceTest {
    private static final String USER_AUTHORISATION = "Bearer token";

    @Mock
    private CallbackHandler handler1;
    @Mock
    private CallbackHandler handler2;

    @Mock
    private FinremCaseDetails caseDetails;

    @Mock
    private CallbackRequest callbackRequest;
    @Mock
    private AboutToStartOrSubmitCallbackResponse response1;
    @Mock
    private AboutToStartOrSubmitCallbackResponse response2;

    private CallbackDispatchService callbackDispatchService;

    @Before
    public void setUp() {
        callbackDispatchService = new CallbackDispatchService(
            Arrays.asList(
                handler1,
                handler2
            )
        );
    }

    @Test
    public void givenMultipleHandlers_WhenDispatchToHandlers_ThenAllErrorsCollected() {

        when(callbackRequest.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseType()).thenReturn(CaseType.CONTESTED);
        when(callbackRequest.getEventType()).thenReturn(EventType.SEND_ORDER);

        when(response1.getErrors()).thenReturn(List.of("error1"));
        when(response2.getErrors()).thenReturn(List.of("error2", "error3"));

        when(handler1.canHandle(any(CallbackType.class), any(CaseType.class), any(EventType.class))).thenReturn(true);
        when(handler1.handle(any(CallbackRequest.class), anyString())).thenReturn(response1);

        when(handler2.canHandle(any(CallbackType.class), any(CaseType.class), any(EventType.class))).thenReturn(true);
        when(handler2.handle(any(CallbackRequest.class), anyString())).thenReturn(response2);

        AboutToStartOrSubmitCallbackResponse callbackResponse =
            callbackDispatchService.dispatchToHandlers(CallbackType.ABOUT_TO_SUBMIT, callbackRequest, USER_AUTHORISATION);

        assertNotNull(callbackResponse);
        List<String> expectedErrors = List.of("error1", "error2", "error3");

        assertThat(callbackResponse.getErrors(), is(expectedErrors));

    }

    @Test
    public void givenOneHandlerCanHandle_WhenDispatchToHandlers_ThenOnlyAbleHandlerAreCalled() {

        when(callbackRequest.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseType()).thenReturn(CaseType.CONTESTED);
        when(callbackRequest.getEventType()).thenReturn(EventType.SEND_ORDER);

        when(handler1.canHandle(any(CallbackType.class), any(CaseType.class), any(EventType.class))).thenReturn(true);
        when(handler1.handle(any(CallbackRequest.class), anyString())).thenReturn(response1);

        when(handler2.canHandle(any(CallbackType.class), any(CaseType.class), any(EventType.class))).thenReturn(false);

        AboutToStartOrSubmitCallbackResponse callbackResponse =
            callbackDispatchService.dispatchToHandlers(CallbackType.ABOUT_TO_SUBMIT, callbackRequest, USER_AUTHORISATION);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getErrors().isEmpty());

        verify(handler1).canHandle(any(CallbackType.class), any(CaseType.class), any(EventType.class));
        verify(handler1, times(1)).handle(any(CallbackRequest.class), anyString());

        verify(handler2).canHandle(any(CallbackType.class), any(CaseType.class), any(EventType.class));
        verify(handler2, times(0)).handle(any(CallbackRequest.class), anyString());
    }

    @Test
    public void givenNoHandler_WhenDispatch_ThenNoErrors() {

        CallbackDispatchService callbackDispatchService =
            new CallbackDispatchService(Collections.emptyList());

        try {

            when(callbackRequest.getCaseDetails()).thenReturn(caseDetails);

            AboutToStartOrSubmitCallbackResponse callbackResponse = callbackDispatchService
                .dispatchToHandlers(CallbackType.ABOUT_TO_SUBMIT, callbackRequest, USER_AUTHORISATION);

            assertNotNull(callbackResponse);
            assertTrue(callbackResponse.getErrors().isEmpty());
        } catch (Exception e) {
            fail("Should not have thrown any exception");
        }
    }

    @Test
    public void givenNullArguments_WhenDispatch_ThenThrowError() {

        assertThatThrownBy(() -> callbackDispatchService
            .dispatchToHandlers(CallbackType.ABOUT_TO_SUBMIT, null, USER_AUTHORISATION))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}