package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_DIRECTION_JUDGE_NAME;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("java:S3740")
public class ConsentApplicationApprovedInContestedAboutToStartHandler implements CallbackHandler {

    private final IdamService service;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.CONSENT_APPLICATION_APPROVED_IN_CONTESTED.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle(CallbackRequest callbackRequest,
                                                                                   String userAuthorisation) {
        log.info("Received request for {} caseId {}", EventType.CONSENT_APPLICATION_APPROVED_IN_CONTESTED,
            callbackRequest.getCaseDetails().getId());
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();
        if (Objects.isNull(caseData.get(CONTESTED_ORDER_DIRECTION_JUDGE_NAME))) {
            caseData.put(CONTESTED_ORDER_DIRECTION_JUDGE_NAME, service.getIdamFullName(userAuthorisation));
        }
        return GenericAboutToStartOrSubmitCallbackResponse.<Map<String, Object>>builder().data(caseData).build();
    }
}
