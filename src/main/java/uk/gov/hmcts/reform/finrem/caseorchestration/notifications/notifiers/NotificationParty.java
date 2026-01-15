package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Getter
public enum NotificationParty {
    APPLICANT("[APPSOLICITOR]", true),
    RESPONDENT("[RESPSOLICITOR]", true),
    INTERVENER_ONE("[INTVRSOLICITOR1]", true),
    INTERVENER_TWO("[INTVRSOLICITOR2]", true),
    INTERVENER_THREE("[INTVRSOLICITOR3]", true),
    INTERVENER_FOUR("[INTVRSOLICITOR4]", true),
    APPLICANT_SOLICITOR_ONLY(CaseRole.APP_SOLICITOR.getCcdCode(), false),
    APPLICANT_BARRISTER_ONLY(CaseRole.APP_BARRISTER.getCcdCode(), false);

    private final String role;
    private final boolean notifyRepresented;

    public static NotificationParty getNotificationPartyFromRole(String role) {
        for (NotificationParty party : NotificationParty.values()) {
            if (party.getRole().equals(role)) {
                return party;
            }
        }
        return null;
    }

    public static Optional<NotificationParty> getNotificationParty(
        CaseRole caseRole, boolean notifyRepresented) {

        return Optional.ofNullable(caseRole)
            .map(CaseRole::getCcdCode)
            .map(LOOKUP::get)
            .map(map -> map.get(notifyRepresented));
    }

    private static final Map<String, Map<Boolean, NotificationParty>> LOOKUP =
        Arrays.stream(NotificationParty.values())
            .collect(Collectors.groupingBy(
                NotificationParty::getRole,
                Collectors.toMap(NotificationParty::isNotifyRepresented, Function.identity())
            ));
}
