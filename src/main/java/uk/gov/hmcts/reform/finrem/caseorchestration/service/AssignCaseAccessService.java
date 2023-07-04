package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.searchuserrole.SearchCaseAssignedUserRolesRequest;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.caseorchestration.config.CacheConfiguration.REQUEST_SCOPED_CACHE_MANAGER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.config.CacheConfiguration.USER_ROLES_CACHE;
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

    public void assignCaseAccess(FinremCaseDetails finremCaseDetails, String authorisationToken) {
        String userId = idamService.getIdamUserId(authorisationToken);
        AssignCaseAccessRequest assignCaseAccessRequest = assignCaseAccessRequestMapper.mapToAssignCaseAccessRequest(finremCaseDetails, userId);
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

    public void removeCaseRoleToUser(Long caseId, String userId, String caseRole, String orgId) {
        removeCaseAccess(caseId, Set.of(userId), caseRole, orgId);
        log.info("User {} removed {} from case {}", userId, caseRole, caseId);
    }

    private void removeCaseAccess(Long caseId, Set<String> users, String caseRole, String orgId) {
        try {
            log.info("about to start removing case access for users {}", users);
            CaseAssignmentUserRolesRequest removeCaseAssignedUserRolesRequest =
                getCaseAssignmentUserRolesRequest(caseId, users, caseRole, orgId);

            caseDataApi.removeCaseUserRoles(systemUserService.getSysUserToken(), serviceAuthTokenGenerator.generate(),
                removeCaseAssignedUserRolesRequest);
        } catch (FeignException ex) {
            log.error("Could not assign the users to the case", ex);
            throw new GrantCaseAccessException(caseId, users, caseRole);
        }
    }

    private CaseAssignmentUserRolesRequest getCaseAssignmentUserRolesRequest(Long caseId, Set<String> users, String caseRole, String orgId) {
        final List<CaseAssignmentUserRoleWithOrganisation> caseAssignedRoles = users.stream()
            .map(user -> buildCaseAssignedUserRoles(caseId, caseRole, orgId, user))
            .collect(Collectors.toList());

        return CaseAssignmentUserRolesRequest.builder()
            .caseAssignmentUserRolesWithOrganisation(caseAssignedRoles)
            .build();
    }

    public void grantCaseRoleToUser(Long caseId, String userId, String caseRole, String orgId) {
        grantCaseAccess(caseId, Set.of(userId), caseRole, orgId);
        log.info("User {} granted {} to case {}", userId, caseRole, caseId);
    }

    private void grantCaseAccess(Long caseId, Set<String> users, String caseRole, String orgId) {
        try {
            log.info("about to start granting case access for users {}", users);
            CaseAssignmentUserRolesRequest addCaseAssignedUserRolesRequest =
                getCaseAssignmentUserRolesRequest(caseId, users, caseRole, orgId);

            caseDataApi.addCaseUserRoles(systemUserService.getSysUserToken(), serviceAuthTokenGenerator.generate(),
                addCaseAssignedUserRolesRequest);
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

    public boolean isCreatorRoleActiveOnCase(CaseDetails caseDetails) {
        log.info("About to start searching for creator role for caseId {}", caseDetails.getId());
        List<CaseAssignmentUserRole> allRoles = getUserRoles(caseDetails.getId().toString())
            .getCaseAssignmentUserRoles();
        List<CaseAssignmentUserRole> creatorRoles = getCreatorRoles(allRoles);

        if (creatorRoles.isEmpty()) {
            log.info("No creator role found for caseId {}", caseDetails.getId());
            return false;
        } else {
            return true;
        }
    }

    public boolean isLegalCounselRepresentingOpposingLitigant(String userId,
                                                              String caseId,
                                                              Set<String> opposingCaseRoles) {
        final List<CaseAssignmentUserRole> allRoleAssignments = getUserRoles(caseId).getCaseAssignmentUserRoles();

        final Predicate<CaseAssignmentUserRole> isBarristerRepresentingOpposingLitigant = roleAssignment ->
            opposingCaseRoles.stream().anyMatch(opposingRole -> opposingRole.equals(roleAssignment.getCaseRole()))
                && roleAssignment.getUserId().equals(userId);

        return allRoleAssignments.stream().anyMatch(isBarristerRepresentingOpposingLitigant);
    }

    @Cacheable(cacheManager = REQUEST_SCOPED_CACHE_MANAGER, cacheNames = USER_ROLES_CACHE)
    public CaseAssignmentUserRolesResource getUserRoles(String caseId) {
        return caseDataApi.getUserRoles(
            systemUserService.getSysUserToken(),
            serviceAuthTokenGenerator.generate(),
            List.of(caseId));
    }

    public CaseAssignmentUserRolesResource searchUserRoles(String caseId) {
        return caseDataApi.searchCaseUserRoles(
            systemUserService.getSysUserToken(),
            serviceAuthTokenGenerator.generate(),
            SearchCaseAssignedUserRolesRequest.builder().caseIds(List.of(caseId)).build());
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

        CaseAssignmentUserRolesRequest revokeAccessRequest = CaseAssignmentUserRolesRequest.builder()
            .caseAssignmentUserRolesWithOrganisation(buildRevokeAccessRequest(caseDetails, userId)).build();

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

    public String getActiveUserCaseRole(final String caseId, final String userAuthorisation) {
        log.info("retrieve active user case role for caseId {}", caseId);
        String idamUserId = idamService.getIdamUserId(userAuthorisation);
        CaseAssignmentUserRolesResource rolesResource1 = getUserRoles(caseId);
        log.info("idamUserId {} case roles {} for caseId {}",
            idamUserId, rolesResource1 != null ? rolesResource1 : "empty", caseId);

        CaseAssignmentUserRolesResource rolesResource = searchUserRoles(caseId);
        if (rolesResource != null) {
            List<CaseAssignmentUserRole> allRoles = rolesResource.getCaseAssignmentUserRoles();
            log.info("All roles {} for caseId {}", allRoles, caseId);
            List<CaseAssignmentUserRole> activeRole = allRoles.stream().filter(role -> role.getUserId().equals(idamUserId)).toList();
            if (!activeRole.isEmpty()) {
                log.info("Active Role {} for caseId {}", activeRole, caseId);
                String caseRole = activeRole.get(0).getCaseRole();
                log.info("case role found {} for caseId {}", caseRole, caseId);
                return caseRole;
            }
        }
        return "case";
    }

}
