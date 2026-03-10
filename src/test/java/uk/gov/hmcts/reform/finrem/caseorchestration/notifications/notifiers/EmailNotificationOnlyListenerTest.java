package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class EmailNotificationOnlyListenerTest {

    private static final String RECIPIENT_NAME = "recipientName";
    private static final String RECIPIENT_EMAIL = "recipientEmail";
    private static final String RECIPIENT_REFERENCE = "recipientReference";

    @Mock
    private BulkPrintService bulkPrintService;
    @Mock
    private EmailService emailService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private InternationalPostalService internationalPostalService;

    class EmailNotificationOnlyListenerImpl extends EmailNotificationOnlyListener {

        EmailNotificationOnlyListenerImpl() {
            super(EmailNotificationOnlyListenerTest.this.bulkPrintService,
                EmailNotificationOnlyListenerTest.this.emailService,
                EmailNotificationOnlyListenerTest.this.notificationService,
                EmailNotificationOnlyListenerTest.this.internationalPostalService);
        }

        @Override
        protected String getNotificationParty() {
            return getClass().getSimpleName();
        }

        @Override
        protected boolean isRelevantParty(SendCorrespondenceEvent event) {
            return true;
        }

        @Override
        protected boolean shouldSendEmailNotification(SendCorrespondenceEvent event) {
            return true;
        }

        @Override
        protected PartySpecificDetails setPartySpecificDetails(SendCorrespondenceEvent event) {
            return new PartySpecificDetails(RECIPIENT_EMAIL, RECIPIENT_NAME, RECIPIENT_REFERENCE);
        }
    }

    private EmailNotificationOnlyListenerImpl emailNotificationOnlyListener;

    @BeforeEach
    void setUp() {
        emailNotificationOnlyListener = new EmailNotificationOnlyListenerImpl();
    }

    @Test
    void givenNotificationRequestProvided_whenEmailNotificationOnlyListenerCalled_thenSendEmailAndNoLetterSent() {
        EmailTemplateNames template = mock(EmailTemplateNames.class);
        NotificationRequest nr = spy(NotificationRequest.builder().build());

        SendCorrespondenceEvent event = SendCorrespondenceEvent.builder()
            .emailTemplate(template)
            .emailNotificationRequest(nr)
            .build();

        emailNotificationOnlyListener.handleNotification(event);

        ArgumentCaptor<NotificationRequest> captor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(emailService).sendConfirmationEmail(captor.capture(), eq(template));
        verifyNoLetterSent();
        assertThat(captor.getValue())
            .extracting(
                NotificationRequest::getName,
                NotificationRequest::getNotificationEmail,
                NotificationRequest::getSolicitorReferenceNumber)
            .contains(RECIPIENT_NAME, RECIPIENT_EMAIL, RECIPIENT_REFERENCE);
    }

    @Test
    void givenNotificationRequestMissing_whenSendEmailNotificationListenerCalled_thenExceptionIsThrown() {
        EmailTemplateNames template = mock(EmailTemplateNames.class);
        SendCorrespondenceEvent event = spy(SendCorrespondenceEvent.builder()
            .emailTemplate(template)
            .caseDetails(FinremCaseDetails.builder().build())
            .build());
        when(event.getCaseId()).thenReturn(TEST_CASE_ID);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            emailNotificationOnlyListener.handleNotification(event));
        assertThat(exception.getMessage()).isEqualTo("Notification Request is required for digital notifications, case ID: "
            + TEST_CASE_ID);
    }

    @Test
    void givenEmailTemplateNamesMissing_whenEmailNotificationOnlyListenerCalled_thenSendEmailAndNoLetterSent() {
        SendCorrespondenceEvent event = spy(SendCorrespondenceEvent.builder()
            .emailTemplate(null)
            .emailNotificationRequest(mock(NotificationRequest.class))
            .caseDetails(FinremCaseDetails.builder().build())
            .build());
        when(event.getCaseId()).thenReturn(TEST_CASE_ID);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            emailNotificationOnlyListener.handleNotification(event));
        assertThat(exception.getMessage()).isEqualTo("Email template is required for digital notifications, case ID: "
            + TEST_CASE_ID);
    }

    private void verifyNoLetterSent() {
        verifyNoInteractions(bulkPrintService, internationalPostalService);
    }
}
