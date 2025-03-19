package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;

/**
 * Utility class for creating log messages for handling CCD callback requests.
 */
@Slf4j
public class CallbackHandlerLogger {

    private CallbackHandlerLogger() {
        // All access through static methods
    }

    /**
     * Creates a log message for an 'About To Start' callback request.
     * @param callbackRequest the callback request
     * @return the log message
     */
    public static String aboutToStart(FinremCallbackRequest callbackRequest) {
        return createLogMessage("About To Start", callbackRequest);
    }

    /**
     * Creates a log message for a 'Mid-Event' callback request.
     * @param callbackRequest the callback request
     * @return the log message
     */
    public static String midEvent(FinremCallbackRequest callbackRequest) {
        return createLogMessage("Mid-Event", callbackRequest);
    }

    /**
     * Creates a log message for an 'About To Submit' callback request.
     * @param callbackRequest the callback request
     * @return the log message
     */
    public static String aboutToSubmit(FinremCallbackRequest callbackRequest) {
        return createLogMessage("About To Submit", callbackRequest);
    }

    /**
     * Creates a log message for a 'Submitted' callback request.
     * @param callbackRequest the callback request
     * @return the log message
     */
    public static String submitted(FinremCallbackRequest callbackRequest) {
        return createLogMessage("Submitted", callbackRequest);
    }

    private static String createLogMessage(String callbackType, FinremCallbackRequest callbackRequest) {
        CaseType caseType = callbackRequest.getCaseDetails().getCaseType();
        EventType eventType = callbackRequest.getEventType();
        String ccdEventType = eventType == null ? "null" : eventType.getCcdType();
        String caseId = String.valueOf(callbackRequest.getCaseDetails().getId());

        return String.format("===> Case ID %s: Handling %s %s callback for event %s",
                caseId, caseType, callbackType, ccdEventType);
    }
}
