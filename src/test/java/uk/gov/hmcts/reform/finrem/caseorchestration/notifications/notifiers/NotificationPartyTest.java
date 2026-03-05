package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;

import java.util.Optional;

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
}
