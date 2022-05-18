package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.CaseDataApiV2;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.AssignCaseAccessServiceConfiguration;
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
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssignCaseAccessService {

    private final AssignCaseAccessServiceConfiguration assignCaseAccessServiceConfiguration;
    private final AssignCaseAccessRequestMapper assignCaseAccessRequestMapper;
    private final IdamService idamService;
    private final RestService restService;
    private final CaseAssignmentApi caseAssignmentApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final CaseDataApiV2 caseDataApi;
    private final SystemUserService systemUserService;

    private final FeatureToggleService featureToggleService;

    private static final String CREATOR_ROLE = "[CREATOR]";

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
        return caseAssignmentApi.applyDecision(authToken, authTokenGenerator.generate(),
            DecisionRequest.decisionRequest(caseDetails));
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
            authTokenGenerator.generate(),
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
            authTokenGenerator.generate(),
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
