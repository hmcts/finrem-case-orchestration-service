package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.notifications.NotificationAudit;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.notifications.NotificationType;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.INTERVENER_THREE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent.isApplicantAddressRequired;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent.isRespondentAddressRequired;

class SendCorrespondenceEventTest {

    @Nested
    class DescribeNotificationParties {

        @ParameterizedTest
        @CsvSource({
            "APPLICANT,applicant",
            "RESPONDENT,respondent",
            "INTERVENER_ONE,intervener 1",
            "INTERVENER_TWO,intervener 2",
            "INTERVENER_THREE,intervener 3",
            "INTERVENER_FOUR,intervener 4",
        })
        void shouldDescribeSingleNotificationParties(NotificationParty notificationParty, String expectedValue) {
            SendCorrespondenceEvent event = SendCorrespondenceEvent.builder()
                .notificationParties(List.of(notificationParty))
                .build();
            assertEquals(expectedValue, event.describeNotificationParties());
        }

        static Stream<Arguments> shouldDescribeMultipleNotificationParties() {
            return Stream.of(
                Arguments.of(List.of(APPLICANT, RESPONDENT), "applicant and respondent"),
                Arguments.of(List.of(RESPONDENT, APPLICANT), "applicant and respondent"),
                Arguments.of(List.of(RESPONDENT, APPLICANT, INTERVENER_ONE), "applicant, intervener 1, and respondent"),
                Arguments.of(List.of(RESPONDENT, INTERVENER_THREE, APPLICANT, INTERVENER_ONE),
                    "applicant, intervener 1, intervener 3, and respondent")
            );
        }

        @ParameterizedTest
        @MethodSource
        void shouldDescribeMultipleNotificationParties(List<NotificationParty> notificationParties, String expectedValue) {
            SendCorrespondenceEvent event = SendCorrespondenceEvent.builder()
                .notificationParties(notificationParties)
                .build();
            assertEquals(expectedValue, event.describeNotificationParties());
        }
    }

    @Nested
    class LetterNotificationOnly {

        @Test
        void shouldReturnFalseByDefault() {
            SendCorrespondenceEvent event = SendCorrespondenceEvent.builder().build();
            assertFalse(event.isLetterNotificationOnly());
        }
    }

    @Nested
    class GetNotificationParties {

        @Test
        void shouldReturnEmptyListIfNotificationPartiesIsNull() {
            SendCorrespondenceEvent event = SendCorrespondenceEvent.builder().build();
            assertThat(event.getNotificationParties()).isEmpty();
        }

        @Test
        void shouldReturnProvidedListIfNotificationPartiesIsNotNull() {
            List<NotificationParty> mockedNotificationParties = mock(List.class);
            SendCorrespondenceEvent event = SendCorrespondenceEvent.builder()
                .notificationParties(mockedNotificationParties)
                .build();
            assertEquals(mockedNotificationParties, event.getNotificationParties());
        }
    }

    @Nested
    class GetCaseDataBeforeTest {

        @Test
        void shouldReturnCaseDataIfCaseDataBeforeIsNotNull() {
            FinremCaseData finremCaseDataBefore = mock(FinremCaseData.class);
            FinremCaseDetails finremCaseDetailsBefore = mock(FinremCaseDetails.class);
            when(finremCaseDetailsBefore.getData()).thenReturn(finremCaseDataBefore);

            SendCorrespondenceEvent event = SendCorrespondenceEvent.builder()
                .caseDetailsBefore(finremCaseDetailsBefore)
                .build();

            assertEquals(finremCaseDataBefore, event.getCaseDataBefore());
        }

        @Test
        void shouldReturnNullIfCaseDataBeforeIsNull() {
            FinremCaseDetails finremCaseDetailsBefore = mock(FinremCaseDetails.class);
            when(finremCaseDetailsBefore.getData()).thenReturn(null);

            SendCorrespondenceEvent event = SendCorrespondenceEvent.builder()
                .caseDetailsBefore(finremCaseDetailsBefore)
                .build();

            assertNull(event.getCaseDataBefore());
        }
    }

    @Nested
    class GetCaseDataTest {

        @Test
        void shouldReturnCaseDataIfCaseDataBeforeIsNotNull() {
            FinremCaseData finremCaseData = mock(FinremCaseData.class);
            FinremCaseDetails finremCaseDetails = mock(FinremCaseDetails.class);
            when(finremCaseDetails.getData()).thenReturn(finremCaseData);

            SendCorrespondenceEvent event = SendCorrespondenceEvent.builder()
                .caseDetails(finremCaseDetails)
                .build();

            assertEquals(finremCaseData, event.getCaseData());
        }

        @Test
        void shouldReturnNullIfCaseDataBeforeIsNull() {
            FinremCaseDetails finremCaseDetails = mock(FinremCaseDetails.class);
            when(finremCaseDetails.getData()).thenReturn(null);

            SendCorrespondenceEvent event = SendCorrespondenceEvent.builder()
                .caseDetailsBefore(finremCaseDetails)
                .build();

            assertNull(event.getCaseData());
        }
    }

    @Nested
    class IsPartyAddressRequiredTests {
        // ---------- applicant ----------

        @Test
        void shouldReturnTrueWhenApplicantHasPostalAudit() {
            var events = List.of(event(List.of(NotificationParty.APPLICANT), audit(NotificationType.POSTAL)));

            assertThat(isApplicantAddressRequired(events)).isTrue();
        }

        @Test
        void shouldReturnFalseForApplicantWhenOnlyRespondentHasPostalAudit() {
            var events = List.of(event(List.of(NotificationParty.RESPONDENT), audit(NotificationType.POSTAL)));

            assertThat(isApplicantAddressRequired(events)).isFalse();
        }

        // ---------- respondent ----------

        @Test
        void shouldReturnTrueWhenRespondentHasPostalAudit() {
            var events = List.of(event(List.of(NotificationParty.RESPONDENT), audit(NotificationType.POSTAL)));

            assertThat(isRespondentAddressRequired(events)).isTrue();
        }

        @Test
        void shouldReturnFalseForRespondentWhenOnlyApplicantHasPostalAudit() {
            var events = List.of(event(List.of(NotificationParty.APPLICANT), audit(NotificationType.POSTAL)));

            assertThat(isRespondentAddressRequired(events)).isFalse();
        }

        // ---------- shared behaviour (both parties) ----------

        @Test
        void shouldReturnFalseWhenEventListIsEmpty() {
            assertThat(isApplicantAddressRequired(List.of())).isFalse();
            assertThat(isRespondentAddressRequired(List.of())).isFalse();
        }

        @Test
        void shouldThrowNullPointerExceptionWhenEventListIsNull() {
            // Documents current behaviour; remove or change if you add null handling
            assertThatThrownBy(() -> isApplicantAddressRequired(null))
                .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> isRespondentAddressRequired(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        void shouldReturnTrueWhenEventNotifiesBothPartiesWithPostalAudit() {
            var events = List.of(event(
                List.of(NotificationParty.APPLICANT, NotificationParty.RESPONDENT),
                audit(NotificationType.POSTAL)));

            assertThat(isApplicantAddressRequired(events)).isTrue();
            assertThat(isRespondentAddressRequired(events)).isTrue();
        }

        @Test
        void shouldReturnTrueWhenAnyOneOfSeveralEventsMatches() {
            var events = List.of(
                event(List.of(NotificationParty.APPLICANT), audit(NotificationType.EMAIL)),
                event(List.of(NotificationParty.RESPONDENT), audit(NotificationType.POSTAL)),
                event(List.of(NotificationParty.APPLICANT)));

            assertThat(isRespondentAddressRequired(events)).isTrue();
            assertThat(isApplicantAddressRequired(events)).isFalse();
        }

        @Test
        void shouldReturnTrueWhenPostalAuditIsAmongMixedAudits() {
            var events = List.of(event(
                List.of(NotificationParty.APPLICANT),
                audit(NotificationType.EMAIL),
                audit(NotificationType.POSTAL)));

            assertThat(isApplicantAddressRequired(events)).isTrue();
        }

        @Test
        void shouldReturnFalseWhenPartyMatchesButEventHasNoAudits() {
            var events = List.of(event(List.of(NotificationParty.APPLICANT)));

            assertThat(isApplicantAddressRequired(events)).isFalse();
        }

        @Test
        void shouldReturnFalseWhenEventHasNoNotificationParties() {
            var events = List.of(event(List.of(), audit(NotificationType.POSTAL)));

            assertThat(isApplicantAddressRequired(events)).isFalse();
            assertThat(isRespondentAddressRequired(events)).isFalse();
        }

        @ParameterizedTest
        @EnumSource(value = NotificationType.class, names = "POSTAL", mode = EnumSource.Mode.EXCLUDE)
        void shouldReturnFalseWhenAuditsAreNotPostal(NotificationType type) {
            var events = List.of(event(
                List.of(NotificationParty.APPLICANT, NotificationParty.RESPONDENT),
                audit(type)));

            assertThat(isApplicantAddressRequired(events)).isFalse();
            assertThat(isRespondentAddressRequired(events)).isFalse();
        }

        // ---------- helpers (adapt to your model) ----------

        private static SendCorrespondenceEvent event(List<NotificationParty> parties, NotificationAudit... audits) {
            return SendCorrespondenceEvent.builder()
                .notificationParties(parties)
                .audits(List.of(audits))
                .build();
        }

        private static NotificationAudit audit(NotificationType type) {
            return NotificationAudit.builder().type(type).build();
        }
    }

    @ParameterizedTest
    @EnumSource(NotificationParty.class)
    void givenEmailNotificationToSendAuditRecorded_thenAddsEmailAudit(NotificationParty notificationParty) {
        EmailTemplateNames emailTemplate = EmailTemplateNames.values()[0];
        String eventId = EventType.MANAGE_HEARINGS.getCcdType();
        SendCorrespondenceEvent event = SendCorrespondenceEvent.builder()
            .emailTemplate(emailTemplate)
            .build();
        event.setEventId(eventId);
        event.recordEmailNotificationToSendAudit(notificationParty);

        assertThat(event.getAudits()).hasSize(1);

        NotificationAudit audit = event.getAudits().getFirst();

        assertCommonAuditFields(audit, notificationParty, NotificationType.EMAIL, YesOrNo.NO, eventId);
        assertThat(audit.getEmailTemplate()).isEqualTo(emailTemplate.name());
    }

    @ParameterizedTest
    @EnumSource(NotificationParty.class)
    void givenEmailNotificationSentAuditRecorded_thenAddsSentEmailAudit(NotificationParty notificationParty) {
        EmailTemplateNames emailTemplate = EmailTemplateNames.values()[0];
        String eventId = EventType.MANAGE_HEARINGS.getCcdType();
        SendCorrespondenceEvent event = SendCorrespondenceEvent.builder()
            .emailTemplate(emailTemplate)
            .build();
        event.setEventId(eventId);
        event.recordEmailNotificationSentAudit(notificationParty);

        assertThat(event.getAudits()).hasSize(1);

        NotificationAudit audit = event.getAudits().getFirst();

        assertCommonAuditFields(audit, notificationParty, NotificationType.EMAIL, YesOrNo.YES, eventId);
        assertThat(audit.getEmailTemplate()).isEqualTo(emailTemplate.name());
    }

    @ParameterizedTest
    @EnumSource(NotificationParty.class)
    void givenPostalNotificationToSendAuditRecorded_thenAddsPostalAudit(NotificationParty notificationParty) {
        String eventId = EventType.MANAGE_HEARINGS.getCcdType();
        SendCorrespondenceEvent event = SendCorrespondenceEvent.builder().build();
        event.setEventId(eventId);
        event.recordPostalNotificationToSendAudit(notificationParty);

        assertThat(event.getAudits()).hasSize(1);

        NotificationAudit audit = event.getAudits().getFirst();

        assertCommonAuditFields(audit, notificationParty, NotificationType.POSTAL, YesOrNo.NO, eventId);
    }

    @ParameterizedTest
    @EnumSource(NotificationParty.class)
    void givenPostalNotificationSentAuditRecorded_thenAddsSentPostalAudit(NotificationParty notificationParty) {
        String eventId = EventType.MANAGE_HEARINGS.getCcdType();
        UUID letterId = UUID.randomUUID();
        SendCorrespondenceEvent event = SendCorrespondenceEvent.builder().build();
        event.setEventId(eventId);
        event.recordPostalNotificationSentAudit(notificationParty, letterId);

        assertThat(event.getAudits()).hasSize(1);

        NotificationAudit audit = event.getAudits().getFirst();

        assertCommonAuditFields(audit, notificationParty, NotificationType.POSTAL, YesOrNo.YES, eventId);
        assertThat(audit.getLetterId()).isEqualTo(letterId.toString());
    }

    private void assertCommonAuditFields(NotificationAudit audit,
                                         NotificationParty notificationParty,
                                         NotificationType type,
                                         YesOrNo wasSent,
                                         String eventId) {
        assertThat(audit.getCreatedAt()).isNotNull();
        assertThat(audit.getWasSent()).isEqualTo(wasSent);
        assertThat(audit.getEventId()).isEqualTo(eventId);
        assertThat(audit.getParty()).isEqualTo(notificationParty.name());
        assertThat(audit.getType()).isEqualTo(type);
    }
}
