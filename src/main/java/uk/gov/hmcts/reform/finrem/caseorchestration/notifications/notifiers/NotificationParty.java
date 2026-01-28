package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;

import java.util.Arrays;
import java.util.Optional;

@RequiredArgsConstructor
@Getter
public enum NotificationParty {
    // notify both applicant and applicant solicitor
    APPLICANT("[APPSOLICITOR]", true, false),
    // notify both respondent and respondent solicitor
    RESPONDENT("[RESPSOLICITOR]", true, false),
    // notify both intervener and intervener solicitor
    INTERVENER_ONE("[INTVRSOLICITOR1]", true, false),
    INTERVENER_TWO("[INTVRSOLICITOR2]", true, false),
    INTERVENER_THREE("[INTVRSOLICITOR3]", true, false),
    INTERVENER_FOUR("[INTVRSOLICITOR4]", true, false),
    // notify former applicant solicitor only
    FORMER_APPLICANT_SOLICITOR_ONLY(CaseRole.APP_SOLICITOR.getCcdCode(), false, true),
    // notify former applicant barristers only
    FORMER_APPLICANT_BARRISTER_ONLY(CaseRole.APP_BARRISTER.getCcdCode(), false, true),
    // notify former respondent solicitor only
    FORMER_RESPONDENT_SOLICITOR_ONLY(CaseRole.RESP_SOLICITOR.getCcdCode(), false, true),
    // notify former respondent barristers only
    FORMER_RESPONDENT_BARRISTER_ONLY(CaseRole.RESP_BARRISTER.getCcdCode(), false, true),
    // notify former interveners solicitor only
    FORMER_INTERVENER_ONE_SOLICITOR_ONLY(CaseRole.INTVR_SOLICITOR_1.getCcdCode(), false, true),
    FORMER_INTERVENER_TWO_SOLICITOR_ONLY(CaseRole.INTVR_SOLICITOR_2.getCcdCode(), false, true),
    FORMER_INTERVENER_THREE_SOLICITOR_ONLY(CaseRole.INTVR_SOLICITOR_3.getCcdCode(), false, true),
    FORMER_INTERVENER_FOUR_SOLICITOR_ONLY(CaseRole.INTVR_SOLICITOR_4.getCcdCode(), false, true),
    // notify former interveners barristers only
    FORMER_INTERVENER_ONE_BARRISTER_ONLY(CaseRole.INTVR_BARRISTER_1.getCcdCode(), false, true),
    FORMER_INTERVENER_TWO_BARRISTER_ONLY(CaseRole.INTVR_BARRISTER_2.getCcdCode(), false, true),
    FORMER_INTERVENER_THREE_BARRISTER_ONLY(CaseRole.INTVR_BARRISTER_3.getCcdCode(), false, true),
    FORMER_INTERVENER_FOUR_BARRISTER_ONLY(CaseRole.INTVR_BARRISTER_4.getCcdCode(), false, true);

    private final String role;
    private final boolean notifyRepresented;
    private final boolean isFormerParty;

    public static NotificationParty getNotificationPartyFromRole(String role) {
        return Arrays.stream(NotificationParty.values())
            .filter(party -> party.getRole().equals(role))
            .filter(NotificationParty::isNotifyRepresented)
            .filter(party -> !party.isFormerParty())
            .findFirst()
            .orElse(null);
    }

    public static Optional<NotificationParty> getNotificationParty(
        CaseRole caseRole, boolean notifyRepresented, boolean historical) {

        return Arrays.stream(NotificationParty.values())
            .filter(party -> party.getRole().equals(caseRole.getCcdCode()))
            .filter(party -> party.isNotifyRepresented() == notifyRepresented)
            .filter(party -> party.isFormerParty() == historical)
            .findFirst();
    }
}
