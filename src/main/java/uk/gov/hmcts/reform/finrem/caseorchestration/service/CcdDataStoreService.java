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

    public void removeUserCaseRole(String caseId, String authorisationToken, String userId, String role) {
        RemoveUserRolesRequest removeUserRolesRequest = removeUserRolesRequestMapper
            .mapToRemoveUserRolesRequest(caseId, userId, role);

        restService.restApiDeleteCall(
            authorisationToken,
            ccdDataStoreServiceConfiguration.getRemoveCaseRolesUrl(),
            removeUserRolesRequest);
    }

    public void removeCreatorRole(CaseDetails caseDetails, String authorisationToken) {
        removeCreatorRole(caseDetails.getId(), authorisationToken);
    }

    public void removeCreatorRole(FinremCaseDetails finremCaseDetails, String authorisationToken) {
        removeCreatorRole(finremCaseDetails.getId(), authorisationToken);
    }

    private void removeCreatorRole(Long caseId, String authorisationToken) {
        String userId = idamService.getIdamUserId(authorisationToken);

        removeUserCaseRole(String.valueOf(caseId), authorisationToken, userId, CREATOR_USER_ROLE);
    }
}
