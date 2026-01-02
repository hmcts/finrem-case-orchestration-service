package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum NotificationParty {
    APPLICANT("[APPSOLICITOR]"),
    RESPONDENT("[RESPSOLICITOR]"),
    INTERVENER_1("[INTVRSOLICITOR1]"),
    INTERVENER_2("[INTVRSOLICITOR2]"),
    INTERVENER_3("[INTVRSOLICITOR3]"),
    INTERVENER_4("[INTVRSOLICITOR4]");

    private final String role;

    public String getRoles() {
        return role;
    }

    public static NotificationParty getNotificationPartyFromRole(String role) {
        for (NotificationParty party : NotificationParty.values()) {
            if (party.getRoles().equals(role)) {
                return party;
            }
        }
        return null;
    }
}
