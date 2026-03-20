package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
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

    static Stream<Arguments> shouldEvaluateOrganisationEquality() {
        Organisation org1 = mock(Organisation.class);
        Organisation org2 = mock(Organisation.class);

        OrganisationPolicy policyWithOrg1 = mock(OrganisationPolicy.class);
        OrganisationPolicy policyWithOrg2 = mock(OrganisationPolicy.class);
        OrganisationPolicy policyWithNullOrg = mock(OrganisationPolicy.class);

        when(policyWithOrg1.getOrganisation()).thenReturn(org1);
        when(policyWithOrg2.getOrganisation()).thenReturn(org2);
        when(policyWithNullOrg.getOrganisation()).thenReturn(null);

        return Stream.of(
            // same organisation
            Arguments.of(policyWithOrg1, policyWithOrg1, org1, org1, true),

            // different organisations
            Arguments.of(policyWithOrg1, policyWithOrg2, org1, org2, false),

            // both policies null
            Arguments.of(null, null, null, null, true),

            // one policy null
            Arguments.of(policyWithOrg1, null, org1, null, false),

            // both organisations null inside policies
            Arguments.of(policyWithNullOrg, policyWithNullOrg, null, null, true)
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldEvaluateOrganisationEquality(OrganisationPolicy policy1,
                                            OrganisationPolicy policy2,
                                            Organisation org1,
                                            Organisation org2,
                                            boolean expected) {

        try (var mockedStatic = mockStatic(Organisation.class)) {
            mockedStatic.when(() ->
                Organisation.isSameOrganisation(org1, org2)
            ).thenReturn(expected);

            boolean result = OrganisationPolicy.isSameOrganisation(policy1, policy2);

            assertThat(result).isEqualTo(expected);
        }
    }
}
