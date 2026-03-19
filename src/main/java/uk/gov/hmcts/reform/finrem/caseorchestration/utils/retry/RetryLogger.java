package uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry;

import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.listener.RetryListenerSupport;
import org.springframework.stereotype.Component;

@Slf4j
@Component("retryLogger")
public class RetryLogger extends RetryListenerSupport {

    @Override
    public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {

        String actionName = (String) context.getAttribute("actionName");
        String caseId = (String) context.getAttribute("caseId");

        log.warn("Attempt {} for {} (case {}) failed: {}", context.getRetryCount(), actionName, caseId, throwable.getMessage()
        );
    }

    @Override
    public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        if (throwable != null) {
            String actionName = (String) context.getAttribute("actionName");

            log.error("All {} retry attempts failed for {}", context.getRetryCount(), actionName, throwable);
        }
    }
}
