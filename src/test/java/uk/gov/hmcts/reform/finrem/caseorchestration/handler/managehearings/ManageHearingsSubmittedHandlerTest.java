package uk.gov.hmcts.reform.finrem.caseorchestration.handler.managehearings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.NotificationAuditWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CorrespondenceEventAuditOrchestrationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.managehearing.ManageHearingsCorresponder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class ManageHearingsSubmittedHandlerTest {

    private static final String NOTIFICATION_EVENT_ID = "event-123";

    @InjectMocks
    private ManageHearingsSubmittedHandler manageHearingsSubmittedHandler;

    @Mock
    private ManageHearingsCorresponder manageHearingsCorresponder;

    @Mock
    private CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService;

    private final String expectedConfirmationHeader = "Manage Hearings completed with error";

    @Test
    void testCanHandle() {
        assertCanHandle(manageHearingsSubmittedHandler, CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.MANAGE_HEARINGS);
    }

    @ParameterizedTest
    @CsvSource({
        "Send hearing correspondence,ADD_HEARING",
        "Send adjourned or vacate hearing correspondence,ADJOURN_OR_VACATE_HEARING"
    })
    void givenSendHearingCorrespondenceEventPublishedFailed_whenHandled_thenPopulateErrorToConfirmationBody(
        String actionName, ManageHearingsAction actionSelection
    ) {
        // Arrange
        FinremCallbackRequest callbackRequest = buildCallbackRequest(actionSelection);

        SendCorrespondenceEvent event = mock(SendCorrespondenceEvent.class);
        when(event.describeNotificationParties()).thenReturn("WHATEVER");
        when(manageHearingsCorresponder.buildCorrespondenceEventIfNeeded(
            actionSelection, callbackRequest, AUTH_TOKEN)).thenReturn(List.of(event));

        // Fail
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(2);
            runnable.run();
            return null;
        }).when(correspondenceEventAuditOrchestrationService).publishEvent(
            eq(event),
            eq(actionName),
            any(Runnable.class)
        );

        // Act
        var response = manageHearingsSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        // then
        assertAll(
            () -> verify(manageHearingsCorresponder).buildCorrespondenceEventIfNeeded(
                actionSelection,
                callbackRequest,
                AUTH_TOKEN
            ),
            () -> assertThat(response.getConfirmationHeader()).contains(expectedConfirmationHeader),
            () -> assertThat(response.getConfirmationBody())
                .contains("Notification to WHATEVER has failed. Please send notification to WHATEVER manually."),
            () -> verify(correspondenceEventAuditOrchestrationService, never()).reconcileAndPersistAudits(any(FinremCaseDetails.class),
                anyString(), eq(event))
        );
    }

    @ParameterizedTest
    @CsvSource({
        "Send hearing correspondence,ADD_HEARING",
        "Send adjourned or vacate hearing correspondence,ADJOURN_OR_VACATE_HEARING"
    })
    void givenSendHearingCorrespondenceEventNeeded_whenHandled_thenPublishEvent(
        String actionName, ManageHearingsAction actionSelection
    ) {
        // Arrange
        FinremCallbackRequest callbackRequest = buildCallbackRequest(actionSelection);

        SendCorrespondenceEvent event = mock(SendCorrespondenceEvent.class);
        when(manageHearingsCorresponder.buildCorrespondenceEventIfNeeded(actionSelection, callbackRequest,
            AUTH_TOKEN)).thenReturn(List.of(event));
        // Act
        var response = manageHearingsSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        // Assert
        assertAll(
            () -> assertThat(response.getErrors()).isNullOrEmpty(),
            () -> verify(manageHearingsCorresponder).buildCorrespondenceEventIfNeeded(actionSelection, callbackRequest,
                    AUTH_TOKEN
                ),
            () -> assertAll(
                () -> verify(event).setNotificationTrackerId(NOTIFICATION_EVENT_ID),
                () -> verify(correspondenceEventAuditOrchestrationService).publishEvent(eq(event), eq(actionName), any(Runnable.class))
            )
        );
    }

    @ParameterizedTest
    @CsvSource({
        "Send hearing correspondence,ADD_HEARING",
        "Send adjourned or vacate hearing correspondence,ADJOURN_OR_VACATE_HEARING"
    })
    void givenMultipleCorrespondenceEvents_whenHandled_thenEachEventIsPopulatedAndPublished(
        String actionName, ManageHearingsAction actionSelection
    ) {
        // Arrange
        FinremCallbackRequest callbackRequest = buildCallbackRequest(actionSelection);
        String expectedEventId = callbackRequest.getEventType().getCcdType();

        List<SendCorrespondenceEvent> events = List.of(
            mock(SendCorrespondenceEvent.class),
            mock(SendCorrespondenceEvent.class),
            mock(SendCorrespondenceEvent.class)
        );

        when(manageHearingsCorresponder.buildCorrespondenceEventIfNeeded(
            actionSelection, callbackRequest, AUTH_TOKEN)).thenReturn(events);

        // Act
        var response = manageHearingsSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        // Assert
        assertAll(
            () -> assertThat(response.getErrors()).isNullOrEmpty(),
            () -> events.forEach(event -> assertAll(
                () -> verify(event).setEventId(expectedEventId),
                () -> verify(event).setNotificationTrackerId(NOTIFICATION_EVENT_ID),
                () -> verify(correspondenceEventAuditOrchestrationService)
                    .publishEvent(eq(event), eq(actionName), any(Runnable.class))
            )),
            () -> verify(correspondenceEventAuditOrchestrationService, times(events.size()))
                .publishEvent(any(SendCorrespondenceEvent.class), eq(actionName), any(Runnable.class))
        );
    }

    @Test
    void givenPendingNotificationAuditUpdates_whenHandleSuccessful_thenMarkPendingNotificationsAsSent() {
        // Arrange
        FinremCallbackRequest callbackRequest = buildCallbackRequest(ManageHearingsAction.ADD_HEARING);

        SendCorrespondenceEvent event = mock(SendCorrespondenceEvent.class);
        when(manageHearingsCorresponder.buildCorrespondenceEventIfNeeded(
            ManageHearingsAction.ADD_HEARING,
            callbackRequest,
            AUTH_TOKEN
        )).thenReturn(List.of(event));

        // Act
        manageHearingsSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(correspondenceEventAuditOrchestrationService).reconcileAndPersistAudits(callbackRequest.getCaseDetails(),
            "markPendingNotificationsAsSent", event);
    }

    private FinremCallbackRequest buildCallbackRequest(ManageHearingsAction action) {
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .manageHearingsWrapper(ManageHearingsWrapper
                .builder()
                .manageHearingsActionSelection(action)
                .build())
            .notificationAuditWrapper(
                NotificationAuditWrapper.builder()
                    .notificationEventId(NOTIFICATION_EVENT_ID)
                    .build()
            )

            .ccdCaseId(CASE_ID)
            .ccdCaseType(CaseType.CONTESTED)
            .build();

        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .data(finremCaseData)
            .id(CASE_ID_IN_LONG)
            .build();

        return FinremCallbackRequest.builder()
            .caseDetails(caseDetails)
            .eventType(EventType.MANAGE_HEARINGS)
            .build();
    }
}
