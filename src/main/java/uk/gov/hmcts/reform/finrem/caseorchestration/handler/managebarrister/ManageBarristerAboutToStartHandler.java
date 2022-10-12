package uk.gov.hmcts.reform.finrem.caseorchestration.handler.managebarrister;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseAssignedRoleService;

import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASEWORKER_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_ROLE;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManageBarristerAboutToStartHandler implements CallbackHandler {

    private final CaseAssignedRoleService caseAssignedRoleService;


    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.MANAGE_BARRISTER.equals(eventType);
    }

    @Override
    public AboutToStartOrSubmitCallbackResponse handle(CallbackRequest callbackRequest,
                                                       String userAuthorisation) {
        log.info("In Manage barrister about to start callback for case {}", callbackRequest.getCaseDetails().getId());
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();

        CaseAssignedUserRolesResource userCaseRole = caseAssignedRoleService.getCaseAssignedUserRole(caseDetails, userAuthorisation);
        if (userCaseRole.getCaseAssignedUserRoles() == null || userCaseRole.getCaseAssignedUserRoles().isEmpty()) {
            caseData.put(CASE_ROLE, CASEWORKER_ROLE);
        } else {
            caseData.put(CASE_ROLE, userCaseRole.getCaseAssignedUserRoles().get(0).getCaseRole());
        }

        log.info("current user case role is {} for case {}", caseData.get(CASE_ROLE), caseDetails.getId());

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build();
    }

}
