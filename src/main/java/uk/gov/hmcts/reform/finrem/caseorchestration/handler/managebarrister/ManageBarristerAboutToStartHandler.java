package uk.gov.hmcts.reform.finrem.caseorchestration.handler.managebarrister;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseAssignedRoleService;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASEWORKER_ROLE_FIELD_SHOW_LABEL;

@Slf4j
@Service
public class ManageBarristerAboutToStartHandler extends FinremCallbackHandler {

    private final CaseAssignedRoleService caseAssignedRoleService;

    public ManageBarristerAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                              CaseAssignedRoleService caseAssignedRoleService) {
        super(finremCaseDetailsMapper);
        this.caseAssignedRoleService = caseAssignedRoleService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.MANAGE_BARRISTER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToStart(callbackRequest));

        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();

        CaseAssignedUserRolesResource userCaseRole = caseAssignedRoleService.getCaseAssignedUserRole(
            callbackRequest.getCaseDetails().getId().toString(), userAuthorisation);
        if (CollectionUtils.isEmpty(userCaseRole.getCaseAssignedUserRoles())) {
            caseData.setCurrentUserCaseRole(CaseRole.CASEWORKER);
            caseData.setCurrentUserCaseRoleLabel(CASEWORKER_ROLE_FIELD_SHOW_LABEL);
        } else {
            String caseRole = userCaseRole.getCaseAssignedUserRoles().getFirst().getCaseRole();
            caseData.setCurrentUserCaseRole(CaseRole.forValue(caseRole));
            caseData.setCurrentUserCaseRoleLabel(caseRole.replace("[", "").replace("]",""));
        }
        caseData.setBarristerParty(null);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData)
            .build();
    }
}
