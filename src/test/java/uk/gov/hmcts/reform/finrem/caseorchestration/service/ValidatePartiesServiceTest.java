package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_ORG2_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_ORG_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_USER_ID;

@ExtendWith(MockitoExtension.class)
class ValidatePartiesServiceTest {

    @InjectMocks
    private ValidatePartiesService validatePartiesService;

    @Mock
    private PrdOrganisationService organisationService;

    @Test
    void givenEmailRegistered_whenEmailInOrg_shouldReturnTrue() {
        when(organisationService.findUserByEmail(TEST_SOLICITOR_EMAIL)).thenReturn(Optional.of(TEST_USER_ID));
        when(organisationService.findOrganisationIdByUserId(TEST_USER_ID)).thenReturn(Optional.of(TEST_ORG_ID));

        assertTrue(validatePartiesService.isEmailRegisteredInOrg(TEST_SOLICITOR_EMAIL, TEST_ORG_ID));
    }

    @Test
    void givenEmailNotRegistered_shouldReturnFalse() {
        when(organisationService.findUserByEmail(TEST_SOLICITOR_EMAIL)).thenReturn(Optional.empty());
        assertFalse(validatePartiesService.isEmailRegisteredInOrg(TEST_SOLICITOR_EMAIL, TEST_ORG_ID));
    }

    @Test
    void givenEmailRegistered_whenEmailNotInOrg_shouldReturnFalse() {
        when(organisationService.findUserByEmail(TEST_SOLICITOR_EMAIL)).thenReturn(Optional.of(TEST_USER_ID));
        when(organisationService.findOrganisationIdByUserId(TEST_USER_ID)).thenReturn(Optional.of(TEST_ORG2_ID));

        assertFalse(validatePartiesService.isEmailRegisteredInOrg(TEST_SOLICITOR_EMAIL, TEST_ORG_ID));
    }

    @Test
    void givenUserIdNotFound_shouldReturnFalse() {
        when(organisationService.findUserByEmail(TEST_SOLICITOR_EMAIL)).thenReturn(Optional.of(TEST_USER_ID));
        when(organisationService.findOrganisationIdByUserId(TEST_USER_ID)).thenReturn(Optional.empty());

        assertFalse(validatePartiesService.isEmailRegisteredInOrg(TEST_SOLICITOR_EMAIL, TEST_ORG_ID));
    }
}
