package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.PREVIOUS_APPLICANT_BARRISTER_ONLY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.PREVIOUS_APPLICANT_SOLICITOR_ONLY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.getNotificationParty;

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
    void shouldReturnPreviousApplicantSolicitorOnly() {
        assertEquals(Optional.of(PREVIOUS_APPLICANT_SOLICITOR_ONLY), getNotificationParty(CaseRole.APP_SOLICITOR, false, true));
        assertEquals(Optional.of(PREVIOUS_APPLICANT_BARRISTER_ONLY), getNotificationParty(CaseRole.APP_BARRISTER, false, true));
    }
}
