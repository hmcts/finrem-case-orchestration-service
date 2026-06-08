package uk.gov.hmcts.reform.finrem.caseorchestration.handler.managehearings;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.notifications.NotificationAudit;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.notifications.NotificationAuditCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.notifications.NotificationToBeSentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.NotificationAuditWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.managehearing.ManageHearingsCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogs;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryErrorHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.ThrowingRunnable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.getThrowingRunnableCaptor;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.mockRunWithRetryWithHandlerInvokesFirstErrorHandler;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.INTERNAL_CHANGE_UPDATE_CASE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class ManageHearingsSubmittedHandlerTest {

    @TestLogs
    private final TestLogger logs = new TestLogger(ManageHearingsSubmittedHandler.class);

    @InjectMocks
    private ManageHearingsSubmittedHandler manageHearingsSubmittedHandler;

    @Mock
    private ManageHearingsCorresponder manageHearingsCorresponder;

    @Mock
    private RetryExecutor retryExecutor;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    private final String expectedConfirmationHeader = "Manage Hearings completed with error";

    @Test
    void testCanHandle() {
        assertCanHandle(manageHearingsSubmittedHandler, CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.MANAGE_HEARINGS);
    }

    @Test
    void givenExceptionThrown_whenSendingHearingCorrespondenceFailed_thenPopulateErrorToConfirmationBody() {
        // Arrange
        FinremCallbackRequest callbackRequest = buildCallbackRequest(ManageHearingsAction.ADD_HEARING);

        SendCorrespondenceEvent event = mock(SendCorrespondenceEvent.class);
        when(event.getCaseId()).thenReturn(CASE_ID);
        when(event.describeNotificationParties()).thenReturn("WHATEVER");
        when(manageHearingsCorresponder.buildHearingCorrespondenceEventIfNeeded(callbackRequest, AUTH_TOKEN)).thenReturn(event);

        mockRunWithRetryWithHandlerInvokesFirstErrorHandler(
            retryExecutor,
            "Send hearing correspondence"
        );

        // Act
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = manageHearingsSubmittedHandler
            .handle(callbackRequest, AUTH_TOKEN);

        // then
        assertAll(
            () -> verify(manageHearingsCorresponder).buildHearingCorrespondenceEventIfNeeded(callbackRequest, AUTH_TOKEN),
            () -> assertThat(response.getConfirmationHeader()).contains(expectedConfirmationHeader),
            () -> assertThat(response.getConfirmationBody())
                .contains("Notification to WHATEVER has failed. Please send notification to WHATEVER manually."),
            () -> verify(coreCaseDataService, never())
                .performPostSubmitCallback(any(), any(), any(), any())
        );
    }

    @Test
    void givenExceptionThrown_whenSendingAdjournedOrVacateHearingCorrespondenceFailed_thenPopulateErrorToConfirmationBody() {
        // Arrange
        FinremCallbackRequest callbackRequest = buildCallbackRequest(ManageHearingsAction.ADJOURN_OR_VACATE_HEARING);

        SendCorrespondenceEvent event = mock(SendCorrespondenceEvent.class);
        when(event.getCaseId()).thenReturn(CASE_ID);
        when(event.describeNotificationParties()).thenReturn("WHATEVER");
        when(manageHearingsCorresponder.buildAdjournedOrVacatedHearingCorrespondenceEventIfNeeded(callbackRequest, AUTH_TOKEN)).thenReturn(event);

        mockRunWithRetryWithHandlerInvokesFirstErrorHandler(
            retryExecutor,
            "Send adjourned or vacate hearing correspondence"
        );

        // Act
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = manageHearingsSubmittedHandler
            .handle(callbackRequest, AUTH_TOKEN);

        // then
        assertAll(
            () -> verify(manageHearingsCorresponder).buildAdjournedOrVacatedHearingCorrespondenceEventIfNeeded(callbackRequest, AUTH_TOKEN),
            () -> assertThat(response.getConfirmationHeader()).contains(expectedConfirmationHeader),
            () -> assertThat(response.getConfirmationBody())
                .contains("Notification to WHATEVER has failed. Please send notification to WHATEVER manually."),
            () -> verify(coreCaseDataService, never())
                .performPostSubmitCallback(any(), any(), any(), any())
        );
    }

    @Test
    void givenHearingCorrespondenceNeeded_whenHandleAddHearingAction_thenPublishSendCorrespondenceEvent() {
        // Arrange
        FinremCallbackRequest callbackRequest = buildCallbackRequest(ManageHearingsAction.ADD_HEARING);

        SendCorrespondenceEvent event = mock(SendCorrespondenceEvent.class);
        when(event.getCaseId()).thenReturn(CASE_ID);
        when(event.describeNotificationParties()).thenReturn("WHATEVER");
        when(manageHearingsCorresponder.buildHearingCorrespondenceEventIfNeeded(callbackRequest, AUTH_TOKEN)).thenReturn(event);

        // Act
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            manageHearingsSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        // Assert
        assertThat(response.getErrors()).isNullOrEmpty();
        assertThat(logs.getInfos()).contains(
            format("Beginning hearing correspondence for Hearing Added action. Case reference: %s", CASE_ID)
        );
        verify(manageHearingsCorresponder).buildHearingCorrespondenceEventIfNeeded(callbackRequest, AUTH_TOKEN);
        verify(manageHearingsCorresponder, never()).buildAdjournedOrVacatedHearingCorrespondenceEventIfNeeded(callbackRequest, AUTH_TOKEN);

        ArgumentCaptor<ThrowingRunnable> publishEventCaptor = getThrowingRunnableCaptor();
        verify(retryExecutor)
            .runWithRetryWithHandler(
                publishEventCaptor.capture(),
                eq("Send hearing correspondence"),
                eq(CASE_ID),
                any(RetryErrorHandler.class)
            );
        publishEventCaptor.getAllValues().forEach(TestSetUpUtils::runSafely);
        assertAll(
            () -> verify(applicationEventPublisher).publishEvent(event),
            () -> verify(coreCaseDataService).performPostSubmitCallback(
                any(), eq(CASE_ID_IN_LONG), eq(INTERNAL_CHANGE_UPDATE_CASE.getCcdType()), any()),
            () -> verifyNoMoreInteractions(retryExecutor)
        );
    }

    @Test
    void givenHearingCorrespondenceNeeded_whenHandleAdjournOrVacateHearingAction_thenPublishSendCorrespondenceEvent() {
        // Arrange
        FinremCallbackRequest callbackRequest = buildCallbackRequest(ManageHearingsAction.ADJOURN_OR_VACATE_HEARING);

        SendCorrespondenceEvent event = mock(SendCorrespondenceEvent.class);
        when(event.getCaseId()).thenReturn(CASE_ID);
        when(event.describeNotificationParties()).thenReturn("WHATEVER");
        when(manageHearingsCorresponder.buildAdjournedOrVacatedHearingCorrespondenceEventIfNeeded(callbackRequest, AUTH_TOKEN)).thenReturn(event);

        // Act
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            manageHearingsSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        // Assert
        assertThat(response.getErrors()).isNullOrEmpty();
        assertThat(logs.getInfos()).contains(
            format("Beginning hearing correspondence for Hearing Adjourned Or Vacated action. Case reference: %s", CASE_ID)
        );
        verify(manageHearingsCorresponder, never()).buildHearingCorrespondenceEventIfNeeded(callbackRequest, AUTH_TOKEN);
        verify(manageHearingsCorresponder).buildAdjournedOrVacatedHearingCorrespondenceEventIfNeeded(callbackRequest, AUTH_TOKEN);

        ArgumentCaptor<ThrowingRunnable> publishEventCaptor = getThrowingRunnableCaptor();
        verify(retryExecutor)
            .runWithRetryWithHandler(
                publishEventCaptor.capture(),
                eq("Send adjourned or vacate hearing correspondence"),
                eq(CASE_ID),
                any(RetryErrorHandler.class)
            );
        publishEventCaptor.getAllValues().forEach(TestSetUpUtils::runSafely);
        assertAll(
            () -> verify(applicationEventPublisher).publishEvent(event),
            () -> verify(coreCaseDataService).performPostSubmitCallback(
                any(), eq(CASE_ID_IN_LONG), eq(INTERNAL_CHANGE_UPDATE_CASE.getCcdType()), any()),
            () -> verifyNoMoreInteractions(retryExecutor)
        );
    }

    @Test
    void givenPendingNotifications_whenFlipFunctionApplied_thenMatchingRowsMarkedSentAndQueueDrained() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest(ManageHearingsAction.ADD_HEARING);

        SendCorrespondenceEvent event = mock(SendCorrespondenceEvent.class);
        when(event.getCaseId()).thenReturn(CASE_ID);
        when(event.describeNotificationParties()).thenReturn("WHATEVER");
        when(manageHearingsCorresponder.buildHearingCorrespondenceEventIfNeeded(callbackRequest, AUTH_TOKEN)).thenReturn(event);

        UUID rowId1 = UUID.randomUUID();
        UUID rowId2 = UUID.randomUUID();
        NotificationAuditCollectionItem row1 = NotificationAuditCollectionItem.builder()
            .id(rowId1).value(NotificationAudit.builder().wasSent(YesOrNo.NO).build()).build();
        NotificationAuditCollectionItem row2 = NotificationAuditCollectionItem.builder()
            .id(rowId2).value(NotificationAudit.builder().wasSent(YesOrNo.NO).build()).build();
        FinremCaseData latestData = FinremCaseData.builder()
            .notificationAuditWrapper(NotificationAuditWrapper.builder()
                .notificationsAudits(new java.util.ArrayList<>(List.of(row1, row2)))
                .notificationsToBeSent(new java.util.ArrayList<>(List.of(
                    NotificationToBeSentCollectionItem.builder().id(UUID.randomUUID()).value(rowId1).build(),
                    NotificationToBeSentCollectionItem.builder().id(UUID.randomUUID()).value(rowId2).build())))
                .build())
            .build();

        when(objectMapper.convertValue(any(), eq(List.class)))
            .thenReturn(List.of(row1, row2));
        CaseDetails latest = CaseDetails.builder().id(CASE_ID_IN_LONG).build();
        FinremCaseDetails mappedLatest = FinremCaseDetails.builder().id(CASE_ID_IN_LONG).data(latestData).build();
        when(finremCaseDetailsMapper.mapToFinremCaseDetails(latest)).thenReturn(mappedLatest);

        manageHearingsSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Function<CaseDetails, Map<String, Object>>> fnCaptor =
            ArgumentCaptor.forClass(Function.class);
        verify(coreCaseDataService).performPostSubmitCallback(
            any(), eq(CASE_ID_IN_LONG), eq(INTERNAL_CHANGE_UPDATE_CASE.getCcdType()), fnCaptor.capture());
        Map<String, Object> updates = fnCaptor.getValue().apply(latest);

        assertAll(
            () -> assertThat(row1.getValue().getWasSent()).isEqualTo(YesOrNo.YES),
            () -> assertThat(row2.getValue().getWasSent()).isEqualTo(YesOrNo.YES),
            () -> assertThat((List<?>) updates.get("notificationsToBeSent")).isEmpty()
        );
    }

    private FinremCallbackRequest buildCallbackRequest(ManageHearingsAction action) {
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .manageHearingsWrapper(ManageHearingsWrapper
                .builder()
                .manageHearingsActionSelection(action)
                .build())
            .build();

        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .data(finremCaseData)
            .id(CASE_ID_IN_LONG)
            .build();

        return FinremCallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
    }
}
