package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssignPartiesAccessService {

    private final AssignCaseAccessService assignCaseAccessService;
    private final PrdOrganisationService prdOrganisationService;
    private final SystemUserService systemUserService;

    public void grantApplicantSolicitor(FinremCaseData finremCaseData) {
        if (finremCaseData.isApplicantRepresentedByASolicitor()) {

        } else {
            log.info("No applicant represented by a solicitor");
        }
    }

    public void assignIntervenerRole(FinremCaseData finremCaseData) {

    }

    public void grantRespondentSolicitor(FinremCaseData finremCaseData) {
        if (finremCaseData.isRespondentRepresentedByASolicitor()) {

        } else {
            log.info("No applicant represented by a solicitor");
        }
    }

    private void grantAccess(Long caseId, String email, String orgId, String caseRole) {
        Optional<String> userId = prdOrganisationService.findUserByEmail(email, systemUserService.getSysUserToken());
        if (userId.isPresent()) {
            assignCaseAccessService.grantCaseRoleToUser(caseId, userId.get(), caseRole, orgId);
        }
    }
}
