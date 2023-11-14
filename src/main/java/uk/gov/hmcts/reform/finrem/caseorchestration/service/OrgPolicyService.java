package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrgPolicyService {

    public void setDefaultOrgIfNotSetAlready(FinremCaseData caseData, Long caseId) {
        OrganisationPolicy appPolicy = caseData.getApplicantOrganisationPolicy();
        log.info("Applicant existing org policy {} for caseId {}", appPolicy, caseId);
        if (appPolicy == null) {
            OrganisationPolicy organisationPolicy = getOrganisationPolicy(CaseRole.APP_SOLICITOR);
            caseData.setApplicantOrganisationPolicy(organisationPolicy);
        }
        OrganisationPolicy respPolicy = caseData.getRespondentOrganisationPolicy();
        log.info("Respondent existing org policy {} for caseId {}", respPolicy, caseId);
        if (respPolicy == null) {
            OrganisationPolicy organisationPolicy = getOrganisationPolicy(CaseRole.RESP_SOLICITOR);
            caseData.setRespondentOrganisationPolicy(organisationPolicy);
        }
    }

    private OrganisationPolicy getOrganisationPolicy(CaseRole role) {
        return OrganisationPolicy
                .builder()
                .organisation(Organisation.builder().organisationID(null).organisationName(null).build())
                .orgPolicyReference(null)
                .orgPolicyCaseAssignedRole(role.getCcdCode())
                .build();
    }
}
