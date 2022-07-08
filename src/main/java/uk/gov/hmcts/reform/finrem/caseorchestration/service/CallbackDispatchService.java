package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandler;
import uk.gov.hmcts.reform.finrem.ccd.callback.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@RequiredArgsConstructor
@Service
public class CallbackDispatchService {

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
                callbackRequest.getCaseDetails().getCaseType(),
                callbackRequest.getEventType())) {

                AboutToStartOrSubmitCallbackResponse handlerCallbackResponse =
                    callbackHandler.handle(callbackRequest, userAuthorisation);

                callbackResponse.setData(handlerCallbackResponse.getData());
                List<String> errors = Optional.ofNullable(handlerCallbackResponse.getErrors()).orElse(Collections.EMPTY_LIST);
                callbackResponse.getErrors().addAll(errors);
                List<String> warnings = Optional.ofNullable(handlerCallbackResponse.getWarnings()).orElse(Collections.EMPTY_LIST);
                callbackResponse.getWarnings().addAll(warnings);
            }
        }

        return callbackResponse;
    }
}
