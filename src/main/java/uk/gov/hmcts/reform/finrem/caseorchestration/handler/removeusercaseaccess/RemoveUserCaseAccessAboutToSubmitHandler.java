package uk.gov.hmcts.reform.finrem.caseorchestration.handler.removeusercaseaccess;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdDataStoreService;

@Service
@Slf4j
public class RemoveUserCaseAccessAboutToSubmitHandler extends FinremCallbackHandler {

    private final CcdDataStoreService ccdDataStoreService;

    public RemoveUserCaseAccessAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                    CcdDataStoreService ccdDataStoreService) {
        super(finremCaseDetailsMapper);
        this.ccdDataStoreService = ccdDataStoreService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && EventType.REMOVE_USER_CASE_ACCESS.equals(eventType)
            && (CaseType.CONSENTED.equals(caseType) || CaseType.CONTESTED.equals(caseType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(
        FinremCallbackRequest finremCallbackRequest, String authToken) {

        String caseId = String.valueOf(finremCallbackRequest.getCaseDetails().getId());
        log.info("Remove User Case Access about to submit callback for Case ID: {}", caseId);

        FinremCaseData caseData = finremCallbackRequest.getCaseDetails().getData();
        removeUserCaseAccess(caseId, caseData, authToken);
        caseData.setUserCaseAccessList(null);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData)
            .build();
    }

    private void removeUserCaseAccess(String caseId, FinremCaseData caseData, String authToken) {
        DynamicListElement dynamicListElement = caseData.getUserCaseAccessList().getValue();
        if (dynamicListElement == null) {
            log.warn("Remove User Case Access Case Id {}: No user selected for removal", caseId);
            return;
        }
        String selectedCode = dynamicListElement.getCode();
        String[] values = selectedCode.split(RemoveUserCaseAccessAboutToStartHandler.CODE_VALUES_SEPARATOR);
        String userId = values[0];
        String caseRole = values[1];

        ccdDataStoreService.removeUserCaseRole(caseId, authToken, userId, caseRole);
    }
}
