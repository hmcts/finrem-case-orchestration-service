package uk.gov.hmcts.reform.finrem.caseorchestration.ccd;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType.getCaseType;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.getEventType;

@RequiredArgsConstructor
@Component
public class CallbackDispatcher {

    private final List<CallbackHandler> callbackHandlers;


    public AboutToStartOrSubmitCallbackResponse dispatchToHandlers(CallbackType callbackType,
                                                                   CallbackRequest callbackRequest,
                                                                   String userAuthorisation) {
        requireNonNull(callbackRequest, "callback must not be null");

        AboutToStartOrSubmitCallbackResponse callbackResponse = AboutToStartOrSubmitCallbackResponse
            .builder()
            .errors(new ArrayList<>())
            .warnings(new ArrayList<>())
            .build();

        for (CallbackHandler callbackHandler : callbackHandlers) {
            if (callbackHandler.canHandle(callbackType,
                getCaseType(callbackRequest.getCaseDetails().getCaseTypeId()),
                getEventType(callbackRequest.getEventId()))) {

                AboutToStartOrSubmitCallbackResponse handlerCallbackResponse =
                    callbackHandler.handle(callbackRequest, userAuthorisation);

                callbackResponse.setData(handlerCallbackResponse.getData());
                callbackResponse.getErrors().addAll(handlerCallbackResponse.getErrors());
                callbackResponse.getWarnings().addAll(handlerCallbackResponse.getWarnings());
            }
        }

        return callbackResponse;
    }
}
