package uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry;

import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.listener.RetryListenerSupport;
import org.springframework.stereotype.Component;

/**
 * Retry listener that logs retry attempts and final failure details.
 *
 * <p>This listener captures contextual information from the {@link RetryContext},
 * such as {@code actionName} and {@code caseId}, and logs:
 * <ul>
 *     <li>Each retry attempt failure (WARN level)</li>
 *     <li>Final failure after all retries are exhausted (ERROR level)</li>
 * </ul>
 *
 * <p><b>Example WARN log (on retry attempt failure):</b>
 * <pre>
 * Attempt 2 for send (case: 12345) failed: Internal Server Error
 * </pre>
 *
 * <p><b>Example ERROR log (after all retries fail):</b>
 * <pre>
 * 12345 - All 3 retry attempts failed for send
 * java.lang.RuntimeException: final failure
 * </pre>
 */
@Slf4j
@Component("retryLogger")
public class RetryLogger extends RetryListenerSupport {

    @Override
    public <T, E extends Throwable> void onError(RetryContext context,
                                                 RetryCallback<T, E> callback,
                                                 Throwable throwable) {

        ContextInfo info = extractContextInfo(context);

        log.warn("{} - Attempt #{} for action ({}) failed:",
            info.caseId(),
            context.getRetryCount(),
            info.actionName(),
            throwable
        );
    }

    @Override
    public <T, E extends Throwable> void close(RetryContext context,
                                               RetryCallback<T, E> callback,
                                               Throwable throwable) {
        if (throwable != null) {
            ContextInfo info = extractContextInfo(context);

            log.error("{} - All {} retry attempts failed for action ({})",
                info.caseId(),
                context.getRetryCount(),
                info.actionName(),
                throwable
            );
        }
    }

    private ContextInfo extractContextInfo(RetryContext context) {
        String actionName = (String) context.getAttribute("actionName");
        String caseId = (String) context.getAttribute("caseId");
        return new ContextInfo(actionName, caseId);
    }

    private record ContextInfo(String actionName, String caseId) {}
}
