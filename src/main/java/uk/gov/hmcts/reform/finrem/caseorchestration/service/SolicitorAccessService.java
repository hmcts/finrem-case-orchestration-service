package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NocLetterNotificationService;

import java.util.Optional;

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
        // Applicant solicitor access update
        if (hasApplicantSolicitorChanged(caseData, caseDataBefore)) {
            try {
                updateApplicantSolicitor(caseData, caseDataBefore);
            } catch (UserNotFoundInOrganisationApiException e) {
                String appSolEmail = caseData.getAppSolicitorEmailIfRepresented();
                log.info("There was a problem updating access to applicant solicitor: %s".formatted(appSolEmail));
                throw new UserNotFoundInOrganisationApiException();
            }
        }

        // Respondent solicitor access update
        if (hasRespondentSolicitorChanged(caseData, caseDataBefore)) {
            try {
                updateRespondentSolicitor(caseData, caseDataBefore);
            } catch (UserNotFoundInOrganisationApiException e) {
                String respSolEmail = caseData.getRespondentSolicitorEmailIfRepresented();
                log.info("There was a problem updating access to respondent solicitor: %s".formatted(respSolEmail));
                throw new UserNotFoundInOrganisationApiException();
            }
        }
    }

    private void updateApplicantSolicitor(FinremCaseData caseData, FinremCaseData caseDataBefore)
        throws UserNotFoundInOrganisationApiException {
        if (hasApplicantSolicitorChanged(caseData, caseDataBefore)) {
            if (caseData.isApplicantRepresentedByASolicitor()) {
                assignPartiesAccessService.grantApplicantSolicitor(caseData);
            }
            if (caseDataBefore.isApplicantRepresentedByASolicitor()) {
                assignPartiesAccessService.revokeApplicantSolicitor(caseDataBefore);
            }
        }
    }

    private void updateRespondentSolicitor(FinremCaseData caseData, FinremCaseData caseDataBefore)
        throws UserNotFoundInOrganisationApiException {
        if (hasRespondentSolicitorChanged(caseData, caseDataBefore)) {
            if (caseData.isRespondentRepresentedByASolicitor()) {
                assignPartiesAccessService.grantRespondentSolicitor(caseData);
            }
            if (caseDataBefore.isRespondentRepresentedByASolicitor()) {
                assignPartiesAccessService.revokeRespondentSolicitor(caseDataBefore);
            }
        }
    }

    private boolean hasApplicantSolicitorChanged(FinremCaseData caseData, FinremCaseData caseDataBefore) {
        String currentEmail = caseData.getAppSolicitorEmail();
        String beforeEmail = caseDataBefore.getAppSolicitorEmail();
        Organisation currentOrg = Optional.ofNullable(caseData.getApplicantOrganisationPolicy())
            .map(OrganisationPolicy::getOrganisation)
            .orElse(null);
        Organisation beforeOrg = Optional.ofNullable(caseDataBefore.getApplicantOrganisationPolicy())
            .map(OrganisationPolicy::getOrganisation)
            .orElse(null);
        boolean isSameOrganisation = Organisation.isSameOrganisation(currentOrg, beforeOrg);
        return currentEmail != null && !currentEmail.equals(beforeEmail) || !isSameOrganisation;
    }

    private boolean hasRespondentSolicitorChanged(FinremCaseData caseData, FinremCaseData caseDataBefore) {
        String currentEmail = caseData.getRespondentSolicitorEmail();
        String beforeEmail = caseDataBefore.getRespondentSolicitorEmail();
        Organisation currentOrg = Optional.ofNullable(caseData.getRespondentOrganisationPolicy())
            .map(OrganisationPolicy::getOrganisation)
            .orElse(null);
        Organisation beforeOrg = Optional.ofNullable(caseDataBefore.getRespondentOrganisationPolicy())
            .map(OrganisationPolicy::getOrganisation)
            .orElse(null);
        boolean isSameOrganisation = Organisation.isSameOrganisation(currentOrg, beforeOrg);
        return currentEmail != null && !currentEmail.equals(beforeEmail) || !isSameOrganisation;
    }
}
