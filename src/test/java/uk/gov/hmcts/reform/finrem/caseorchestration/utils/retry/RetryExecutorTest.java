package uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.retry.RetryListener;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.feignException;

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

        @ParameterizedTest(name = "should not retry on non-retryable exception")
        @ValueSource(ints = {0}) // dummy parameter to allow parameterized test
        void shouldNotRetryOnNonRetryableException(int ignored) {
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

        @ParameterizedTest(name = "should not retry on non-retryable exception")
        @ValueSource(ints = {0}) // dummy parameter to allow parameterized test
        void shouldNotRetryOnNonRetryableException(int ignored) {
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
