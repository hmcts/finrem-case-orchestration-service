package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
}
