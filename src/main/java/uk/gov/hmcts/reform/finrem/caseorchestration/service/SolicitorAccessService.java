package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;

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
                ContactDetailsWrapper contactDetailsWrapper = caseData.getContactDetailsWrapper();
                String appSolEmail = YesOrNo.isYes(contactDetailsWrapper.getApplicantRepresented())
                    ? contactDetailsWrapper.getApplicantSolicitorEmail() : null;
                log.info("There was a problem updating access to applicant solicitor: %s".formatted(appSolEmail));
                throw new UserNotFoundInOrganisationApiException();
            }
        }

        // Respondent solicitor access update
        if (hasRespondentSolicitorChanged(caseData, caseDataBefore)) {
            try {
                updateRespondentSolicitor(caseData, caseDataBefore);
            } catch (UserNotFoundInOrganisationApiException e) {
                ContactDetailsWrapper contactDetailsWrapper = caseData.getContactDetailsWrapper();
                String respSolEmail = YesOrNo.isYes(contactDetailsWrapper.getContestedRespondentRepresented())
                    ? contactDetailsWrapper.getRespondentSolicitorEmail() : null;
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
        Optional<ContactDetailsWrapper> currentContact = Optional.ofNullable(caseData.getContactDetailsWrapper());
        Optional<ContactDetailsWrapper> beforeContact = Optional.ofNullable(caseDataBefore.getContactDetailsWrapper());
        String currentEmail = currentContact.map(ContactDetailsWrapper::getApplicantSolicitorEmail).orElse("");
        String beforeEmail = beforeContact.map(ContactDetailsWrapper::getApplicantSolicitorEmail).orElse("");
        Organisation currentOrg = Optional.ofNullable(caseData.getApplicantOrganisationPolicy())
            .map(uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy::getOrganisation)
            .orElse(null);
        Organisation beforeOrg = Optional.ofNullable(caseDataBefore.getApplicantOrganisationPolicy())
            .map(uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy::getOrganisation)
            .orElse(null);
        boolean isSameOrganisation = Organisation.isSameOrganisation(currentOrg, beforeOrg);
        return !currentEmail.equals(beforeEmail) || !isSameOrganisation;
    }

    private boolean hasRespondentSolicitorChanged(FinremCaseData caseData, FinremCaseData caseDataBefore) {
        Optional<ContactDetailsWrapper> currentContact = Optional.ofNullable(caseData.getContactDetailsWrapper());
        Optional<ContactDetailsWrapper> beforeContact = Optional.ofNullable(caseDataBefore.getContactDetailsWrapper());
        String currentEmail = currentContact.map(ContactDetailsWrapper::getRespondentSolicitorEmail).orElse("");
        String beforeEmail = beforeContact.map(ContactDetailsWrapper::getRespondentSolicitorEmail).orElse("");
        Organisation currentOrg = Optional.ofNullable(caseData.getRespondentOrganisationPolicy())
            .map(uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy::getOrganisation)
            .orElse(null);
        Organisation beforeOrg = Optional.ofNullable(caseDataBefore.getRespondentOrganisationPolicy())
            .map(uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy::getOrganisation)
            .orElse(null);
        boolean isSameOrganisation = Organisation.isSameOrganisation(currentOrg, beforeOrg);
        return !currentEmail.equals(beforeEmail) || !isSameOrganisation;
    }
}
