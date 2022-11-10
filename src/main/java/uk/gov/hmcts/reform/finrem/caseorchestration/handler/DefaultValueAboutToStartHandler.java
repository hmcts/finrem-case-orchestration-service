package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;

import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CIVIL_PARTNERSHIP;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultValueAboutToStartHandler implements CallbackHandler {

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && (CaseType.CONSENTED.equals(caseType) || CaseType.CONTESTED.equals(caseType))
            && (EventType.SOLICITOR_CREATE.equals(eventType)
            || EventType.AMEND_CASE.equals(eventType)
            || EventType.AMEND_CONTESTED_APP_DETAILS.equals(eventType)
            || EventType.AMEND_CONTESTED_PAPER_APP_DETAILS.equals(eventType));
    }

    @Override
    public AboutToStartOrSubmitCallbackResponse handle(CallbackRequest callbackRequest,
                                                       String userAuthorisation) {
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();
        caseData.putIfAbsent(CIVIL_PARTNERSHIP, NO_VALUE);
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build();
    }
}
