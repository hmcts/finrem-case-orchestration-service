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

class CitizenRespondentPartyListenerTest extends BasePartyListenerTest {

    @InjectMocks
    private CitizenRespondentPartyListener underTest;

    @Test
    void shouldNotHandleIrrelevantNotificationParty() {
        SendCorrespondenceEvent otherEvent = SendCorrespondenceEvent.builder()
            .notificationParties(List.of(NotificationParty.CITIZEN_APPLICANT))
            .build();

        underTest.handleNotification(otherEvent);

        verifyNoInteractions(emailService, notificationService);
        verifyNoLetterSent();
    }

    @Test
    void shouldSendEmailWhenRespondentEmailAddressIsPresent() {
        SendCorrespondenceEvent event = eventWithRespondentEmail("respondent@example.com");

        underTest.handleNotification(event);

        ArgumentCaptor<NotificationRequest> notificationRequestCaptor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(emailService).sendConfirmationEmail(notificationRequestCaptor.capture(), eq(EmailTemplateNames.FR_CUI_DOCUMENTS_UPLOADED));

        assertThat(notificationRequestCaptor.getValue().getNotificationEmail()).isEqualTo("respondent@example.com");
        assertThat(notificationRequestCaptor.getValue().getName()).isEqualTo("Respondent One");
        assertThat(notificationRequestCaptor.getValue().getSolicitorReferenceNumber()).isEqualTo("");
    }

    @Test
    void shouldNotSendEmailWhenRespondentEmailAddressIsBlank() {
        SendCorrespondenceEvent event = eventWithRespondentEmail("   ");

        underTest.handleNotification(event);

        verifyNoInteractions(emailService);
        verifyNoLetterSent();
    }

    @Test
    void shouldNotSendEmailWhenRespondentEmailAddressIsNull() {
        SendCorrespondenceEvent event = eventWithRespondentEmail(null);

        underTest.handleNotification(event);

        verifyNoInteractions(emailService);
        verifyNoLetterSent();
    }

    @Test
    void shouldNotSendEmailWhenCaseDataIsMissing() {
        SendCorrespondenceEvent event = SendCorrespondenceEvent.builder()
            .caseDetails(FinremCaseDetails.builder().build())
            .notificationParties(List.of(NotificationParty.CITIZEN_RESPONDENT))
            .emailNotificationRequest(emailNotificationRequest(null))
            .build();

        underTest.handleNotification(event);

        verifyNoInteractions(emailService);
        verifyNoLetterSent();
    }

    private SendCorrespondenceEvent eventWithRespondentEmail(String email) {
        FinremCaseData caseData = FinremCaseData.builder()
            .ccdCaseType(uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED)
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .respondentEmail(email)
                .respondentFmName("Respondent")
                .respondentLname("One")
                .build())
            .build();

        return SendCorrespondenceEvent.builder()
            .caseDetails(FinremCaseDetails.builder().data(caseData).build())
            .notificationParties(List.of(NotificationParty.CITIZEN_RESPONDENT))
            .emailNotificationRequest(emailNotificationRequest(null))
            .emailTemplate(EmailTemplateNames.FR_CUI_DOCUMENTS_UPLOADED)
            .build();
    }
}
