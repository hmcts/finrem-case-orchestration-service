package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CcdDataStoreServiceConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.AddUserRolesRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.RemoveUserRolesRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.AddUserRolesRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseAssignedUserRoleWithOrganisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseUsers;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.RemoveUserRolesRequest;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_ORG_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_USER_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CREATOR_USER_ROLE;

public class CcdDataStoreServiceTest extends BaseServiceTest {

    @Autowired private CcdDataStoreService ccdDataStoreService;

    @MockBean private CcdDataStoreServiceConfiguration ccdDataStoreServiceConfiguration;
    @MockBean private RemoveUserRolesRequestMapper removeUserRolesRequestMapper;
    @MockBean private AddUserRolesRequestMapper addUserRolesRequestMapper;
    @MockBean private IdamService idamService;
    @MockBean private RestService restService;

    RemoveUserRolesRequest removeUserRolesRequest;
    AddUserRolesRequest addUserRolesRequest;

    @Before
    public void setUp() {
        removeUserRolesRequest = RemoveUserRolesRequest
            .builder()
            .case_users(
                Arrays.asList(
                    CaseUsers
                        .builder()
                        .case_id(TEST_CASE_ID)
                        .user_id(TEST_USER_ID)
                        .case_role(CREATOR_USER_ROLE)
                        .build()))
            .build();

        addUserRolesRequest = AddUserRolesRequest
            .builder()
            .case_users(
                Arrays.asList(
                    CaseAssignedUserRoleWithOrganisation
                        .builder()
                        .organisation_id(TEST_ORG_ID)
                        .case_id(TEST_CASE_ID)
                        .user_id(TEST_USER_ID)
                        .case_role(CREATOR_USER_ROLE)
                        .build()))
            .build();

        when(idamService.getIdamUserId(AUTH_TOKEN)).thenReturn(TEST_USER_ID);
        when(removeUserRolesRequestMapper.mapToRemoveUserRolesRequest(any(CaseDetails.class), eq(TEST_USER_ID), eq(CREATOR_USER_ROLE)))
            .thenReturn(removeUserRolesRequest);
        when(addUserRolesRequestMapper.mapToAddUserRolesRequest(any(CaseDetails.class), eq(TEST_USER_ID), eq(CREATOR_USER_ROLE), eq(TEST_ORG_ID)))
            .thenReturn(addUserRolesRequest);
        when(ccdDataStoreServiceConfiguration.getRemoveCaseRolesUrl()).thenReturn(TEST_URL);
    }

    @Test
    public void removeCreatorRole() {
        CaseDetails caseDetails = buildCaseDetails();

        ccdDataStoreService.removeCreatorRole(caseDetails, AUTH_TOKEN);

        verify(idamService, times(1)).getIdamUserId(AUTH_TOKEN);
        verify(removeUserRolesRequestMapper, times(1)).mapToRemoveUserRolesRequest(caseDetails, TEST_USER_ID, CREATOR_USER_ROLE);
        verify(ccdDataStoreServiceConfiguration, times(1)).getRemoveCaseRolesUrl();
        verify(restService, times(1)).restApiDeleteCall(AUTH_TOKEN, TEST_URL, removeUserRolesRequest);
    }

    @Test
    public void addCreatorRole() {
        CaseDetails caseDetails = buildCaseDetails();

        ccdDataStoreService.addCreatorRole(caseDetails, AUTH_TOKEN, TEST_ORG_ID);

        verify(idamService, times(1)).getIdamUserId(AUTH_TOKEN);
        verify(addUserRolesRequestMapper, times(1)).mapToAddUserRolesRequest(caseDetails, TEST_USER_ID, CREATOR_USER_ROLE, TEST_ORG_ID);
        verify(ccdDataStoreServiceConfiguration, times(1)).getRemoveCaseRolesUrl();
        verify(restService, times(1)).restApiPostCall(AUTH_TOKEN, TEST_URL, addUserRolesRequest);
    }
}