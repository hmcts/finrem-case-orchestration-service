package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
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

    protected final FinremCaseDetailsMapper finremCaseDetailsMapper;

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
    private static List<Class> getClassesWithTemporaryFieldAnnotation() {
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

    /**
     * Executes the given action with retry logic.
     *
     * <p>If the action throws an exception, it will be retried until the configured
     * number of attempts is exhausted. Each failure is logged together with the
     * remaining retry count.</p>
     *
     * <p>When all retry attempts are exhausted, the last encountered exception
     * is rethrown to the caller.</p>
     *
     * @param log          the logger used to record retry attempts and failures
     * @param action       the operation to execute
     * @param caseId       identifier used for logging context
     * @param actionName   human-readable description of the action being performed
     * @param attemptsLeft number of attempts remaining (must be greater than 0)
     *
     * @throws Exception if the action continues to fail after all retry attempts
     */
    protected void executeWithRetry(Logger log, Runnable action, String caseId, String actionName, int attemptsLeft) {
        try {
            action.run();

        } catch (Exception e) {
            log.error("{} - Failed {}. Attempts left: {}",
                caseId, actionName, attemptsLeft - 1, e);

            if (attemptsLeft > 1) {
                executeWithRetry(log,
                    action,
                    caseId,
                    actionName,
                    attemptsLeft - 1
                );
            } else {
                log.error("{} - All retry attempts exhausted while {}",
                    caseId, actionName);
                throw e;
            }
        }
    }

    /**
     * Executes the given action with retry logic while suppressing exceptions.
     *
     * <p>If the action throws an exception, it will be retried until the configured
     * number of attempts is exhausted. Failures are logged, but no exception is
     * propagated to the caller.</p>
     *
     * <p>This method should be used for non-critical operations where failure
     * must not interrupt the main workflow.</p>
     *
     * @param log          the logger used to record retry attempts and failures
     * @param action       the operation to execute
     * @param caseId       identifier used for logging context
     * @param actionName   human-readable description of the action being performed
     * @param attemptsLeft number of attempts remaining (must be greater than 0)
     */
    protected void executeWithRetrySafely(Logger log, Runnable action, String caseId, String actionName, int attemptsLeft) {
        try {
            action.run();
        } catch (Exception e) {
            log.error("{} - Failed {}. Attempts left: {}",
                caseId, actionName, attemptsLeft - 1, e);

            if (attemptsLeft > 1) {
                executeWithRetrySafely(log, action, caseId, actionName, attemptsLeft - 1);
            } else {
                log.error("{} - All retry attempts exhausted while {}",
                    caseId, actionName);
            }
        }
    }
}
