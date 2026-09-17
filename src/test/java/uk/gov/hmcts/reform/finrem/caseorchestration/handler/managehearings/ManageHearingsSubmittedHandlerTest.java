package uk.gov.hmcts.reform.finrem.caseorchestration.handler.managehearings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogs;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryErrorHandler;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class ManageHearingsSubmittedHandlerTest {

    private static final String NOTIFICATION_EVENT_ID = "event-123";

    @TestLogs
    private final TestLogger logs = new TestLogger(ManageHearingsSubmittedHandler.class);

    @InjectMocks
    private ManageHearingsSubmittedHandler manageHearingsSubmittedHandler;

    @Mock
    private ManageHearingsCorresponder manageHearingsCorresponder;

    @Mock
    private CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService;

    private final String expectedConfirmationHeader = "Manage Hearings completed with error";

    @BeforeEach
    void setUp() {
        lenient().when(correspondenceEventAuditOrchestrationService.publishEvent(any(), anyString(), any()))
            .thenReturn(true);
    }

    @Test
    void testCanHandle() {
        assertCanHandle(manageHearingsSubmittedHandler, CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.MANAGE_HEARINGS);
    }

    @Test
    void givenExceptionThrown_whenSendingHearingCorrespondenceFailed_thenPopulateErrorToConfirmationBody() {
        // Arrange
        FinremCallbackRequest callbackRequest = buildCallbackRequest(ManageHearingsAction.ADD_HEARING);

        SendCorrespondenceEvent event = mock(SendCorrespondenceEvent.class);
        when(event.describeNotificationParties()).thenReturn("WHATEVER");
        when(manageHearingsCorresponder.buildCorrespondenceEventIfNeeded(
            ManageHearingsAction.ADD_HEARING,
            callbackRequest,
            AUTH_TOKEN
        )).thenReturn(event);

        when(correspondenceEventAuditOrchestrationService.publishEvent(any(), anyString(), any()))
            .thenAnswer(invocation -> {
                RetryErrorHandler handler = invocation.getArgument(2);
                handler.handle(new RuntimeException("failed"), "Send hearing correspondence", CASE_ID);
                return false;
            });

        // Act
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = manageHearingsSubmittedHandler
            .handle(callbackRequest, AUTH_TOKEN);

        // then
        assertAll(
            () -> verify(manageHearingsCorresponder).buildCorrespondenceEventIfNeeded(
                ManageHearingsAction.ADD_HEARING,
                callbackRequest,
                AUTH_TOKEN
            ),
            () -> assertThat(response.getConfirmationHeader()).contains(expectedConfirmationHeader),
            () -> assertThat(response.getConfirmationBody())
                .contains("Notification to WHATEVER has failed. Please send notification to WHATEVER manually."),
            () -> verify(correspondenceEventAuditOrchestrationService, never())
                .reconcileAndPersistAudits(any(), any(), anyString())
        );
    }

    @Test
    void givenExceptionThrown_whenSendingAdjournedOrVacateHearingCorrespondenceFailed_thenPopulateErrorToConfirmationBody() {
        // Arrange
        FinremCallbackRequest callbackRequest = buildCallbackRequest(ManageHearingsAction.ADJOURN_OR_VACATE_HEARING);

        SendCorrespondenceEvent event = mock(SendCorrespondenceEvent.class);
        when(event.describeNotificationParties()).thenReturn("WHATEVER");
        when(manageHearingsCorresponder.buildCorrespondenceEventIfNeeded(
            ManageHearingsAction.ADJOURN_OR_VACATE_HEARING,
            callbackRequest,
            AUTH_TOKEN
        )).thenReturn(event);

        when(correspondenceEventAuditOrchestrationService.publishEvent(any(), anyString(), any()))
            .thenAnswer(invocation -> {
                RetryErrorHandler handler = invocation.getArgument(2);
                handler.handle(new RuntimeException("failed"), "Send adjourned or vacate hearing correspondence", CASE_ID);
                return false;
            });

        // Act
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = manageHearingsSubmittedHandler
            .handle(callbackRequest, AUTH_TOKEN);

        // then
        assertAll(
            () -> verify(manageHearingsCorresponder).buildCorrespondenceEventIfNeeded(
                ManageHearingsAction.ADJOURN_OR_VACATE_HEARING,
                callbackRequest,
                AUTH_TOKEN
            ),
            () -> assertThat(response.getConfirmationHeader()).contains(expectedConfirmationHeader),
            () -> assertThat(response.getConfirmationBody())
                .contains("Notification to WHATEVER has failed. Please send notification to WHATEVER manually.")
        );
    }

    @Test
    void givenHearingCorrespondenceNeeded_whenHandleAddHearingAction_thenPublishSendCorrespondenceEvent() {
        // Arrange
        FinremCallbackRequest callbackRequest = buildCallbackRequest(ManageHearingsAction.ADD_HEARING);

        SendCorrespondenceEvent event = mock(SendCorrespondenceEvent.class);
        when(manageHearingsCorresponder.buildCorrespondenceEventIfNeeded(
            ManageHearingsAction.ADD_HEARING,
            callbackRequest,
            AUTH_TOKEN
        )).thenReturn(event);
        // Act
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            manageHearingsSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        // Assert
        assertThat(response.getErrors()).isNullOrEmpty();
        assertThat(logs.getInfos()).contains(
            format("Sending hearing correspondence for Hearing Added action. Case reference: %s", CASE_ID)
        );
        verify(manageHearingsCorresponder).buildCorrespondenceEventIfNeeded(
            ManageHearingsAction.ADD_HEARING,
            callbackRequest,
            AUTH_TOKEN
        );
        verify(correspondenceEventAuditOrchestrationService)
            .publishEvent(eq(event), eq("Send hearing correspondence"), any(RetryErrorHandler.class));
        assertAll(
            () -> verify(event).setNotificationTrackerId(NOTIFICATION_EVENT_ID),
            () -> verify(correspondenceEventAuditOrchestrationService)
                .reconcileAndPersistAudits(eq(callbackRequest.getCaseDetails()), eq(event), eq("markPendingNotificationsAsSent"))
        );
    }

    @Test
    void givenHearingCorrespondenceNeeded_whenHandleAdjournOrVacateHearingAction_thenPublishSendCorrespondenceEvent() {
        // Arrange
        FinremCallbackRequest callbackRequest = buildCallbackRequest(ManageHearingsAction.ADJOURN_OR_VACATE_HEARING);

        SendCorrespondenceEvent event = mock(SendCorrespondenceEvent.class);
        when(manageHearingsCorresponder.buildCorrespondenceEventIfNeeded(
            ManageHearingsAction.ADJOURN_OR_VACATE_HEARING,
            callbackRequest,
            AUTH_TOKEN
        )).thenReturn(event);

        // Act
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            manageHearingsSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        // Assert
        assertThat(response.getErrors()).isNullOrEmpty();
        assertThat(logs.getInfos()).contains(
            format("Sending hearing correspondence for Hearing Adjourned Or Vacated action. Case reference: %s", CASE_ID)
        );
        verify(manageHearingsCorresponder).buildCorrespondenceEventIfNeeded(
            ManageHearingsAction.ADJOURN_OR_VACATE_HEARING,
            callbackRequest,
            AUTH_TOKEN
        );

        verify(correspondenceEventAuditOrchestrationService)
            .publishEvent(eq(event), eq("Send adjourned or vacate hearing correspondence"), any(RetryErrorHandler.class));
        assertAll(
            () -> verify(correspondenceEventAuditOrchestrationService)
                .reconcileAndPersistAudits(eq(callbackRequest.getCaseDetails()), eq(event), eq("markPendingNotificationsAsSent"))
        );
    }

    @Test
    void givenPendingNotificationAuditUpdates_whenHandleSuccessful_thenReconcileAndPersistAudits() {
        // Arrange
        FinremCallbackRequest callbackRequest = buildCallbackRequest(ManageHearingsAction.ADD_HEARING);

        SendCorrespondenceEvent event = mock(SendCorrespondenceEvent.class);
        when(manageHearingsCorresponder.buildCorrespondenceEventIfNeeded(
            ManageHearingsAction.ADD_HEARING,
            callbackRequest,
            AUTH_TOKEN
        )).thenReturn(event);

        // Act
        manageHearingsSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        // Assert
        verify(correspondenceEventAuditOrchestrationService)
            .reconcileAndPersistAudits(eq(callbackRequest.getCaseDetails()), eq(event), eq("markPendingNotificationsAsSent"));
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
