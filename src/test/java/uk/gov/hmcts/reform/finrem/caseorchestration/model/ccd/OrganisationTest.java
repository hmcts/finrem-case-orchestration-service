package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.organisation;

class OrganisationTest {

    @Test
    void shouldReturnTrue_whenOrganisationIdsAreTheSame() {
        assertTrue(Organisation.isSameOrganisation(organisation("A"), organisation("A")));
    }

    @MethodSource
    @ParameterizedTest
    void shouldReturnFalse_whenOrganisationsAreDifferentOrNull(Organisation org1, Organisation org2) {
        assertFalse(Organisation.isSameOrganisation(org1, org2));
    }

    private static Stream<Arguments> shouldReturnFalse_whenOrganisationsAreDifferentOrNull() {
        return Stream.of(
            Arguments.of(organisation("A "), organisation("A")),
            Arguments.of(organisation("A"), organisation("B")),
            Arguments.of(null, organisation("B")),
            Arguments.of(organisation(null), organisation("B")),
            Arguments.of(organisation(null), organisation(null)),
            Arguments.of(null, null)
        );
    }
}
