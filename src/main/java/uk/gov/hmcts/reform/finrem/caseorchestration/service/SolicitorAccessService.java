package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SolicitorAccessService {

    private final AssignPartiesAccessService assignPartiesAccessService;

    /**
     * Adds Applicant solicitor to the case and grants access.
     *
     * @param caseData the current case data
     */
    public void addApplicantSolicitor(FinremCaseData caseData) {
        log.info("Adding solicitor for case ID: {}", caseData.getCcdCaseId());
        assignPartiesAccessService.grantApplicantSolicitor(caseData);
    }

    /**
     * Removes Applicant solicitor from the case and revokes access.
     *
     * @param caseData the current case data
     */
    public void removeApplicantSolicitor(FinremCaseData caseData) {
        log.info("Removing solicitor for case ID: {}", caseData.getCcdCaseId());
        assignPartiesAccessService.revokeApplicantSolicitor(caseData);
    }

    /**
     * Updates Applicant solicitor details and grants/revokes access as needed.
     *
     * @param caseData       the current case data
     * @param caseDataBefore the case data before the update
     */
    public void updateApplicantSolicitor(FinremCaseData caseData, FinremCaseData caseDataBefore) {
        if (hasApplicantSolicitorChanged(caseData, caseDataBefore)) {
            if (caseData.isApplicantRepresentedByASolicitor()) {
                addApplicantSolicitor(caseData);
            }
            if (caseDataBefore.isApplicantRepresentedByASolicitor()) {
                removeApplicantSolicitor(caseDataBefore);
            }
        }
    }

    /**
     * Adds Respondent solicitor to the case and grants access.
     *
     * @param caseData the current case data
     */
    public void addRespondentSolicitor(FinremCaseData caseData) {
        log.info("Adding respondent solicitor for case ID: {}", caseData.getCcdCaseId());
        assignPartiesAccessService.grantRespondentSolicitor(caseData);
    }

    /**
     * Removes Respondent solicitor from the case and revokes access.
     *
     * @param caseData the current case data
     */
    public void removeRespondentSolicitor(FinremCaseData caseData) {
        log.info("Removing respondent solicitor for case ID: {}", caseData.getCcdCaseId());
        assignPartiesAccessService.revokeRespondentSolicitor(caseData);
    }

    /**
     * Updates Respondent solicitor details and grants/revokes access as needed.
     *
     * @param caseData       the current case data
     * @param caseDataBefore the case data before the update
     */
    public void updateRespondentSolicitor(FinremCaseData caseData, FinremCaseData caseDataBefore) {
        if (hasRespondentSolicitorChanged(caseData, caseDataBefore)) {
            if (caseData.isRespondentRepresentedByASolicitor()) {
                addRespondentSolicitor(caseData);
            }
            if (caseDataBefore.isRespondentRepresentedByASolicitor()) {
                removeRespondentSolicitor(caseDataBefore);
            }
        }
    }

    /**
     * Checks if the Applicant solicitor has changed by comparing email and organisation policy.
     *
     * @param caseData       the current case data
     * @param caseDataBefore the case data before the update
     * @return true if solicitor has changed, false otherwise
     */
    public static boolean hasApplicantSolicitorChanged(FinremCaseData caseData, FinremCaseData caseDataBefore) {
        Optional<ContactDetailsWrapper> currentContact = Optional.ofNullable(caseData.getContactDetailsWrapper());
        Optional<ContactDetailsWrapper> beforeContact = Optional.ofNullable(caseDataBefore.getContactDetailsWrapper());
        String currentEmail = currentContact.map(ContactDetailsWrapper::getApplicantSolicitorEmail).orElse("");
        String beforeEmail = beforeContact.map(ContactDetailsWrapper::getApplicantSolicitorEmail).orElse("");
        String currentOrgId = Optional.ofNullable(caseData.getApplicantOrganisationPolicy())
            .map(orgPolicy -> orgPolicy.getOrganisation() != null ? orgPolicy.getOrganisation().getOrganisationID() : "").orElse("");
        String beforeOrgId = Optional.ofNullable(caseDataBefore.getApplicantOrganisationPolicy())
            .map(orgPolicy -> orgPolicy.getOrganisation() != null ? orgPolicy.getOrganisation().getOrganisationID() : "").orElse("");
        return !currentEmail.equals(beforeEmail) || !currentOrgId.equals(beforeOrgId);
    }

    /**
     * Checks if the Respondent solicitor has changed by comparing email and organisation policy.
     *
     * @param caseData       the current case data
     * @param caseDataBefore the case data before the update
     * @return true if solicitor has changed, false otherwise
     */
    public static boolean hasRespondentSolicitorChanged(FinremCaseData caseData, FinremCaseData caseDataBefore) {
        Optional<ContactDetailsWrapper> currentContact = Optional.ofNullable(caseData.getContactDetailsWrapper());
        Optional<ContactDetailsWrapper> beforeContact = Optional.ofNullable(caseDataBefore.getContactDetailsWrapper());
        String currentEmail = currentContact.map(ContactDetailsWrapper::getRespondentSolicitorEmail).orElse("");
        String beforeEmail = beforeContact.map(ContactDetailsWrapper::getRespondentSolicitorEmail).orElse("");
        String currentOrgId = Optional.ofNullable(caseData.getRespondentOrganisationPolicy())
            .map(orgPolicy -> orgPolicy.getOrganisation() != null ? orgPolicy.getOrganisation().getOrganisationID() : "").orElse("");
        String beforeOrgId = Optional.ofNullable(caseDataBefore.getRespondentOrganisationPolicy())
            .map(orgPolicy -> orgPolicy.getOrganisation() != null ? orgPolicy.getOrganisation().getOrganisationID() : "").orElse("");
        return !currentEmail.equals(beforeEmail) || !currentOrgId.equals(beforeOrgId);
    }
}
