package uk.gov.hmcts.reform.finrem.caseorchestration;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.notifications.NotificationType;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SendCorrespondenceEventTest {

    private static final String CASE_ID = "1234567890";

    @Test
    void givenNullNotificationParties_whenGetNotificationParties_thenReturnsEmptyMutableList() {
        SendCorrespondenceEvent event = SendCorrespondenceEvent.builder().build();

        List<NotificationParty> notificationParties = event.getNotificationParties();

        assertThat(notificationParties).isEmpty();

        notificationParties.add(NotificationParty.APPLICANT);

        assertThat(event.getNotificationParties()).containsExactly(NotificationParty.APPLICANT);
    }

    @Test
    void givenEmailNotificationRecorded_whenGetNotificationTypeForParty_thenReturnsEmailAndIncrementsEmailCount() {
        SendCorrespondenceEvent event = SendCorrespondenceEvent.builder()
            .notificationParties(new ArrayList<>(List.of(NotificationParty.APPLICANT)))
            .build();

        event.recordEmailNotification(NotificationParty.APPLICANT);

        assertThat(event.getNotificationTypeForParty(NotificationParty.APPLICANT))
            .isEqualTo(NotificationType.EMAIL);
        assertThat(event.getEmailCount()).isEqualTo(1);
        assertThat(event.getLetterCount()).isZero();
        assertThat(event.getEmailOrLetters()).containsExactly(true);
    }

    @Test
    void givenPostalNotificationRecorded_whenGetNotificationTypeForParty_thenReturnsPostalAndIncrementsLetterCount() {
        SendCorrespondenceEvent event = SendCorrespondenceEvent.builder()
            .notificationParties(new ArrayList<>(List.of(NotificationParty.RESPONDENT)))
            .build();

        event.recordPostalNotification(NotificationParty.RESPONDENT);

        assertThat(event.getNotificationTypeForParty(NotificationParty.RESPONDENT))
            .isEqualTo(NotificationType.POSTAL);
        assertThat(event.getLetterCount()).isEqualTo(1);
        assertThat(event.getEmailCount()).isZero();
        assertThat(event.getEmailOrLetters()).containsExactly(false);
    }

    @Test
    void givenNotificationRecordedOutOfOrder_whenRecordNotification_thenStoresResultAtMatchingPartyIndex() {
        SendCorrespondenceEvent event = SendCorrespondenceEvent.builder()
            .notificationParties(new ArrayList<>(List.of(
                NotificationParty.APPLICANT,
                NotificationParty.RESPONDENT
            )))
            .build();

        event.recordEmailNotification(NotificationParty.RESPONDENT);

        assertThat(event.getEmailOrLetters()).containsExactly(null, true);
        assertThat(event.getNotificationTypeForParty(NotificationParty.RESPONDENT))
            .isEqualTo(NotificationType.EMAIL);

        event.recordPostalNotification(NotificationParty.APPLICANT);

        assertThat(event.getEmailOrLetters()).containsExactly(false, true);
        assertThat(event.getNotificationTypeForParty(NotificationParty.APPLICANT))
            .isEqualTo(NotificationType.POSTAL);
    }

    @Test
    void givenNullParty_whenRecordEmailNotification_thenThrowsException() {
        SendCorrespondenceEvent event = SendCorrespondenceEvent.builder()
            .notificationParties(new ArrayList<>(List.of(NotificationParty.APPLICANT)))
            .build();

        assertThatThrownBy(() -> event.recordEmailNotification(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Notification party is required to record notification type");
    }

    @Test
    void givenPartyWasNotRequested_whenRecordPostalNotification_thenThrowsException() {
        SendCorrespondenceEvent event = SendCorrespondenceEvent.builder()
            .notificationParties(new ArrayList<>(List.of(NotificationParty.APPLICANT)))
            .build();

        assertThatThrownBy(() -> event.recordPostalNotification(NotificationParty.RESPONDENT))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Notification party was not requested: RESPONDENT");
    }

    @Test
    void givenPartyWasNotRequested_whenGetNotificationTypeForParty_thenThrowsException() {
        SendCorrespondenceEvent event = SendCorrespondenceEvent.builder()
            .notificationParties(new ArrayList<>(List.of(NotificationParty.APPLICANT)))
            .build();

        assertThatThrownBy(() -> event.getNotificationTypeForParty(NotificationParty.RESPONDENT))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Notification party was not requested: RESPONDENT");
    }

    @Test
    void givenNoNotificationTypeRecorded_whenGetNotificationTypeForParty_thenThrowsException() {
        SendCorrespondenceEvent event = SendCorrespondenceEvent.builder()
            .notificationParties(new ArrayList<>(List.of(NotificationParty.APPLICANT)))
            .build();

        assertThatThrownBy(() -> event.getNotificationTypeForParty(NotificationParty.APPLICANT))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("No notification type recorded for party: APPLICANT");
    }

    @Test
    void givenCaseDetails_whenGetCaseDataAndCaseId_thenReturnsCaseDataAndCaseId() {
        FinremCaseData caseData = FinremCaseData.builder()
            .ccdCaseId(CASE_ID)
            .build();

        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .data(caseData)
            .build();

        SendCorrespondenceEvent event = SendCorrespondenceEvent.builder()
            .caseDetails(caseDetails)
            .build();

        assertThat(event.getCaseData()).isSameAs(caseData);
        assertThat(event.getCaseId()).isEqualTo(CASE_ID);
    }

    @Test
    void givenCaseDetailsBefore_whenGetCaseDataBefore_thenReturnsPreviousCaseData() {
        FinremCaseData caseDataBefore = FinremCaseData.builder()
            .ccdCaseId(CASE_ID)
            .build();

        FinremCaseDetails caseDetailsBefore = FinremCaseDetails.builder()
            .data(caseDataBefore)
            .build();

        SendCorrespondenceEvent event = SendCorrespondenceEvent.builder()
            .caseDetailsBefore(caseDetailsBefore)
            .build();

        assertThat(event.getCaseDataBefore()).isSameAs(caseDataBefore);
    }

    @Test
    void givenNoCaseDetails_whenGetCaseDataAndCaseId_thenReturnsNull() {
        SendCorrespondenceEvent event = SendCorrespondenceEvent.builder().build();

        assertThat(event.getCaseData()).isNull();
        assertThat(event.getCaseDataBefore()).isNull();
        assertThat(event.getCaseId()).isNull();
    }

    @Test
    void givenNotificationParties_whenDescribeNotificationParties_thenReturnsSortedHumanReadableDescription() {
        SendCorrespondenceEvent event = SendCorrespondenceEvent.builder()
            .notificationParties(new ArrayList<>(List.of(
                NotificationParty.RESPONDENT,
                NotificationParty.APPLICANT
            )))
            .build();

        assertThat(event.describeNotificationParties())
            .isEqualTo("applicant and respondent");
    }

    @Test
    void givenIntervenerNotificationParties_whenDescribeNotificationParties_thenReturnsHumanReadableLabels() {
        SendCorrespondenceEvent event = SendCorrespondenceEvent.builder()
            .notificationParties(new ArrayList<>(List.of(
                NotificationParty.INTERVENER_TWO,
                NotificationParty.INTERVENER_ONE
            )))
            .build();

        assertThat(event.describeNotificationParties())
            .isEqualTo("intervener 1 and intervener 2");
    }

    @Test
    void givenAllNotificationParties_whenDescribeNotificationParties_thenReturnsAllHumanReadableLabels() {
        SendCorrespondenceEvent event = SendCorrespondenceEvent.builder()
            .notificationParties(new ArrayList<>(List.of(
                NotificationParty.INTERVENER_FOUR,
                NotificationParty.RESPONDENT,
                NotificationParty.INTERVENER_THREE,
                NotificationParty.APPLICANT,
                NotificationParty.INTERVENER_TWO,
                NotificationParty.INTERVENER_ONE
            )))
            .build();

        assertThat(event.describeNotificationParties())
            .isEqualTo("applicant, intervener 1, intervener 2, intervener 3, intervener 4, and respondent");
    }

    @Test
    void givenDryRunFlagUpdated_whenIsDryRun_thenReturnsUpdatedValue() {
        SendCorrespondenceEvent event = SendCorrespondenceEvent.builder().build();

        assertThat(event.isDryRun()).isFalse();

        event.setDryRun(true);

        assertThat(event.isDryRun()).isTrue();

        event.setDryRun(false);

        assertThat(event.isDryRun()).isFalse();
    }
}
