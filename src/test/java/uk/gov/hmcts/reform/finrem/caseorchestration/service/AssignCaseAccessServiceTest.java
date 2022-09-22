package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseUser;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.AssignCaseAccessServiceConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.AssignCaseAccessRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.AssignCaseAccessRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRoleWithOrganisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRolesResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRolesResponse;

import java.util.List;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CASE_TYPE_ID_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_BARRISTER_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_POLICY;

public class AssignCaseAccessServiceTest extends BaseServiceTest {

    public static final String TEST_USER_ID = "someUserId";
    public static final String CASE_ID = "1234567890";
    private static final String CREATOR_ROLE = "[CREATOR]";
    private static final String ORG_ID = "otherID";
    private static final String ACA_ENDPOINT = TEST_URL + "?use_user_token=true";
    private static final String TEST_S2S_TOKEN = "someS2SToken";

    @Autowired private AssignCaseAccessService assignCaseAccessService;

    @MockBean private AssignCaseAccessServiceConfiguration assignCaseAccessServiceConfiguration;
    @MockBean private AssignCaseAccessRequestMapper assignCaseAccessRequestMapper;
    @MockBean private IdamService idamService;
    @MockBean private RestService restService;
    @MockBean private FeatureToggleService featureToggleService;
    @MockBean private SystemUserService systemUserService;

    @ClassRule
    public static WireMockClassRule caseDataApi = new WireMockClassRule(4452);

    AssignCaseAccessRequest assignCaseAccessRequest;

    @Before
    public void setUp() {
        assignCaseAccessRequest = AssignCaseAccessRequest
            .builder()
            .case_id(TEST_CASE_ID)
            .case_type_id(CASE_TYPE_ID_CONTESTED)
            .assignee_id(TestConstants.TEST_USER_ID)
            .build();

        when(idamService.getIdamUserId(AUTH_TOKEN)).thenReturn(TestConstants.TEST_USER_ID);
        when(assignCaseAccessRequestMapper.mapToAssignCaseAccessRequest(any(CaseDetails.class), eq(TestConstants.TEST_USER_ID)))
            .thenReturn(assignCaseAccessRequest);
        when(assignCaseAccessServiceConfiguration.getCaseAssignmentsUrl()).thenReturn(TEST_URL);
        when(featureToggleService.isUseUserTokenEnabled()).thenReturn(true);
    }

    @Test
    public void assignCaseAccess() {
        CaseDetails caseDetails = buildCaseDetails();

        assignCaseAccessService.assignCaseAccess(caseDetails, AUTH_TOKEN);

        verify(idamService, times(1)).getIdamUserId(AUTH_TOKEN);
        verify(assignCaseAccessRequestMapper, times(1))
            .mapToAssignCaseAccessRequest(caseDetails, TestConstants.TEST_USER_ID);
        verify(assignCaseAccessServiceConfiguration, times(1)).getCaseAssignmentsUrl();
        verify(restService, times(1))
            .restApiPostCall(AUTH_TOKEN, ACA_ENDPOINT, assignCaseAccessRequest);
    }

    @Test
    public void shouldRevokeCreatorRoleWhenCreatorWasAppSolicitor() throws JsonProcessingException {
        when(systemUserService.getSysUserToken()).thenReturn(TEST_S2S_TOKEN);

        caseDataApi.stubFor(get(urlEqualTo("/case-users?case_ids=123"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(mapper.writeValueAsString(generateResourceWhenCreatorWasSolicitor()))));

        caseDataApi.stubFor(delete(urlEqualTo("/case-users"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(mapper.writeValueAsString(CaseAssignmentUserRolesResponse.builder()
                    .statusMessage("Success")
                    .build()))));

        CaseDetails caseDetails = buildCaseDetails();
        CaseAssignmentUserRolesResponse response = assignCaseAccessService.findAndRevokeCreatorRole(caseDetails);
        assertThat(response.getStatusMessage()).isEqualTo("Success");
    }

    @Test
    public void shouldNotRevokeCreatorRoleWhenCreatorWasNotAppSolicitor() throws JsonProcessingException {
        CaseDetails caseDetails = buildCaseDetails();
        when(systemUserService.getSysUserToken()).thenReturn(TEST_S2S_TOKEN);

        caseDataApi.stubFor(get(urlEqualTo("/case-users?case_ids=123"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(mapper.writeValueAsString(generateResourceWhenCreatorWasNotSolicitor()))));

        CaseAssignmentUserRolesResponse response = assignCaseAccessService.findAndRevokeCreatorRole(caseDetails);
        assertThat(response).isEqualTo(null);
    }

    @Test
    public void shouldReturnNullWhenNoCreator() throws JsonProcessingException {
        CaseDetails caseDetails = buildCaseDetails();
        when(systemUserService.getSysUserToken()).thenReturn(TEST_S2S_TOKEN);

        caseDataApi.stubFor(get(urlEqualTo("/case-users?case_ids=123"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(mapper.writeValueAsString(generateResourceWithNoCreatorRole()))));

        CaseAssignmentUserRolesResponse response = assignCaseAccessService.findAndRevokeCreatorRole(caseDetails);
        assertThat(response).isEqualTo(null);
    }

    @Test
    public void givenValidRequest_whenAddCaseRolesForUser_thenAddRoles() throws JsonProcessingException {
        final CaseUser caseUser = CaseUser.builder().caseRoles(Set.of(APPLICANT_BARRISTER_ROLE)).userId(TEST_USER_ID).build();
        caseDataApi.stubFor(put(urlEqualTo("/cases/" + CASE_ID + "/users/" + TEST_USER_ID))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(mapper.writeValueAsString(caseUser))));

        assignCaseAccessService.addCaseRolesForUser(CASE_ID, TEST_USER_ID, Set.of(APPLICANT_BARRISTER_ROLE), AUTH_TOKEN);

        WireMock.verify(putRequestedFor(urlMatching("/cases/1234567890/users/someUserId")));
    }

    @Test
    public void givenValidRequest_whenGrantCaseAccessForBarrister_thenGrantAccess() throws JsonProcessingException {
        final CaseAssignmentUserRoleWithOrganisation requestInfo =
            CaseAssignmentUserRoleWithOrganisation.builder()
                .caseDataId(CASE_ID)
                .userId(TEST_USER_ID)
                .organisationId(ORG_ID)
                .caseRole(APPLICANT_BARRISTER_ROLE)
                .build();

        final CaseAssignmentUserRolesRequest requestBody = CaseAssignmentUserRolesRequest.builder()
            .caseAssignmentUserRolesWithOrganisation(List.of(requestInfo))
            .build();

        caseDataApi.stubFor(post(urlEqualTo("/case-users"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(mapper.writeValueAsString(requestBody))));

        assignCaseAccessService.grantCaseRoleToUser(Long.parseLong(CASE_ID), TEST_USER_ID, APPLICANT_BARRISTER_ROLE, ORG_ID);

        WireMock.verify(postRequestedFor(urlMatching("/case-users")));
    }

    private CaseAssignmentUserRolesResource generateResourceWhenCreatorWasSolicitor() {
        List<CaseAssignmentUserRole> roles = List.of(
            CaseAssignmentUserRole.builder()
                .caseRole(CREATOR_ROLE)
                .caseDataId(TEST_CASE_ID)
                .userId(TestConstants.TEST_USER_ID).build(),
            CaseAssignmentUserRole.builder()
                .caseRole(APP_SOLICITOR_POLICY)
                .caseDataId(TEST_CASE_ID)
                .userId(TestConstants.TEST_USER_ID).build()
        );

        return CaseAssignmentUserRolesResource.builder().caseAssignmentUserRoles(roles).build();
    }

    private CaseAssignmentUserRolesResource generateResourceWhenCreatorWasNotSolicitor() {
        List<CaseAssignmentUserRole> roles = List.of(
            CaseAssignmentUserRole.builder()
                .caseRole(CREATOR_ROLE)
                .caseDataId(TEST_CASE_ID)
                .userId(TestConstants.TEST_USER_ID).build(),
            CaseAssignmentUserRole.builder()
                .caseRole(APP_SOLICITOR_POLICY)
                .caseDataId(TEST_CASE_ID)
                .userId(ORG_ID).build()
        );

        return CaseAssignmentUserRolesResource.builder().caseAssignmentUserRoles(roles).build();
    }

    private CaseAssignmentUserRolesResource generateResourceWithNoCreatorRole() {
        List<CaseAssignmentUserRole> roles = List.of(
            CaseAssignmentUserRole.builder()
                .caseRole(RESP_SOLICITOR_POLICY)
                .caseDataId(TEST_CASE_ID)
                .userId(TestConstants.TEST_USER_ID).build(),
            CaseAssignmentUserRole.builder()
                .caseRole(APP_SOLICITOR_POLICY)
                .caseDataId(TEST_CASE_ID)
                .userId(ORG_ID).build()
        );

        return CaseAssignmentUserRolesResource.builder().caseAssignmentUserRoles(roles).build();
    }

}