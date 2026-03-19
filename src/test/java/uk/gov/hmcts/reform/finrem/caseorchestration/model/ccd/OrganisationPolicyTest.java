package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrganisationPolicyTest {

    @Test
    void shouldReturnDefaultOrganisationPolicy() {
        CaseRole role = mock(CaseRole.class);
        when(role.getCcdCode()).thenReturn("[APPLICANT]");

        OrganisationPolicy result = OrganisationPolicy.getDefaultOrganisationPolicy(role);

        assertThat(result).isNotNull();
        assertThat(result.getOrgPolicyCaseAssignedRole()).isEqualTo("[APPLICANT]");
        assertThat(result.getOrgPolicyReference()).isNull();

        assertThat(result.getOrganisation()).isNotNull();
        assertThat(result.getOrganisation().getOrganisationID()).isNull();
        assertThat(result.getOrganisation().getOrganisationName()).isNull();
    }
}
