package uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryContext;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Component;
import org.springframework.util.function.ThrowingSupplier;

/**
 * Utility component that executes actions with automatic retry support.
 *
 * <p>This class wraps operations that may fail due to transient downstream
 * service errors (e.g. Feign client calls) and retries them using
 * Spring Retry's {@link Retryable} mechanism.</p>
 *
 * <p>Retries are triggered for specific {@link FeignException} types
 * (HTTP 500, 503, and 504). Retry attempts are logged via the configured
 * {@code retryLogger} listener. Additional contextual information such as
 * the action name and case ID is stored in the {@link RetryContext} to
 * assist with logging and diagnostics.</p>
 */
@Slf4j
@Component
public class RetryExecutor {

    /**
     * Executes a supplier operation with retry support and returns its result.
     *
     * <p>The supplied operation will be retried automatically if a supported
     * {@link FeignException} is thrown. Retry context attributes are populated
     * with the provided action name and case ID for logging purposes.</p>
     *
     * @param supplier the operation to execute
     * @param actionName a descriptive name of the action being performed
     * @param caseId the case identifier associated with the operation
     * @param <T> the result type returned by the supplier
     * @return the result produced by the supplier
     * @throws Exception if the operation ultimately fails after all retry attempts
     */
    @Retryable(
        value = {
            FeignException.InternalServerError.class,
            FeignException.ServiceUnavailable.class,
            FeignException.GatewayTimeout.class
        },
        backoff = @Backoff(delay = 2000),
        listeners = "retryLogger"
    )
    public <T> T supplyWithRetry(ThrowingSupplier<T> supplier, String actionName, String caseId) throws Exception {
        RetryContext context = RetrySynchronizationManager.getContext();
        if (context != null) {
            context.setAttribute("actionName", actionName);
            context.setAttribute("caseId", caseId);
        }

        return supplier.getWithException();
    }

    /**
     * Executes a runnable action with retry support.
     *
     * <p>The action will be retried automatically if a supported
     * {@link FeignException} is thrown. Retry context attributes are populated
     * with the provided action name and case ID for logging purposes.</p>
     *
     * @param action the operation to execute
     * @param actionName a descriptive name of the action being performed
     * @param caseId the case identifier associated with the operation
     * @throws Exception if the operation ultimately fails after all retry attempts
     */
    @Retryable(
        value = {
            FeignException.InternalServerError.class,
            FeignException.ServiceUnavailable.class,
            FeignException.GatewayTimeout.class
        },
        backoff = @Backoff(delay = 2000),
        listeners = "retryLogger"
    )
    public void runWithRetry(ThrowingRunnable action, String actionName, String caseId) throws Exception {

        RetryContext context = RetrySynchronizationManager.getContext();
        if (context != null) {
            context.setAttribute("actionName", actionName);
            context.setAttribute("caseId", caseId);
        }
        action.run();
    }
}
