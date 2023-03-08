package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CcdDataStoreServiceConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.RemoveUserRolesRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.RemoveUserRolesRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CREATOR_USER_ROLE;

@Service
@Slf4j
@RequiredArgsConstructor
public class CcdDataStoreService {

    private final CcdDataStoreServiceConfiguration ccdDataStoreServiceConfiguration;
    private final RemoveUserRolesRequestMapper removeUserRolesRequestMapper;
    private final IdamService idamService;
    private final RestService restService;

    public void removeCreatorRole(CaseDetails caseDetails, String authorisationToken) {
        removeRole(caseDetails, authorisationToken, CREATOR_USER_ROLE);
    }

    public void removeCreatorRole(FinremCaseDetails finremCaseDetails, String authorisationToken) {
        removeRole(finremCaseDetails, authorisationToken, CREATOR_USER_ROLE);
    }

    private void removeRole(CaseDetails caseDetails, String authorisationToken, String role) {
        String userId = idamService.getIdamUserId(authorisationToken);
        RemoveUserRolesRequest removeUserRolesRequest = removeUserRolesRequestMapper.mapToRemoveUserRolesRequest(caseDetails, userId, role);

        restService.restApiDeleteCall(
            authorisationToken,
            ccdDataStoreServiceConfiguration.getRemoveCaseRolesUrl(),
            removeUserRolesRequest);
    }

    private void removeRole(FinremCaseDetails finremCaseDetails, String authorisationToken, String role) {
        String userId = idamService.getIdamUserId(authorisationToken);
        RemoveUserRolesRequest removeUserRolesRequest = removeUserRolesRequestMapper.mapToRemoveUserRolesRequest(finremCaseDetails, userId, role);

        restService.restApiDeleteCall(
            authorisationToken,
            ccdDataStoreServiceConfiguration.getRemoveCaseRolesUrl(),
            removeUserRolesRequest);
    }
}
