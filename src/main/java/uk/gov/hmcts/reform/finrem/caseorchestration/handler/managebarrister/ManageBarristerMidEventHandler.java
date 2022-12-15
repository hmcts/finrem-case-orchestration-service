package uk.gov.hmcts.reform.finrem.caseorchestration.handler.managebarrister;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.BarristerValidationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.ManageBarristerService;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManageBarristerMidEventHandler implements CallbackHandler<Map<String, Object>> {

    private final ManageBarristerService manageBarristerService;
    private final BarristerValidationService barristerValidationService;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.MANAGE_BARRISTER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle(CallbackRequest callbackRequest, String userAuthorisation) {
        log.info("In the manage barrister mid-event handler for case {}", callbackRequest.getCaseDetails().getId());
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        String authTokenToUse = manageBarristerService.getAuthTokenToUse(caseDetails, userAuthorisation);
        List<BarristerData> barristers = manageBarristerService.getBarristersForParty(caseDetails, userAuthorisation);
        String caseRole = manageBarristerService.getCaseRole(caseDetails, userAuthorisation);

        List<String> errors = barristerValidationService.validateBarristerEmails(barristers,
            authTokenToUse, caseDetails.getId().toString(), caseRole);

        if (!errors.isEmpty()) {
            return GenericAboutToStartOrSubmitCallbackResponse.<Map<String, Object>>builder().data(caseDetails.getData()).errors(errors).build();
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<Map<String, Object>>builder().data(caseDetails.getData()).build();
    }
}
