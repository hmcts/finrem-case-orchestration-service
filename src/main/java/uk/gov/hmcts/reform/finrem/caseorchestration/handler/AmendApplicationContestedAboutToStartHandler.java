package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnStartDefaultValueService;

import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.URGENT_CASE_QUESTION;

@Slf4j
@Service
@RequiredArgsConstructor
public class AmendApplicationContestedAboutToStartHandler implements CallbackHandler<Map<String, Object>> {

    private final OnStartDefaultValueService service;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && (EventType.AMEND_CONTESTED_APP_DETAILS.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle(CallbackRequest callbackRequest,
                                                                                   String userAuthorisation) {
        service.defaultCivilPartnershipField(callbackRequest);
        service.defaultTypeOfApplication(callbackRequest);
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();
        if (CaseType.CONTESTED.getCcdType().equals(callbackRequest.getCaseDetails().getCaseTypeId())) {
            caseData.putIfAbsent(URGENT_CASE_QUESTION, NO_VALUE);
        }
        return GenericAboutToStartOrSubmitCallbackResponse.<Map<String, Object>>builder()
            .data(callbackRequest.getCaseDetails().getData()).build();
    }
}
