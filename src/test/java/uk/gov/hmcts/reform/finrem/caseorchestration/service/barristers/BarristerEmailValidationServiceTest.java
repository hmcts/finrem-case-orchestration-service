package uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PrdOrganisationService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdServiceTest.AUTH_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class BarristerEmailValidationServiceTest {

    private static final String REGISTERED_EMAIL = "email";
    private static final String ANOTHER_REGISTERED_EMAIL = "email3";
    private static final String NON_REGISTERED_EMAIL = "email2";

    private static final Barrister VALID_BARRISTER = Barrister.builder()
        .email(REGISTERED_EMAIL)
        .build();
    private static final Barrister INVALID_BARRISTER = Barrister.builder()
        .email(NON_REGISTERED_EMAIL)
        .build();

    @Mock
    private PrdOrganisationService organisationService;

    @InjectMocks
    private BarristerEmailValidationService barristerEmailValidationService;

    @Test
    public void givenRegisteredBarrister_whenValidateBarristerEmail_thenValidateWithNoErrors() {
        when(organisationService.findUserByEmail(REGISTERED_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of("UserId"));

        List<String> actualErrors = barristerEmailValidationService.validateBarristerEmails(
            List.of(BarristerData.builder().barrister(VALID_BARRISTER).build()), AUTH_TOKEN);

        assertThat(actualErrors).isEmpty();
    }

    @Test
    public void givenMultipleRegisteredBarristers_whenValidateBarristerEmails_thenValidateWithNoErrors() {
        when(organisationService.findUserByEmail(REGISTERED_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of("UserId"));
        when(organisationService.findUserByEmail(ANOTHER_REGISTERED_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of("AnotherUserId"));

        List<String> actualErrors = barristerEmailValidationService.validateBarristerEmails(List.of(
            buildValidBarristerData(REGISTERED_EMAIL),
            buildValidBarristerData(ANOTHER_REGISTERED_EMAIL)), AUTH_TOKEN);

        assertThat(actualErrors).isEmpty();
    }

    @Test
    public void givenMultipleUnregisteredBarristers_whenValidateBarristerEmails_thenReturnCorrectErrorMessages() {
        when(organisationService.findUserByEmail(NON_REGISTERED_EMAIL, AUTH_TOKEN)).thenReturn(Optional.empty());

        List<String> actualErrors = barristerEmailValidationService.validateBarristerEmails(List.of(
            buildInvalidBarristerData(),
            buildInvalidBarristerData()), AUTH_TOKEN);

        assertThat(actualErrors).containsExactly(
            """
                Email address for Barrister 1 is not registered with myHMCTS.
                They can register at https://manage-org.platform.hmcts.net/register-org/register""",
            """
                Email address for Barrister 2 is not registered with myHMCTS.
                They can register at https://manage-org.platform.hmcts.net/register-org/register"""
        );
    }

    private BarristerData buildValidBarristerData(String email) {
        return BarristerData.builder()
            .barrister(VALID_BARRISTER.toBuilder()
                .email(email)
                .build())
            .build();
    }

    private BarristerData buildInvalidBarristerData() {
        return BarristerData.builder()
            .barrister(INVALID_BARRISTER)
            .build();
    }
}