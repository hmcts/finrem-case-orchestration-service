package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
        assertFalse(caseRoleService.isLoginWithApplicantSolicitor(FinremCaseData.builder().ccdCaseId(CASE_ID).build(), AUTH_TOKEN));
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
        assertFalse(caseRoleService.isLoginWithApplicantSolicitor(FinremCaseData.builder().ccdCaseId(CASE_ID).build(), AUTH_TOKEN));
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
        assertFalse(caseRoleService.isLoginWithApplicantSolicitor(FinremCaseData.builder().ccdCaseId(CASE_ID).build(), AUTH_TOKEN));
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
        assertFalse(caseRoleService.isLoginWithApplicantSolicitor(FinremCaseData.builder().ccdCaseId(CASE_ID).build(), AUTH_TOKEN));
    }

    @Test
    void givenSingleRoleAssigned_whenGetUserCaseRole_thenReturnCaseRole() {
        // given
        CaseAssignedUserRole userRole = CaseAssignedUserRole.builder().caseRole(APP_SOLICITOR.getCcdCode()).build();

        CaseAssignedUserRolesResource resource = new CaseAssignedUserRolesResource();
        resource.setCaseAssignedUserRoles(List.of(userRole));

        when(caseAssignedRoleService.getCaseAssignedUserRole(CASE_ID, AUTH_TOKEN)).thenReturn(resource);

        // when
        CaseRole result = caseRoleService.getUserCaseRole(CASE_ID, AUTH_TOKEN);

        // then
        assertThat(result).isEqualTo(APP_SOLICITOR);
        assertTrue(caseRoleService.isLoginWithApplicantSolicitor(FinremCaseData.builder().ccdCaseId(CASE_ID).build(), AUTH_TOKEN));
    }

    @Test
    void givenSingleRoleAssigned_whenGetUserOrCaseworkerCaseRole_thenReturnCaseRole() {
        // given
        CaseAssignedUserRole userRole = CaseAssignedUserRole.builder().caseRole(APP_SOLICITOR.getCcdCode()).build();

        CaseAssignedUserRolesResource resource = new CaseAssignedUserRolesResource();
        resource.setCaseAssignedUserRoles(List.of(userRole));

        when(caseAssignedRoleService.getCaseAssignedUserRole(CASE_ID, AUTH_TOKEN)).thenReturn(resource);

        // when
        CaseRole result = caseRoleService.getUserOrCaseworkerCaseRole(CASE_ID, AUTH_TOKEN);

        // then
        assertThat(result).isEqualTo(APP_SOLICITOR);
        assertTrue(caseRoleService.isLoginWithApplicantSolicitor(FinremCaseData.builder().ccdCaseId(CASE_ID).build(), AUTH_TOKEN));
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
        assertTrue(caseRoleService.isLoginWithApplicantSolicitor(FinremCaseData.builder().ccdCaseId(CASE_ID).build(), AUTH_TOKEN));
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
        assertTrue(caseRoleService.isLoginWithApplicantSolicitor(FinremCaseData.builder().ccdCaseId(CASE_ID).build(), AUTH_TOKEN));
    }
}
