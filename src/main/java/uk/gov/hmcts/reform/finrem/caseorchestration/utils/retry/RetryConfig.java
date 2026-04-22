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

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RetryConfig {

    @Bean(name = "retryLoggerInterceptor")
    public RetryOperationsInterceptor retryLoggerInterceptor() {
        RetryTemplate retryTemplate = new RetryTemplate();

        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(FeignException.InternalServerError.class, true);
        retryableExceptions.put(FeignException.ServiceUnavailable.class, true);
        retryableExceptions.put(FeignException.GatewayTimeout.class, true);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(3, retryableExceptions);
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
