package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;

public interface CallbackHandler<D> {

    boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType);

    GenericAboutToStartOrSubmitCallbackResponse<D> handle(CallbackRequest callbackRequest,
                                                          String userAuthorisation);
}
