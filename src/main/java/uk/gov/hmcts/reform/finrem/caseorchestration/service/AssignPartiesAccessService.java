package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssignPartiesAccessService {

    private final AssignCaseAccessService assignCaseAccessService;
    private final PrdOrganisationService prdOrganisationService;
    private final SystemUserService systemUserService;

    public void grantApplicantSolicitor(FinremCaseData finremCaseData) {
        String caseId = finremCaseData.getCcdCaseId();
        if (finremCaseData.isApplicantRepresentedByASolicitor()
            && isOrgIdExists(finremCaseData.getApplicantOrganisationPolicy())) {
            String appSolicitorEmail = finremCaseData.getAppSolicitorEmail();
            String appOrgId = finremCaseData.getApplicantOrganisationPolicy().getOrganisation().getOrganisationID();
            grantAccess(Long.valueOf(caseId), appSolicitorEmail, appOrgId, CaseRole.APP_SOLICITOR.getCcdCode());
        } else {
            log.info("{} - No applicant represented by a solicitor or organisation policy missing", caseId);
        }
    }

    public void grantRespondentSolicitor(FinremCaseData finremCaseData) {
        String caseId = finremCaseData.getCcdCaseId();
        if (finremCaseData.isRespondentRepresentedByASolicitor()
            && isOrgIdExists(finremCaseData.getRespondentOrganisationPolicy())) {
            String respondentSolicitorEmail = finremCaseData.getRespondentSolicitorEmail();
            String appOrgId = finremCaseData.getRespondentOrganisationPolicy().getOrganisation().getOrganisationID();
            grantAccess(Long.valueOf(caseId), respondentSolicitorEmail, appOrgId, CaseRole.RESP_SOLICITOR.getCcdCode());
        } else {
            log.info("{} - No respondent represented by a solicitor or organisation policy missing", caseId);
        }
    }

    private boolean isOrgIdExists(OrganisationPolicy organisationPolicy) {
        return organisationPolicy.getOrganisation() != null &&
            StringUtils.isNotBlank(organisationPolicy.getOrganisation().getOrganisationID());
    }

    private void grantAccess(Long caseId, String email, String orgId, String caseRole) {
        Optional<String> userId = prdOrganisationService.findUserByEmail(email, systemUserService.getSysUserToken());
        userId.ifPresentOrElse(s -> assignCaseAccessService.grantCaseRoleToUser(caseId, s, caseRole, orgId),
            () -> log.info("{} - Attempting to grant {} but system is unable find any user with email address {} ", caseId,
                email, caseRole));
    }
}
