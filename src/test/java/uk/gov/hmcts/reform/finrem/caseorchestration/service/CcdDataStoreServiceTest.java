package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CcdDataStoreServiceConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.RemoveUserRolesRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseUsers;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.RemoveUserRolesRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_USER_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CREATOR_USER_ROLE;

public class CcdDataStoreServiceTest extends BaseServiceTest {

    @Autowired
    private CcdDataStoreService ccdDataStoreService;
    @Autowired
    private RemoveUserRolesRequestMapper removeUserRolesRequestMapper;
    @MockBean
    private CcdDataStoreServiceConfiguration ccdDataStoreServiceConfiguration;
    @MockBean
    private IdamService idamService;
    @MockBean
    private RestService restService;

    private final String caseId = "123";

    @Before
    public void setUp() {
        when(idamService.getIdamUserId(AUTH_TOKEN)).thenReturn(TEST_USER_ID);
        when(ccdDataStoreServiceConfiguration.getRemoveCaseRolesUrl()).thenReturn(TEST_URL);
    }

    @Test
    public void removeCreatorRole() {
        CaseDetails caseDetails = buildCaseDetails();

        ccdDataStoreService.removeCreatorRole(caseDetails, AUTH_TOKEN);

        verify(idamService, times(1)).getIdamUserId(AUTH_TOKEN);
        verify(ccdDataStoreServiceConfiguration, times(1)).getRemoveCaseRolesUrl();
        ArgumentCaptor<RemoveUserRolesRequest> argumentCaptor = ArgumentCaptor.forClass(RemoveUserRolesRequest.class);
        verify(restService, times(1)).restApiDeleteCall(eq(AUTH_TOKEN), eq(TEST_URL), argumentCaptor.capture());
        verifyRemoveUserRolesRequest(argumentCaptor.getValue(), CREATOR_USER_ROLE);
    }

    @Test
    public void removeFinremCaseDetailsCreatorRole() {
        FinremCaseDetails caseDetails = buildFinremCaseDetails();

        ccdDataStoreService.removeCreatorRole(caseDetails, AUTH_TOKEN);

        verify(idamService, times(1)).getIdamUserId(AUTH_TOKEN);
        verify(ccdDataStoreServiceConfiguration, times(1)).getRemoveCaseRolesUrl();
        ArgumentCaptor<RemoveUserRolesRequest> argumentCaptor = ArgumentCaptor.forClass(RemoveUserRolesRequest.class);
        verify(restService, times(1)).restApiDeleteCall(eq(AUTH_TOKEN), eq(TEST_URL), argumentCaptor.capture());
        verifyRemoveUserRolesRequest(argumentCaptor.getValue(), CREATOR_USER_ROLE);
    }

    @Test
    public void removeUserCaseRole() {
        ccdDataStoreService.removeUserCaseRole(caseId, AUTH_TOKEN, TEST_USER_ID, "[APPSOLICITOR]");

        verifyNoInteractions(idamService);
        verify(ccdDataStoreServiceConfiguration, times(1)).getRemoveCaseRolesUrl();
        ArgumentCaptor<RemoveUserRolesRequest> argumentCaptor = ArgumentCaptor.forClass(RemoveUserRolesRequest.class);
        verify(restService, times(1)).restApiDeleteCall(eq(AUTH_TOKEN), eq(TEST_URL), argumentCaptor.capture());
        verifyRemoveUserRolesRequest(argumentCaptor.getValue(), "[APPSOLICITOR]");
    }

    private void verifyRemoveUserRolesRequest(RemoveUserRolesRequest removeUserRolesRequest, String expectedCaseRole) {
        assertThat(removeUserRolesRequest.getCase_users()).hasSize(1);
        CaseUsers caseUsers = removeUserRolesRequest.getCase_users().get(0);
        assertThat(caseUsers.getCaseId()).isEqualTo(caseId);
        assertThat(caseUsers.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(caseUsers.getCaseRole()).isEqualTo(expectedCaseRole);
    }
}
