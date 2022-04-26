package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DataStoreClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRolesResource;

import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
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

    public Map<String, Object> setCaseAssignedUserRole(CaseDetails caseDetails,
                                                       String authToken) {

        CaseAssignedUserRolesResource resource = dataStoreClient.getCaseAssignedUserRoles(caseDetails.getId(),
            authToken, authTokenGenerator.generate());
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
}
