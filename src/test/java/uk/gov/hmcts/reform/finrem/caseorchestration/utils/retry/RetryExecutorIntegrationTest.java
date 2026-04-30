package uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.feignException;

@SpringJUnitConfig(classes = {
    RetryExecutor.class,
    RetryConfig.class,
    RetryExecutorIntegrationTest.EnableRetryConfig.class
})
class RetryExecutorIntegrationTest {

    private static final int DEFAULT_MAX_ATTEMPTS = 3;

    @Autowired
    private RetryExecutor retryExecutor;

    @Configuration
    @EnableRetry
    static class EnableRetryConfig {
    }

    @Nested
    class RunWithRetryTest {

        @ParameterizedTest(name = "should retry on status {0} and succeed")
        @ValueSource(ints = {500, 503, 504})
        void shouldRetryOnRetryableStatus(int status) throws Exception {
            AtomicInteger counter = new AtomicInteger();

            retryExecutor.runWithRetry(() -> {
                if (counter.getAndIncrement() < DEFAULT_MAX_ATTEMPTS - 1) {
                    throw feignException(status, "Retryable Error");
                }
            }, "send", CASE_ID);

            assertThat(counter.get()).isEqualTo(DEFAULT_MAX_ATTEMPTS);
        }

        @ParameterizedTest(name = "should retry once on first status {0} then succeed")
        @ValueSource(ints = {504}) // can expand if needed
        void shouldRetryOnceThenSucceed(int status) throws Exception {
            AtomicInteger counter = new AtomicInteger();

            retryExecutor.runWithRetry(() -> {
                if (counter.getAndIncrement() == 0) {
                    throw feignException(status, "Retryable Error");
                }
            }, "send", CASE_ID);

            assertThat(counter.get()).isEqualTo(2);
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
    }

    @Nested
    class SupplyWithRetryTest {

        @ParameterizedTest(name = "should retry on status {0} and succeed")
        @ValueSource(ints = {500, 503, 504})
        void shouldRetryOnRetryableStatus(int status) throws Exception {
            AtomicInteger counter = new AtomicInteger();

            String result = retryExecutor.supplyWithRetry(() -> {
                if (counter.getAndIncrement() < DEFAULT_MAX_ATTEMPTS - 1) {
                    throw feignException(status, "Retryable Error");
                }
                return "success";
            }, "send", CASE_ID);

            assertThat(result).isEqualTo("success");
            assertThat(counter.get()).isEqualTo(DEFAULT_MAX_ATTEMPTS);
        }

        @ParameterizedTest(name = "should retry once on first status {0} then succeed")
        @ValueSource(ints = {504}) // can expand if needed
        void shouldRetryOnceThenSucceed(int status) throws Exception {
            AtomicInteger counter = new AtomicInteger();

            String result = retryExecutor.supplyWithRetry(() -> {
                if (counter.getAndIncrement() == 0) {
                    throw feignException(status, "Retryable Error");
                }
                return "success";
            }, "send", CASE_ID);

            assertThat(result).isEqualTo("success");
            assertThat(counter.get()).isEqualTo(2);
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
    }
}
