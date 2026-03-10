package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.InvalidCaseDataException;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.TemporaryField;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.StopRepresentationWrapper;

import java.lang.reflect.Field;
import java.util.List;

import static org.apache.commons.lang3.reflect.FieldUtils.getFieldsListWithAnnotation;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RequiredArgsConstructor
public abstract class FinremCallbackHandler implements CallbackHandler<FinremCaseData> {

    private static final String RETRYING_MESSAGE = "{} - Failed {}. Attempts left: {}";

    private static final String RETRY_EXHAUSTING_MESSAGE = "{} - All retry attempts exhausted while {}";

    @SuppressWarnings("java:S112")
    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Exception;
    }

    @SuppressWarnings("java:S112")
    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }

    protected final FinremCaseDetailsMapper finremCaseDetailsMapper;

    protected static final int DEFAULT_RETRY_ATTEMPTS = 3;

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(CallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        return removeTemporaryFieldsAfterHandled(
            handle(mapToFinremCallbackRequest(callbackRequest), userAuthorisation));
    }

    public abstract GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequestWithFinremCaseDetails,
                                                                                       String userAuthorisation);

    private FinremCallbackRequest mapToFinremCallbackRequest(CallbackRequest callbackRequest) {
        FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(callbackRequest.getCaseDetails());
        FinremCaseDetails finremCaseDetailsBefore = null;
        if (callbackRequest.getCaseDetailsBefore() != null) {
            finremCaseDetailsBefore = finremCaseDetailsMapper.mapToFinremCaseDetails(callbackRequest.getCaseDetailsBefore());
        }
        finremCaseDetails.getData().setCcdCaseId(finremCaseDetails.getCaseIdAsString());
        return FinremCallbackRequest.builder()
            .caseDetails(finremCaseDetails)
            .caseDetailsBefore(finremCaseDetailsBefore)
            .eventType(EventType.getEventType(callbackRequest.getEventId()))
            .build();
    }

    protected void validateCaseData(FinremCallbackRequest callbackRequest) {
        if (callbackRequest == null
            || callbackRequest.getCaseDetails() == null
            || callbackRequest.getCaseDetails().getData() == null) {
            throw new InvalidCaseDataException(BAD_REQUEST.value(), "Missing data from callbackRequest.");
        }
    }

    protected boolean shouldClearTemporaryFields() {
        return false;
    }

    /**
     * Removes all fields marked with {@link TemporaryField} from the case data
     * in the given callback response.
     *
     * <p>
     * This method first checks whether temporary fields should be cleared. If not,
     * it returns the original response.
     *
     * <p>
     * If clearing is required, the method maps the response data into
     * {@link FinremCaseDetails}, converts it into a CCD {@link CaseDetails} object,
     * finds all fields annotated with {@link TemporaryField}, and removes those field
     * names from the case data. It then maps the cleaned data back into
     * {@link FinremCaseData} and returns a new response containing the updated data.
     *
     * @param response the callback response containing case data to clean
     * @return a response with temporary fields removed, or the original response if
     *         clearing is not needed
     */
    protected GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> removeTemporaryFieldsAfterHandled(
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response) {
        if (!shouldClearTemporaryFields()) {
            return response;
        }

        FinremCaseDetails toBeSanitised = FinremCaseDetails.builder()
            .data(response.getData()).build();
        CaseDetails toBeSanitisedCaseDetails = finremCaseDetailsMapper.mapToCaseDetails(toBeSanitised);

        if (toBeSanitisedCaseDetails.getData() != null) {
            getClassesWithTemporaryFieldAnnotation().forEach(clazz ->
                getFieldsListWithAnnotation(clazz, TemporaryField.class).stream()
                    .map(Field::getName)
                    .forEach(toBeSanitisedCaseDetails.getData()::remove));
        }

        return response.toBuilder().data(finremCaseDetailsMapper.mapToFinremCaseData(toBeSanitisedCaseDetails.getData())).build();
    }

    /**
     * Returns the list of classes that contain fields annotated with {@link TemporaryField}
     * and should have those temporary fields cleared during sanitisation.
     *
     * <p><strong>Developer note:</strong> If you introduce a new class that uses
     * {@code @TemporaryField}, you must add it to this list so that its temporary
     * fields are removed correctly.</p>
     *
     * @return a list of classes containing {@code @TemporaryField}-annotated fields
     */
    private static List<Class<?>> getClassesWithTemporaryFieldAnnotation() {
        return List.of(StopRepresentationWrapper.class);
    }

    protected GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> submittedResponse() {
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().build();
    }

    protected GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> submittedResponse(String confirmationHeader,
                                                                                            String confirmationBody) {
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .confirmationHeader(confirmationHeader)
            .confirmationBody(confirmationBody)
            .build();
    }

    protected GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response(FinremCaseData finremCaseData) {
        return response(finremCaseData, null, null);
    }

    protected GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response(FinremCaseData finremCaseData,
                                                                                   List<String> warnings, List<String> errors) {
        var builder = GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder();
        builder.data(finremCaseData);
        if (errors != null && !errors.isEmpty()) {
            builder.errors(errors);
        }
        if (warnings != null && !warnings.isEmpty()) {
            builder.warnings(warnings);
        }
        return builder.build();
    }

    protected <T> T executeWithRetry(
        Logger log,
        ThrowingSupplier<T> action,
        String caseId,
        String actionName
    ) {
        return executeWithRetrySuppressingException(log, action, caseId, actionName, DEFAULT_RETRY_ATTEMPTS);
    }

    protected <T> T executeWithRetry(
        Logger log,
        ThrowingSupplier<T> action,
        String caseId,
        String actionName,
        int attempts
    ) {

        int remainingAttempts = attempts;

        while (remainingAttempts > 0) {
            try {
                return action.get();
            } catch (Exception ex) {
                remainingAttempts--;

                if (remainingAttempts > 0) {
                    log.warn(RETRYING_MESSAGE, caseId, actionName, remainingAttempts, ex);
                } else {
                    log.error(RETRY_EXHAUSTING_MESSAGE, caseId, actionName, ex);
                    throw new RuntimeException(ex);
                }
            }
        }

        throw new IllegalStateException("Retry logic failure");
    }

    protected void executeWithRetry(
        Logger log,
        ThrowingRunnable action,
        String caseId,
        String actionName
    ) {
        executeWithRetry(log, action, caseId, actionName, DEFAULT_RETRY_ATTEMPTS);
    }

    protected void executeWithRetry(
        Logger log,
        ThrowingRunnable action,
        String caseId,
        String actionName,
        int attempts
    ) {
        executeWithRetry(log, () -> {
            action.run();
            return null;
        }, caseId, actionName, attempts);
    }

    protected <T> T executeWithRetrySuppressingException(
        Logger log,
        ThrowingSupplier<T> action,
        String caseId,
        String actionName
    ) {
        return executeWithRetrySuppressingException(log, action, caseId, actionName, DEFAULT_RETRY_ATTEMPTS);
    }

    protected <T> T executeWithRetrySuppressingException(
        Logger log,
        ThrowingSupplier<T> action,
        String caseId,
        String actionName,
        int attempts
    ) {
        int remainingAttempts = attempts;

        while (remainingAttempts > 0) {
            try {
                return action.get();
            } catch (Exception ex) {
                remainingAttempts--;

                if (remainingAttempts > 0) {
                    log.warn(RETRYING_MESSAGE, caseId, actionName, remainingAttempts, ex);
                } else {
                    log.error("{} - All retry attempts exhausted while {}. Exceptions were suppressed",
                        caseId, actionName, ex);
                    return null;
                }
            }
        }

        return null;
    }

    protected void executeWithRetrySuppressingException(
        Logger log,
        ThrowingRunnable action,
        String caseId,
        String actionName
    ) {
        executeWithRetrySuppressingException(log, action, caseId, actionName, DEFAULT_RETRY_ATTEMPTS);
    }

    protected void executeWithRetrySuppressingException(
        Logger log,
        ThrowingRunnable action,
        String caseId,
        String actionName,
        int attempts
    ) {
        executeWithRetrySuppressingException(
            log,
            () -> {
                action.run();
                return null;
            },
            caseId,
            actionName,
            attempts
        );
    }

    protected String toConfirmationBody(String... messages) {
        StringBuilder body = new StringBuilder("<ul>");

        if (messages != null) {
            for (String error : messages) {
                if (error != null && !error.isBlank()) {
                    body.append("<li><h2>%s</h2></li>".formatted(StringEscapeUtils.escapeHtml4(error)));
                }
            }
        }

        body.append("</ul>");
        return body.toString();
    }
}
