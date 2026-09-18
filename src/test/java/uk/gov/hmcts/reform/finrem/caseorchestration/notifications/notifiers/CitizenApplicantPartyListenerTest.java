package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class CitizenApplicantPartyListenerTest extends BasePartyListenerTest {

    @InjectMocks
    private CitizenApplicantPartyListener underTest;

    @Test
    void shouldNotHandleIrrelevantNotificationParty() {
        SendCorrespondenceEvent otherEvent = SendCorrespondenceEvent.builder()
            .notificationParties(List.of(NotificationParty.CITIZEN_RESPONDENT))
            .build();

        underTest.handleNotification(otherEvent);

        verifyNoInteractions(emailService, notificationService);
        verifyNoLetterSent();
    }

    @Test
    void shouldSendEmailWhenApplicantEmailAddressIsPresent() {
        SendCorrespondenceEvent event = eventWithApplicantEmail("applicant@example.com");

        underTest.handleNotification(event);

        ArgumentCaptor<NotificationRequest> notificationRequestCaptor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(emailService).sendConfirmationEmail(notificationRequestCaptor.capture(), eq(EmailTemplateNames.FR_CUI_DOCUMENTS_UPLOADED));

        assertThat(notificationRequestCaptor.getValue().getNotificationEmail()).isEqualTo("applicant@example.com");
        assertThat(notificationRequestCaptor.getValue().getName()).isEqualTo("Applicant One");
        assertThat(notificationRequestCaptor.getValue().getSolicitorReferenceNumber()).isEqualTo("");
    }

    @Test
    void shouldNotSendEmailWhenApplicantEmailAddressIsBlank() {
        SendCorrespondenceEvent event = eventWithApplicantEmail("   ");

        underTest.handleNotification(event);

        verifyNoInteractions(emailService);
        verifyNoLetterSent();
    }

    @Test
    void shouldNotSendEmailWhenApplicantEmailAddressIsNull() {
        SendCorrespondenceEvent event = eventWithApplicantEmail(null);

        underTest.handleNotification(event);

        verifyNoInteractions(emailService);
        verifyNoLetterSent();
    }

    @Test
    void shouldNotSendEmailWhenCaseDataMissing() {
        SendCorrespondenceEvent event = SendCorrespondenceEvent.builder()
            .caseDetails(FinremCaseDetails.builder().build())
            .notificationParties(List.of(NotificationParty.CITIZEN_APPLICANT))
            .emailNotificationRequest(emailNotificationRequest(null))
            .build();

        underTest.handleNotification(event);

        verifyNoInteractions(emailService);
        verifyNoLetterSent();
    }

    private SendCorrespondenceEvent eventWithApplicantEmail(String email) {
        FinremCaseData caseData = FinremCaseData.builder()
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .applicantEmail(email)
                .applicantFmName("Applicant")
                .applicantLname("One")
                .build())
            .build();

        return SendCorrespondenceEvent.builder()
            .caseDetails(FinremCaseDetails.builder().data(caseData).build())
            .notificationParties(List.of(NotificationParty.CITIZEN_APPLICANT))
            .emailNotificationRequest(emailNotificationRequest(null))
            .emailTemplate(EmailTemplateNames.FR_CUI_DOCUMENTS_UPLOADED)
            .build();
    }
}
