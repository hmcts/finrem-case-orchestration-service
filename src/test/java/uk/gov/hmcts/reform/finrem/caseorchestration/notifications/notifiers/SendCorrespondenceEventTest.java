package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.INTERVENER_THREE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.RESPONDENT;

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

    @Test
    void givenEmailNotificationToSendAuditRecorded_thenAddsEmailAudit() {
        SendCorrespondenceEvent event = SendCorrespondenceEvent.builder()
            .emailTemplate(EmailTemplateNames.values()[0])
            .build();

        event.recordEmailNotificationToSendAudit(NotificationParty.APPLICANT);

        NotificationAudit audit = event.getNotificationAudits().getFirst();

        assertThat(audit.getParty()).isEqualTo(NotificationParty.APPLICANT.name());
        assertThat(audit.getType()).isEqualTo(NotificationType.EMAIL);
        assertThat(audit.getWasSent()).isNull();
    }

    @Test
    void givenEmailNotificationSentAuditRecorded_thenAddsSentEmailAudit() {
        SendCorrespondenceEvent event = SendCorrespondenceEvent.builder()
            .emailTemplate(EmailTemplateNames.values()[0])
            .build();

        event.recordEmailNotificationSentAudit(NotificationParty.APPLICANT);

        NotificationAudit audit = event.getNotificationAudits().getFirst();

        assertThat(audit.getParty()).isEqualTo(NotificationParty.APPLICANT.name());
        assertThat(audit.getType()).isEqualTo(NotificationType.EMAIL);
        assertThat(audit.getWasSent()).isEqualTo(YesOrNo.YES);
    }

    @Test
    void givenPostalNotificationToSendAuditRecorded_thenAddsPostalAudit() {
        SendCorrespondenceEvent event = SendCorrespondenceEvent.builder().build();

        event.recordPostalNotificationToSendAudit(NotificationParty.RESPONDENT);

        NotificationAudit audit = event.getNotificationAudits().getFirst();

        assertThat(audit.getParty()).isEqualTo(NotificationParty.RESPONDENT.name());
        assertThat(audit.getType()).isEqualTo(NotificationType.POSTAL);
        assertThat(audit.getWasSent()).isNull();
    }

    @Test
    void givenPostalNotificationSentAuditRecorded_thenAddsSentPostalAudit() {
        UUID letterId = UUID.randomUUID();
        SendCorrespondenceEvent event = SendCorrespondenceEvent.builder().build();

        event.recordPostalNotificationSentAudit(NotificationParty.RESPONDENT, letterId);

        NotificationAudit audit = event.getNotificationAudits().getFirst();

        assertThat(audit.getParty()).isEqualTo(NotificationParty.RESPONDENT.name());
        assertThat(audit.getType()).isEqualTo(NotificationType.POSTAL);
        assertThat(audit.getWasSent()).isEqualTo(YesOrNo.YES);
        assertThat(audit.getLetterId()).isEqualTo(letterId.toString());
    }
}
