package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import uk.gov.hmcts.reform.finrem.ccd.callback.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseType;
import uk.gov.hmcts.reform.finrem.ccd.domain.EventType;

public interface CallbackHandler {

    boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType);

    AboutToStartOrSubmitCallbackResponse handle(CallbackRequest callbackRequest,
                                                String userAuthorisation);
}
