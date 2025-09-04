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
