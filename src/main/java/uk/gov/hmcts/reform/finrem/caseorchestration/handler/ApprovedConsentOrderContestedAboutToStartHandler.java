package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnStartDefaultValueService;

import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovedConsentOrderContestedAboutToStartHandler implements CallbackHandler {

    private final OnStartDefaultValueService onStartDefaultValueService;

    @Override
    public boolean canHandle(final CallbackType callbackType, final CaseType caseType,
                             final EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.APPROVE_APPLICATION.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle(CallbackRequest callbackRequest,
                                                                                   String userAuthorisation) {
        onStartDefaultValueService.defaultContestedOrderJudgeName(callbackRequest, userAuthorisation);
        onStartDefaultValueService.defaultContestedOrderDate(callbackRequest);
        return GenericAboutToStartOrSubmitCallbackResponse.<Map<String, Object>>builder().data(callbackRequest.getCaseDetails().getData()).build();
    }
}
