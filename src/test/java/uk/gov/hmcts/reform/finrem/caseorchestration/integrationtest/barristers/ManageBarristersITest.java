package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.barristers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.CaseDataApiV2;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DataStoreClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.OrganisationApi;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.PrdOrganisationConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.CcdCallbackController;
import uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.IntegrationTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRoleWithOrganisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRolesResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationUser;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CallbackDispatchService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseAssignedRoleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PrdOrganisationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;

import java.util.List;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDetailsFromResource;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_BARRISTER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASEWORKER_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MANAGE_BARRISTER_PARTY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REPRESENTATION_UPDATE_HISTORY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_BARRISTER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_BARRISTER_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdServiceTest.AUTH_TOKEN;

@RunWith(SpringRunner.class)
@WebMvcTest(CcdCallbackController.class)
@ContextConfiguration(classes = {
    ManageBarristerTestConfiguration.class, PrdOrganisationService.class, PrdOrganisationConfiguration.class,
    AssignCaseAccessService.class, CaseAssignedRoleService.class, CcdCallbackController.class, CallbackDispatchService.class})
public class ManageBarristersITest implements IntegrationTest {

    private static final String SERVICE_AUTH_TOKEN = "serviceAuth";
    private static final String CASE_ID = "12345678";
    private static final String USER_ID = "userId";
    private static final String APP_BARRISTER_EMAIL_ONE = "appbarr@gmail.com";
    private static final String RESP_BARRISTER_EMAIL_ONE = "respbarr@gmail.com";
    private static final String SOLICITOR_NAME = "solName";
    private static final String APP_BARR_ORG_ID = "orgId";
    private static final String RESP_BARR_ORG_ID = "respOrgId";
    private static final String BARRISTER_ID = "barristerId";
    private static final String RESP_BARRISTER_ID = "respBarristerId";
    private static final String SYS_USER_TOKEN = "sysUserToken";
    private static final String RESP_SOL_ID = "someOtherId";
    private static final String CASEWORKER_NAME = "the Caseworker";

    @Autowired
    private CcdCallbackController ccdCallbackController;

    @MockBean
    private DataStoreClient dataStoreClient;
    @MockBean
    private AuthTokenGenerator authTokenGenerator;
    @MockBean
    private IdamService idamService;
    @MockBean
    private OrganisationApi organisationApi;
    @MockBean
    private CaseDataApiV2 caseDataApiV2;
    @MockBean
    private SystemUserService systemUserService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void givenValidRequest_whenManageBarristerAboutToStart_thenProcess() {
        when(dataStoreClient.getUserRoles(AUTH_TOKEN, SERVICE_AUTH_TOKEN, CASE_ID, USER_ID))
            .thenReturn(CaseAssignedUserRolesResource.builder()
                .caseAssignedUserRoles(List.of(CaseAssignedUserRole.builder().caseRole(APP_SOLICITOR_POLICY).build()))
                .build());
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
        when(idamService.getIdamUserId(AUTH_TOKEN)).thenReturn(USER_ID);

        CallbackRequest request = buildCallbackRequest();

        ResponseEntity<AboutToStartOrSubmitCallbackResponse> response =
            ccdCallbackController.ccdAboutToStart(AUTH_TOKEN, request);

        verify(dataStoreClient).getUserRoles(AUTH_TOKEN, SERVICE_AUTH_TOKEN, CASE_ID, USER_ID);
        assertNotNull(response.getBody());
        String caseRole = Objects.toString(response.getBody().getData().get(CASE_ROLE), StringUtils.EMPTY);
        assertThat(caseRole, is(APP_SOLICITOR_POLICY));
    }

    @Test
    public void givenValidRequest_whenManageBarristerMidEvent_thenProcess() {
        when(dataStoreClient.getUserRoles(AUTH_TOKEN, SERVICE_AUTH_TOKEN, CASE_ID, USER_ID))
            .thenReturn(CaseAssignedUserRolesResource.builder()
                .caseAssignedUserRoles(List.of(CaseAssignedUserRole.builder().caseRole(APP_SOLICITOR_POLICY).build()))
                .build());
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
        when(idamService.getIdamUserId(AUTH_TOKEN)).thenReturn(USER_ID);
        when(organisationApi.findUserByEmail(AUTH_TOKEN, SERVICE_AUTH_TOKEN, APP_BARRISTER_EMAIL_ONE))
            .thenReturn(OrganisationUser.builder().userIdentifier(BARRISTER_ID).build());
        when(systemUserService.getSysUserToken()).thenReturn(SYS_USER_TOKEN);
        CaseAssignmentUserRolesResource caseAssignmentUserRolesResource = getCaseAssignmentUserRolesResource();
        when(caseDataApiV2.getUserRoles(SYS_USER_TOKEN, SERVICE_AUTH_TOKEN, List.of(CASE_ID)))
            .thenReturn(caseAssignmentUserRolesResource);

        CallbackRequest request = buildCallbackRequest();
        request.getCaseDetails().getData().put(APPLICANT_BARRISTER_COLLECTION, applicantBarristerCollection());

        ResponseEntity<AboutToStartOrSubmitCallbackResponse> response =
            ccdCallbackController.ccdMidEvent(AUTH_TOKEN, request);

        assertNotNull(response.getBody());
        assertThat(response.getBody().getErrors(), hasSize(0));
    }

    @Test
    public void givenBarristerIsUnregistered_whenManageBarristerMidEvent_thenReturnCorrectError() {
        when(dataStoreClient.getUserRoles(AUTH_TOKEN, SERVICE_AUTH_TOKEN, CASE_ID, USER_ID))
            .thenReturn(CaseAssignedUserRolesResource.builder()
                .caseAssignedUserRoles(List.of(CaseAssignedUserRole.builder().caseRole(APP_SOLICITOR_POLICY).build()))
                .build());
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
        when(idamService.getIdamUserId(AUTH_TOKEN)).thenReturn(USER_ID);
        when(organisationApi.findUserByEmail(AUTH_TOKEN, SERVICE_AUTH_TOKEN, APP_BARRISTER_EMAIL_ONE))
            .thenThrow(FeignException.NotFound.class);

        CallbackRequest request = buildCallbackRequest();
        request.getCaseDetails().getData().put(APPLICANT_BARRISTER_COLLECTION, applicantBarristerCollection());

        ResponseEntity<AboutToStartOrSubmitCallbackResponse> response =
            ccdCallbackController.ccdMidEvent(AUTH_TOKEN, request);

        assertNotNull(response.getBody());
        assertThat(response.getBody().getErrors(), hasSize(1));
        assertThat(response.getBody().getErrors().get(0), is(""" 
                Email address for Barrister is not registered with myHMCTS.
                They can register at https://manage-org.platform.hmcts.net/register-org/register"""));
    }

    @Test
    public void givenBarristerAlreadyRepresentsOpposingLitigant_whenManageBarristerMidEvent_thenReturnCorrectError() {
        when(dataStoreClient.getUserRoles(AUTH_TOKEN, SERVICE_AUTH_TOKEN, CASE_ID, USER_ID))
            .thenReturn(CaseAssignedUserRolesResource.builder()
                .caseAssignedUserRoles(List.of(CaseAssignedUserRole.builder().caseRole(APP_SOLICITOR_POLICY).build()))
                .build());
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
        when(idamService.getIdamUserId(AUTH_TOKEN)).thenReturn(USER_ID);
        when(organisationApi.findUserByEmail(AUTH_TOKEN, SERVICE_AUTH_TOKEN, APP_BARRISTER_EMAIL_ONE))
            .thenReturn(OrganisationUser.builder().userIdentifier(RESP_SOL_ID).build());
        when(systemUserService.getSysUserToken()).thenReturn(SYS_USER_TOKEN);
        CaseAssignmentUserRolesResource caseAssignmentUserRolesResource = getCaseAssignmentUserRolesResource();
        when(caseDataApiV2.getUserRoles(SYS_USER_TOKEN, SERVICE_AUTH_TOKEN, List.of(CASE_ID)))
            .thenReturn(caseAssignmentUserRolesResource);

        CallbackRequest request = buildCallbackRequest();
        request.getCaseDetails().getData().put(APPLICANT_BARRISTER_COLLECTION, applicantBarristerCollection());

        ResponseEntity<AboutToStartOrSubmitCallbackResponse> response =
            ccdCallbackController.ccdMidEvent(AUTH_TOKEN, request);

        assertNotNull(response.getBody());
        assertThat(response.getBody().getErrors().get(0), is("Barrister is already representing another party on this case"));
    }

    @Test
    public void givenValidRequest_whenManageBarristerAboutToSubmitAsSolicitor_thenProcess() {
        when(dataStoreClient.getUserRoles(AUTH_TOKEN, SERVICE_AUTH_TOKEN, CASE_ID, USER_ID))
            .thenReturn(CaseAssignedUserRolesResource.builder()
                .caseAssignedUserRoles(List.of(CaseAssignedUserRole.builder().caseRole(RESP_SOLICITOR_POLICY).build()))
                .build());
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
        when(idamService.getIdamUserId(AUTH_TOKEN)).thenReturn(USER_ID);
        when(idamService.getIdamFullName(AUTH_TOKEN)).thenReturn(SOLICITOR_NAME);
        when(organisationApi.findUserByEmail(AUTH_TOKEN, SERVICE_AUTH_TOKEN, RESP_BARRISTER_EMAIL_ONE))
            .thenReturn(OrganisationUser.builder().userIdentifier(RESP_BARRISTER_ID).build());
        when(systemUserService.getSysUserToken()).thenReturn(SYS_USER_TOKEN);

        CallbackRequest request = buildCallbackRequest();
        request.getCaseDetails().getData().put(RESPONDENT_BARRISTER_COLLECTION, respondentBarristerCollection());

        ResponseEntity<AboutToStartOrSubmitCallbackResponse> response =
            ccdCallbackController.ccdAboutToSubmit(AUTH_TOKEN, request);

        verify(caseDataApiV2).addCaseUserRoles(SYS_USER_TOKEN, SERVICE_AUTH_TOKEN, caseAssignmentUserRolesRequest());

        assertNotNull(response.getBody());
        List<Element<RepresentationUpdate>> representationUpdateHistory = objectMapper
            .convertValue(response.getBody().getData().get(REPRESENTATION_UPDATE_HISTORY), new TypeReference<>() {});

        assertThat(representationUpdateHistory, hasSize(1));
        RepresentationUpdate update = representationUpdateHistory.get(0).getValue();
        assertThat(update.getAdded(), is(ChangedRepresentative.builder()
            .email(RESP_BARRISTER_EMAIL_ONE)
            .organisation(Organisation.builder().organisationID(RESP_BARR_ORG_ID).build())
            .build()));
        assertThat(update.getBy(), is(SOLICITOR_NAME));
        assertThat(update.getClientName(), is("Jane Smith"));
    }

    @Test
    public void givenValidRequestAsCaseworker_whenManageBarristerAboutToSubmit_thenProcess() {
        when(dataStoreClient.getUserRoles(AUTH_TOKEN, SERVICE_AUTH_TOKEN, CASE_ID, USER_ID))
            .thenReturn(CaseAssignedUserRolesResource.builder()
                .caseAssignedUserRoles(List.of(CaseAssignedUserRole.builder().caseRole(RESP_SOLICITOR_POLICY).build()))
                .build());
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
        when(idamService.getIdamUserId(AUTH_TOKEN)).thenReturn(USER_ID);
        when(idamService.getIdamFullName(AUTH_TOKEN)).thenReturn(CASEWORKER_NAME);
        when(organisationApi.findUserByEmail(SYS_USER_TOKEN, SERVICE_AUTH_TOKEN, RESP_BARRISTER_EMAIL_ONE))
            .thenReturn(OrganisationUser.builder().userIdentifier(RESP_BARRISTER_ID).build());
        when(systemUserService.getSysUserToken()).thenReturn(SYS_USER_TOKEN);

        CallbackRequest request = buildCallbackRequest();
        request.getCaseDetails().getData().put(RESPONDENT_BARRISTER_COLLECTION, respondentBarristerCollection());
        request.getCaseDetails().getData().put(CASE_ROLE, CASEWORKER_ROLE);
        request.getCaseDetails().getData().put(MANAGE_BARRISTER_PARTY, RESPONDENT);

        ResponseEntity<AboutToStartOrSubmitCallbackResponse> response =
            ccdCallbackController.ccdAboutToSubmit(AUTH_TOKEN, request);

        verify(caseDataApiV2).addCaseUserRoles(SYS_USER_TOKEN, SERVICE_AUTH_TOKEN, caseAssignmentUserRolesRequest());

        assertNotNull(response.getBody());
        List<Element<RepresentationUpdate>> representationUpdateHistory = objectMapper
            .convertValue(response.getBody().getData().get(REPRESENTATION_UPDATE_HISTORY), new TypeReference<>() {});

        assertThat(representationUpdateHistory, hasSize(1));
        RepresentationUpdate update = representationUpdateHistory.get(0).getValue();
        assertThat(update.getAdded(), is(ChangedRepresentative.builder()
            .email(RESP_BARRISTER_EMAIL_ONE)
            .organisation(Organisation.builder().organisationID(RESP_BARR_ORG_ID).build())
            .build()));
        assertThat(update.getBy(), is(CASEWORKER_NAME));
        assertThat(update.getClientName(), is("Jane Smith"));
    }

    private CallbackRequest buildCallbackRequest() {
        CaseDetails caseDetails = caseDetailsFromResource("/fixtures/barristers/"
            + "manage-barrister-about-to-start.json", objectMapper);

        CaseDetails caseDetailsBefore = caseDetailsFromResource("/fixtures/barristers/"
            + "manage-barrister-about-to-start.json", objectMapper);

        return CallbackRequest.builder()
            .eventId(EventType.MANAGE_BARRISTER.getCcdType())
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore)
            .build();
    }

    private List<BarristerData> applicantBarristerCollection() {
        return List.of(BarristerData.builder()
                .barrister(Barrister.builder()
                    .email(APP_BARRISTER_EMAIL_ONE)
                    .organisation(Organisation.builder().organisationID(APP_BARR_ORG_ID).build())
                    .build())
            .build());
    }

    private List<BarristerData> respondentBarristerCollection() {
        return List.of(BarristerData.builder()
            .barrister(Barrister.builder()
                .email(RESP_BARRISTER_EMAIL_ONE)
                .organisation(Organisation.builder().organisationID(RESP_BARR_ORG_ID).build())
                .build())
            .build());
    }

    private CaseAssignmentUserRolesResource getCaseAssignmentUserRolesResource() {
        return CaseAssignmentUserRolesResource.builder()
            .caseAssignmentUserRoles(List.of(CaseAssignmentUserRole.builder()
                    .caseDataId(CASE_ID)
                    .caseRole(RESP_SOLICITOR_POLICY)
                    .userId(RESP_SOL_ID)
                    .build(),
                CaseAssignmentUserRole.builder()
                    .caseDataId(CASE_ID)
                    .caseRole(APP_SOLICITOR_POLICY)
                    .userId(USER_ID)
                    .build())).build();
    }

    private CaseAssignmentUserRolesRequest caseAssignmentUserRolesRequest() {
        return CaseAssignmentUserRolesRequest.builder()
            .caseAssignmentUserRolesWithOrganisation(List.of(
                CaseAssignmentUserRoleWithOrganisation.builder()
                    .caseDataId(CASE_ID)
                    .userId(RESP_BARRISTER_ID)
                    .caseRole(RESPONDENT_BARRISTER_ROLE)
                    .organisationId(RESP_BARR_ORG_ID)
                    .build()))
            .build();
    }
}
