package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.List;

import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.PREVIOUS_APPLICANT_SOLICITOR_ONLY;

@ExtendWith(MockitoExtension.class)
class PreviousApplicantSolicitorListenerTest {

    @Mock
    private BulkPrintService bulkPrintService;
    @Mock
    private EmailService emailService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private InternationalPostalService internationalPostalService;

    @InjectMocks
    private PreviousApplicantSolicitorListener underTest;

    private static NotificationRequest emailNotificationRequest(String solicitorReferenceNumber) {
        return NotificationRequest.builder()
            .notificationEmail(TEST_SOLICITOR_EMAIL)
            .name(TEST_SOLICITOR_NAME)
            .solicitorReferenceNumber(solicitorReferenceNumber)
            .build();
    }

    private static SendCorrespondenceEvent sendCorrespondenceEventWithTargetNotificationParty(FinremCaseDetails caseDetailsBefore,
                                                                                              EmailTemplateNames emailTemplate) {
        return sendCorrespondenceEventWithTargetNotificationParty(caseDetailsBefore, emailTemplate, null);
    }

    private static SendCorrespondenceEvent sendCorrespondenceEventWithTargetNotificationParty(FinremCaseDetails caseDetailsBefore,
                                                                                              EmailTemplateNames emailTemplate,
                                                                                              String solicitorReferenceNumber) {
        return SendCorrespondenceEvent.builder()
            .caseDetails(FinremCaseDetails.builder().data(FinremCaseData.builder().build()).build())
            .caseDetailsBefore(caseDetailsBefore)
            .notificationParties(List.of(PREVIOUS_APPLICANT_SOLICITOR_ONLY))
            .emailNotificationRequest(emailNotificationRequest(solicitorReferenceNumber))
            .emailTemplate(emailTemplate)
            .build();
    }

    private void verifyNoLetterSent() {
        verifyNoInteractions(bulkPrintService, internationalPostalService);
    }

    @ParameterizedTest
    @EnumSource(value = NotificationParty.class, mode = EnumSource.Mode.EXCLUDE, names = {"PREVIOUS_APPLICANT_SOLICITOR_ONLY"})
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
        FinremCaseDetails caseDetailsBefore = mock(FinremCaseDetails.class);
        EmailTemplateNames emailTemplate = mock(EmailTemplateNames.class);

        SendCorrespondenceEvent event = sendCorrespondenceEventWithTargetNotificationParty(caseDetailsBefore, emailTemplate,
            solicitorReferenceNumber);
        when(notificationService.isApplicantSolicitorEmailPopulatedAndPresented(caseDetailsBefore)).thenReturn(true);

        underTest.handleNotification(event);

        ArgumentCaptor<NotificationRequest> notificationRequestArgumentCaptor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(emailService).sendConfirmationEmail(notificationRequestArgumentCaptor.capture(), eq(emailTemplate));
        assertThat(notificationRequestArgumentCaptor.getValue())
            .extracting(
                NotificationRequest::getName,
                NotificationRequest::getNotificationEmail,
                NotificationRequest::getSolicitorReferenceNumber)
            .contains(TEST_SOLICITOR_NAME, TEST_SOLICITOR_EMAIL, ofNullable(solicitorReferenceNumber).orElse(""));
        verifyNoLetterSent();
    }

    @Test
    void shouldNotSendEmailNotificationIfApplicantSolicitorEmailIsNotPresent() {
        FinremCaseDetails caseDetailsBefore = mock(FinremCaseDetails.class);

        SendCorrespondenceEvent event = sendCorrespondenceEventWithTargetNotificationParty(caseDetailsBefore, mock(EmailTemplateNames.class));
        when(notificationService.isApplicantSolicitorEmailPopulatedAndPresented(caseDetailsBefore)).thenReturn(false);

        underTest.handleNotification(event);

        verifyNoInteractions(emailService);
        verifyNoLetterSent();
    }

    @Test
    void shouldNotSendEmailNotificationIfCaseDetailsBeforeIsAbsent() {
        FinremCaseDetails caseDetailsBefore = null;

        SendCorrespondenceEvent event = sendCorrespondenceEventWithTargetNotificationParty(caseDetailsBefore, mock(EmailTemplateNames.class));

        underTest.handleNotification(event);

        verifyNoInteractions(emailService);
        verifyNoLetterSent();
    }
}
