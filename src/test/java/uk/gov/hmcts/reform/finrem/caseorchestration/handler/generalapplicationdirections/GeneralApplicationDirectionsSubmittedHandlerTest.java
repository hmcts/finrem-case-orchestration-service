package uk.gov.hmcts.reform.finrem.caseorchestration.handler.generalapplicationdirections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationDirectionsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.managehearing.ManageHearingsCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryErrorHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.ThrowingRunnable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.assertCondition;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.getThrowingRunnableCaptor;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.mockRunWithRetryWithHandlerInvokesFirstErrorHandler;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class GeneralApplicationDirectionsSubmittedHandlerTest {

    @InjectMocks
    private GeneralApplicationDirectionsSubmittedHandler generalApplicationDirectionsSubmittedHandler;

    @Mock
    private ManageHearingsCorresponder manageHearingsCorresponder;

    @Mock
    private GeneralApplicationDirectionsService generalApplicationDirectionsService;

    @Mock
    private RetryExecutor retryExecutor;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Test
    void testCanHandle() {
        assertCanHandle(generalApplicationDirectionsSubmittedHandler, CallbackType.SUBMITTED, CaseType.CONTESTED,
            EventType.GENERAL_APPLICATION_DIRECTIONS_MH);
    }

    /**
     * Creates a FinremCallbackRequest with a FinremCaseDetails containing empty FinremCaseData and a fixed case ID.
     *
     * @return a FinremCallbackRequest for use in tests
     */
    private FinremCallbackRequest createCallbackRequest() {
        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .id(12345L)
            .data(FinremCaseData.builder().build())
            .build();
        return FinremCallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
    }

    /*
     * Verifies that when a hearing is required, the handler calls the manageHearingsCorresponder to send hearing correspondence.
     */
    @Test
    void shouldSendHearingCorrespondenceOnSubmittedCallback() {
        // Arrange
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG,
            FinremCaseData.builder().build());

        when(generalApplicationDirectionsService.isHearingRequired(callbackRequest.getCaseDetails()))
            .thenReturn(true);

        SendCorrespondenceEvent event1 = mock(SendCorrespondenceEvent.class);
        when(event1.getNotificationParties()).thenReturn(List.of(
            NotificationParty.APPLICANT
        ));

        List<SendCorrespondenceEvent> events = List.of(event1);
        when(manageHearingsCorresponder.buildHearingCorrespondenceEventsIfNeeded(callbackRequest,
            AUTH_TOKEN)).thenReturn(events);

        // Act
        var response = generalApplicationDirectionsSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        // Assert
        ArgumentCaptor<ThrowingRunnable> sendHearingRunnableCaptor = getThrowingRunnableCaptor();
        assertAll(
            () -> assertThat(response.getConfirmationHeader()).isNullOrEmpty(),
            () -> assertThat(response.getConfirmationBody()).isNullOrEmpty(),
            () -> verify(retryExecutor)
                .runWithRetryWithHandler(
                    sendHearingRunnableCaptor.capture(),
                    eq("Send hearing corresponder to party: APPLICANT on general application direction event"),
                    eq(CASE_ID),
                    any(RetryErrorHandler.class)
                ),
            () -> {
                sendHearingRunnableCaptor.getAllValues().forEach(TestSetUpUtils::runSafely);
                verify(applicationEventPublisher).publishEvent(event1);
            }
        );
    }

    @Test
    void shouldSendMultipleHearingCorrespondencesOnSubmittedCallback() {
        // Arrange
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG,
            FinremCaseData.builder().build());

        when(generalApplicationDirectionsService.isHearingRequired(callbackRequest.getCaseDetails()))
            .thenReturn(true);

        SendCorrespondenceEvent event1 = mock(SendCorrespondenceEvent.class);
        when(event1.getNotificationParties()).thenReturn(List.of(
            NotificationParty.APPLICANT
        ));
        SendCorrespondenceEvent event2 = mock(SendCorrespondenceEvent.class);
        when(event2.getNotificationParties()).thenReturn(List.of(
            NotificationParty.RESPONDENT
        ));

        List<SendCorrespondenceEvent> events = List.of(event1, event2);
        when(manageHearingsCorresponder.buildHearingCorrespondenceEventsIfNeeded(callbackRequest,
            AUTH_TOKEN)).thenReturn(events);

        // Act
        var response = generalApplicationDirectionsSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        // Assert
        ArgumentCaptor<ThrowingRunnable> sendHearingRunnableCaptor = getThrowingRunnableCaptor();
        assertAll(
            () -> assertThat(response.getConfirmationHeader()).isNullOrEmpty(),
            () -> assertThat(response.getConfirmationBody()).isNullOrEmpty(),
            () -> verify(retryExecutor)
                .runWithRetryWithHandler(
                    sendHearingRunnableCaptor.capture(),
                    eq("Send hearing corresponder to party: APPLICANT on general application direction event"),
                    eq(CASE_ID),
                    any(RetryErrorHandler.class)
                ),
            () -> verify(retryExecutor)
                .runWithRetryWithHandler(
                    sendHearingRunnableCaptor.capture(),
                    eq("Send hearing corresponder to party: RESPONDENT on general application direction event"),
                    eq(CASE_ID),
                    any(RetryErrorHandler.class)
                ),
            () -> {
                sendHearingRunnableCaptor.getAllValues().forEach(TestSetUpUtils::runSafely);
                verify(applicationEventPublisher).publishEvent(event1);
                verify(applicationEventPublisher).publishEvent(event2);
                verifyNoMoreInteractions(applicationEventPublisher);
            }
        );
    }

    @Test
    void givenExceptionThrown_whenSendingApplicantHearingCorrespondenceFailed_thenPopulateErrorToConfirmationBody() {
        // Arrange
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG,
            FinremCaseData.builder().build());

        when(generalApplicationDirectionsService.isHearingRequired(callbackRequest.getCaseDetails()))
            .thenReturn(true);

        SendCorrespondenceEvent event1 = mock(SendCorrespondenceEvent.class);
        when(event1.getNotificationParties()).thenReturn(List.of(
            NotificationParty.APPLICANT
        ));
        SendCorrespondenceEvent event2 = mock(SendCorrespondenceEvent.class);
        when(event2.getNotificationParties()).thenReturn(List.of(
            NotificationParty.RESPONDENT
        ));

        List<SendCorrespondenceEvent> events = List.of(event1, event2);
        when(manageHearingsCorresponder.buildHearingCorrespondenceEventsIfNeeded(callbackRequest,
            AUTH_TOKEN)).thenReturn(events);

        mockRunWithRetryWithHandlerInvokesFirstErrorHandler(
            retryExecutor,
            "Send hearing corresponder to party: APPLICANT on general application direction event"
        );
        doNothing().when(retryExecutor).runWithRetryWithHandler(
            any(),
            eq("Send hearing corresponder to party: RESPONDENT on general application direction event"),
            any(),
            any(RetryErrorHandler.class)
        );

        // Act
        var response = generalApplicationDirectionsSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);
        assertAll(
            () -> verify(retryExecutor).runWithRetryWithHandler(
                any(ThrowingRunnable.class),
                eq("Send hearing corresponder to party: APPLICANT on general application direction event"),
                eq(CASE_ID),
                any(RetryErrorHandler.class)
            ),
            () -> verify(retryExecutor).runWithRetryWithHandler(
                any(ThrowingRunnable.class),
                eq("Send hearing corresponder to party: RESPONDENT on general application direction event"),
                eq(CASE_ID),
                any(RetryErrorHandler.class)
            ),
            () -> assertThat(response.getConfirmationHeader()).contains("General Application Direction completed with error"),
            () -> assertCondition(response.getConfirmationBody(),
                "Notification to APPLICANT has failed. Please send notification manually.",
                true),
            () -> assertCondition(response.getConfirmationBody(),
                "Notification to RESPONDENT has failed. Please send notification manually.",
                false)
        );
    }

    /*
     * Verifies that when a hearing is not required, the handler does not call the manageHearingsCorresponder.
     */
    @Test
    void shouldNotSendHearingCorrespondenceWhenHearingNotRequired() {
        // Arrange
        FinremCallbackRequest callbackRequest = createCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();

        when(generalApplicationDirectionsService.isHearingRequired(caseDetails)).thenReturn(false);

        // Act
        var response = generalApplicationDirectionsSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        // Assert
        assertThat(response.getConfirmationHeader()).isNullOrEmpty();
        assertThat(response.getConfirmationBody()).isNullOrEmpty();
        verifyNoMoreInteractions(manageHearingsCorresponder);
    }
}
