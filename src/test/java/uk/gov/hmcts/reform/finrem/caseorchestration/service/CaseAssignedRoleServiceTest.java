package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DataStoreClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRolesResource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SERVICE_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_USER_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_POLICY;

@ExtendWith(MockitoExtension.class)
class CaseAssignedRoleServiceTest {

    private CaseAssignedRoleService caseAssignedRoleService;
    private static final String OTHER_ROLES = "otherRoles";

    @Mock
    private DataStoreClient dataStoreClient;

    @Mock
    private CaseDataService caseDataService;

    @Mock
    private CaseAssignedUserRolesResource caseAssignedUserRolesResource;

    @Mock
    private CaseDetails caseDetails;

    @Mock
    private List<CaseAssignedUserRole> userRoles;

    @Mock
    private CaseAssignedUserRole caseAssignedUserRole;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private IdamService idamService;

    @BeforeEach
    void setUp() {
        Map<String, Object> caseData = new HashMap<>();
        caseDetails = CaseDetails.builder().id(Long.valueOf(CASE_ID)).data(caseData).build();
        caseAssignedRoleService = new CaseAssignedRoleService(dataStoreClient, caseDataService, authTokenGenerator, idamService);
    }

    @Test
    void setCaseAssignedRoleSuccessAppSolicitor() {
        mockMethodCalls(APP_SOLICITOR_POLICY, false);

        Map<String, Object> returnedValue = caseAssignedRoleService.setCaseAssignedUserRole(caseDetails, AUTH_TOKEN);
        assertThat(returnedValue).hasSize(2)
            .containsEntry(APPLICANT_REPRESENTED, YES_VALUE)
            .containsEntry(CASE_ROLE, APP_SOLICITOR_POLICY);
    }

    @Test
    void setCaseAssignedRoleSuccessRespSolicitorConsented() {
        mockMethodCalls(RESP_SOLICITOR_POLICY, true);

        Map<String, Object> returnedValue = caseAssignedRoleService.setCaseAssignedUserRole(caseDetails, AUTH_TOKEN);
        assertThat(returnedValue).hasSize(2)
            .containsEntry(CONSENTED_RESPONDENT_REPRESENTED, YES_VALUE)
            .containsEntry(CASE_ROLE, RESP_SOLICITOR_POLICY);
    }

    @Test
    void setCaseAssignedRoleSuccessRespSolicitorContested() {
        mockMethodCalls(RESP_SOLICITOR_POLICY, false);

        Map<String, Object> returnedValue = caseAssignedRoleService.setCaseAssignedUserRole(caseDetails, AUTH_TOKEN);
        assertThat(returnedValue).hasSize(2)
            .containsEntry(CONTESTED_RESPONDENT_REPRESENTED, YES_VALUE)
            .containsEntry(CASE_ROLE, RESP_SOLICITOR_POLICY);
    }

    @Test
    void setCaseAssignedRoleReturningOtherRoles() {
        mockMethodCalls(OTHER_ROLES, false);

        Map<String, Object> returnedValue = caseAssignedRoleService.setCaseAssignedUserRole(caseDetails, AUTH_TOKEN);
        assertThat(returnedValue).hasSize(1)
            .containsEntry(CASE_ROLE, OTHER_ROLES);
    }

    @Test
    void getCaseAssignedUserRoleWhenCaseIdPassed() {
        mockMethodCalls(OTHER_ROLES, false);

        CaseAssignedUserRolesResource caseAssignedUserRole1 =
            caseAssignedRoleService.getCaseAssignedUserRole(String.valueOf(caseDetails.getId()), AUTH_TOKEN);
        assertEquals(OTHER_ROLES, caseAssignedUserRole1.getCaseAssignedUserRoles().getFirst().getCaseRole());
    }

    @Test
    void getCaseAssignedUserRole() {
        mockMethodCalls(OTHER_ROLES, false);

        CaseAssignedUserRolesResource caseAssignedUserRole1 =
            caseAssignedRoleService.getCaseAssignedUserRole(caseDetails.getId().toString(), AUTH_TOKEN);
        assertEquals(OTHER_ROLES, caseAssignedUserRole1.getCaseAssignedUserRoles().getFirst().getCaseRole());
    }

    private void mockMethodCalls(String role, boolean isConsentedApplication) {
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(idamService.getIdamUserId(AUTH_TOKEN)).thenReturn(TEST_USER_ID);
        when(dataStoreClient.getUserRoles(AUTH_TOKEN, TEST_SERVICE_TOKEN, caseDetails.getId().toString(), TEST_USER_ID))
            .thenReturn(caseAssignedUserRolesResource);
        when(caseAssignedUserRolesResource.getCaseAssignedUserRoles()).thenReturn(userRoles);
        when(userRoles.getFirst()).thenReturn(caseAssignedUserRole);
        when(caseAssignedUserRole.getCaseRole()).thenReturn(role);
        lenient().when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(isConsentedApplication);
    }
}
