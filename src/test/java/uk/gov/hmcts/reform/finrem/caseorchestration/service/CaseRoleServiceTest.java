package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.APP_SOLICITOR;

@ExtendWith(MockitoExtension.class)
class CaseRoleServiceTest {

    @InjectMocks
    private CaseRoleService caseRoleService;

    @Mock
    private CaseAssignedRoleService caseAssignedRoleService;

    @Test
    void shouldReturnNullWhenNoRolesAssigned() {
        // given
        when(caseAssignedRoleService.getCaseAssignedUserRole(CASE_ID, AUTH_TOKEN))
            .thenReturn(null);

        // when
        CaseRole result = caseRoleService.getUserCaseRole(CASE_ID, AUTH_TOKEN);

        // then
        assertThat(result).isNull();
    }

    @Test
    void shouldReturnNullWhenRoleListIsEmpty() {
        // given
        CaseAssignedUserRolesResource resource = new CaseAssignedUserRolesResource();
        resource.setCaseAssignedUserRoles(List.of());

        when(caseAssignedRoleService.getCaseAssignedUserRole(CASE_ID, AUTH_TOKEN)).thenReturn(resource);

        // when
        CaseRole result = caseRoleService.getUserCaseRole(CASE_ID, AUTH_TOKEN);

        // then
        assertThat(result).isNull();
    }

    @Test
    void shouldReturnMappedCaseRoleWhenPresent() {
        // given
        CaseAssignedUserRole userRole = CaseAssignedUserRole.builder().caseRole(APP_SOLICITOR.getCcdCode()).build();

        CaseAssignedUserRolesResource resource = new CaseAssignedUserRolesResource();
        resource.setCaseAssignedUserRoles(List.of(userRole));

        when(caseAssignedRoleService.getCaseAssignedUserRole(CASE_ID, AUTH_TOKEN)).thenReturn(resource);

        // when
        CaseRole result = caseRoleService.getUserCaseRole(CASE_ID, AUTH_TOKEN);

        // then
        assertThat(result).isEqualTo(APP_SOLICITOR);
    }
}
