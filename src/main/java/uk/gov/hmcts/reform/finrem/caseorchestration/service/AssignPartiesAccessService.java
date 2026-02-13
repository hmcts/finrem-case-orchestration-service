package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;

import java.util.Optional;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssignPartiesAccessService {

    private final AssignCaseAccessService assignCaseAccessService;
    private final PrdOrganisationService prdOrganisationService;
    private final SystemUserService systemUserService;

    /**
     * Grants case access to the applicant's solicitor where the applicant is legally represented.
     *
     * <p>
     * This method retrieves the applicant solicitor's email address and organisation identifier
     * from the provided {@link FinremCaseData}. If the applicant is represented by a solicitor,
     * the solicitor is granted the {@link CaseRole#APP_SOLICITOR} role on the case.
     * </p>
     *
     * <p>
     * An {@link IllegalStateException} is thrown if the applicant organisation policy
     * is missing when representation is indicated.
     * </p>
     *
     * @param finremCaseData the case data containing applicant representation details,
     *                       solicitor email, organisation policy, and CCD case ID
     * @throws IllegalStateException if the applicant organisation policy is not present
     *                               while the applicant is marked as represented
     */
    public void grantApplicantSolicitor(FinremCaseData finremCaseData) {
        String caseId = finremCaseData.getCcdCaseId();
        if (finremCaseData.isApplicantRepresentedByASolicitor()) {
            String appSolicitorEmail = finremCaseData.getAppSolicitorEmail();
            String appOrgId = ofNullable(finremCaseData.getApplicantOrganisationPolicy())
                .map(OrganisationPolicy::getOrganisation)
                .map(Organisation::getOrganisationID)
                .orElseThrow(() -> new IllegalStateException(
                    format("%s - Applicant organisation policy is missing", caseId)
                ));
            grantAccess(Long.valueOf(caseId), appSolicitorEmail, appOrgId, CaseRole.APP_SOLICITOR.getCcdCode());
        } else {
            log.info("{} - No applicant represented by a solicitor", caseId);
        }
    }

    /**
     * Grants case access to the respondent's solicitor where the respondent is legally represented.
     *
     * <p>
     * This method retrieves the respondent solicitor's email address and organisation identifier
     * from the provided {@link FinremCaseData}. If the respondent is represented by a solicitor,
     * the solicitor is granted the {@link CaseRole#APP_SOLICITOR} role on the case.
     * </p>
     *
     * <p>
     * An {@link IllegalStateException} is thrown if the respondent organisation policy
     * is missing when representation is indicated.
     * </p>
     *
     * @param finremCaseData the case data containing respondent representation details,
     *                       solicitor email, organisation policy, and CCD case ID
     * @throws IllegalStateException if the respondent organisation policy is not present
     *                               while the respondent is marked as represented
     */
    public void grantRespondentSolicitor(FinremCaseData finremCaseData) {
        String caseId = finremCaseData.getCcdCaseId();
        if (finremCaseData.isRespondentRepresentedByASolicitor()) {
            String respondentSolicitorEmail = finremCaseData.getRespondentSolicitorEmail();
            String appOrgId = ofNullable(finremCaseData.getRespondentOrganisationPolicy())
                .map(OrganisationPolicy::getOrganisation)
                .map(Organisation::getOrganisationID)
                .orElseThrow(() -> new IllegalStateException(
                    format("%s - Respondent organisation policy is missing", caseId)
                ));
            grantAccess(Long.valueOf(caseId), respondentSolicitorEmail, appOrgId, CaseRole.RESP_SOLICITOR.getCcdCode());
        } else {
            log.info("{} - No respondent represented by a solicitor", caseId);
        }
    }

    public void assignIntervenerRole(FinremCaseData finremCaseData) {
        // TODO
    }

    private void grantAccess(Long caseId, String email, String orgId, String caseRole) {
        Optional<String> userId = prdOrganisationService.findUserByEmail(email, systemUserService.getSysUserToken());
        userId.ifPresent(s -> assignCaseAccessService.grantCaseRoleToUser(caseId, s, caseRole, orgId));
    }
}
