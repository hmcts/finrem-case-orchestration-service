package uk.gov.hmcts.reform.finrem.caseorchestration.handler.generalapplicationdirections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CorrespondenceEventAuditOrchestrationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationDirectionsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.managehearing.ManageHearingsCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.assertCondition;
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
    private CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService;

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

        SendCorrespondenceEvent event = mock(SendCorrespondenceEvent.class);
        when(event.describeNotificationParties()).thenReturn("APPLICANT");

        List<SendCorrespondenceEvent> events = List.of(event);
        when(manageHearingsCorresponder.buildHearingCorrespondenceEventsIfNeeded(callbackRequest,
            AUTH_TOKEN)).thenReturn(events);

        // Act
        var response = generalApplicationDirectionsSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        // Assert
        assertAll(
            () -> assertThat(response.getConfirmationHeader()).isNullOrEmpty(),
            () -> assertThat(response.getConfirmationBody()).isNullOrEmpty(),
            () -> verify(correspondenceEventAuditOrchestrationService).publishEvent(eq(event),
                eq("Send hearing corresponder to party: APPLICANT on general application direction event"),
                any(Runnable.class))
        );
    }

    @Test
    void shouldSendMultipleHearingCorrespondencesOnSubmittedCallback() {
        // Arrange
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG,
            FinremCaseData.builder().build());

        when(generalApplicationDirectionsService.isHearingRequired(callbackRequest.getCaseDetails()))
            .thenReturn(true);

        SendCorrespondenceEvent applicantEvent = mock(SendCorrespondenceEvent.class);
        when(applicantEvent.describeNotificationParties()).thenReturn("APPLICANT");
        SendCorrespondenceEvent respondentEvent = mock(SendCorrespondenceEvent.class);
        when(respondentEvent.describeNotificationParties()).thenReturn("RESPONDENT");

        List<SendCorrespondenceEvent> events = List.of(applicantEvent, respondentEvent);
        when(manageHearingsCorresponder.buildHearingCorrespondenceEventsIfNeeded(callbackRequest,
            AUTH_TOKEN)).thenReturn(events);

        // Act
        var response = generalApplicationDirectionsSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        // Assert
        assertAll(
            () -> assertThat(response.getConfirmationHeader()).isNullOrEmpty(),
            () -> assertThat(response.getConfirmationBody()).isNullOrEmpty(),
            () -> verify(correspondenceEventAuditOrchestrationService).publishEvent(eq(applicantEvent),
                eq("Send hearing corresponder to party: APPLICANT on general application direction event"),
                any(Runnable.class)),
            () -> verify(correspondenceEventAuditOrchestrationService).publishEvent(eq(respondentEvent),
                eq("Send hearing corresponder to party: RESPONDENT on general application direction event"),
                any(Runnable.class))
        );
    }

    @Test
    void givenExceptionThrown_whenSendingApplicantHearingCorrespondenceFailed_thenPopulateErrorToConfirmationBody() {
        // Arrange
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG,
            FinremCaseData.builder().build());

        when(generalApplicationDirectionsService.isHearingRequired(callbackRequest.getCaseDetails()))
            .thenReturn(true);

        SendCorrespondenceEvent applicantEvent = mock(SendCorrespondenceEvent.class);
        when(applicantEvent.describeNotificationParties()).thenReturn("APPLICANT");
        SendCorrespondenceEvent respondentEvent = mock(SendCorrespondenceEvent.class);
        when(respondentEvent.describeNotificationParties()).thenReturn("RESPONDENT");

        List<SendCorrespondenceEvent> events = List.of(applicantEvent, respondentEvent);
        when(manageHearingsCorresponder.buildHearingCorrespondenceEventsIfNeeded(callbackRequest,
            AUTH_TOKEN)).thenReturn(events);

        doAnswer(invocation -> {
            invocation.getArgument(2, Runnable.class).run();
            return null;
        }).when(correspondenceEventAuditOrchestrationService).publishEvent(
            eq(applicantEvent),
            eq("Send hearing corresponder to party: APPLICANT on general application direction event"),
            any(Runnable.class)
        );

        // Act
        var response = generalApplicationDirectionsSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);
        assertAll(
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
