package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SendCorrespondenceEventTest {

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
}
