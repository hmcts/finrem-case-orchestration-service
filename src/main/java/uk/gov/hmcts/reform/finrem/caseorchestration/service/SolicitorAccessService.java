package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;

@Service
@Slf4j
@RequiredArgsConstructor
public class SolicitorAccessService {

    private final AssignPartiesAccessService assignPartiesAccessService;

    /**
     * This method checks if there has been a change in the applicant or respondent solicitor details and updates their access accordingly.
     * If there is an issue with updating access (e.g. user not found in Organisation API), it rethrows the exception with a descriptive message.
     *
     * @param caseData       the current case data
     * @param caseDataBefore the case data before the update
     */
    public void checkAndAssignSolicitorAccess(FinremCaseData caseData,
                                              FinremCaseData caseDataBefore) throws UserNotFoundInOrganisationApiException {
        log.info("Check and Auto Assign Solicitor Access for Case ID: {}", caseData.getCcdCaseId());
        // Applicant solicitor access update
        if (hasApplicantSolicitorChanged(caseData, caseDataBefore)) {
            try {
                updateApplicantSolicitor(caseData, caseDataBefore);
            } catch (UserNotFoundInOrganisationApiException e) {
                throw new UserNotFoundInOrganisationApiException();
            }
        }

        // Respondent solicitor access update
        if (hasRespondentSolicitorChanged(caseData, caseDataBefore)) {
            try {
                updateRespondentSolicitor(caseData, caseDataBefore);
            } catch (UserNotFoundInOrganisationApiException e) {
                throw new UserNotFoundInOrganisationApiException();
            }
        }
    }

    private void updateApplicantSolicitor(FinremCaseData caseData, FinremCaseData caseDataBefore)
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

    private void updateRespondentSolicitor(FinremCaseData caseData, FinremCaseData caseDataBefore)
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
        String beforeEmail = caseDataBefore.getAppSolicitorEmail();
        boolean isSameOrganisation = OrganisationPolicy.isSameOrganisation(caseData.getApplicantOrganisationPolicy(),
            caseDataBefore.getApplicantOrganisationPolicy());
        boolean emailChanged = StringUtils.equalsIgnoreCase(currentEmail, beforeEmail);
        return emailChanged || !isSameOrganisation;
    }

    private boolean hasRespondentSolicitorChanged(FinremCaseData caseData, FinremCaseData caseDataBefore) {
        String currentEmail = caseData.getRespondentSolicitorEmail();
        String beforeEmail = caseDataBefore.getRespondentSolicitorEmail();
        boolean isSameOrganisation = OrganisationPolicy.isSameOrganisation(caseData.getRespondentOrganisationPolicy(),
            caseDataBefore.getRespondentOrganisationPolicy());
        boolean emailChanged = StringUtils.equalsIgnoreCase(currentEmail, beforeEmail);
        return emailChanged || !isSameOrganisation;
    }
}
