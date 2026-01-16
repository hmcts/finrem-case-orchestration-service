package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;

import java.util.Arrays;
import java.util.Optional;

@RequiredArgsConstructor
@Getter
public enum NotificationParty {
    APPLICANT("[APPSOLICITOR]", true, false),
    RESPONDENT("[RESPSOLICITOR]", true, false),
    INTERVENER_ONE("[INTVRSOLICITOR1]", true, false),
    INTERVENER_TWO("[INTVRSOLICITOR2]", true, false),
    INTERVENER_THREE("[INTVRSOLICITOR3]", true, false),
    INTERVENER_FOUR("[INTVRSOLICITOR4]", true, false),
    PREVIOUS_APPLICANT_SOLICITOR_ONLY(CaseRole.APP_SOLICITOR.getCcdCode(), false, true),
    PREVIOUS_APPLICANT_BARRISTER_ONLY(CaseRole.APP_BARRISTER.getCcdCode(), false, true);

    private final String role;
    private final boolean notifyRepresented;
    private final boolean historical;

    public static NotificationParty getNotificationPartyFromRole(String role) {
        return Arrays.stream(NotificationParty.values())
            .filter(party -> party.getRole().equals(role))
            .filter(NotificationParty::isNotifyRepresented)
            .filter(party -> !party.isHistorical())
            .findFirst()
            .orElse(null);
    }

    public static Optional<NotificationParty> getNotificationParty(
        CaseRole caseRole, boolean notifyRepresented, boolean historical) {

        return Arrays.stream(NotificationParty.values())
            .filter(party -> party.getRole().equals(caseRole.getCcdCode()))
            .filter(party -> party.isNotifyRepresented() == notifyRepresented)
            .filter(party -> party.isHistorical() == historical)
            .findFirst();
    }
}
