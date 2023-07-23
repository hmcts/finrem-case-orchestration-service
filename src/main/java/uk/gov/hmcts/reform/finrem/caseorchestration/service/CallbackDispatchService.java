package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.getEventType;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.forValue;


@RequiredArgsConstructor
@Service
public class CallbackDispatchService {

    private final List<CallbackHandler> callbackHandlers;

    public GenericAboutToStartOrSubmitCallbackResponse dispatchToHandlers(CallbackType callbackType,
                                                                          CallbackRequest callbackRequest,
                                                                          String userAuthorisation) {
        requireNonNull(callbackRequest, "callback must not be null");

        GenericAboutToStartOrSubmitCallbackResponse callbackResponse = GenericAboutToStartOrSubmitCallbackResponse
            .builder()
            .errors(new ArrayList<>())
            .warnings(new ArrayList<>())
            .build();

        for (CallbackHandler callbackHandler : callbackHandlers) {
            if (callbackHandler.canHandle(callbackType,
                forValue(callbackRequest.getCaseDetails().getCaseTypeId()),
                getEventType(callbackRequest.getEventId()))) {

                GenericAboutToStartOrSubmitCallbackResponse handlerCallbackResponse =
                    callbackHandler.handle(callbackRequest, userAuthorisation);

                callbackResponse.setData(handlerCallbackResponse.getData());
                List<String> errors = Optional.ofNullable(handlerCallbackResponse.getErrors()).orElse(Collections.emptyList());
                callbackResponse.getErrors().addAll(errors);
                List<String> warnings = Optional.ofNullable(handlerCallbackResponse.getWarnings()).orElse(Collections.emptyList());
                callbackResponse.getWarnings().addAll(warnings);
                callbackResponse.setState(handlerCallbackResponse.getState());
            }
        }

        return callbackResponse;
    }
}
