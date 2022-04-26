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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRolesResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRolesResponse;

import java.util.List;

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
    private final SystemUserService systemUserService;
    private final CaseDataApiV2 coreCaseDataApi;

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
        return caseAssignmentApi.applyDecision(authToken, authTokenGenerator.generate(),
            DecisionRequest.decisionRequest(caseDetails));
    }

    public AboutToStartOrSubmitCallbackResponse applyDecision(CaseDetails caseDetails) {
        return applyDecision(systemUserService.getSysUserToken(), caseDetails);
    }

    public CaseAssignmentUserRolesResource getUsersWithAccess(String caseRoleId) {
        String userToken = systemUserService.getSysUserToken();

        return coreCaseDataApi.getUserRoles(userToken, authTokenGenerator.generate(), List.of(caseRoleId));
    }

    public CaseAssignmentUserRolesResponse revokeUserAccess(CaseAssignmentUserRolesRequest request) {
        String userToken = systemUserService.getSysUserToken();

        return coreCaseDataApi.removeCaseUserRoles(userToken, authTokenGenerator.generate(), request);
    }
}
