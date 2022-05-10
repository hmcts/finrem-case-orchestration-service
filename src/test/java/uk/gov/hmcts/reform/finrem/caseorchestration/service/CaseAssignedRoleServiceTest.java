package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DataStoreClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRolesResource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_POLICY;

@RunWith(MockitoJUnitRunner.class)
public class CaseAssignedRoleServiceTest {

    private static final String SERVICE_AUTH_TOKEN = "serviceAuthToken";
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
    private  AuthTokenGenerator authTokenGenerator;

    @Mock
    private IdamService idamService;


    @Before
    public void setUp() {
        Map<String, Object> caseData = new HashMap<>();
        caseDetails = CaseDetails.builder().id(1234L).data(caseData).build();
        caseAssignedRoleService = new CaseAssignedRoleService(dataStoreClient, caseDataService, authTokenGenerator, idamService);
    }

    @Test
    public void setCaseAssignedRoleSuccessAppSolicitor() {

        mockMethodCalls(APP_SOLICITOR_POLICY, false);

        Map<String, Object> returnedValue = caseAssignedRoleService.setCaseAssignedUserRole(caseDetails, AUTH_TOKEN);
        assertEquals(2, returnedValue.size());
        assertEquals(YES_VALUE, returnedValue.get(APPLICANT_REPRESENTED));
        assertEquals(APP_SOLICITOR_POLICY, returnedValue.get(CASE_ROLE));
    }

    @Test
    public void setCaseAssignedRoleSuccessRespSolicitorConsented() {

        mockMethodCalls(RESP_SOLICITOR_POLICY, true);

        Map<String, Object> returnedValue = caseAssignedRoleService.setCaseAssignedUserRole(caseDetails, AUTH_TOKEN);
        assertEquals(2, returnedValue.size());
        assertEquals(YES_VALUE, returnedValue.get(CONSENTED_RESPONDENT_REPRESENTED));
        assertEquals(RESP_SOLICITOR_POLICY,returnedValue.get(CASE_ROLE));
    }

    @Test
    public void setCaseAssignedRoleSuccessRespSolicitorContested() {

        mockMethodCalls(RESP_SOLICITOR_POLICY, false);

        Map<String, Object> returnedValue = caseAssignedRoleService.setCaseAssignedUserRole(caseDetails, AUTH_TOKEN);
        assertEquals(2, returnedValue.size());
        assertEquals(YES_VALUE, returnedValue.get(CONTESTED_RESPONDENT_REPRESENTED));
        assertEquals(RESP_SOLICITOR_POLICY, returnedValue.get(CASE_ROLE));
    }

    @Test
    public void setCaseAssignedRoleReturningOtherRoles() {

        mockMethodCalls(OTHER_ROLES, false);

        Map<String, Object> returnedValue = caseAssignedRoleService.setCaseAssignedUserRole(caseDetails, AUTH_TOKEN);
        assertEquals(1, returnedValue.size());
        assertEquals(OTHER_ROLES, returnedValue.get(CASE_ROLE));
    }

    private void mockMethodCalls(String role, boolean isConsentedApplication) {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
        when(idamService.getIdamUserId(AUTH_TOKEN)).thenReturn("123");
        when(dataStoreClient.getUserRoles(AUTH_TOKEN, SERVICE_AUTH_TOKEN, caseDetails.getId().toString(), "123"))
            .thenReturn(caseAssignedUserRolesResource);
        when(caseAssignedUserRolesResource.getCaseAssignedUserRoles()).thenReturn(userRoles);
        when(userRoles.get(0)).thenReturn(caseAssignedUserRole);
        when(caseAssignedUserRole.getCaseRole()).thenReturn(role);
        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(isConsentedApplication);
    }
}