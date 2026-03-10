package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;

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
    APPLICANT("[APPSOLICITOR]", NOTIFY_REPRESENTED, DO_NOT_NOTIFY_FORMER_PARTY),
    // notify both respondent and respondent solicitor
    RESPONDENT("[RESPSOLICITOR]", NOTIFY_REPRESENTED, DO_NOT_NOTIFY_FORMER_PARTY),
    // notify both intervener and intervener solicitor
    INTERVENER_ONE("[INTVRSOLICITOR1]", NOTIFY_REPRESENTED, DO_NOT_NOTIFY_FORMER_PARTY),
    INTERVENER_TWO("[INTVRSOLICITOR2]", NOTIFY_REPRESENTED, DO_NOT_NOTIFY_FORMER_PARTY),
    INTERVENER_THREE("[INTVRSOLICITOR3]", NOTIFY_REPRESENTED, DO_NOT_NOTIFY_FORMER_PARTY),
    INTERVENER_FOUR("[INTVRSOLICITOR4]", NOTIFY_REPRESENTED, DO_NOT_NOTIFY_FORMER_PARTY),
    // notify former applicant solicitor only
    FORMER_APPLICANT_SOLICITOR_ONLY(CaseRole.APP_SOLICITOR.getCcdCode(), DO_NOT_NOTIFY_REPRESENTED, NOTIFY_FORMER_PARTY),
    // notify former applicant barristers only
    FORMER_APPLICANT_BARRISTER_ONLY(CaseRole.APP_BARRISTER.getCcdCode(), DO_NOT_NOTIFY_REPRESENTED, NOTIFY_FORMER_PARTY),
    // notify former respondent solicitor only
    FORMER_RESPONDENT_SOLICITOR_ONLY(CaseRole.RESP_SOLICITOR.getCcdCode(), DO_NOT_NOTIFY_REPRESENTED, NOTIFY_FORMER_PARTY),
    // notify former respondent barristers only
    FORMER_RESPONDENT_BARRISTER_ONLY(CaseRole.RESP_BARRISTER.getCcdCode(), DO_NOT_NOTIFY_REPRESENTED, NOTIFY_FORMER_PARTY),
    // notify former interveners solicitor only
    FORMER_INTERVENER_ONE_SOLICITOR_ONLY(CaseRole.INTVR_SOLICITOR_1.getCcdCode(), DO_NOT_NOTIFY_REPRESENTED, NOTIFY_FORMER_PARTY),
    FORMER_INTERVENER_TWO_SOLICITOR_ONLY(CaseRole.INTVR_SOLICITOR_2.getCcdCode(), DO_NOT_NOTIFY_REPRESENTED, NOTIFY_FORMER_PARTY),
    FORMER_INTERVENER_THREE_SOLICITOR_ONLY(CaseRole.INTVR_SOLICITOR_3.getCcdCode(), DO_NOT_NOTIFY_REPRESENTED, NOTIFY_FORMER_PARTY),
    FORMER_INTERVENER_FOUR_SOLICITOR_ONLY(CaseRole.INTVR_SOLICITOR_4.getCcdCode(), DO_NOT_NOTIFY_REPRESENTED, NOTIFY_FORMER_PARTY),
    // notify former interveners barristers only
    FORMER_INTERVENER_ONE_BARRISTER_ONLY(CaseRole.INTVR_BARRISTER_1.getCcdCode(), DO_NOT_NOTIFY_REPRESENTED, NOTIFY_FORMER_PARTY),
    FORMER_INTERVENER_TWO_BARRISTER_ONLY(CaseRole.INTVR_BARRISTER_2.getCcdCode(), DO_NOT_NOTIFY_REPRESENTED, NOTIFY_FORMER_PARTY),
    FORMER_INTERVENER_THREE_BARRISTER_ONLY(CaseRole.INTVR_BARRISTER_3.getCcdCode(), DO_NOT_NOTIFY_REPRESENTED, NOTIFY_FORMER_PARTY),
    FORMER_INTERVENER_FOUR_BARRISTER_ONLY(CaseRole.INTVR_BARRISTER_4.getCcdCode(), DO_NOT_NOTIFY_REPRESENTED, NOTIFY_FORMER_PARTY);

    private final String role;
    private final NotifyRepresented notifyRepresented;
    private final NotifyFormerParty notifyFormerParty;

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

    boolean isNotifyRepresented() {
        return notifyRepresented == NOTIFY_REPRESENTED;
    }

    boolean isNotNotifyFormerParty() {
        return notifyFormerParty == DO_NOT_NOTIFY_FORMER_PARTY;
    }
}
