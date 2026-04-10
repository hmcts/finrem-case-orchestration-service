package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
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

    /**
     * Grants case access to the applicant's solicitor when representation
     * and organisation details are valid.
     *
     * <p>Access is granted only if:
     * <ul>
     *     <li>The applicant is represented by a solicitor, and</li>
     *     <li>An organisation policy exists with a valid organisation ID.</li>
     * </ul>
     *
     * <p>If either condition is not satisfied, no access is granted and an
     * informational log entry is recorded.
     *
     * <p>If the solicitor cannot be found within the specified organisation,
     * the exception is caught and ignored to allow processing to continue
     * without failing the overall workflow.
     *
     * @param finremCaseData the financial remedy case data containing applicant
     *                       representation status, solicitor email, and
     *                       organisation policy information
     */
    public void grantApplicantSolicitor(FinremCaseData finremCaseData) throws UserNotFoundInOrganisationApiException {
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

    /**
     * Grants case access to the respondent's solicitor when representation
     * and organisation details are valid.
     *
     * <p>Access is granted only if:
     * <ul>
     *     <li>The respondent is represented by a solicitor, and</li>
     *     <li>An organisation policy exists with a valid organisation ID.</li>
     * </ul>
     *
     * <p>If either condition is not satisfied, no access is granted and an
     * informational log entry is recorded. No exception is thrown in this case.
     *
     * @param finremCaseData the financial remedy case data containing respondent
     *                       representation status, solicitor email, and
     *                       organisation policy information
     * @throws UserNotFoundInOrganisationApiException if the specified solicitor
     *         cannot be found within the provided organisation when attempting
     *         to grant access
     */
    public void grantRespondentSolicitor(FinremCaseData finremCaseData) throws UserNotFoundInOrganisationApiException {
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

    /**
     * Grants case access to an intervener solicitor when the intervener is represented,
     * a valid organisation policy exists, and a solicitor case role is provided.
     *
     * <p>The method retrieves the solicitor email, organisation ID, and CCD case role
     * from the supplied {@link IntervenerWrapper} and delegates access assignment
     * to {@code grantAccess}. When access is successfully granted, the returned user ID
     * is stored in the wrapper.</p>
     *
     * <p>If the intervener is not represented, the organisation information is missing,
     * or no solicitor case role is defined, no access is granted and an informational
     * log entry is recorded.</p>
     *
     * @param caseId            the CCD case identifier
     * @param intervenerWrapper the wrapper containing intervener solicitor,
     *                          organisation, and case-role details
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
            Optional<String> userId = grantAccess(caseId, intervenerSolEmail, appOrgId, intrvRole);
            userId.ifPresent(intervenerWrapper::setSolUserId);
        } else {
            log.info("{} - No intervener represented by a solicitor or organisation policy missing", caseId);
        }
    }

    /**
     * Revokes case access from the applicant's solicitor if they were previously
     * represented and had access granted.
     *
     * <p>
     * Access is revoked only when:
     * <ul>
     *     <li>The applicant was previously represented by a solicitor, and</li>
     *     <li>A valid organisation policy with an organisation ID existed at that time.</li>
     * </ul>
     * If either condition is not met, no access is revoked and an informational
     * log entry is recorded.
     *
     * @param finremCaseData the financial remedy case data before the update,
     *                             containing previous applicant representation details and organisation information
     */
    public void revokeApplicantSolicitor(FinremCaseData finremCaseData) throws UserNotFoundInOrganisationApiException {
        String caseId = finremCaseData.getCcdCaseId();
        if (finremCaseData.isApplicantRepresentedByASolicitor()
            && isOrgIdExists(finremCaseData.getApplicantOrganisationPolicy())) {
            String appSolicitorEmail = finremCaseData.getAppSolicitorEmail();
            String appOrgId = finremCaseData.getApplicantOrganisationPolicy().getOrganisation().getOrganisationID();
            revokeAccess(Long.valueOf(caseId), appSolicitorEmail, appOrgId, CaseRole.APP_SOLICITOR.getCcdCode());
        } else {
            log.info("{} - Revoke access failed - No applicant represented by a solicitor or organisation policy missing", caseId);
        }
    }

    /**
     * Revokes case access from the respondent's solicitor if they were previously
     * represented and had access granted.
     *
     * <p>
     * Access is revoked only when:
     * <ul>
     *     <li>The respondent was previously represented by a solicitor, and</li>
     *     <li>A valid organisation policy with an organisation ID existed at that time.</li>
     * </ul>
     * If either condition is not met, no access is revoked and an informational
     * log entry is recorded.
     *
     * @param finremCaseData the financial remedy case data before the update,
     *                             containing previous respondent representation details and organisation information
     */
    public void revokeRespondentSolicitor(FinremCaseData finremCaseData) throws UserNotFoundInOrganisationApiException {
        String caseId = finremCaseData.getCcdCaseId();
        if (finremCaseData.isRespondentRepresentedByASolicitor()
            && isOrgIdExists(finremCaseData.getRespondentOrganisationPolicy())) {
            String respondentSolicitorEmail = finremCaseData.getRespondentSolicitorEmail();
            String respOrgId = finremCaseData.getRespondentOrganisationPolicy().getOrganisation().getOrganisationID();
            revokeAccess(Long.valueOf(caseId), respondentSolicitorEmail, respOrgId, CaseRole.RESP_SOLICITOR.getCcdCode());
        } else {
            log.info("{} - Revoke access failed - No respondent represented by a solicitor or organisation policy missing", caseId);
        }
    }

    private boolean isRepresented(IntervenerWrapper intervenerWrapper) {
        return YesOrNo.YES.equals(intervenerWrapper.getIntervenerRepresented());
    }

    private boolean isOrgIdExists(OrganisationPolicy organisationPolicy) {
        return Optional.ofNullable(organisationPolicy)
            .map(OrganisationPolicy::getOrganisation)
            .map(Organisation::getOrganisationID)
            .filter(StringUtils::isNotBlank)
            .isPresent();
    }

    private Optional<String> grantAccess(Long caseId, String email, String orgId, String caseRole)
        throws UserNotFoundInOrganisationApiException {
        Optional<String> userId = StringUtils.isBlank(email) ? Optional.empty() :
            prdOrganisationService.findUserByEmail(email);
        userId.ifPresentOrElse(s -> assignCaseAccessService.grantCaseRoleToUser(caseId, s, caseRole, orgId),
            () -> log.info("{} - Attempting to grant role {} but unable to find user", caseId, caseRole));
        if (userId.isEmpty()) {
            throw new UserNotFoundInOrganisationApiException();
        }
        return userId;
    }

    private void revokeAccess(Long caseId, String email, String orgId, String caseRole)
        throws UserNotFoundInOrganisationApiException {
        String userId = prdOrganisationService.findUserByEmail(email)
            .orElseThrow(() -> new UserNotFoundInOrganisationApiException(
                String.format("CaseID %s - Attempting to revoke role {} but system is unable to find user with role %s", caseId, caseRole)
            ));
        assignCaseAccessService.removeCaseRoleToUser(caseId, userId, caseRole, orgId);
    }
}
