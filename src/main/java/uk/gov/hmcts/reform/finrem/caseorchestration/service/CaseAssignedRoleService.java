package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

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

    private final CaseDataService caseDataService;
    private final CaseRoleService caseRoleService;

    public Map<String, Object> setCaseAssignedUserRole(CaseDetails caseDetails,
                                                       String authToken) {

        CaseAssignedUserRolesResource resource = caseRoleService.getCaseAssignedUserRole(caseDetails.getId().toString(), authToken);
        String caseRole = resource.getCaseAssignedUserRoles().getFirst().getCaseRole();

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

    public FinremCaseData setCaseAssignedUserRole(FinremCaseDetails finremCaseDetails,
                                                  String authToken) {
        CaseAssignedUserRolesResource resource = caseRoleService.getCaseAssignedUserRole(finremCaseDetails.getId().toString(), authToken);
        String caseRole = resource.getCaseAssignedUserRoles().getFirst().getCaseRole();

        boolean isConsented = caseDataService.isConsentedApplication(finremCaseDetails);

        if (caseRole.equals(APP_SOLICITOR_POLICY)) {
            finremCaseDetails.getData().getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);
            finremCaseDetails.getData().setCurrentUserCaseRole(CaseRole.APP_SOLICITOR);
        } else if (caseRole.equals(RESP_SOLICITOR_POLICY)) {
            if (isConsented) {
                finremCaseDetails.getData().getContactDetailsWrapper().setConsentedRespondentRepresented(YesOrNo.YES);
            } else {
                finremCaseDetails.getData().getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.YES);
            }
            finremCaseDetails.getData().setCurrentUserCaseRole(CaseRole.RESP_SOLICITOR);
        }

        return finremCaseDetails.getData();
    }
}
