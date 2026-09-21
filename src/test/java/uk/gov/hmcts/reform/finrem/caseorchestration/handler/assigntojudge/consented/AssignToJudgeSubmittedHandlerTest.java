package uk.gov.hmcts.reform.finrem.caseorchestration.handler.assigntojudge.consented;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge.consented.AssignToJudgeCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryErrorHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.ThrowingRunnable;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.getThrowingRunnableCaptor;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.mockRunWithRetryWithHandlerInvokesFirstErrorHandler;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.runSafely;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.REFER_TO_JUDGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.REFER_TO_JUDGE_FROM_AWAITING_RESPONSE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.REFER_TO_JUDGE_FROM_CLOSE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.REFER_TO_JUDGE_FROM_CONSENT_ORDER_APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.REFER_TO_JUDGE_FROM_CONSENT_ORDER_MADE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.REFER_TO_JUDGE_FROM_ORDER_MADE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.REFER_TO_JUDGE_FROM_RESPOND_TO_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class AssignToJudgeSubmittedHandlerTest {
    
    private static final String TRACKER_ID = "tracker-id-123";

    private static final String DESCRIBED_NOTIFICATION_PARTIES = "applicant solicitor";

    @Mock
    private RetryExecutor retryExecutor;

    @InjectMocks
    private AssignToJudgeSubmittedHandler handlerUnderTest;

    @Mock
    private AssignToJudgeCorresponder assignToJudgeCorresponder;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @BeforeEach
    void setup() {
        lenient().doNothing().when(retryExecutor).runWithRetryWithHandler(any(), anyString(), any(), any());
    }

    @Test
    void testCanHandle() {
        assertCanHandle(handlerUnderTest,
            Arguments.of(CallbackType.SUBMITTED, CONSENTED, REFER_TO_JUDGE),
            Arguments.of(CallbackType.SUBMITTED, CONSENTED, REFER_TO_JUDGE_FROM_ORDER_MADE),
            Arguments.of(CallbackType.SUBMITTED, CONSENTED, REFER_TO_JUDGE_FROM_CONSENT_ORDER_APPROVED),
            Arguments.of(CallbackType.SUBMITTED, CONSENTED, REFER_TO_JUDGE_FROM_CONSENT_ORDER_MADE),
            Arguments.of(CallbackType.SUBMITTED, CONSENTED, REFER_TO_JUDGE_FROM_AWAITING_RESPONSE),
            Arguments.of(CallbackType.SUBMITTED, CONSENTED, REFER_TO_JUDGE_FROM_RESPOND_TO_ORDER),
            Arguments.of(CallbackType.SUBMITTED, CONSENTED, REFER_TO_JUDGE_FROM_CLOSE)
        );
    }

    @Test
    void givenCase_whenSendCorrespondenceFailed_thenPopulateErrorToConfirmationBody() {
        // Arrange
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, REFER_TO_JUDGE);

        SendCorrespondenceEvent event = mock(SendCorrespondenceEvent.class);
        when(event.getNotificationTrackerId()).thenReturn(TRACKER_ID);
        when(event.describeNotificationParties()).thenReturn(DESCRIBED_NOTIFICATION_PARTIES);

        List<SendCorrespondenceEvent> events = new ArrayList<>(List.of(event));
        when(assignToJudgeCorresponder.buildSendCorrespondenceEvents(REFER_TO_JUDGE, callbackRequest.getCaseDetails(),
            AUTH_TOKEN)).thenReturn(events);

        mockRunWithRetryWithHandlerInvokesFirstErrorHandler(
            retryExecutor,
            "sending assign to judge correspondence %s (%s)".formatted(TRACKER_ID,
                DESCRIBED_NOTIFICATION_PARTIES)
        );

        // Act
        var response = handlerUnderTest.handle(callbackRequest, AUTH_TOKEN);

        // then
        assertAll(
            () -> assertThat(response.getConfirmationHeader()).contains("Assign to judge event submitted with errors."),
            () -> assertThat(response.getConfirmationBody())
                .contains("There was a problem sending assign to judge correspondence (%s). Please send it manually."
                    .formatted(DESCRIBED_NOTIFICATION_PARTIES))
        );
    }

    @Test
    void givenCase_whenHandled_shouldPublishSendCorrespondenceEvent() {
        // Arrange
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, REFER_TO_JUDGE);

        SendCorrespondenceEvent event = mock(SendCorrespondenceEvent.class);
        when(event.getNotificationTrackerId()).thenReturn(TRACKER_ID);
        when(event.describeNotificationParties()).thenReturn(DESCRIBED_NOTIFICATION_PARTIES);

        List<SendCorrespondenceEvent> events = new ArrayList<>(List.of(event));
        when(assignToJudgeCorresponder.buildSendCorrespondenceEvents(REFER_TO_JUDGE, callbackRequest.getCaseDetails(),
            AUTH_TOKEN)).thenReturn(events);

        // Act
        handlerUnderTest.handle(callbackRequest, AUTH_TOKEN);

        ArgumentCaptor<ThrowingRunnable> runnableCaptor = getThrowingRunnableCaptor();
        verify(retryExecutor)
            .runWithRetryWithHandler(
                runnableCaptor.capture(),
                eq("sending assign to judge correspondence %s (%s)".formatted(TRACKER_ID,
                    DESCRIBED_NOTIFICATION_PARTIES)),
                eq(CASE_ID),
                any(RetryErrorHandler.class)
            );
        runSafely(runnableCaptor.getValue());
        verify(applicationEventPublisher).publishEvent(event);
    }

    @Test
    void givenCase_whenHandled_shouldPublishMultipleSendCorrespondenceEvents() {
        // Arrange
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, REFER_TO_JUDGE);

        SendCorrespondenceEvent firstEvent = mock(SendCorrespondenceEvent.class);
        when(firstEvent.getNotificationTrackerId()).thenReturn(TRACKER_ID);
        when(firstEvent.describeNotificationParties()).thenReturn(DESCRIBED_NOTIFICATION_PARTIES);

        SendCorrespondenceEvent secondEvent = mock(SendCorrespondenceEvent.class);
        when(secondEvent.getNotificationTrackerId()).thenReturn(TRACKER_ID);
        when(secondEvent.describeNotificationParties()).thenReturn(DESCRIBED_NOTIFICATION_PARTIES + "2");

        List<SendCorrespondenceEvent> events = new ArrayList<>(List.of(firstEvent, secondEvent));
        when(assignToJudgeCorresponder.buildSendCorrespondenceEvents(REFER_TO_JUDGE, callbackRequest.getCaseDetails(),
            AUTH_TOKEN)).thenReturn(events);

        // Act
        handlerUnderTest.handle(callbackRequest, AUTH_TOKEN);

        ArgumentCaptor<ThrowingRunnable> runnableCaptor = getThrowingRunnableCaptor();
        verify(retryExecutor)
            .runWithRetryWithHandler(
                runnableCaptor.capture(),
                eq("sending assign to judge correspondence %s (%s)".formatted(TRACKER_ID,
                    DESCRIBED_NOTIFICATION_PARTIES)),
                eq(CASE_ID),
                any(RetryErrorHandler.class)
            );
        verify(retryExecutor)
            .runWithRetryWithHandler(
                runnableCaptor.capture(),
                eq("sending assign to judge correspondence %s (%s)".formatted(TRACKER_ID,
                    DESCRIBED_NOTIFICATION_PARTIES + "2")),
                eq(CASE_ID),
                any(RetryErrorHandler.class)
            );
        runnableCaptor.getAllValues().forEach(TestSetUpUtils::runSafely);
        verify(applicationEventPublisher).publishEvent(firstEvent);
        verify(applicationEventPublisher).publishEvent(secondEvent);
    }
}
