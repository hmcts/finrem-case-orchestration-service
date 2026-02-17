package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssignPartiesAccessService {

    private final AssignCaseAccessService assignCaseAccessService;
    private final PrdOrganisationService prdOrganisationService;
    private final SystemUserService systemUserService;

    /**
     * Grants case access to the applicant's solicitor.
     *
     * <p>
     * Access is granted only when:
     * <ul>
     *     <li>The applicant is represented by a solicitor, and</li>
     *     <li>A valid organisation policy with an organisation ID exists.</li>
     * </ul>
     * If either condition is not met, no access is granted and an informational
     * log entry is recorded.
     *
     * @param finremCaseData the financial remedy case data containing applicant
     *                       representation details and organisation information
     */
    public void grantApplicantSolicitor(FinremCaseData finremCaseData) {
        String caseId = finremCaseData.getCcdCaseId();
        if (finremCaseData.isApplicantRepresentedByASolicitor()
            && isOrgIdExists(finremCaseData.getApplicantOrganisationPolicy())) {
            String appSolicitorEmail = finremCaseData.getAppSolicitorEmail();
            String appOrgId = finremCaseData.getApplicantOrganisationPolicy().getOrganisation().getOrganisationID();
            try {
                grantAccess(Long.valueOf(caseId), appSolicitorEmail, appOrgId, CaseRole.APP_SOLICITOR.getCcdCode());
            } catch (UserNotFoundInOrganisationApiException e) {
                // ignore it
            }
        } else {
            log.info("{} - No applicant represented by a solicitor or organisation policy missing", caseId);
        }
    }

    /**
     * Revokes case access from the applicant's solicitor where the applicant is no longer legally represented.
     *
     * <p>
     * This method retrieves the applicant solicitor's email address and organisation identifier
     * from the provided {@link FinremCaseData}. If the applicant is no longer represented by a solicitor,
     * the solicitor's {@link CaseRole#APP_SOLICITOR} role on the case is revoked.
     * </p>
     *
     * <p>
     * An {@link IllegalStateException} is thrown if the applicant organisation policy
     * is missing when representation is indicated.
     * </p>
     *
     * @param finremCaseDataBefore the case data containing applicant representation details,
     *                       solicitor email, organisation policy, and CCD case ID
     * @throws IllegalStateException if the applicant organisation policy is not present
     *                               while the applicant is marked as represented
     */
    public void revokeApplicantSolicitor(String caseId, FinremCaseData finremCaseDataBefore) {
        if (finremCaseDataBefore.isApplicantRepresentedByASolicitor()) {
            String appSolicitorEmail = finremCaseDataBefore.getAppSolicitorEmail();
            String appOrgId = ofNullable(finremCaseDataBefore.getApplicantOrganisationPolicy())
                .map(OrganisationPolicy::getOrganisation)
                .map(Organisation::getOrganisationID)
                .orElseThrow(() -> new IllegalStateException(
                    format("%s - Applicant organisation policy is missing", caseId)
                ));
            revokeAccess(Long.valueOf(caseId), appSolicitorEmail, appOrgId, CaseRole.APP_SOLICITOR.getCcdCode());
        } else {
            log.info("{} - No applicant represented by a solicitor", caseId);
        }
    }

    /**
     * Grants case access to the respondent's solicitor.
     *
     * <p>
     * Access is granted only when:
     * <ul>
     *     <li>The respondent is represented by a solicitor, and</li>
     *     <li>A valid organisation policy with an organisation ID exists.</li>
     * </ul>
     * If either condition is not met, no access is granted and an informational
     * log entry is recorded.
     *
     * @param finremCaseData the financial remedy case data containing respondent
     *                       representation details and organisation information
     */
    public void grantRespondentSolicitor(FinremCaseData finremCaseData) {
        String caseId = finremCaseData.getCcdCaseId();
        if (finremCaseData.isRespondentRepresentedByASolicitor()
            && isOrgIdExists(finremCaseData.getRespondentOrganisationPolicy())) {
            String respondentSolicitorEmail = finremCaseData.getRespondentSolicitorEmail();
            String appOrgId = finremCaseData.getRespondentOrganisationPolicy().getOrganisation().getOrganisationID();
            try {
                grantAccess(Long.valueOf(caseId), respondentSolicitorEmail, appOrgId, CaseRole.RESP_SOLICITOR.getCcdCode());
            }  catch (UserNotFoundInOrganisationApiException e) {
                // ignore it
            }
        } else {
            log.info("{} - No respondent represented by a solicitor or organisation policy missing", caseId);
        }
    }

    /**
     * Grants case access to an intervener solicitor when the intervener is represented
     * and a valid organisation policy exists.
     *
     * <p>The method retrieves the solicitor email, organisation ID, and case role
     * from the provided {@link IntervenerWrapper} and delegates the access assignment
     * to {@code grantAccess}. If the intervener is not represented or the organisation
     * information is missing, no access is granted and an informational log entry is created.</p>
     *
     * @param caseId            the CCD case identifier
     * @param intervenerWrapper the wrapper containing intervener solicitor and
     *                          organisation details
     * @throws UserNotFoundInOrganisationApiException if the solicitor cannot be
     *         located via the Organisation API during access assignment
     */
    public void grantIntervenerSolicitor(Long caseId, IntervenerWrapper intervenerWrapper)
        throws UserNotFoundInOrganisationApiException {
        if (isRepresented(intervenerWrapper)
            && isOrgIdExists(intervenerWrapper.getIntervenerOrganisation())
            && intervenerWrapper.getIntervenerSolicitorCaseRole() != null) {
            String intervenerSolEmail = intervenerWrapper.getIntervenerSolEmail();
            String appOrgId = intervenerWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationID();
            String intrvRole = intervenerWrapper.getIntervenerSolicitorCaseRole().getCcdCode();
            grantAccess(caseId, intervenerSolEmail, appOrgId, intrvRole);
        } else {
            log.info("{} - No intervener represented by a solicitor or organisation policy missing", caseId);
        }
    }

    private boolean isRepresented(IntervenerWrapper intervenerWrapper) {
        return YesOrNo.YES.equals(intervenerWrapper.getIntervenerRepresented());
    }

    private boolean isOrgIdExists(OrganisationPolicy organisationPolicy) {
        return organisationPolicy.getOrganisation() != null
            && StringUtils.isNotBlank(organisationPolicy.getOrganisation().getOrganisationID());
    }

    private void grantAccess(Long caseId, String email, String orgId, String caseRole)
        throws UserNotFoundInOrganisationApiException {
        Optional<String> userId = prdOrganisationService.findUserByEmail(email, systemUserService.getSysUserToken());
        userId.ifPresentOrElse(s -> assignCaseAccessService.grantCaseRoleToUser(caseId, s, caseRole, orgId),
            () -> log.info("{} - Attempting to grant {} but system is unable find any user with email address {} ", caseId,
                email, caseRole));
        if (userId.isEmpty()) {
            throw new UserNotFoundInOrganisationApiException();
        }
    }

    private void revokeAccess(Long caseId, String email, String orgId, String caseRole) {
        Optional<String> userId = prdOrganisationService.findUserByEmail(email, systemUserService.getSysUserToken());
        userId.ifPresentOrElse(s -> assignCaseAccessService.removeCaseRoleToUser(caseId, s, caseRole, orgId),
            () -> log.info("{} - Attempting to revoke {} but system is unable find any user with email address {} ", caseId,
                email, caseRole));
    }
}
