package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum NotificationParty {
    APPLICANT("[APPSOLICITOR]"),
    RESPONDENT("[RESPSOLICITOR]"),
    INTERVENER_ONE("[INTVRSOLICITOR1]"),
    INTERVENER_TWO("[INTVRSOLICITOR2]"),
    INTERVENER_THREE("[INTVRSOLICITOR3]"),
    INTERVENER_FOUR("[INTVRSOLICITOR4]");

    @Getter
    private final String role;

    public static NotificationParty getNotificationPartyFromRole(String role) {
        for (NotificationParty party : NotificationParty.values()) {
            if (party.getRole().equals(role)) {
                return party;
            }
        }
        return null;
    }
}
