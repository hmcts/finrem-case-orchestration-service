package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRolesResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_POLICY;

@RequiredArgsConstructor
@Service
@Slf4j
public class CheckSolicitorIsDigitalService {

    private final AssignCaseAccessService assignCaseAccessService;

    public boolean isApplicantSolicitorDigital(String caseId) {
        return isSolicitorDigital(caseId, APP_SOLICITOR_POLICY);
    }

    public boolean isRespondentSolicitorDigital(String caseId) {
        return isSolicitorDigital(caseId, RESP_SOLICITOR_POLICY);
    }

    public boolean isIntervenerSolicitorDigital(String caseId, CaseRole caseRole) {
        return isSolicitorDigital(caseId, caseRole.getCcdCode());
    }

    public boolean isIntervenerSolicitorDigital(String caseId, String caseRole) {
        return isSolicitorDigital(caseId, caseRole);
    }

    private boolean isSolicitorDigital(String caseId, String caseRole) {
        log.info("{} - Checking if the given caseRole ({}) in case_users table.", caseId, caseRole);
        CaseAssignmentUserRolesResource rolesResource = assignCaseAccessService.searchUserRoles(caseId);
        if (rolesResource == null || rolesResource.getCaseAssignmentUserRoles() == null) {
            log.info("{} - No roles found.", caseId);
            return false;
        }
        log.info("{} - Found {} roles, roles are {}", caseId, rolesResource.getCaseAssignmentUserRoles().size(),
            rolesResource.getCaseAssignmentUserRoles());
        return rolesResource.getCaseAssignmentUserRoles().stream()
            .map(CaseAssignmentUserRole::getCaseRole)
            .anyMatch(caseRole::equals);
    }
}
