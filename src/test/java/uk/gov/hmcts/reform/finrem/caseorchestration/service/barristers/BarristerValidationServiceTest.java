package uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PrdOrganisationService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.APP_BARRISTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdServiceTest.AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class BarristerValidationServiceTest {

    private static final String REGISTERED_EMAIL = "email";
    private static final String ANOTHER_REGISTERED_EMAIL = "email3";
    private static final String CASE_ID = "1234567890";

    private static final Barrister VALID_BARRISTER = Barrister.builder()
        .email(REGISTERED_EMAIL)
        .build();

    @Mock
    private PrdOrganisationService organisationService;
    @Mock
    private AssignCaseAccessService assignCaseAccessService;

    @InjectMocks
    private BarristerValidationService barristerValidationService;

    @Test
    void givenRegisteredBarrister_whenValidateBarristerEmail_thenValidateWithNoErrors() {
        when(organisationService.findUserByEmail(REGISTERED_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of("UserId"));
        when(assignCaseAccessService.isLegalCounselRepresentingOpposingLitigant(any(), any(), any())).thenReturn(false);

        List<String> actualErrors = barristerValidationService.validateBarristerEmails(
            List.of(BarristerData.builder()
                .barrister(VALID_BARRISTER)
                .build()),
            AUTH_TOKEN, CASE_ID, APP_BARRISTER);

        assertThat(actualErrors).isEmpty();
    }

    @Test
    void givenMultipleRegisteredBarristers_whenValidateBarristerEmails_thenValidateWithNoErrors() {
        when(organisationService.findUserByEmail(REGISTERED_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of("UserId"));
        when(organisationService.findUserByEmail(ANOTHER_REGISTERED_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of("AnotherUserId"));
        when(assignCaseAccessService.isLegalCounselRepresentingOpposingLitigant(eq("UserId"), any(), any())).thenReturn(false);
        when(assignCaseAccessService.isLegalCounselRepresentingOpposingLitigant(eq("AnotherUserId"), any(), any())).thenReturn(false);

        List<String> actualErrors = barristerValidationService.validateBarristerEmails(List.of(
            buildValidBarristerData(REGISTERED_EMAIL),
            buildValidBarristerData(ANOTHER_REGISTERED_EMAIL)), AUTH_TOKEN, CASE_ID, APP_BARRISTER);

        assertThat(actualErrors).isEmpty();
    }

    @Test
    void givenMultipleUnregisteredBarristers_whenValidateBarristerEmails_thenReturnCorrectErrorMessages() {
        when(organisationService.findUserByEmail("barrister1@test.com", AUTH_TOKEN)).thenReturn(Optional.empty());
        when(organisationService.findUserByEmail("barrister2@test.com", AUTH_TOKEN)).thenReturn(Optional.empty());

        List<String> actualErrors = barristerValidationService.validateBarristerEmails(List.of(
            buildInvalidBarristerData("barrister1@test.com"),
            buildInvalidBarristerData("barrister2@test.com")), AUTH_TOKEN, CASE_ID, APP_BARRISTER);

        assertThat(actualErrors).containsExactly(
            """
                Email address for Barrister 1 is not registered with myHMCTS.
                They can register at https://manage-org.platform.hmcts.net/register-org/register""",
            """
                Email address for Barrister 2 is not registered with myHMCTS.
                They can register at https://manage-org.platform.hmcts.net/register-org/register"""
        );
    }

    @ParameterizedTest
    @EnumSource(value = CaseRole.class, names = {"APP_BARRISTER", "RESP_BARRISTER",
        "INTVR_BARRISTER_1", "INTVR_BARRISTER_2", "INTVR_BARRISTER_3", "INTVR_BARRISTER_4"})
    void givenBarristerHasRepresentedOpposingLitigant_whenValidateBarristers_thenReturnCorrectErrorMessage(CaseRole barristerCaseRole) {
        when(organisationService.findUserByEmail(REGISTERED_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of("UserId"));
        when(organisationService.findUserByEmail(ANOTHER_REGISTERED_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of("AnotherUserId"));
        when(assignCaseAccessService.isLegalCounselRepresentingOpposingLitigant(eq("UserId"), any(), any())).thenReturn(true);
        when(assignCaseAccessService.isLegalCounselRepresentingOpposingLitigant(eq("AnotherUserId"), any(), any())).thenReturn(true);

        List<String> actualErrors = barristerValidationService.validateBarristerEmails(List.of(
            buildValidBarristerData(REGISTERED_EMAIL),
            buildValidBarristerData(ANOTHER_REGISTERED_EMAIL)), AUTH_TOKEN, CASE_ID, barristerCaseRole);

        assertThat(actualErrors).containsExactly(
            "Barrister 1 is already representing another party on this case",
            "Barrister 2 is already representing another party on this case"
        );
    }

    @Test
    void givenBarristersWithSameEmail_whenValidateBarristers_thenReturnErrorMessage() {
        List<String> actualErrors = barristerValidationService.validateBarristerEmails(List.of(
            buildValidBarristerData("barrister1@test.com"),
            buildValidBarristerData("barrister1@test.com")), AUTH_TOKEN, CASE_ID, APP_BARRISTER);

        assertThat(actualErrors).containsOnly("Duplicate barrister email: barrister1@test.com");
    }

    private BarristerData buildValidBarristerData(String email) {
        return BarristerData.builder()
            .barrister(VALID_BARRISTER.toBuilder()
                .email(email)
                .build())
            .build();
    }

    private BarristerData buildInvalidBarristerData(String emailAddress) {
        return BarristerData.builder()
            .barrister(Barrister.builder()
                .email(emailAddress)
                .build())
            .build();
    }
}
