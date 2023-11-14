package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

class OrgPolicyServiceTest {
    private OrgPolicyService orgPolicyService;

    @BeforeEach
    void setUp() {
        orgPolicyService = new OrgPolicyService();
    }

    @Test
    void givenContestedCase_whenBothPartyUnRepresented_thenAddDefaultRole() {
        FinremCaseDetails finremCaseDetails = getFinremCaseDataDetails();
        FinremCaseData data = finremCaseDetails.getData();
        orgPolicyService.setDefaultOrgIfNotSetAlready(data, finremCaseDetails.getId());

        assertEquals(CaseRole.APP_SOLICITOR.getCcdCode(),
            data.getApplicantOrganisationPolicy().getOrgPolicyCaseAssignedRole());
        assertEquals(CaseRole.RESP_SOLICITOR.getCcdCode(),
            data.getRespondentOrganisationPolicy().getOrgPolicyCaseAssignedRole());
    }

    @Test
    void givenContestedCase_whenRespondentUnRepresented_thenAddDefaultRole() {
        FinremCaseDetails finremCaseDetails = getFinremCaseDataDetails();
        FinremCaseData data = finremCaseDetails.getData();
        data.setApplicantOrganisationPolicy(getOrganisationPolicy(CaseRole.APP_SOLICITOR));
        orgPolicyService.setDefaultOrgIfNotSetAlready(data, finremCaseDetails.getId());
        assertEquals(CaseRole.APP_SOLICITOR.getCcdCode(),
            data.getApplicantOrganisationPolicy().getOrgPolicyCaseAssignedRole());
        assertEquals("XX9191910", data.getApplicantOrganisationPolicy().getOrgPolicyReference());
        assertNull(data.getRespondentOrganisationPolicy().getOrgPolicyReference());
        assertEquals(CaseRole.RESP_SOLICITOR.getCcdCode(),
            data.getRespondentOrganisationPolicy().getOrgPolicyCaseAssignedRole());
    }

    @Test
    void givenContestedCase_whenApplicantUnRepresented_thenAddDefaultRole() {
        FinremCaseDetails finremCaseDetails = getFinremCaseDataDetails();
        FinremCaseData data = finremCaseDetails.getData();
        data.setRespondentOrganisationPolicy(getOrganisationPolicy(CaseRole.RESP_SOLICITOR));
        orgPolicyService.setDefaultOrgIfNotSetAlready(data, finremCaseDetails.getId());
        assertEquals(CaseRole.APP_SOLICITOR.getCcdCode(),
            data.getApplicantOrganisationPolicy().getOrgPolicyCaseAssignedRole());
        assertNull(data.getApplicantOrganisationPolicy().getOrgPolicyReference());
        assertEquals(CaseRole.RESP_SOLICITOR.getCcdCode(),
            data.getRespondentOrganisationPolicy().getOrgPolicyCaseAssignedRole());
        assertEquals("XX9191910", data.getRespondentOrganisationPolicy().getOrgPolicyReference());
    }

    private OrganisationPolicy getOrganisationPolicy(CaseRole role) {
        return OrganisationPolicy
            .builder()
            .organisation(Organisation.builder().organisationID("abc")
                .organisationName("abc limited").build())
            .orgPolicyReference("XX9191910")
            .orgPolicyCaseAssignedRole(role.getCcdCode())
            .build();
    }

    private FinremCaseDetails getFinremCaseDataDetails() {
        return FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).build();
    }
}