package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_INTV_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_INTV_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.FORMER_INTERVENER_FOUR_SOLICITOR_ONLY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.FORMER_INTERVENER_ONE_SOLICITOR_ONLY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.FORMER_INTERVENER_THREE_SOLICITOR_ONLY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.FORMER_INTERVENER_TWO_SOLICITOR_ONLY;

class FormerIntervenerSolicitorListenerTest extends BasePartyListenerTest {

    @InjectMocks
    private FormerIntervenerSolicitorListener underTest;

    FormerIntervenerSolicitorListenerTest() {
        super(FORMER_INTERVENER_ONE_SOLICITOR_ONLY, FORMER_INTERVENER_TWO_SOLICITOR_ONLY,
            FORMER_INTERVENER_THREE_SOLICITOR_ONLY, FORMER_INTERVENER_FOUR_SOLICITOR_ONLY);
    }

    @ParameterizedTest
    @EnumSource(value = NotificationParty.class, mode = EnumSource.Mode.EXCLUDE, names = {
        "FORMER_INTERVENER_ONE_SOLICITOR_ONLY", "FORMER_INTERVENER_TWO_SOLICITOR_ONLY",
        "FORMER_INTERVENER_THREE_SOLICITOR_ONLY", "FORMER_INTERVENER_FOUR_SOLICITOR_ONLY"
    })
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
        IntervenerOne intervenerOne = IntervenerOne.builder()
            .intervenerRepresented(YesOrNo.YES)
            .intervenerSolEmail(TEST_INTV_SOLICITOR_EMAIL)
            .build();

        FinremCaseData caseDataBefore = mock(FinremCaseData.class);
        when(caseDataBefore.getIntervenerById(any(Integer.class)))
            .thenReturn(intervenerOne);
        FinremCaseDetails caseDetailsBefore = mock(FinremCaseDetails.class);
        when(caseDetailsBefore.getData()).thenReturn(caseDataBefore);

        EmailTemplateNames emailTemplate = mock(EmailTemplateNames.class);

        SendCorrespondenceEvent event = sendCorrespondenceEventWithTargetNotificationParty(caseDetailsBefore, emailTemplate,
            solicitorReferenceNumber, IntervenerType.INTERVENER_ONE);

        underTest.handleNotification(event);

        ArgumentCaptor<NotificationRequest> notificationRequestArgumentCaptor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(emailService).sendConfirmationEmail(notificationRequestArgumentCaptor.capture(), eq(emailTemplate));
        assertThat(notificationRequestArgumentCaptor.getValue())
            .extracting(
                NotificationRequest::getName,
                NotificationRequest::getNotificationEmail,
                NotificationRequest::getSolicitorReferenceNumber)
            .contains(event.getEmailNotificationRequest().getName(), event.getEmailNotificationRequest().getNotificationEmail(),
                solicitorReferenceNumber);
        verifyNoLetterSent();
    }

    @Test
    void shouldNotSendEmailNotificationIfIntervenerSolicitorEmailIsNotPresent() {
        IntervenerOne intervenerOne = IntervenerOne.builder()
            .intervenerRepresented(YesOrNo.YES)
            .intervenerSolEmail(null)
            .build();

        FinremCaseData caseDataBefore = mock(FinremCaseData.class);
        when(caseDataBefore.getIntervenerById(any(Integer.class)))
            .thenReturn(intervenerOne);
        FinremCaseDetails caseDetailsBefore = mock(FinremCaseDetails.class);
        when(caseDetailsBefore.getData()).thenReturn(caseDataBefore);

        EmailTemplateNames emailTemplate = mock(EmailTemplateNames.class);

        SendCorrespondenceEvent event = sendCorrespondenceEventWithTargetNotificationParty(caseDetailsBefore, emailTemplate,
            TEST_INTV_SOLICITOR_REFERENCE, IntervenerType.INTERVENER_ONE);

        underTest.handleNotification(event);

        verifyNoInteractions(emailService);
        verifyNoLetterSent();
    }

    @Test
    void shouldNotSendEmailNotificationIfIntervenerNotRepresented() {
        IntervenerOne intervenerOne = IntervenerOne.builder()
            .intervenerRepresented(YesOrNo.NO)
            .intervenerSolEmail(null)
            .build();

        FinremCaseData caseDataBefore = mock(FinremCaseData.class);
        when(caseDataBefore.getIntervenerById(any(Integer.class)))
            .thenReturn(intervenerOne);
        FinremCaseDetails caseDetailsBefore = mock(FinremCaseDetails.class);
        when(caseDetailsBefore.getData()).thenReturn(caseDataBefore);

        EmailTemplateNames emailTemplate = mock(EmailTemplateNames.class);

        SendCorrespondenceEvent event = sendCorrespondenceEventWithTargetNotificationParty(caseDetailsBefore, emailTemplate,
            TEST_INTV_SOLICITOR_REFERENCE, IntervenerType.INTERVENER_ONE);

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
