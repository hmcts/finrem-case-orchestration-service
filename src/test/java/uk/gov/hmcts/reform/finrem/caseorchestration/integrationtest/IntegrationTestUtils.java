package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.ResourceUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRolesResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseAssignedRoleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PrdOrganisationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static java.util.Optional.ofNullable;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.ABOUT_TO_START_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.MID_EVENT_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SYSTEM_TOKEN;

/**
 * Utility class for integration tests providing helper methods to perform common operations.
 * Methods assume the case ID used in tests is 1758091882065221.
 */
public class IntegrationTestUtils {

    private static final String INTEGRATION_TEST_CASE_ID = "1758091882065221";

    /**
     * Helper method to perform an about to start callback request.
     *
     * @param mockMvc      the MockMvc instance to use for the request
     * @param jsonResource the JSON resource file to use as the request body
     * @return the ResultActions of the performed request
     * @throws Exception if an error occurs during the request
     */
    public static ResultActions performAboutToStartCallback(MockMvc mockMvc, String jsonResource) throws Exception {
        return performCallback(mockMvc, ABOUT_TO_START_URL, jsonResource);
    }

    /**
     * Helper method to perform a mid-event callback request.
     *
     * @param mockMvc      the MockMvc instance to use for the request
     * @param jsonResource the JSON resource file to use as the request body
     * @return the ResultActions of the performed request
     * @throws Exception if an error occurs during the request
     */
    public static ResultActions performMidEventCallback(MockMvc mockMvc, String jsonResource) throws Exception {
        return performCallback(mockMvc, MID_EVENT_URL, jsonResource);
    }

    /**
     * Helper method to perform an about to submit callback request.
     *
     * @param mockMvc      the MockMvc instance to use for the request
     * @param jsonResource the JSON resource file to use as the request body
     * @return the ResultActions of the performed request
     * @throws Exception if an error occurs during the request
     */
    public static ResultActions performAboutToSubmitCallback(MockMvc mockMvc, String jsonResource) throws Exception {
        return performCallback(mockMvc, ABOUT_TO_SUBMIT_URL, jsonResource);
    }

    /**
     * Helper method to perform a submitted callback request.
     *
     * @param mockMvc      the MockMvc instance to use for the request
     * @param jsonResource the JSON resource file to use as the request body
     * @return the ResultActions of the performed request
     * @throws Exception if an error occurs during the request
     */
    public static ResultActions performSubmittedCallback(MockMvc mockMvc, String jsonResource) throws Exception {
        return performCallback(mockMvc, SUBMITTED_URL, jsonResource);
    }

    private static ResultActions performCallback(MockMvc mockMvc, String url, String jsonResource) throws Exception {
        String request = getJsonFromFile(jsonResource);

        return mockMvc.perform(post(url)
            .content(request)
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON));
    }

    /**
     * Helper method to mock the AssignCaseAccessService to return specified case roles for solicitors.
     *
     * @param assignCaseAccessService the AssignCaseAccessService to mock
     * @param caseRoles               the case roles to return for the solicitors
     */
    public static void givenSolicitorsHaveCaseAccess(AssignCaseAccessService assignCaseAccessService, CaseRole... caseRoles) {
        List<CaseAssignmentUserRole> caseAssignmentUserRoles = Arrays.stream(caseRoles)
            .map(caseRole -> CaseAssignmentUserRole.builder()
                .userId(UUID.randomUUID().toString())
                .caseRole(caseRole.getCcdCode())
                .build())
            .toList();

        CaseAssignmentUserRolesResource caseAssignmentUserRolesResource = CaseAssignmentUserRolesResource.builder()
            .caseAssignmentUserRoles(caseAssignmentUserRoles)
            .build();

        when(assignCaseAccessService.getUserRoles(INTEGRATION_TEST_CASE_ID))
            .thenReturn(caseAssignmentUserRolesResource);
    }

    /**
     * Helper method to mock the CaseAssignedRoleService to return a specified role for the current user.
     *
     * @param caseAssignedRoleService the CaseAssignedRoleService to mock
     * @param currentUserCaseRole     the case role to return for the current user
     */
    public static void givenCurrentUserHasRole(CaseAssignedRoleService caseAssignedRoleService, CaseRole currentUserCaseRole) {
        if (currentUserCaseRole == CaseRole.CASEWORKER) {
            mockCurrentUserCaseAccess(caseAssignedRoleService, Collections.emptyList());
        } else {
            List<CaseAssignedUserRole> caseAssignedUserRoles = List.of(
                CaseAssignedUserRole.builder()
                    .userId(UUID.randomUUID().toString())
                    .caseRole(currentUserCaseRole.getCcdCode())
                    .build());
            mockCurrentUserCaseAccess(caseAssignedRoleService, caseAssignedUserRoles);
        }
    }

    /**
     * Helper method to mock the PrdOrganisationService to return a user ID for a given email.
     *
     * @param prdOrganisationService the PrdOrganisationService to mock
     * @param email                  the email to look up
     */
    public static void givenProfessionalUserWithEmail(PrdOrganisationService prdOrganisationService, String email) {
        String userId = UUID.randomUUID().toString();
        when(prdOrganisationService.findUserByEmail(email, TEST_SYSTEM_TOKEN)).thenReturn(ofNullable(userId));
    }

    /**
     * Helper method to mock the IdamService to return a full name for the current user.
     *
     * @param idamService the IdamService to mock
     * @param fullName    the full name to return for the current user
     */
    public static void givenCurrentUserHasName(IdamService idamService, String fullName) {
        when(idamService.getIdamFullName(AUTH_TOKEN)).thenReturn(fullName);
    }

    /**
     * Helper method to mock the SystemUserService to return a test authentication token.
     *
     * @param systemUserService the SystemUserService to mock
     */
    public static void givenSystemUserService(SystemUserService systemUserService) {
        when(systemUserService.getSysUserToken()).thenReturn(TEST_SYSTEM_TOKEN);
    }

    private static void mockCurrentUserCaseAccess(CaseAssignedRoleService caseAssignedRoleService,
                                                  List<CaseAssignedUserRole> caseAssignedUserRoles) {
        CaseAssignedUserRolesResource caseAssignedUserRolesResource = CaseAssignedUserRolesResource.builder()
            .caseAssignedUserRoles(caseAssignedUserRoles)
            .build();
        when(caseAssignedRoleService.getCaseAssignedUserRole(INTEGRATION_TEST_CASE_ID, AUTH_TOKEN))
            .thenReturn(caseAssignedUserRolesResource);
    }

    private static String getJsonFromFile(String resource) throws IOException {
        File file = ResourceUtils.getFile(Objects.requireNonNull(IntegrationTestUtils.class.getResource(resource)));
        return Files.readString(file.toPath());
    }
}
