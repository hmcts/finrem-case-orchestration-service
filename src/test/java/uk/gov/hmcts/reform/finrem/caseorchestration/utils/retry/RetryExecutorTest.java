package uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry;

import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.retry.RetryListener;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;

@SpringJUnitConfig(RetryExecutorTest.Config.class)
class RetryExecutorTest {

    private static final int DEFAULT_MAX_ATTEMPTS = 3;

    @Autowired
    private RetryExecutor retryExecutor;

    @Configuration
    @EnableRetry
    @Import(RetryExecutor.class)
    static class Config {

        @Bean(name = "retryLogger")
        RetryListener retryLogger() {
            RetryListener listener = mock(RetryListener.class);

            when(listener.open(any(), any())).thenReturn(true);

            return listener;
        }
    }

    @Test
    void shouldRetryOnServiceUnavailable() throws Exception {
        AtomicInteger counter = new AtomicInteger();

        String result = retryExecutor.supplyWithRetry(() -> {
            if (counter.getAndIncrement() < DEFAULT_MAX_ATTEMPTS - 1) {
                throw feignException(503, "Service Unavailable");
            }
            return "success";
        }, "send", CASE_ID);

        assertThat(result).isEqualTo("success");
        assertThat(counter.get()).isEqualTo(DEFAULT_MAX_ATTEMPTS);
    }

    @Test
    void shouldRetryOnGatewayTimeout() throws Exception {
        AtomicInteger counter = new AtomicInteger();

        String result = retryExecutor.supplyWithRetry(() -> {
            if (counter.getAndIncrement() < DEFAULT_MAX_ATTEMPTS - 1) {
                throw feignException(504, "Gateway Timeout");
            }
            return "success";
        }, "send", CASE_ID);

        assertThat(result).isEqualTo("success");
        assertThat(counter.get()).isEqualTo(DEFAULT_MAX_ATTEMPTS);
    }

    @Test
    void shouldNotRetryOnNonRetryableException() {
        AtomicInteger counter = new AtomicInteger();

        assertThatThrownBy(() ->
            retryExecutor.supplyWithRetry(() -> {
                counter.incrementAndGet();
                throw new RuntimeException("Not retryable");
            }, "send", CASE_ID)
        ).isInstanceOf(RuntimeException.class);

        assertThat(counter.get()).isEqualTo(1);
    }

    @Test
    void shouldRetryRunWithRetry() throws Exception {
        AtomicInteger counter = new AtomicInteger();

        retryExecutor.runWithRetry(() -> {
            if (counter.getAndIncrement() < DEFAULT_MAX_ATTEMPTS - 1) {
                throw feignException(500, "Internal Server Error");
            }
        }, "send", CASE_ID);

        assertThat(counter.get()).isEqualTo(DEFAULT_MAX_ATTEMPTS);
    }

    @Test
    void shouldRetryOnceWhenGatewayTimeoutThenSucceed() throws Exception {
        AtomicInteger counter = new AtomicInteger();

        String result = retryExecutor.supplyWithRetry(() -> {
            if (counter.getAndIncrement() == 0) {
                throw feignException(504, "Gateway Timeout");
            }
            return "success";
        }, "send", CASE_ID);

        assertThat(result).isEqualTo("success");
        assertThat(counter.get()).isEqualTo(2);
    }

    private FeignException feignException(int status, String reason) {
        return FeignException.errorStatus(
            "test",
            feign.Response.builder()
                .status(status)
                .reason(reason)
                .request(feign.Request.create(
                    feign.Request.HttpMethod.GET,
                    "/test",
                    Map.of(),
                    null,
                    null,
                    null))
                .build()
        );
    }
}
