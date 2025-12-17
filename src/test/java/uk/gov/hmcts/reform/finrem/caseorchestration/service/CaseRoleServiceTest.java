package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.APP_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.RESP_SOLICITOR;

@ExtendWith(MockitoExtension.class)
class CaseRoleServiceTest {

    @InjectMocks
    private CaseRoleService caseRoleService;

    @Mock
    private CaseAssignedRoleService caseAssignedRoleService;

    @Test
    void givenNullAssignedRoles_whenGetUserCaseRole_thenReturnNull() {
        // given
        when(caseAssignedRoleService.getCaseAssignedUserRole(CASE_ID, AUTH_TOKEN))
            .thenReturn(null);

        // when
        CaseRole result = caseRoleService.getUserCaseRole(CASE_ID, AUTH_TOKEN);

        // then
        assertThat(result).isNull();
        assertFalse(caseRoleService.isApplicantRepresentative(FinremCaseData.builder().ccdCaseId(CASE_ID).build(), AUTH_TOKEN));
    }

    @Test
    void givenNullAssignedRoles_whenGetUserOrCaseworkerCaseRole_thenReturnCaseworker() {
        // given
        when(caseAssignedRoleService.getCaseAssignedUserRole(CASE_ID, AUTH_TOKEN))
            .thenReturn(null);

        // when
        CaseRole result = caseRoleService.getUserOrCaseworkerCaseRole(CASE_ID, AUTH_TOKEN);

        // then
        assertThat(result).isEqualTo(CaseRole.CASEWORKER);
        assertFalse(caseRoleService.isApplicantRepresentative(FinremCaseData.builder().ccdCaseId(CASE_ID).build(), AUTH_TOKEN));
    }

    @Test
    void givenEmptyAssignedRoles_whenGetUserCaseRole_thenReturnNull() {
        // given
        CaseAssignedUserRolesResource resource = new CaseAssignedUserRolesResource();
        resource.setCaseAssignedUserRoles(List.of());

        when(caseAssignedRoleService.getCaseAssignedUserRole(CASE_ID, AUTH_TOKEN)).thenReturn(resource);

        // when
        CaseRole result = caseRoleService.getUserCaseRole(CASE_ID, AUTH_TOKEN);

        // then
        assertThat(result).isNull();
        assertFalse(caseRoleService.isApplicantRepresentative(FinremCaseData.builder().ccdCaseId(CASE_ID).build(), AUTH_TOKEN));
    }

    @Test
    void givenEmptyAssignedRoles_whenGetUserOrCaseworkerCaseRole_thenReturnCaseworker() {
        // given
        CaseAssignedUserRolesResource resource = new CaseAssignedUserRolesResource();
        resource.setCaseAssignedUserRoles(List.of());

        when(caseAssignedRoleService.getCaseAssignedUserRole(CASE_ID, AUTH_TOKEN)).thenReturn(resource);

        // when
        CaseRole result = caseRoleService.getUserOrCaseworkerCaseRole(CASE_ID, AUTH_TOKEN);

        // then
        assertThat(result).isEqualTo(CaseRole.CASEWORKER);
        assertFalse(caseRoleService.isApplicantRepresentative(FinremCaseData.builder().ccdCaseId(CASE_ID).build(), AUTH_TOKEN));
    }

    @ParameterizedTest
    @CsvSource({
        "APP_SOLICITOR, true",
        "APP_BARRISTER, true",
        "RESP_SOLICITOR, false",
        "RESP_BARRISTER, false"
    })
    void givenSingleRoleAssigned_whenGetUserCaseRole_thenReturnCaseRole(String roleName, boolean isApplicantTest) {
        // given
        CaseRole caseRole = CaseRole.valueOf(roleName);
        CaseAssignedUserRole userRole = CaseAssignedUserRole.builder().caseRole(caseRole.getCcdCode()).build();

        CaseAssignedUserRolesResource resource = new CaseAssignedUserRolesResource();
        resource.setCaseAssignedUserRoles(List.of(userRole));

        when(caseAssignedRoleService.getCaseAssignedUserRole(CASE_ID, AUTH_TOKEN)).thenReturn(resource);

        // when
        CaseRole result = caseRoleService.getUserCaseRole(CASE_ID, AUTH_TOKEN);

        // then
        assertThat(result).isEqualTo(caseRole);
        FinremCaseData caseData = FinremCaseData.builder().ccdCaseId(CASE_ID).build();
        assertIsApplicantRepresentative(caseData, caseRole, isApplicantTest);
        assertIsRespondentRepresentative(caseData, caseRole, isApplicantTest);
    }

    @ParameterizedTest
    @CsvSource({
        "APP_SOLICITOR, true",
        "APP_BARRISTER, true",
        "RESP_SOLICITOR, false",
        "RESP_BARRISTER, false"
    })
    void givenSingleRoleAssigned_whenGetUserOrCaseworkerCaseRole_thenReturnCaseRole(String roleName, boolean isApplicantTest) {
        // given
        CaseRole caseRole = CaseRole.valueOf(roleName);
        CaseAssignedUserRole userRole = CaseAssignedUserRole.builder().caseRole(caseRole.getCcdCode()).build();

        CaseAssignedUserRolesResource resource = new CaseAssignedUserRolesResource();
        resource.setCaseAssignedUserRoles(List.of(userRole));

        when(caseAssignedRoleService.getCaseAssignedUserRole(CASE_ID, AUTH_TOKEN)).thenReturn(resource);

        // when
        CaseRole result = caseRoleService.getUserOrCaseworkerCaseRole(CASE_ID, AUTH_TOKEN);

        // then
        assertThat(result).isEqualTo(caseRole);
        FinremCaseData caseData = FinremCaseData.builder().ccdCaseId(CASE_ID).build();
        assertIsApplicantRepresentative(caseData, caseRole, isApplicantTest);
        assertIsRespondentRepresentative(caseData, caseRole, isApplicantTest);
    }

    @Test
    void givenMultipleRolesAssigned_whenGetUserCaseRole_thenReturnFirstCaseRole() {
        // given
        CaseAssignedUserRole appSolicitor = CaseAssignedUserRole.builder().caseRole(APP_SOLICITOR.getCcdCode()).build();
        CaseAssignedUserRole respSolicitor = CaseAssignedUserRole.builder().caseRole(RESP_SOLICITOR.getCcdCode()).build();

        CaseAssignedUserRolesResource resource = new CaseAssignedUserRolesResource();
        resource.setCaseAssignedUserRoles(List.of(appSolicitor, respSolicitor));

        when(caseAssignedRoleService.getCaseAssignedUserRole(CASE_ID, AUTH_TOKEN)).thenReturn(resource);

        // when
        CaseRole result = caseRoleService.getUserCaseRole(CASE_ID, AUTH_TOKEN);

        // then
        assertThat(result).isEqualTo(APP_SOLICITOR);
        assertTrue(caseRoleService.isApplicantRepresentative(FinremCaseData.builder().ccdCaseId(CASE_ID).build(), AUTH_TOKEN));
    }

    @Test
    void givenMultipleRolesAssigned_whenGetUserOrCaseworkerCaseRole_thenReturnFirstCaseRole() {
        // given
        CaseAssignedUserRole appSolicitor = CaseAssignedUserRole.builder().caseRole(APP_SOLICITOR.getCcdCode()).build();
        CaseAssignedUserRole respSolicitor = CaseAssignedUserRole.builder().caseRole(RESP_SOLICITOR.getCcdCode()).build();

        CaseAssignedUserRolesResource resource = new CaseAssignedUserRolesResource();
        resource.setCaseAssignedUserRoles(List.of(appSolicitor, respSolicitor));

        when(caseAssignedRoleService.getCaseAssignedUserRole(CASE_ID, AUTH_TOKEN)).thenReturn(resource);

        // when
        CaseRole result = caseRoleService.getUserOrCaseworkerCaseRole(CASE_ID, AUTH_TOKEN);

        // then
        assertThat(result).isEqualTo(APP_SOLICITOR);
        assertTrue(caseRoleService.isApplicantRepresentative(FinremCaseData.builder().ccdCaseId(CASE_ID).build(), AUTH_TOKEN));
    }

    private void assertIsRespondentRepresentative(FinremCaseData caseData, CaseRole caseRole, boolean isApplicantTest) {
        // For Respondent Check: The result should be the inverse of the expected test flag (!isApplicantTest)
        // - If APP_SOLICITOR (true), assert isRespondentRepresentative() is false.
        // - If RESP_SOLICITOR (false), assert isRespondentRepresentative() is true.
        assertThat(caseRoleService.isRespondentRepresentative(caseData, AUTH_TOKEN))
            .as("Role %s should match Respondent representative status %s", caseRole, !isApplicantTest)
            .isEqualTo(!isApplicantTest);
    }

    private void assertIsApplicantRepresentative(FinremCaseData caseData, CaseRole caseRole, boolean isApplicantTest) {
        // For Applicant Check: The result should equal the expected test flag (isApplicantTest)
        // - If APP_SOLICITOR (true), assert isApplicantRepresentative() is true.
        // - If RESP_SOLICITOR (false), assert isApplicantRepresentative() is false.
        assertThat(caseRoleService.isApplicantRepresentative(caseData, AUTH_TOKEN))
            .as("Role %s should match Applicant representative status %s", caseRole, isApplicantTest)
            .isEqualTo(isApplicantTest);
    }
}
