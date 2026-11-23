package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_REFERENCE;

class RespondentSolicitorListenerTest extends BasePartyListenerTest {

    @InjectMocks
    private RespondentSolicitorListener underTest;

    @ParameterizedTest
    @EnumSource(value = NotificationParty.class, mode = EnumSource.Mode.EXCLUDE, names = {"RESPONDENT_SOLICITOR_ONLY"})
    void shouldNotHandleIrrelevantNotificationParty(NotificationParty notificationParty) {
        SendCorrespondenceEvent otherEvent = SendCorrespondenceEvent.builder()
            .notificationParties(List.of(notificationParty))
            .build();

        underTest.handleNotification(otherEvent);
        verifyNoInteractions(emailService, notificationService);
    }

    @ParameterizedTest
    @ValueSource(strings = {TEST_SOLICITOR_REFERENCE})
    @NullAndEmptySource
    void shouldSendEmailNotification(String solicitorReferenceNumber) {
        FinremCaseDetails caseDetails = mock(FinremCaseDetails.class);
        EmailTemplateNames emailTemplate = mock(EmailTemplateNames.class);

        SendCorrespondenceEvent event = sendCorrespondenceEventWithTargetNotificationParty(caseDetails, mock(FinremCaseDetails.class),
            emailTemplate, solicitorReferenceNumber);
        when(notificationService.isRespondentSolicitorEmailPopulatedAndPresented(caseDetails)).thenReturn(true);

        underTest.handleNotification(event);

        ArgumentCaptor<NotificationRequest> notificationRequestArgumentCaptor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(emailService).sendConfirmationEmail(notificationRequestArgumentCaptor.capture(), eq(emailTemplate));
        assertThat(notificationRequestArgumentCaptor.getValue())
            .extracting(
                NotificationRequest::getName,
                NotificationRequest::getNotificationEmail,
                NotificationRequest::getSolicitorReferenceNumber)
            .contains(TEST_SOLICITOR_NAME, TEST_SOLICITOR_EMAIL, solicitorReferenceNumber);
        verifyNoLetterSent();
    }

    @Test
    void shouldNotSendEmailNotificationIfRespondentSolicitorEmailIsNotPresent() {
        FinremCaseDetails caseDetails = mock(FinremCaseDetails.class);

        SendCorrespondenceEvent event = sendCorrespondenceEventWithTargetNotificationParty(caseDetails, mock(FinremCaseDetails.class),
            mock(EmailTemplateNames.class));
        when(notificationService.isRespondentSolicitorEmailPopulatedAndPresented(caseDetails)).thenReturn(false);

        underTest.handleNotification(event);

        verifyNoInteractions(emailService);
        verifyNoLetterSent();
    }

    @Test
    void shouldNotSendEmailNotificationIfCaseDetailsBeforeIsAbsent() {
        FinremCaseDetails caseDetails = null;

        SendCorrespondenceEvent event = sendCorrespondenceEventWithTargetNotificationParty(
            caseDetails, mock(FinremCaseDetails.class), mock(EmailTemplateNames.class));

        underTest.handleNotification(event);

        verifyNoInteractions(emailService);
        verifyNoLetterSent();
    }
}
