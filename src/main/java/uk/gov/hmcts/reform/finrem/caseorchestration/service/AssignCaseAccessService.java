package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseUser;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.CaseDataApiV2;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.AssignCaseAccessServiceConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.GrantCaseAccessException;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.AssignCaseAccessRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.AssignCaseAccessRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.DecisionRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRoleWithOrganisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRolesResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRolesResponse;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssignCaseAccessService {

    private static final String CREATOR_ROLE = "[CREATOR]";
    private final AssignCaseAccessServiceConfiguration assignCaseAccessServiceConfiguration;
    private final AssignCaseAccessRequestMapper assignCaseAccessRequestMapper;
    private final IdamService idamService;
    private final RestService restService;
    private final CaseAssignmentApi caseAssignmentApi;
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final CaseDataApiV2 caseDataApi;
    private final SystemUserService systemUserService;
    private final FeatureToggleService featureToggleService;

    public void assignCaseAccess(CaseDetails caseDetails, String authorisationToken) {
        String userId = idamService.getIdamUserId(authorisationToken);
        AssignCaseAccessRequest assignCaseAccessRequest = assignCaseAccessRequestMapper.mapToAssignCaseAccessRequest(caseDetails, userId);
        String url = assignCaseAccessServiceConfiguration.getCaseAssignmentsUrl()
            + (featureToggleService.isUseUserTokenEnabled() ? "?use_user_token=true" : "");

        restService.restApiPostCall(
            authorisationToken,
            url,
            assignCaseAccessRequest
        );
    }

    public AboutToStartOrSubmitCallbackResponse applyDecision(String authToken, CaseDetails caseDetails) {
        return caseAssignmentApi.applyDecision(authToken, serviceAuthTokenGenerator.generate(),
            DecisionRequest.decisionRequest(caseDetails));
    }

    public void addCaseRolesForUser(String caseId, String userId, Set<String> caseRoles, String userAuthToken) {
        final CaseUser caseUser = CaseUser.builder().userId(userId).caseRoles(caseRoles).build();
        log.info("Grant case roles {} to user {} for case {}", caseRoles, userId, caseId);

        caseDataApi.updateCaseRolesForUser(
            userAuthToken,
            serviceAuthTokenGenerator.generate(),
            caseId,
            userId,
            caseUser);
    }

    public void grantCaseRoleToUser(Long caseId, String userId, String caseRole, String orgId) {
        grantCaseAccess(caseId, Set.of(userId), caseRole, orgId);
        log.info("User {} granted {} to case {}", userId, caseRole, caseId);
    }

    private void grantCaseAccess(Long caseId, Set<String> users, String caseRole, String orgId) {
        try {
            final String userToken = systemUserService.getSysUserToken();
            final String serviceToken = serviceAuthTokenGenerator.generate();

            log.info("about to start granting case access for users {}", users);

            final List<CaseAssignmentUserRoleWithOrganisation> caseAssignedRoles = users.stream()
                .map(user -> buildCaseAssignedUserRoles(caseId, caseRole, orgId, user))
                .toList();

            CaseAssignmentUserRolesRequest addCaseAssignedUserRolesRequest = CaseAssignmentUserRolesRequest.builder()
                    .caseAssignmentUserRolesWithOrganisation(caseAssignedRoles)
                    .build();

            caseDataApi.addCaseUserRoles(userToken, serviceToken, addCaseAssignedUserRolesRequest);
        } catch (FeignException ex) {
            log.error("Could not assign the users to the case", ex);
            throw new GrantCaseAccessException(caseId, users, caseRole);
        }
    }

    private CaseAssignmentUserRoleWithOrganisation buildCaseAssignedUserRoles(Long caseId,
                                                                              String caseRole,
                                                                              String organisationId,
                                                                              String userId) {
        return CaseAssignmentUserRoleWithOrganisation.builder()
            .caseDataId(caseId.toString())
            .organisationId(organisationId)
            .userId(userId)
            .caseRole(caseRole)
            .build();
    }

    public CaseAssignmentUserRolesResponse findAndRevokeCreatorRole(CaseDetails caseDetails) {

        log.info("About to start revoking creator role for caseId {}", caseDetails.getId());
        List<CaseAssignmentUserRole> allRoles = getUserRoles(caseDetails.getId().toString())
            .getCaseAssignmentUserRoles();
        List<CaseAssignmentUserRole> creatorRoles = getCreatorRoles(allRoles);

        if (creatorRoles.isEmpty()) {
            log.info("No creator role found for caseId {}", caseDetails.getId());
            return null;
        }

        if (creatorRoles.size() > 1) {
            throw new IllegalStateException("Multiple creator roles found for case");
        }

        Optional<CaseAssignmentUserRole> userToRemove = getUserToRemove(creatorRoles, allRoles);

        if (userToRemove.isEmpty()) {
            log.info("Applicant solicitor did not create case with id {}", caseDetails.getId());
            return null;
        }

        return revokeCreatorRole(caseDetails, userToRemove.get().getUserId());
    }

    private CaseAssignmentUserRolesResource getUserRoles(String caseId) {
        return caseDataApi.getUserRoles(
            systemUserService.getSysUserToken(),
            serviceAuthTokenGenerator.generate(),
            List.of(caseId));
    }

    private List<CaseAssignmentUserRole> getCreatorRoles(List<CaseAssignmentUserRole> allRoles) {
        return allRoles.stream()
            .filter(role -> role.getCaseRole().equalsIgnoreCase(CREATOR_ROLE))
            .collect(Collectors.toList());
    }

    private Optional<CaseAssignmentUserRole> getUserToRemove(List<CaseAssignmentUserRole> creatorRoles,
                                                             List<CaseAssignmentUserRole> allRoles) {
        final Predicate<CaseAssignmentUserRole> creatorWasApplicantSolicitor = solicitorRole ->
            solicitorRole.getUserId().equals(creatorRoles.get(0).getUserId())
                && solicitorRole.getCaseRole().equals(APP_SOLICITOR_POLICY);

        return allRoles.stream()
            .filter(creatorWasApplicantSolicitor)
            .findFirst();
    }

    private CaseAssignmentUserRolesResponse revokeCreatorRole(CaseDetails caseDetails, String userId) {

        CaseAssignmentUserRolesRequest revokeAccessRequest = CaseAssignmentUserRolesRequest
            .builder()
            .caseAssignmentUserRolesWithOrganisation(buildRevokeAccessRequest(caseDetails, userId))
            .build();

        CaseAssignmentUserRolesResponse response = caseDataApi.removeCaseUserRoles(
            systemUserService.getSysUserToken(),
            serviceAuthTokenGenerator.generate(),
            revokeAccessRequest);

        log.info("CCD response after Revoke Creator Role Access Request: {}", response);
        return response;
    }

    private List<CaseAssignmentUserRoleWithOrganisation> buildRevokeAccessRequest(CaseDetails caseDetails,
                                                                                  String userId) {
        return List.of(CaseAssignmentUserRoleWithOrganisation.builder()
            .caseDataId(caseDetails.getId().toString())
            .caseRole(CREATOR_ROLE)
            .userId(userId)
            .build());
    }
}
