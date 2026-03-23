package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.FORMER_APPLICANT_BARRISTER_ONLY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.FORMER_APPLICANT_SOLICITOR_ONLY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.getNotificationParty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotifyFormerParty.NOTIFY_FORMER_PARTY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotifyRepresented.DO_NOT_NOTIFY_REPRESENTED;

class NotificationPartyTest {

    @Test
    void shouldReturnNotificationPartyForValidRole() {
        assertEquals(NotificationParty.APPLICANT, NotificationParty.getNotificationPartyFromRole("[APPSOLICITOR]"));
        assertEquals(NotificationParty.RESPONDENT, NotificationParty.getNotificationPartyFromRole("[RESPSOLICITOR]"));
    }

    @Test
    void shouldReturnNullForInvalidRole() {
        assertNull(NotificationParty.getNotificationPartyFromRole("INVALID_ROLE"));
    }

    @Test
    void shouldBeCaseSensitive() {
        assertNull(NotificationParty.getNotificationPartyFromRole("[appsolicitor]"));
        assertEquals(NotificationParty.APPLICANT, NotificationParty.getNotificationPartyFromRole("[APPSOLICITOR]"));
    }

    @Test
    void shouldReturnFormerNotificationParties() {
        assertEquals(Optional.of(FORMER_APPLICANT_SOLICITOR_ONLY),
            getNotificationParty(CaseRole.APP_SOLICITOR, DO_NOT_NOTIFY_REPRESENTED, NOTIFY_FORMER_PARTY));
        assertEquals(Optional.of(FORMER_APPLICANT_BARRISTER_ONLY),
            getNotificationParty(CaseRole.APP_BARRISTER, DO_NOT_NOTIFY_REPRESENTED, NOTIFY_FORMER_PARTY));
    }

    @ParameterizedTest
    @EnumSource(IntervenerType.class) // Runs for all IntervenerType values
    void shouldReturnCorrectFormerIntervenerSolicitor(IntervenerType intervenerType) {
        // Act
        NotificationParty result = NotificationParty.getFormerIntervenerSolicitor(intervenerType);

        // Assert
        switch (intervenerType) {
            case INTERVENER_ONE -> assertThat(result).isEqualTo(NotificationParty.FORMER_INTERVENER_ONE_SOLICITOR_ONLY);
            case INTERVENER_TWO -> assertThat(result).isEqualTo(NotificationParty.FORMER_INTERVENER_TWO_SOLICITOR_ONLY);
            case INTERVENER_THREE -> assertThat(result).isEqualTo(NotificationParty.FORMER_INTERVENER_THREE_SOLICITOR_ONLY);
            case INTERVENER_FOUR -> assertThat(result).isEqualTo(NotificationParty.FORMER_INTERVENER_FOUR_SOLICITOR_ONLY);
            default ->
                throw new IllegalStateException("Unexpected intervener type: " + intervenerType);
        }
    }

    @ParameterizedTest
    @EnumSource(IntervenerType.class)
    void shouldReturnCorrectFormerIntervenerBarrister(IntervenerType intervenerType) {
        // Act
        NotificationParty result = NotificationParty.getFormerIntervenerBarrister(intervenerType);

        // Assert
        switch (intervenerType) {
            case INTERVENER_ONE -> assertThat(result)
                .isEqualTo(NotificationParty.FORMER_INTERVENER_ONE_BARRISTER_ONLY);
            case INTERVENER_TWO -> assertThat(result)
                .isEqualTo(NotificationParty.FORMER_INTERVENER_TWO_BARRISTER_ONLY);
            case INTERVENER_THREE -> assertThat(result)
                .isEqualTo(NotificationParty.FORMER_INTERVENER_THREE_BARRISTER_ONLY);
            case INTERVENER_FOUR -> assertThat(result)
                .isEqualTo(NotificationParty.FORMER_INTERVENER_FOUR_BARRISTER_ONLY);
            default ->
                throw new IllegalStateException("Unexpected intervener type: " + intervenerType);
        }
    }

}
