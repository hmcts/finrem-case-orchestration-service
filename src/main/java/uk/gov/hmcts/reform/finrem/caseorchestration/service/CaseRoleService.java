package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.APP_BARRISTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.APP_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.INTVR_BARRISTER_1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.INTVR_BARRISTER_2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.INTVR_BARRISTER_3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.INTVR_BARRISTER_4;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.INTVR_SOLICITOR_1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.INTVR_SOLICITOR_2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.INTVR_SOLICITOR_3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.INTVR_SOLICITOR_4;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.RESP_BARRISTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.RESP_SOLICITOR;

@Service
@Slf4j
@RequiredArgsConstructor
public class CaseRoleService {

    private final CaseAssignedRoleService caseAssignedRoleService;

    /**
     * Retrieves the case role of the logged-in user for a given case.
     *
     * <p>
     * This method calls {@code caseAssignedRoleService} to fetch the list of
     * case-user-role assignments for the specified case ID and authorisation token.
     * If roles are returned, the first case role is mapped to a {@link CaseRole}
     * and returned. If no roles are found, or the resource is {@code null}, the
     * method returns {@code null}.
     * </p>
     *
     * <p><b>Note:</b> If the logged-in user has the {@code CASEWORKER} role, this method
     * will return {@code null}.</p>
     *
     * @param id   the case ID
     * @param auth the authorisation token of the logged-in user
     * @return the {@link CaseRole} of the logged-in user, or {@code null} if no role is found
     */
    public CaseRole getUserCaseRole(String id, String auth) {
        CaseAssignedUserRolesResource caseAssignedUserRole = caseAssignedRoleService.getCaseAssignedUserRole(id, auth);

        if (caseAssignedUserRole != null) {
            List<CaseAssignedUserRole> caseAssignedUserRoleList = caseAssignedUserRole.getCaseAssignedUserRoles();

            if (!caseAssignedUserRoleList.isEmpty()) {
                // Using getFirst() to maintain consistency with the existing logic in
                // CaseAssignedRoleService.setCaseAssignedUserRole.
                String loggedInUserCaseRole = caseAssignedUserRoleList.getFirst().getCaseRole();
                return CaseRole.forValue(loggedInUserCaseRole);
            }
        }

        return null;
    }

    /**
     * Retrieves the case role of the logged-in user for a given case.
     * If the user has no specific case role, defaults to {@code CASEWORKER}.
     *
     * @param id   the case ID
     * @param auth the authorisation token of the logged-in user
     * @return the {@link CaseRole} of the logged-in user, or {@code CASEWORKER} if no specific role is found
     */
    public CaseRole getUserOrCaseworkerCaseRole(String id, String auth) {
        return Optional.ofNullable(getUserCaseRole(id, auth))
            .orElse(CaseRole.CASEWORKER);
    }

    /**
     * Checks if the user associated with the provided authorisation token
     * has a {@link CaseRole} that represents the **Applicant's legal representative**.
     *
     * <p>The method determines this by retrieving the user's specific case role
     * for the given financial remedy case and checking if it matches one of
     * the defined applicant representative roles (e.g., Applicant Solicitor or Applicant Barrister).</p>
     *
     * @param finremCaseData The case data object, typically containing the CCD Case ID
     *                       required to determine the user's role in the context of the case.
     * @param userAuthorisation The user's authorisation token (e.g., JWT) used to identify
     *                          the current user whose role is being checked.
     * @return {@code true} if the user has a role of **Applicant Solicitor** (APP_SOLICITOR)
     *         or **Applicant Barrister** (APP_BARRISTER), {@code false} otherwise.
     * @see CaseRole
     */
    public boolean isApplicantRepresentative(FinremCaseData finremCaseData, String userAuthorisation) {
        CaseRole caseRole = getUserCaseRole(finremCaseData.getCcdCaseId(), userAuthorisation);
        return Optional.ofNullable(caseRole)
            .filter(role -> List.of(APP_SOLICITOR, APP_BARRISTER).contains(role))
            .isPresent();
    }

    /**
     * Checks if the user associated with the provided authorisation token
     * has a {@link CaseRole} that represents the **Respondent's legal representative**.
     *
     * <p>The method determines this by retrieving the user's specific case role
     * for the given financial remedy case and checking if it matches one of
     * the defined respondent representative roles (e.g., Respondent Solicitor or Respondent Barrister).
     * It safely handles the case where the retrieved user role might be {@code null}.</p>
     *
     * @param finremCaseData The case data object, typically containing the CCD Case ID     *
     *                       required to determine the user's role in the context of the case.
     * @param userAuthorisation The user's authorisation token (e.g., JWT) used to identify
     *                          the current user whose role is being checked.
     * @return {@code true} if the user has a role of **Respondent Solicitor** (RESP_SOLICITOR)
     *         or **Respondent Barrister** (RESP_BARRISTER), {@code false} otherwise.
     * @see CaseRole
     */
    public boolean isRespondentRepresentative(FinremCaseData finremCaseData, String userAuthorisation) {
        CaseRole caseRole = getUserCaseRole(finremCaseData.getCcdCaseId(), userAuthorisation);
        return Optional.ofNullable(caseRole)
            .filter(role -> List.of(RESP_SOLICITOR, RESP_BARRISTER).contains(role))
            .isPresent();
    }

    /**
     * Checks if the user associated with the provided authorisation token
     * has a {@link CaseRole} that represents the **Intervener's legal representative**.
     *
     * <p>The method determines this by retrieving the user's specific case role
     * for the given financial remedy case and checking if it matches one of
     * the defined intervener representative roles (e.g., Intervener Solicitor or Intervener Barrister).
     * It safely handles the case where the retrieved user role might be {@code null}.</p>
     *
     * @param finremCaseData The case data object, typically containing the CCD Case ID     *
     *                       required to determine the user's role in the context of the case.
     * @param userAuthorisation The user's authorisation token (e.g., JWT) used to identify
     *                          the current user whose role is being checked.
     * @return {@code true} if the user has a role of **Intervener Solicitor** (INTVR_SOLICITOR_1-4)
     *         or **Intervener Barrister** (INTVR_BARRISTER_1-4), {@code false} otherwise.
     * @see CaseRole
     */
    public boolean isIntervenerRepresentative(FinremCaseData finremCaseData, String userAuthorisation) {
        CaseRole caseRole = getUserCaseRole(finremCaseData.getCcdCaseId(), userAuthorisation);
        return Optional.ofNullable(caseRole)
            .filter(role -> List.of(INTVR_SOLICITOR_1, INTVR_SOLICITOR_2, INTVR_SOLICITOR_3, INTVR_SOLICITOR_4,
                INTVR_BARRISTER_1, INTVR_BARRISTER_2, INTVR_BARRISTER_3, INTVR_BARRISTER_4).contains(role))
            .isPresent();
    }

    /**
     * Returns the intervener index (1–4) if the user is an Intervener Solicitor
     * or Intervener Barrister for the given case.
     *
     * @param finremCaseData The case data containing the CCD Case ID.
     * @param userAuthorisation The user's authorisation token.
     * @return an {@link Optional} containing the intervener index (1–4),
     *         or {@link Optional#empty()} if the user is not an intervener representative.
     */
    public Optional<Integer> getIntervenerIndex(FinremCaseData finremCaseData, String userAuthorisation) {
        CaseRole caseRole = getUserCaseRole(finremCaseData.getCcdCaseId(), userAuthorisation);

        if (caseRole == null) {
            return Optional.empty();
        }

        return switch (caseRole) {
            case INTVR_SOLICITOR_1, INTVR_BARRISTER_1 -> Optional.of(1);
            case INTVR_SOLICITOR_2, INTVR_BARRISTER_2 -> Optional.of(2);
            case INTVR_SOLICITOR_3, INTVR_BARRISTER_3 -> Optional.of(3);
            case INTVR_SOLICITOR_4, INTVR_BARRISTER_4 -> Optional.of(4);
            default -> Optional.empty();
        };
    }

    /**
     * Returns the intervener index (1–4) if the user is an Intervener Solicitor
     * for the given case.
     *
     * @param finremCaseData The case data containing the CCD Case ID.
     * @param userAuthorisation The user's authorisation token.
     * @return an {@link Optional} containing the intervener index (1–4),
     *         or {@link Optional#empty()} if the user is not an intervener solicitor.
     */
    public Optional<Integer> getIntervenerSolicitorIndex(FinremCaseData finremCaseData, String userAuthorisation) {
        CaseRole caseRole = getUserCaseRole(finremCaseData.getCcdCaseId(), userAuthorisation);

        if (caseRole == null) {
            return Optional.empty();
        }

        return switch (caseRole) {
            case INTVR_SOLICITOR_1 -> Optional.of(1);
            case INTVR_SOLICITOR_2 -> Optional.of(2);
            case INTVR_SOLICITOR_3 -> Optional.of(3);
            case INTVR_SOLICITOR_4 -> Optional.of(4);
            default -> Optional.empty();
        };
    }

}
