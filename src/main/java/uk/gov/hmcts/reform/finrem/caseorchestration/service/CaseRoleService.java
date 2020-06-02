package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseUserApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseUser;
import uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.HashSet;
import java.util.Set;

import static uk.gov.hmcts.reform.finrem.caseorchestration.service.util.AuthUtil.getBearerToken;

public class CaseRoleService {

    @Autowired
    private CaseUserApi caseUserApi;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private IdamUserService userService;

    public void addApplicantSolicitorRole(String authorisation, String caseId) {
        addRole(authorisation, caseId, OrchestrationConstants.APP_SOL_ROLE);
    }

    public void addRespondentSolicitorRole(String authorisation, String caseId) {
        addRole(authorisation, caseId, OrchestrationConstants.RESP_SOL_ROLE);
    }

    private void addRole(String authorisation, String caseId, String caseRole) {
        User solicitorUser = userService.retrieveUser(getBearerToken(authorisation));
        User caseworkerUser = userService.retrieveAnonymousCaseWorkerDetails();
        Set<String> caseRoles = new HashSet<>();
        caseRoles.add(caseRole);
        updateCaseRoles(caseworkerUser, caseId, solicitorUser.getUserDetails().getId(), caseRoles);
    }

    private void updateCaseRoles(User anonymousCaseWorker, String caseId, String userId, Set<String> caseRoles) {
        caseUserApi.updateCaseRolesForUser(
            anonymousCaseWorker.getAuthToken(),
            authTokenGenerator.generate(),
            caseId,
            userId,
            new CaseUser(userId, caseRoles)
        );
    }
}
