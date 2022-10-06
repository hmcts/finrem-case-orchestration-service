package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseManagementLocationService;

@Service
@Slf4j
@RequiredArgsConstructor
public class SetCaseManagementLocationAboutToSubmitHandler implements CallbackHandler {
    private final CaseManagementLocationService caseManagementLocationService;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.SET_CASE_MANAGEMENT_LOCATION.equals(eventType);
    }

    @Override
    public AboutToStartOrSubmitCallbackResponse handle(CallbackRequest callbackRequest, String userAuthorisation) {
        log.info("About to start handling set Case management location about to submit for {}",
            callbackRequest.getCaseDetails().getId());

        return caseManagementLocationService.setCaseManagementLocation(callbackRequest);
    }
}
