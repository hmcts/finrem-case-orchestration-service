package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.getEventType;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.forValue;


@RequiredArgsConstructor
@Service
@Slf4j
public class CallbackDispatchService {

    @SuppressWarnings("java:S3740")
    private final List<CallbackHandler> callbackHandlers;

    @SuppressWarnings("java:S3740")
    public GenericAboutToStartOrSubmitCallbackResponse dispatchToHandlers(CallbackType callbackType,
                                                                          CallbackRequest callbackRequest,
                                                                          String userAuthorisation) {
        requireNonNull(callbackRequest, "callback must not be null");

        GenericAboutToStartOrSubmitCallbackResponse callbackResponse = GenericAboutToStartOrSubmitCallbackResponse
            .builder()
            .errors(new ArrayList<>())
            .warnings(new ArrayList<>())
            .build();

        CaseType caseType = forValue(callbackRequest.getCaseDetails().getCaseTypeId());
        EventType eventType = getEventType(callbackRequest.getEventId());
        boolean handled = false;
        for (CallbackHandler callbackHandler : callbackHandlers) {
            if (callbackHandler.canHandle(callbackType, caseType, eventType)) {

                GenericAboutToStartOrSubmitCallbackResponse handlerCallbackResponse =
                    callbackHandler.handle(callbackRequest, userAuthorisation);

                callbackResponse.setData(handlerCallbackResponse.getData());
                List<String> errors = Optional.ofNullable(handlerCallbackResponse.getErrors()).orElse(Collections.emptyList());
                callbackResponse.getErrors().addAll(errors);
                List<String> warnings = Optional.ofNullable(handlerCallbackResponse.getWarnings()).orElse(Collections.emptyList());
                callbackResponse.getWarnings().addAll(warnings);
                callbackResponse.setState(handlerCallbackResponse.getState());

                handled = true;
            }
        }

        if (!handled) {
            Long caseId = callbackRequest.getCaseDetails().getId();
            log.warn("No handler for case id {} callback {} case type {} event {}",
                caseId, callbackType, caseType, eventType);
        }

        return callbackResponse;
    }
}
