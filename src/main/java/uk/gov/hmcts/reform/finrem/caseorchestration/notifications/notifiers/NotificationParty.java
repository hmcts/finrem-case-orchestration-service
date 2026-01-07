package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum NotificationParty {
    //TODO: Test if this works for unrecognized roles
    APPLICANT("[APPSOLICITOR]"),
    RESPONDENT("[RESPSOLICITOR]"),
    INTERVENER_ONE("[INTVRSOLICITOR1]"),
    INTERVENER_TWO("[INTVRSOLICITOR2]"),
    INTERVENER_THREE("[INTVRSOLICITOR3]"),
    INTERVENER_FOUR("[INTVRSOLICITOR4]");

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
