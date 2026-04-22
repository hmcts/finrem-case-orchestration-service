package uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.retry.RetryContext;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Component;
import org.springframework.util.function.ThrowingSupplier;

import java.util.Arrays;
import java.util.Optional;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;

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
@EnableAspectJAutoProxy(exposeProxy = true)
public class RetryExecutor {


    private static final RetryErrorHandler SUPPRESS_HANDLER = (ex, actionName, caseId) ->
        log.error(
            "{} - unexpected exception when executing {} in suppress mode. This indicates a bug or retry exhausted in RetryExecutor.",
            caseId, actionName, ex);

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
//        value = {
//            FeignException.InternalServerError.class,
//            FeignException.ServiceUnavailable.class,
//            FeignException.GatewayTimeout.class
//        },
        interceptor = "retryLoggerInterceptor"
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
//        value = {
//            FeignException.InternalServerError.class,
//            FeignException.ServiceUnavailable.class,
//            FeignException.GatewayTimeout.class
//        },
        interceptor = "retryLoggerInterceptor"
    )
    public void runWithRetry(ThrowingRunnable action, String actionName, String caseId) throws Exception {

        RetryContext context = RetrySynchronizationManager.getContext();
        if (context != null) {
            context.setAttribute("actionName", actionName);
            context.setAttribute("caseId", caseId);
        }
        action.run();
    }

    /**
     * Executes the given runnable action with retry support and invokes the provided error handlers
     * if all retry attempts fail.
     *
     * <p>This method delegates to {@link #runWithRetry(ThrowingRunnable, String, String)}.</p>
     *
     * <p>If all retry attempts are exhausted and an exception is thrown, each provided
     * {@link RetryErrorHandler} is invoked in order with the exception, action name, and case ID.</p>
     *
     * <p>At least one {@link RetryErrorHandler} must be provided. If none are supplied,
     * this method throws an {@link IllegalStateException}.</p>
     *
     * @param action the operation to execute
     * @param actionName a descriptive name of the action being performed (used for logging and error handling)
     * @param caseId the case identifier associated with the operation
     * @param errorHandlers one or more handlers to process the exception after retries are exhausted
     * @throws IllegalStateException if no {@code errorHandlers} are provided
     */
    public void runWithRetryWithHandler(
        ThrowingRunnable action,
        String actionName,
        String caseId,
        RetryErrorHandler... errorHandlers
    ) {
        if (errorHandlers.length == 0) {
            throw new IllegalStateException("no handler provided");
        }
        try {
            ((RetryExecutor) AopContext.currentProxy()).runWithRetry(action, actionName, caseId);
        } catch (Exception ex) {
            log.error("{} - Exception caught when {}", caseId, actionName, ex);
            emptyIfNull(Arrays.asList(errorHandlers)).forEach(errorHandler ->
                errorHandler.handle(ex, actionName, caseId));
        }
    }

    /**
     * Executes a runnable action with retry support and suppresses any exception
     * after all retry attempts are exhausted.
     *
     * <p>This method delegates to {@link #runWithRetry(ThrowingRunnable, String, String)}.
     * If an exception occurs, it is handled by an internal suppressing handler
     * that logs the error without rethrowing it.</p>
     *
     * @param action the operation to execute
     * @param actionName a descriptive name of the action being performed
     * @param caseId the case identifier associated with the operation
     */
    public void runWithRetrySuppressException(
        ThrowingRunnable action,
        String actionName,
        String caseId
    ) {
        try {
            ((RetryExecutor) AopContext.currentProxy()).runWithRetry(action, actionName, caseId);
        } catch (Exception ex) {
            SUPPRESS_HANDLER.handle(ex, actionName, caseId);
        }
    }

    /**
     * Executes the given supplier with retry support and invokes the provided error handlers
     * if all retry attempts fail.
     *
     * <p>If the operation completes successfully, the result is wrapped in an {@link Optional}
     * (which may be empty if the supplier returns {@code null}).</p>
     *
     * <p>If all retry attempts are exhausted and an exception is thrown, each provided
     * {@link RetryErrorHandler} is invoked with the exception, action name, and case ID.
     * In this case, {@link Optional#empty()} is returned.</p>
     *
     * <p>At least one {@link RetryErrorHandler} must be provided. If none are supplied,
     * this method throws an {@link IllegalStateException}.</p>
     *
     * @param action the operation to execute
     * @param actionName a descriptive name of the action being performed (used for logging and error handling)
     * @param caseId the case identifier associated with the operation
     * @param errorHandlers one or more handlers to process the exception after retries are exhausted
     * @param <T> the result type returned by the supplier
     * @return an {@link Optional} containing the result if successful, otherwise {@link Optional#empty()}
     * @throws IllegalStateException if no {@code errorHandlers} are provided
     */
    public <T> Optional<T> supplyWithRetryWithHandler(
        ThrowingSupplier<T> action,
        String actionName,
        String caseId,
        RetryErrorHandler... errorHandlers
    ) {
        if (errorHandlers.length == 0) {
            throw new IllegalStateException("no handler provided");
        }
        try {
            return Optional.ofNullable(((RetryExecutor) AopContext.currentProxy()).supplyWithRetry(action, actionName, caseId));
        } catch (Exception ex) {
            log.error("{} - Exception caught when {}", caseId, actionName, ex);
            for (RetryErrorHandler handler : errorHandlers) {
                handler.handle(ex, actionName, caseId);
            }
            return Optional.empty();
        }
    }

    /**
     * Executes a supplier operation with retry support and suppresses any exception
     * after all retry attempts are exhausted.
     *
     * <p>If the operation succeeds, the result is wrapped in an {@link Optional}.
     * If it fails, the exception is handled by an internal suppressing handler
     * and {@link Optional#empty()} is returned.</p>
     *
     * @param action the operation to execute
     * @param actionName a descriptive name of the action being performed
     * @param caseId the case identifier associated with the operation
     * @param <T> the result type returned by the supplier
     * @return an {@link Optional} containing the result if successful, otherwise empty
     */
    public <T> Optional<T> supplyWithRetrySuppressException(
        ThrowingSupplier<T> action,
        String actionName,
        String caseId
    ) {
        try {
            return Optional.ofNullable(
                ((RetryExecutor) AopContext.currentProxy()).supplyWithRetry(action, actionName, caseId)
            );
        } catch (Exception ex) {
            SUPPRESS_HANDLER.handle(ex, actionName, caseId);
            return Optional.empty();
        }
    }
}
