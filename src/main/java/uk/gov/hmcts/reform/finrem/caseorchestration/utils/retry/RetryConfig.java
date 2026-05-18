package uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry;

import feign.FeignException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryListener;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.interceptor.RetryInterceptorBuilder;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.Map;

/**
 * Configuration class for retry behaviour using Spring Retry.
 *
 * <p>Defines a custom {@link RetryOperationsInterceptor} bean that can be referenced
 * by {@code @Retryable(interceptor = "retryLoggerInterceptor")} to apply a
 * consistent retry strategy with logging.</p>
 *
 * <p>The configured retry behaviour includes:</p>
 * <ul>
 *     <li>Retry up to <b>3 attempts</b> (initial attempt + 2 retries)</li>
 *     <li>Retry only for specific {@link FeignException} subclasses:
 *         <ul>
 *             <li>{@link FeignException.InternalServerError}</li>
 *             <li>{@link FeignException.ServiceUnavailable}</li>
 *             <li>{@link FeignException.GatewayTimeout}</li>
 *         </ul>
 *     </li>
 *     <li>A fixed backoff delay of <b>2000 milliseconds</b> between retry attempts</li>
 *     <li>A custom {@link RetryLogger} listener to log retry attempts and errors</li>
 * </ul>
 *
 * <p><b>Important:</b> When using a custom interceptor via {@code @Retryable(interceptor = "...")},
 * any attributes defined on {@code @Retryable} (such as {@code value}, {@code maxAttempts},
 * or {@code backoff}) are ignored. All retry configuration must be defined within the
 * {@link RetryTemplate} used by the interceptor.</p>
 *
 * <p>This interceptor is <b>stateless</b> and suitable for concurrent use.</p>
 */
@Configuration
public class RetryConfig {

    @Bean(name = "retryLoggerInterceptor")
    public RetryOperationsInterceptor retryLoggerInterceptor() {
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = Map.of(
            FeignException.InternalServerError.class, true,
            FeignException.ServiceUnavailable.class, true,
            FeignException.GatewayTimeout.class, true);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(3, retryableExceptions);

        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(retryPolicy);

        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(2000);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        retryTemplate.setListeners(new RetryListener[]{ new RetryLogger() });

        return RetryInterceptorBuilder.stateless()
            .retryOperations(retryTemplate)
            .build();
    }
}
