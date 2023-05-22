package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DataStoreClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRolesResource;

import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.config.CacheConfiguration.REQUEST_SCOPED_CACHE_MANAGER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.config.CacheConfiguration.USER_ROLES_CACHE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_POLICY;

@Service
@Slf4j
@RequiredArgsConstructor
public class CaseAssignedRoleService {

    private final DataStoreClient dataStoreClient;
    private final CaseDataService caseDataService;
    private final AuthTokenGenerator authTokenGenerator;
    private final IdamService idamService;

    public Map<String, Object> setCaseAssignedUserRole(CaseDetails caseDetails,
                                                       String authToken) {
        String caseId = String.valueOf(caseDetails.getId());
        log.info("Setting caseRole for case Id {}", caseId);
        CaseAssignedUserRolesResource resource = getCaseAssignedUserRole(caseId, authToken);
        String caseRole = resource.getCaseAssignedUserRoles().get(0).getCaseRole();

        boolean isConsented = caseDataService.isConsentedApplication(caseDetails);

        if (caseRole.equals(APP_SOLICITOR_POLICY)) {
            caseDetails.getData().put(APPLICANT_REPRESENTED, YES_VALUE);
        } else if (caseRole.equals(RESP_SOLICITOR_POLICY)) {
            caseDetails.getData().put(isConsented ? CONSENTED_RESPONDENT_REPRESENTED
                : CONTESTED_RESPONDENT_REPRESENTED, YES_VALUE);
        }

        caseDetails.getData().put(CASE_ROLE, caseRole);

        return caseDetails.getData();
    }

    @Cacheable(cacheManager = REQUEST_SCOPED_CACHE_MANAGER, cacheNames = USER_ROLES_CACHE)
    public CaseAssignedUserRolesResource getCaseAssignedUserRole(String caseId, String authToken) {
        return dataStoreClient.getUserRoles(authToken, authTokenGenerator.generate(),
            caseId, idamService.getIdamUserId(authToken));
    }
}
