package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.AssignCaseAccessException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationsResponse;

import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_ORGANISATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_ORGANISATION_ID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssignApplicantSolicitorService {
    private final AssignCaseAccessService assignCaseAccessService;
    private final CcdDataStoreService ccdDataStoreService;
    private final FeatureToggleService featureToggleService;
    private final PrdOrganisationService prdOrganisationService;
    private final AssignCaseAccessException assignCaseAccessException;

    public void setApplicantSolicitor(CallbackRequest callbackRequest, String userAuthorisation) throws AssignCaseAccessException {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Received request for assign applicant solicitor for Case ID: {}", caseDetails.getId());

        if (featureToggleService.isAssignCaseAccessEnabled()) {
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
        } else {
            log.info("Assign case info not enabled, Case ID: {}", caseDetails.getId());
        }

    }

    private String getApplicantOrgId(CaseDetails caseDetails) {
        Map<String, Object> applicantOrgPolicy = (Map<String, Object>) caseDetails.getData().get(ORGANISATION_POLICY_APPLICANT);
        if (applicantOrgPolicy != null) {
            Map<String, Object> applicantOrganisation = (Map<String, Object>) applicantOrgPolicy.get(ORGANISATION_POLICY_ORGANISATION);

            if (applicantOrganisation != null) {
                return (String) applicantOrganisation.get(ORGANISATION_POLICY_ORGANISATION_ID);
            }
        }
        return null;
    }
}

