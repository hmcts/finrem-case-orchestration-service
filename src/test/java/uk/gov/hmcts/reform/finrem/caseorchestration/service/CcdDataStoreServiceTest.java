package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CcdDataStoreServiceConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CaseAssignedUserRolesRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseAssignedUserRolesRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseUsers;

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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CREATOR_USER_ROLE;

public class CcdDataStoreServiceTest extends BaseServiceTest {

    @Autowired private CcdDataStoreService ccdDataStoreService;

    @MockBean private CcdDataStoreServiceConfiguration ccdDataStoreServiceConfiguration;
    @MockBean private CaseAssignedUserRolesRequestMapper caseAssignedUserRolesRequestMapper;
    @MockBean private IdamService idamService;
    @MockBean private RestService restService;

    CaseAssignedUserRolesRequest caseAssignedUserRolesRequest;

    @Before
    public void setUp() {
        caseAssignedUserRolesRequest = CaseAssignedUserRolesRequest
            .builder()
            .case_users(
                Arrays.asList(
                    CaseUsers
                        .builder()
                        .case_id(TEST_CASE_ID)
                        .user_id(TEST_USER_ID)
                        .case_role(CREATOR_USER_ROLE)
                        .organisation_id(TEST_ORG_ID)
                        .build()))
            .build();

        when(idamService.getIdamUserId(AUTH_TOKEN)).thenReturn(TEST_USER_ID);
        when(caseAssignedUserRolesRequestMapper.mapToCaseAssignedUserRolesRequest(
            any(CaseDetails.class), eq(TEST_USER_ID), eq(APP_SOLICITOR_POLICY), eq(TEST_ORG_ID)))
            .thenReturn(caseAssignedUserRolesRequest);
        when(ccdDataStoreServiceConfiguration.getCaseUsersUrl()).thenReturn(TEST_URL);
    }

    @Test
    public void addCreatorRole() {
        CaseDetails caseDetails = buildCaseDetails();

        ccdDataStoreService.addApplicantSolicitorRole(caseDetails, AUTH_TOKEN, TEST_ORG_ID);

        verify(idamService, times(1)).getIdamUserId(AUTH_TOKEN);
        verify(caseAssignedUserRolesRequestMapper, times(1))
            .mapToCaseAssignedUserRolesRequest(caseDetails, TEST_USER_ID, APP_SOLICITOR_POLICY, TEST_ORG_ID);
        verify(ccdDataStoreServiceConfiguration, times(1)).getCaseUsersUrl();
        verify(restService, times(1)).restApiPostCall(AUTH_TOKEN, TEST_URL, caseAssignedUserRolesRequest);
    }
}