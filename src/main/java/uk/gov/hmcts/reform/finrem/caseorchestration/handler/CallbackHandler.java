package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;

public interface CallbackHandler {

    boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType);

    AboutToStartOrSubmitCallbackResponse handle(CallbackRequest callbackRequest,
                                                String userAuthorisation);
}
