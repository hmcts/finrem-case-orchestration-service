package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NocLetterNotificationService;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SolicitorAccessService {

    private final AssignPartiesAccessService assignPartiesAccessService;
    private final NotificationService notificationService;
    private final NocLetterNotificationService nocLetterNotificationService;
    private final FinremCaseDetailsMapper finremCaseDetailsMapper;

    /**
     * Checks for changes in solicitor details and updates access accordingly.
     *
     */
    public void checkAndAssignSolicitorAccess(FinremCaseData caseData,
                                              FinremCaseData caseDataBefore,
                                              List<String> errors) {

        // Applicant solicitor access update
        if (hasApplicantSolicitorChanged(caseData, caseDataBefore)) {
            try {
                updateApplicantSolicitor(caseData, caseDataBefore);
            } catch (UserNotFoundInOrganisationApiException e) {
                errors.add("Unable to update Applicant Solicitor access for Case");
            }
        }

        // Respondent solicitor access update
        if (hasRespondentSolicitorChanged(caseData, caseDataBefore)) {
            try {
                updateRespondentSolicitor(caseData, caseDataBefore);
            } catch (UserNotFoundInOrganisationApiException e) {
                errors.add("Unable to update Respondent Solicitor access for Case");
            }
        }
    }

    /**
     * This method checks if the contact details update includes a representative change and sends Notice of Change
     * notifications to the caseworker if required.
     *
     */
    public void sendNoticeOfChangeNotificationsCaseworker(FinremCallbackRequest callbackRequest,
                                                          String userAuthorisation) {

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        Optional<ContactDetailsWrapper> contactDetailsWrapper = Optional.ofNullable(caseDetails.getData().getContactDetailsWrapper());
        boolean requiresNotifications = contactDetailsWrapper
            .map(ContactDetailsWrapper::getUpdateIncludesRepresentativeChange)
            .filter(YesOrNo.YES::equals)
            .isPresent();
        if (!requiresNotifications) {
            return;
        }

        log.info("Received request to send Notice of Change email and letter for Case ID: {}", caseDetails.getId());
        notificationService.sendNoticeOfChangeEmailCaseworker(caseDetails);

        log.info("Call the noc letter service");
        nocLetterNotificationService.sendNoticeOfChangeLetters(
            finremCaseDetailsMapper.mapToCaseDetails(caseDetails),
            finremCaseDetailsMapper.mapToCaseDetails(callbackRequest.getCaseDetailsBefore()), userAuthorisation);

        contactDetailsWrapper.ifPresent(wrapper -> {
            wrapper.setUpdateIncludesRepresentativeChange(null);
            wrapper.setNocParty(null);
        });
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
        String currentOrgId = Optional.ofNullable(caseData.getApplicantOrganisationPolicy())
            .map(orgPolicy -> orgPolicy.getOrganisation() != null ? orgPolicy.getOrganisation().getOrganisationID() : "").orElse("");
        String beforeOrgId = Optional.ofNullable(caseDataBefore.getApplicantOrganisationPolicy())
            .map(orgPolicy -> orgPolicy.getOrganisation() != null ? orgPolicy.getOrganisation().getOrganisationID() : "").orElse("");
        return !currentEmail.equals(beforeEmail) || !currentOrgId.equals(beforeOrgId);
    }

    private boolean hasRespondentSolicitorChanged(FinremCaseData caseData, FinremCaseData caseDataBefore) {
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
