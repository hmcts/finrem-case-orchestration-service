package uk.gov.hmcts.reform.finrem.caseorchestration.handler.consented;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignPartiesAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationAuditService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UserNotFoundInOrganisationApiException;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge.consented.AssignToJudgeCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryErrorHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.ThrowingRunnable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.getThrowingRunnableCaptor;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.mockRunWithRetryWithHandlerInvokesFirstErrorHandler;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.runSafely;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.ISSUE_APPLICATION;

public abstract class IssueApplicationConsentedSubmittedHandlerContractTest {

    protected String expectedConfirmationHeader = "Application Issued with Errors";

    protected abstract ApplicationEventPublisher applicationEventPublisher();

    protected abstract FinremCallbackHandler handler();

    protected abstract RetryExecutor retryExecutor();

    protected abstract AssignToJudgeCorresponder assignToJudgeCorresponder();

    protected abstract AssignPartiesAccessService assignPartiesAccessService();

    protected abstract NotificationAuditService notificationAuditService();

    protected abstract CoreCaseDataService coreCaseDataService();

    @BeforeEach
    void setup() {
        lenient().doNothing().when(retryExecutor()).runWithRetryWithHandler(any(), anyString(), any(), any());
    }

    @Test
    void givenCase_whenGrantRespondentSolicitorErrorFailed_thenPopulateErrorToConfirmationBody() {
        // Arrange
        FinremCaseData spiedFinremCaseData = spy(
            FinremCaseData.builder()
                .contactDetailsWrapper(ContactDetailsWrapper.builder()
                    .consentedRespondentRepresented(YesOrNo.YES).build())
                .build()
        );
        when(spiedFinremCaseData.getRespondentSolicitorEmail()).thenReturn(TEST_RESP_SOLICITOR_EMAIL);

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(spiedFinremCaseData);

        mockRunWithRetryWithHandlerInvokesFirstErrorHandler(
            retryExecutor(),
            "granting respondent solicitor"
        );

        // Act
        var response = handler().handle(callbackRequest, AUTH_TOKEN);

        // then
        assertAll(
            () -> assertThat(response.getConfirmationHeader()).contains(expectedConfirmationHeader),
            () -> assertThat(response.getConfirmationBody())
                .doesNotContain("There was a problem sending issue application correspondence. Please send it manually.")
                .contains("There was a problem granting access to respondent solicitor: " + TEST_RESP_SOLICITOR_EMAIL)
        );
    }

    @Test
    void givenNoErrorsEvolved_whenHandled_thenDoesNotPopulateErrorToConfirmationBody() {
        FinremCaseData spiedFinremCaseData = spy(
            FinremCaseData.builder()
                .contactDetailsWrapper(ContactDetailsWrapper.builder()
                    .consentedRespondentRepresented(YesOrNo.YES).build())
                .build()
        );
        when(spiedFinremCaseData.getRespondentSolicitorEmail()).thenReturn(TEST_RESP_SOLICITOR_EMAIL);

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(spiedFinremCaseData);

        // Act
        var response = handler().handle(callbackRequest, AUTH_TOKEN);

        // then
        assertAll(
            () -> assertThat(response.getConfirmationHeader()).isNull(),
            () -> assertThat(response.getConfirmationBody()).isNull()
        );
    }

    @Test
    void givenCaseWithRepresentedRespondentSolicitor_whenHandled_shouldGrantRespondentSolicitor()
        throws UserNotFoundInOrganisationApiException {
        // Arrange
        FinremCaseData caseData = spy(FinremCaseData.builder()
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .consentedRespondentRepresented(YesOrNo.YES)
                .build())
            .build());
        when(caseData.getRespondentSolicitorEmail()).thenReturn(TEST_RESP_SOLICITOR_EMAIL);

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG,
            caseData);

        // Act
        handler().handle(callbackRequest, AUTH_TOKEN);

        ArgumentCaptor<ThrowingRunnable> runnableCaptor = getThrowingRunnableCaptor();
        verify(retryExecutor())
            .runWithRetryWithHandler(
                runnableCaptor.capture(),
                eq("granting respondent solicitor"),
                eq(CASE_ID),
                any(RetryErrorHandler.class)
            );
        runSafely(runnableCaptor.getValue());
        verify(assignPartiesAccessService()).grantRespondentSolicitor(caseData);
    }

    @Test
    void givenCaseWithoutRespondentSolicitorEmail_whenHandled_shouldGrantRespondentSolicitor()
        throws UserNotFoundInOrganisationApiException {
        // Arrange
        FinremCaseData caseData = FinremCaseData.builder()
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .consentedRespondentRepresented(YesOrNo.YES)
                .build())
            .build();

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG,
            caseData);

        // Act
        handler().handle(callbackRequest, AUTH_TOKEN);

        verify(retryExecutor(), never())
            .runWithRetryWithHandler(
                any(ThrowingRunnable.class),
                eq("granting respondent solicitor"),
                eq(CASE_ID),
                any(RetryErrorHandler.class)
            );
        verify(assignPartiesAccessService(), never()).grantRespondentSolicitor(caseData);
    }

    @Test
    void givenRespondentUnrepresented_whenHandled_shouldGrantRespondentSolicitor()
        throws UserNotFoundInOrganisationApiException {
        // Arrange
        FinremCaseData caseData = FinremCaseData.builder()
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .consentedRespondentRepresented(YesOrNo.NO)
                .build())
            .build();

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG,
            caseData);

        // Act
        handler().handle(callbackRequest, AUTH_TOKEN);

        verify(retryExecutor(), never())
            .runWithRetryWithHandler(
                any(ThrowingRunnable.class),
                eq("granting respondent solicitor"),
                eq(CASE_ID),
                any(RetryErrorHandler.class)
            );
        verify(assignPartiesAccessService(), never()).grantRespondentSolicitor(caseData);
    }

    @Test
    void givenCase_whenSendIssueApplicationCorrespondenceFailed_thenPopulateErrorToConfirmationBody() {
        // Arrange
        FinremCaseData spiedFinremCaseData = spy(
            FinremCaseData.builder()
                .contactDetailsWrapper(ContactDetailsWrapper.builder()
                    .consentedRespondentRepresented(YesOrNo.YES).build())
                .build()
        );
        when(spiedFinremCaseData.getRespondentSolicitorEmail()).thenReturn(TEST_RESP_SOLICITOR_EMAIL);

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, CaseType.CONSENTED, ISSUE_APPLICATION,
            spiedFinremCaseData);

        stubSingleSendCorrespondenceEvent(callbackRequest.getCaseDetails());

        mockRunWithRetryWithHandlerInvokesFirstErrorHandler(
            retryExecutor(),
            "sending issue application correspondence TRACKER-ID (APPLICANT AND RESPONDENT)"
        );

        // Act
        var response = handler().handle(callbackRequest, AUTH_TOKEN);

        // then
        assertAll(
            () -> assertThat(response.getConfirmationHeader()).contains(expectedConfirmationHeader),
            () -> assertThat(response.getConfirmationBody())
                .containsOnlyOnce("There was a problem sending issue application correspondence (%s). Please send it manually."
                    .formatted("APPLICANT AND RESPONDENT"))
        );
    }

    @Test
    void givenCase_whenHandled_shouldPublishSendCorrespondenceEvent() {
        // Arrange
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, ISSUE_APPLICATION);

        final SendCorrespondenceEvent event = stubSingleSendCorrespondenceEvent(callbackRequest.getCaseDetails());
        // Act
        handler().handle(callbackRequest, AUTH_TOKEN);

        ArgumentCaptor<ThrowingRunnable> runnableCaptor = getThrowingRunnableCaptor();
        verify(retryExecutor())
            .runWithRetryWithHandler(
                runnableCaptor.capture(),
                eq("sending issue application correspondence TRACKER-ID (APPLICANT AND RESPONDENT)"),
                eq(CASE_ID),
                any(RetryErrorHandler.class)
            );
        runSafely(runnableCaptor.getValue());
        verify(applicationEventPublisher()).publishEvent(event);
    }

    @Test
    void givenCase_whenHandled_shouldPublisMultipleSendCorrespondenceEvents() {
        // Arrange
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, ISSUE_APPLICATION);

        final List<SendCorrespondenceEvent> events = stubMultipleSendCorrespondenceEvents(callbackRequest.getCaseDetails());
        // Act
        handler().handle(callbackRequest, AUTH_TOKEN);

        ArgumentCaptor<ThrowingRunnable> runnableCaptor = getThrowingRunnableCaptor();
        verify(retryExecutor())
            .runWithRetryWithHandler(
                runnableCaptor.capture(),
                eq("sending issue application correspondence TRACKER-ID-1 (APPLICANT)"),
                eq(CASE_ID),
                any(RetryErrorHandler.class)
            );
        verify(retryExecutor())
            .runWithRetryWithHandler(
                runnableCaptor.capture(),
                eq("sending issue application correspondence TRACKER-ID-2 (RESPONDENT)"),
                eq(CASE_ID),
                any(RetryErrorHandler.class)
            );
        runnableCaptor.getAllValues().forEach(TestSetUpUtils::runSafely);
        for (SendCorrespondenceEvent event : events) {
            verify(applicationEventPublisher()).publishEvent(event);
        }
    }

    private SendCorrespondenceEvent stubSingleSendCorrespondenceEvent(FinremCaseDetails caseDetails) {
        SendCorrespondenceEvent event = mock(SendCorrespondenceEvent.class);
        when(event.getNotificationTrackerId()).thenReturn("TRACKER-ID");
        when(event.describeNotificationParties()).thenReturn("APPLICANT AND RESPONDENT");
        List<SendCorrespondenceEvent> events = List.of(event);
        when(assignToJudgeCorresponder().buildSendCorrespondenceEvents(ISSUE_APPLICATION, caseDetails,
            AUTH_TOKEN)).thenReturn(events);
        return event;
    }

    private List<SendCorrespondenceEvent> stubMultipleSendCorrespondenceEvents(FinremCaseDetails caseDetails) {
        SendCorrespondenceEvent event = mock(SendCorrespondenceEvent.class);
        when(event.getNotificationTrackerId()).thenReturn("TRACKER-ID-1");
        when(event.describeNotificationParties()).thenReturn("APPLICANT");

        SendCorrespondenceEvent event2 = mock(SendCorrespondenceEvent.class);
        when(event2.getNotificationTrackerId()).thenReturn("TRACKER-ID-2");
        when(event2.describeNotificationParties()).thenReturn("RESPONDENT");

        List<SendCorrespondenceEvent> events = List.of(event, event2);
        when(assignToJudgeCorresponder().buildSendCorrespondenceEvents(ISSUE_APPLICATION, caseDetails,
            AUTH_TOKEN)).thenReturn(events);

        return events;
    }
}
