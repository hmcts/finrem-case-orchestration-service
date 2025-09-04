package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;

import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.CASEWORKER;

@Service
@Slf4j
@RequiredArgsConstructor
public class CaseRoleService {

    private final CaseAssignedRoleService caseAssignedRoleService;

    /**
     * Retrieves the case role of the logged-in user for a given case ID.
     * <p>
     * If the user has an assigned case role, the first one found is returned.
     * Otherwise, the default {@link CaseRole#CASEWORKER} is returned.
     *
     * @param id   the case identifier
     * @param auth the user authorisation token
     * @return the user's {@link CaseRole}, or {@link CaseRole#CASEWORKER} if none is assigned
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

        return CASEWORKER;
    }
}
