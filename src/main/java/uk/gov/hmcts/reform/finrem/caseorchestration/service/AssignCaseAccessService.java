package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.AssignCaseAccessServiceConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.AssignCaseAccessRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.AssignCaseAccessRequest;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssignCaseAccessService {

    private final AssignCaseAccessServiceConfiguration assignCaseAccessServiceConfiguration;
    private final AssignCaseAccessRequestMapper assignCaseAccessRequestMapper;
    private final IdamService idamService;
    private final RestService restService;

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
}
