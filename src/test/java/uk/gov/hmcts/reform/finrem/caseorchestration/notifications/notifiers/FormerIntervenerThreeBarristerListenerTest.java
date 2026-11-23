package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.BarristerCollectionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_INTV_BARRISTER_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_INTV_BARRISTER_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_INTV_BARRISTER_USER_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_INTV_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_ORG_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.barrister;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.barristers;

class FormerIntervenerThreeBarristerListenerTest extends BasePartyListenerTest {

    @InjectMocks
    private FormerIntervenerThreeBarristerListener underTest;

    private static SendCorrespondenceEvent.SendCorrespondenceEventBuilder notifyingIntervenerBarristerEvent(
        Barrister barrister) {
        return SendCorrespondenceEvent.builder()
            .caseDetailsBefore(FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .barristerCollectionWrapper(BarristerCollectionWrapper.builder()
                        .intvr3Barristers(List.of(
                            BarristerCollectionItem.builder().value(barrister).build()
                        ))
                        .build())
                    .build())
                .build());
    }

    @ParameterizedTest
    @EnumSource(value = NotificationParty.class, mode = EnumSource.Mode.EXCLUDE, names = {
        "FORMER_INTERVENER_THREE_BARRISTER_ONLY"
    })
    void shouldNotHandleIrrelevantNotificationParty(NotificationParty notificationParty) {
        Barrister barrister = mock(Barrister.class);

        SendCorrespondenceEvent otherEvent = notifyingIntervenerBarristerEvent(barrister)
            .notificationParties(List.of(notificationParty))
            .barrister(barrister)
            .build();

        underTest.handleNotification(otherEvent);
        verifyNoInteractions(emailService, notificationService);
    }

    @ParameterizedTest
    @ValueSource(strings = {TEST_SOLICITOR_REFERENCE})
    @NullAndEmptySource
    void shouldSendEmailNotification(String solicitorReferenceNumber) {
        FinremCaseData caseDataBefore = FinremCaseData.builder()
            .barristerCollectionWrapper(BarristerCollectionWrapper.builder()
                .intvr3Barristers(
                    barristers(TEST_ORG_ID, TEST_INTV_BARRISTER_USER_ID, TEST_INTV_BARRISTER_NAME, TEST_INTV_BARRISTER_EMAIL)
                )
                .build())
            .build();

        FinremCaseDetails caseDetailsBefore = mock(FinremCaseDetails.class);
        when(caseDetailsBefore.getData()).thenReturn(caseDataBefore);

        EmailTemplateNames emailTemplate = mock(EmailTemplateNames.class);

        SendCorrespondenceEvent event = sendCorrespondenceEventWithTargetNotificationParty(mock(FinremCaseDetails.class),
            caseDetailsBefore, emailTemplate, solicitorReferenceNumber)
            .toBuilder()
            .barrister(barrister(TEST_ORG_ID, TEST_INTV_BARRISTER_USER_ID, TEST_INTV_BARRISTER_NAME, TEST_INTV_BARRISTER_EMAIL))
            .build();

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
    void shouldNotSendEmailNotificationIfCaseDetailsBeforeIsAbsent() {
        FinremCaseDetails caseDetailsBefore = null;

        SendCorrespondenceEvent event = sendCorrespondenceEventWithTargetNotificationParty(mock(FinremCaseDetails.class),
            caseDetailsBefore, mock(EmailTemplateNames.class));

        underTest.handleNotification(event);

        verifyNoInteractions(emailService);
        verifyNoLetterSent();
    }

    @Test
    void shouldNotSendEmailNotificationIfBarristerNotFound() {
        FinremCaseData caseDataBefore = FinremCaseData.builder()
            .barristerCollectionWrapper(BarristerCollectionWrapper.builder()
                .intvr3Barristers(
                    barristers(TEST_ORG_ID, "whatever", TEST_INTV_BARRISTER_NAME, TEST_INTV_BARRISTER_EMAIL)
                )
                .build())
            .build();

        FinremCaseDetails caseDetailsBefore = mock(FinremCaseDetails.class);
        when(caseDetailsBefore.getData()).thenReturn(caseDataBefore);

        EmailTemplateNames emailTemplate = mock(EmailTemplateNames.class);

        SendCorrespondenceEvent event = sendCorrespondenceEventWithTargetNotificationParty(mock(FinremCaseDetails.class),
            caseDetailsBefore, emailTemplate, TEST_INTV_SOLICITOR_REFERENCE)
            .toBuilder()
            .barrister(barrister(TEST_ORG_ID, TEST_INTV_BARRISTER_USER_ID, TEST_INTV_BARRISTER_NAME, TEST_INTV_BARRISTER_EMAIL))
            .build();

        underTest.handleNotification(event);

        verifyNoInteractions(emailService);
        verifyNoLetterSent();
    }
}
