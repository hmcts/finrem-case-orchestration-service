package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CcdDataStoreServiceConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CaseAssignedUserRolesRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseAssignedUserRolesRequest;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;

@Service
@Slf4j
@RequiredArgsConstructor
public class CcdDataStoreService {

    private final CcdDataStoreServiceConfiguration ccdDataStoreServiceConfiguration;
    private final CaseAssignedUserRolesRequestMapper caseAssignedUserRolesRequestMapper;
    private final IdamService idamService;
    private final RestService restService;

    public void addApplicantSolicitorRole(CaseDetails caseDetails, String authorisationToken, String organisationId) {
        addRole(caseDetails, authorisationToken, APP_SOLICITOR_POLICY, organisationId);
    }

    private void addRole(CaseDetails caseDetails, String authorisationToken, String role, String organisationId) {
        String userId = idamService.getIdamUserId(authorisationToken);
        CaseAssignedUserRolesRequest caseAssignedUserRolesRequest =
            caseAssignedUserRolesRequestMapper.mapToCaseAssignedUserRolesRequest(caseDetails, userId, role, organisationId);

        restService.restApiPostCall(
            authorisationToken,
            ccdDataStoreServiceConfiguration.getCaseUsersUrl(),
            caseAssignedUserRolesRequest);
    }
}
