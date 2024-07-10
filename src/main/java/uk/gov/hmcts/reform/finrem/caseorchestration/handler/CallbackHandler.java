package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;

public interface CallbackHandler<D> {

    boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType);

    default boolean canHandle(CaseType caseType, EventType eventType, CallbackContext context) {
        return canHandle(context.getCallbackType(), caseType, eventType);
    }

    GenericAboutToStartOrSubmitCallbackResponse<D> handle(CallbackRequest callbackRequest,
                                                          String userAuthorisation);

    default GenericAboutToStartOrSubmitCallbackResponse<D> handle(CallbackRequest callbackRequest,  String userAuthorisation,
                                                                  CallbackContext context) {
        return handle(callbackRequest, userAuthorisation);
    }
}
