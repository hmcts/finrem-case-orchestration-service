package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;

import java.util.List;

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
     * @param id   the case ID
     * @param auth the authorisation token of the logged-in user
     * @return the {@link CaseRole} of the logged-in user, or {@code null} if no role is found
     */
    public CaseRole getUserCaseRole(String id, String auth) {
        CaseAssignedUserRolesResource caseAssignedUserRole = caseAssignedRoleService.getCaseAssignedUserRole(id, auth);

        if (caseAssignedUserRole != null) {
            List<CaseAssignedUserRole> caseAssignedUserRoleList = caseAssignedUserRole.getCaseAssignedUserRoles();

            if (!caseAssignedUserRoleList.isEmpty()) {
                String loggedInUserCaseRole = caseAssignedUserRoleList.getFirst().getCaseRole();
                return CaseRole.forValue(loggedInUserCaseRole);
            }
        }

        return null;
    }
}
