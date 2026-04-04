package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.EmailUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class SolicitorAccessService {

    private final AssignPartiesAccessService assignPartiesAccessService;

    /**
     * Checks if the solicitor access has changed for both applicant and respondent, and grants or revokes access accordingly.
     *
     * @param caseData       the current case data after the update
     * @param caseDataBefore the case data before the update
     * @throws UserNotFoundInOrganisationApiException if the solicitor email is not found in the Organisation API
     *                                                when trying to grant access
     */
    public void checkAndAssignSolicitorAccess(FinremCaseData caseData,
                                              FinremCaseData caseDataBefore) throws UserNotFoundInOrganisationApiException {
        log.info("Check and Auto Assign Solicitor Access for Case ID: {}", caseData.getCcdCaseId());
        // Applicant solicitor access update
        if (hasApplicantSolicitorChanged(caseData, caseDataBefore)) {
            grantOrRevokeApplicantSolicitorAccess(caseData, caseDataBefore);
        }

        // Respondent solicitor access update
        if (hasRespondentSolicitorChanged(caseData, caseDataBefore)) {
            grantOrRevokeRespondentSolicitorAccess(caseData, caseDataBefore);
        }
    }

    private void grantOrRevokeApplicantSolicitorAccess(FinremCaseData caseData, FinremCaseData caseDataBefore)
        throws UserNotFoundInOrganisationApiException {
        if (caseData.isApplicantRepresentedByASolicitor()) {
            log.info("Grant New Applicant Solicitor Access for Case ID: {}", caseData.getCcdCaseId());
            assignPartiesAccessService.grantApplicantSolicitor(caseData);
        }
        if (caseDataBefore.isApplicantRepresentedByASolicitor()) {
            log.info("Revoke Old Applicant Solicitor Access for Case ID: {}", caseData.getCcdCaseId());
            assignPartiesAccessService.revokeApplicantSolicitor(caseDataBefore);
        }
    }

    private void grantOrRevokeRespondentSolicitorAccess(FinremCaseData caseData, FinremCaseData caseDataBefore)
        throws UserNotFoundInOrganisationApiException {
        if (caseData.isRespondentRepresentedByASolicitor()) {
            log.info("Grant New Respondent Solicitor Access for Case ID: {}", caseData.getCcdCaseId());
            assignPartiesAccessService.grantRespondentSolicitor(caseData);
        }
        if (caseDataBefore.isRespondentRepresentedByASolicitor()) {
            log.info("Revoke Old Respondent Solicitor Access for Case ID: {}", caseData.getCcdCaseId());
            assignPartiesAccessService.revokeRespondentSolicitor(caseDataBefore);
        }
    }

    private boolean hasApplicantSolicitorChanged(FinremCaseData caseData, FinremCaseData caseDataBefore) {
        String currentEmail = caseData.getAppSolicitorEmail();
        String previousEmail = caseDataBefore.getAppSolicitorEmail();
        boolean isSameOrganisation = OrganisationPolicy.isSameOrganisation(
            caseData.getApplicantOrganisationPolicy(),
            caseDataBefore.getApplicantOrganisationPolicy());
        return EmailUtils.hasSolicitorEmailChanged(currentEmail, previousEmail)
            || !isSameOrganisation;
    }

    private boolean hasRespondentSolicitorChanged(FinremCaseData caseData, FinremCaseData caseDataBefore) {
        String currentEmail = caseData.getRespondentSolicitorEmail();
        String previousEmail = caseDataBefore.getRespondentSolicitorEmail();
        boolean isSameOrganisation = OrganisationPolicy.isSameOrganisation(
            caseData.getRespondentOrganisationPolicy(),
            caseDataBefore.getRespondentOrganisationPolicy());
        return EmailUtils.hasSolicitorEmailChanged(currentEmail, previousEmail)
            || !isSameOrganisation;
    }
}
