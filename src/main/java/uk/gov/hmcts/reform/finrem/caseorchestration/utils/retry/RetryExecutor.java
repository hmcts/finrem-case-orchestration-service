package uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryContext;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Component;
import org.springframework.util.function.ThrowingSupplier;

@Slf4j
@Component
public class RetryExecutor {

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
