package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;

import java.util.Arrays;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotifyFormerParty.DO_NOT_NOTIFY_FORMER_PARTY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotifyFormerParty.NOTIFY_FORMER_PARTY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotifyRepresented.DO_NOT_NOTIFY_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotifyRepresented.NOTIFY_REPRESENTED;

@RequiredArgsConstructor
@Getter
public enum NotificationParty {
    // notify both applicant and applicant solicitor
    APPLICANT("[APPSOLICITOR]", NOTIFY_REPRESENTED, DO_NOT_NOTIFY_FORMER_PARTY,
        "the applicant or their legal representative"),
    // notify both respondent and respondent solicitor
    RESPONDENT("[RESPSOLICITOR]", NOTIFY_REPRESENTED, DO_NOT_NOTIFY_FORMER_PARTY,
        "the respondent or their legal representative"),
    // notify both intervener and intervener solicitor
    INTERVENER_ONE("[INTVRSOLICITOR1]", NOTIFY_REPRESENTED, DO_NOT_NOTIFY_FORMER_PARTY,
        "the intervener one or their legal representative"),
    INTERVENER_TWO("[INTVRSOLICITOR2]", NOTIFY_REPRESENTED, DO_NOT_NOTIFY_FORMER_PARTY,
        "the intervener two or their legal representative"),
    INTERVENER_THREE("[INTVRSOLICITOR3]", NOTIFY_REPRESENTED, DO_NOT_NOTIFY_FORMER_PARTY,
        "the intervener three or their legal representative"),
    INTERVENER_FOUR("[INTVRSOLICITOR4]", NOTIFY_REPRESENTED, DO_NOT_NOTIFY_FORMER_PARTY,
        "the intervener four or their legal representative"),
    // notify applicant solicitor only
    APPLICANT_SOLICITOR_ONLY(CaseRole.APP_SOLICITOR.getCcdCode(), DO_NOT_NOTIFY_REPRESENTED, DO_NOT_NOTIFY_FORMER_PARTY,
        "the applicant solicitor only"),
    // notify respondent solicitor only
    RESPONDENT_SOLICITOR_ONLY(CaseRole.RESP_SOLICITOR.getCcdCode(), DO_NOT_NOTIFY_REPRESENTED, DO_NOT_NOTIFY_FORMER_PARTY,
        "the respondent solicitor only"),
    // notify former applicant solicitor only
    FORMER_APPLICANT_SOLICITOR_ONLY(CaseRole.APP_SOLICITOR.getCcdCode(), DO_NOT_NOTIFY_REPRESENTED, NOTIFY_FORMER_PARTY,
        "the former applicant solicitor only"),
    // notify former applicant barristers only
    FORMER_APPLICANT_BARRISTER_ONLY(CaseRole.APP_BARRISTER.getCcdCode(), DO_NOT_NOTIFY_REPRESENTED, NOTIFY_FORMER_PARTY,
        "the former applicant barrister only"),
    // notify former respondent solicitor only
    FORMER_RESPONDENT_SOLICITOR_ONLY(CaseRole.RESP_SOLICITOR.getCcdCode(), DO_NOT_NOTIFY_REPRESENTED, NOTIFY_FORMER_PARTY,
        "the former respondent solicitor only"),
    // notify former respondent barristers only
    FORMER_RESPONDENT_BARRISTER_ONLY(CaseRole.RESP_BARRISTER.getCcdCode(), DO_NOT_NOTIFY_REPRESENTED, NOTIFY_FORMER_PARTY,
        "the former respondent barrister only"),
    // notify former interveners solicitor only
    FORMER_INTERVENER_ONE_SOLICITOR_ONLY(CaseRole.INTVR_SOLICITOR_1.getCcdCode(), DO_NOT_NOTIFY_REPRESENTED, NOTIFY_FORMER_PARTY,
        "the former intervener one solicitor only"),
    FORMER_INTERVENER_TWO_SOLICITOR_ONLY(CaseRole.INTVR_SOLICITOR_2.getCcdCode(), DO_NOT_NOTIFY_REPRESENTED, NOTIFY_FORMER_PARTY,
        "the former intervener two solicitor only"),
    FORMER_INTERVENER_THREE_SOLICITOR_ONLY(CaseRole.INTVR_SOLICITOR_3.getCcdCode(), DO_NOT_NOTIFY_REPRESENTED, NOTIFY_FORMER_PARTY,
        "the former intervener three solicitor only"),
    FORMER_INTERVENER_FOUR_SOLICITOR_ONLY(CaseRole.INTVR_SOLICITOR_4.getCcdCode(), DO_NOT_NOTIFY_REPRESENTED, NOTIFY_FORMER_PARTY,
        "the former intervener four solicitor only"),
    // notify former interveners barristers only
    FORMER_INTERVENER_ONE_BARRISTER_ONLY(CaseRole.INTVR_BARRISTER_1.getCcdCode(), DO_NOT_NOTIFY_REPRESENTED, NOTIFY_FORMER_PARTY,
        "the former intervener one barrister only"),
    FORMER_INTERVENER_TWO_BARRISTER_ONLY(CaseRole.INTVR_BARRISTER_2.getCcdCode(), DO_NOT_NOTIFY_REPRESENTED, NOTIFY_FORMER_PARTY,
        "the former intervener two barrister only"),
    FORMER_INTERVENER_THREE_BARRISTER_ONLY(CaseRole.INTVR_BARRISTER_3.getCcdCode(), DO_NOT_NOTIFY_REPRESENTED, NOTIFY_FORMER_PARTY,
        "the former intervener three barrister only"),
    FORMER_INTERVENER_FOUR_BARRISTER_ONLY(CaseRole.INTVR_BARRISTER_4.getCcdCode(), DO_NOT_NOTIFY_REPRESENTED, NOTIFY_FORMER_PARTY,
        "the former intervener four barrister only"),;

    private final String role;
    private final NotifyRepresented notifyRepresented;
    private final NotifyFormerParty notifyFormerParty;
    private final String description;

    public static NotificationParty getNotificationPartyFromRole(String role) {
        return Arrays.stream(NotificationParty.values())
            .filter(party -> role.equals(party.getRole()))
            .filter(NotificationParty::isNotifyRepresented)
            .filter(NotificationParty::isNotNotifyFormerParty)
            .findFirst()
            .orElse(null);
    }

    public static Optional<NotificationParty> getNotificationParty(
        CaseRole caseRole, NotifyRepresented notifyRepresented, NotifyFormerParty formerParty) {

        return Arrays.stream(NotificationParty.values())
            .filter(party -> party.getRole().equals(caseRole.getCcdCode()))
            .filter(party -> party.getNotifyRepresented() == notifyRepresented)
            .filter(party -> party.getNotifyFormerParty() == formerParty)
            .findFirst();
    }

    /**
     * Returns the {@link NotificationParty} representing the former barrister of a given intervener.
     *
     * <p>This is used when sending notifications to a barrister after their representation of
     * the intervener has ended.</p>
     *
     * @param intervenerType the type of intervener whose former barrister is required
     * @return the corresponding {@link NotificationParty} for the former intervener barrister
     */
    public static NotificationParty getFormerIntervenerBarrister(IntervenerType intervenerType) {
        return switch(intervenerType) {
            case INTERVENER_ONE -> FORMER_INTERVENER_ONE_BARRISTER_ONLY;
            case INTERVENER_TWO -> FORMER_INTERVENER_TWO_BARRISTER_ONLY;
            case INTERVENER_THREE -> FORMER_INTERVENER_THREE_BARRISTER_ONLY;
            case INTERVENER_FOUR -> FORMER_INTERVENER_FOUR_BARRISTER_ONLY;
        };
    }

    /**
     * Returns the {@link NotificationParty} representing the former solicitor of a given intervener.
     *
     * <p>Used when sending notifications to a solicitor after they have stopped representing
     * the intervener.</p>
     *
     * @param intervenerType the type of intervener whose former solicitor is required
     * @return the corresponding {@link NotificationParty} for the former intervener solicitor
     */
    public static NotificationParty getFormerIntervenerSolicitor(IntervenerType intervenerType) {
        return switch(intervenerType) {
            case INTERVENER_ONE -> FORMER_INTERVENER_ONE_SOLICITOR_ONLY;
            case INTERVENER_TWO -> FORMER_INTERVENER_TWO_SOLICITOR_ONLY;
            case INTERVENER_THREE -> FORMER_INTERVENER_THREE_SOLICITOR_ONLY;
            case INTERVENER_FOUR -> FORMER_INTERVENER_FOUR_SOLICITOR_ONLY;
        };
    }

    boolean isNotifyRepresented() {
        return notifyRepresented == NOTIFY_REPRESENTED;
    }

    boolean isNotNotifyFormerParty() {
        return notifyFormerParty == DO_NOT_NOTIFY_FORMER_PARTY;
    }
}
