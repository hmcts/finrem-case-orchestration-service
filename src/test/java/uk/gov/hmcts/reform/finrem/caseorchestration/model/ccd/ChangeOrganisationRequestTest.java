package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChangeOrganisationRequestTest {

    @Test
    void whenBothAddAndRemoveAreNull_thenReturnTrue() {
        ChangeOrganisationRequest request = ChangeOrganisationRequest.builder().build();

        assertTrue(request.isNoOrganisationsToAddOrRemove());
    }

    @Test
    void whenOrganisationToAddHasId_thenReturnFalse() {
        ChangeOrganisationRequest request = ChangeOrganisationRequest.builder()
            .organisationToAdd(Organisation.builder().organisationID("ORG1").build())
            .build();

        assertFalse(request.isNoOrganisationsToAddOrRemove());
    }

    @Test
    void whenOrganisationToRemoveHasId_thenReturnFalse() {
        ChangeOrganisationRequest request = ChangeOrganisationRequest.builder()
            .organisationToRemove(Organisation.builder().organisationID("ORG1").build())
            .build();

        assertFalse(request.isNoOrganisationsToAddOrRemove());
    }

    @Test
    void whenBothAddAndRemoveHaveId_thenReturnFalse() {
        ChangeOrganisationRequest request = ChangeOrganisationRequest.builder()
            .organisationToAdd(Organisation.builder().organisationID("ORG1").build())
            .organisationToRemove(Organisation.builder().organisationID("ORG2").build())
            .build();

        assertFalse(request.isNoOrganisationsToAddOrRemove());
    }

    @Test
    void whenAddAndRemovePresentButIdsAreNull_thenReturnTrue() {
        ChangeOrganisationRequest request = ChangeOrganisationRequest.builder()
            .organisationToAdd(Organisation.builder().organisationID(null).build())
            .organisationToRemove(Organisation.builder().organisationID(null).build())
            .build();

        assertTrue(request.isNoOrganisationsToAddOrRemove());
    }
}
