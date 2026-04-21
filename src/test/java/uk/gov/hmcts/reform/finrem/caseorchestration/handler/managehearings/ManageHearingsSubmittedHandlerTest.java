package uk.gov.hmcts.reform.finrem.caseorchestration.handler.managehearings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.managehearing.ManageHearingsCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogs;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryErrorHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.ThrowingRunnable;

import java.util.List;

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
        when(event.getNotificationParties()).thenReturn(List.of(NotificationParty.APPLICANT));
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
            () -> assertThat(response.getConfirmationHeader()).contains("Manage Hearings completed with error"),
            () -> assertThat(response.getConfirmationBody())
                .contains("Notification to applicant has failed. Please send notification to applicant manually.")
        );
    }

    @Test
    void givenExceptionThrown_whenSendingAdjournedOrVacateHearingCorrespondenceFailed_thenPopulateErrorToConfirmationBody() {
        // Arrange
        FinremCallbackRequest callbackRequest = buildCallbackRequest(ManageHearingsAction.ADJOURN_OR_VACATE_HEARING);

        SendCorrespondenceEvent event = mock(SendCorrespondenceEvent.class);
        when(event.getCaseId()).thenReturn(CASE_ID);
        when(event.getNotificationParties()).thenReturn(List.of(NotificationParty.APPLICANT));
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
            () -> assertThat(response.getConfirmationHeader()).contains("Manage Hearings completed with error"),
            () -> assertThat(response.getConfirmationBody())
                .contains("Notification to applicant has failed. Please send notification to applicant manually.")
        );
    }

    @ParameterizedTest
    @EnumSource(value = NotificationParty.class, names = {
        "APPLICANT",
        "RESPONDENT",
        "INTERVENER_ONE",
        "INTERVENER_TWO",
        "INTERVENER_THREE",
        "INTERVENER_FOUR"})
    void givenSinglePartyToBeNotified_whenHandleAddHearingAction_thenPublishSendCorrespondenceEvent(NotificationParty notificationParty) {
        // Arrange
        FinremCallbackRequest callbackRequest = buildCallbackRequest(ManageHearingsAction.ADD_HEARING);

        SendCorrespondenceEvent event = mock(SendCorrespondenceEvent.class);
        when(event.getCaseId()).thenReturn(CASE_ID);
        when(event.getNotificationParties()).thenReturn(List.of(notificationParty));
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
        verify(applicationEventPublisher).publishEvent(event);
        verifyNoMoreInteractions(retryExecutor);
    }

    @ParameterizedTest
    @EnumSource(value = NotificationParty.class, names = {
        "APPLICANT",
        "RESPONDENT",
        "INTERVENER_ONE",
        "INTERVENER_TWO",
        "INTERVENER_THREE",
        "INTERVENER_FOUR"})
    void givenSinglePartyToBeNotified_whenHandleAdjournOrVacateHearingAction_thenPublishSendCorrespondenceEvent(NotificationParty notificationParty) {
        // Arrange
        FinremCallbackRequest callbackRequest = buildCallbackRequest(ManageHearingsAction.ADJOURN_OR_VACATE_HEARING);

        SendCorrespondenceEvent event = mock(SendCorrespondenceEvent.class);
        when(event.getCaseId()).thenReturn(CASE_ID);
        when(event.getNotificationParties()).thenReturn(List.of(notificationParty));
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
        verify(applicationEventPublisher).publishEvent(event);
        verifyNoMoreInteractions(retryExecutor);
    }

    @Test
    void givenMultiplePartiesToBeNotified_whenHandleAddHearingAction_thenPublishSendCorrespondenceEvent() {
        // Arrange
        FinremCallbackRequest callbackRequest = buildCallbackRequest(ManageHearingsAction.ADD_HEARING);

        SendCorrespondenceEvent event = mock(SendCorrespondenceEvent.class);
        when(event.getCaseId()).thenReturn(CASE_ID);
        when(event.getNotificationParties()).thenReturn(List.of(NotificationParty.RESPONDENT,
            NotificationParty.INTERVENER_ONE,
            NotificationParty.APPLICANT));
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
        verify(applicationEventPublisher).publishEvent(event);
        verifyNoMoreInteractions(retryExecutor);
    }

    @Test
    void shouldHandleSubmittedCallbackForVacateHearing() {
        // Arrange
        FinremCallbackRequest callbackRequest = buildCallbackRequest(ManageHearingsAction.ADJOURN_OR_VACATE_HEARING);

        // Act
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            manageHearingsSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        // Assert
        assertThat(response.getErrors()).isNullOrEmpty();
        assertThat(logs.getInfos()).contains(
            format("Beginning hearing correspondence for Hearing Adjourned Or Vacated action. Case reference: %s", CASE_ID)
        );
        verify(manageHearingsCorresponder, never()).sendHearingCorrespondence(callbackRequest, AUTH_TOKEN);
        verify(manageHearingsCorresponder).buildAdjournedOrVacatedHearingCorrespondenceEventIfNeeded(callbackRequest, AUTH_TOKEN);
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
