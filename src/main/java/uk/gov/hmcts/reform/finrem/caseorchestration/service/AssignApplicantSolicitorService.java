package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.AssignCaseAccessException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationsResponse;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssignApplicantSolicitorService {
    private final AssignCaseAccessService assignCaseAccessService;
    private final CcdDataStoreService ccdDataStoreService;
    private final PrdOrganisationService prdOrganisationService;

    @SuppressWarnings("java:S1141")
    public void setApplicantSolicitor(FinremCaseDetails caseDetails, String userAuthorisation) throws AssignCaseAccessException {

        log.info("Received request for assign applicant solicitor for Case ID: {}", caseDetails.getId());

        try {
            String applicantOrgId = getApplicantOrgId(caseDetails);

            if (applicantOrgId != null) {
                OrganisationsResponse prdOrganisation = prdOrganisationService.retrieveOrganisationsData(userAuthorisation);

                if (prdOrganisation.getOrganisationIdentifier().equals(applicantOrgId)) {
                    log.info("Assigning case access for Case ID: {}", caseDetails.getId());
                    try {
                        assignCaseAccessService.assignCaseAccess(caseDetails, userAuthorisation);
                        ccdDataStoreService.removeCreatorRole(caseDetails, userAuthorisation);
                    } catch (Exception e) {
                        log.error("Assigning case access threw exception for Case ID: {}, {}",
                            caseDetails.getId(), e.getMessage());
                        throw new AssignCaseAccessException(e.getMessage());
                    }
                } else {
                    String errorMessage = "Applicant solicitor does not belong to chosen applicant organisation";
                    log.info("{} for Case ID: {}", errorMessage, caseDetails.getId());
                    throw new AssignCaseAccessException(errorMessage);
                }
            } else {
                String errorMessage = "Applicant organisation not selected";
                log.info("{} for Case ID: {}", errorMessage, caseDetails.getId());
                throw new AssignCaseAccessException(errorMessage);
            }
        } catch (Exception e) {
            log.error("Exception when trying to assign case access for Case ID: {}, {}",
                caseDetails.getId(), e.getMessage());
            throw new AssignCaseAccessException(e.getMessage());
        }

    }

    private String getApplicantOrgId(FinremCaseDetails caseDetails) {
        OrganisationPolicy applicantOrgPolicy = caseDetails.getData().getApplicantOrganisationPolicy();
        if (applicantOrgPolicy != null) {
            Organisation applicantOrganisation = applicantOrgPolicy.getOrganisation();
            if (applicantOrganisation != null) {
                return applicantOrganisation.getOrganisationID();
            }
        }
        return null;
    }
}

