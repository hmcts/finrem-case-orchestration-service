package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRolesResource;
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

    private boolean isSolicitorDigital(String caseId, String caseRole) {
        CaseAssignmentUserRolesResource rolesResource = assignCaseAccessService.getUserRoles(caseId);
        log.info("CheckSolicitorIsDigitalService::rolesResource =={}==getCaseAssignmentUserRoles{} ==caseId{}==",
            rolesResource.getCaseAssignmentUserRoles(), caseId);

        if (rolesResource == null || rolesResource.getCaseAssignmentUserRoles() == null) {
            return false;
        }

        return rolesResource.getCaseAssignmentUserRoles().stream()
            .map(CaseAssignmentUserRole::getCaseRole)
            .anyMatch(caseRole::equals);
    }
}
