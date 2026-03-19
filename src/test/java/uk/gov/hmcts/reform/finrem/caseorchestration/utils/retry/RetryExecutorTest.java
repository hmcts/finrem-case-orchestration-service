package uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.retry.RetryListener;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.util.function.ThrowingSupplier;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogs;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.feignException;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.getThrowingSupplierCaptor;

@SpringJUnitConfig(RetryExecutorTest.Config.class)
@ExtendWith(MockitoExtension.class) // For @Mock/@Spy
class RetryExecutorTest {
    @TestLogs
    private final TestLogger logs = new TestLogger(RetryExecutor.class);

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
    class RunWithRetryWithHandlerTest {

        @Mock
        private RetryErrorHandler handler1;
        @Mock
        private RetryErrorHandler handler2;
        @Spy
        RetryExecutor spyExecutor;

        @Test
        void shouldExecuteSuccessfully_whenNoExceptionThrown() {
            ThrowingRunnable action = mock(ThrowingRunnable.class);

            spyExecutor.runWithRetryWithHandler(action, "testAction", CASE_ID, handler1, handler2);

            assertAll(
                () -> verify(spyExecutor).runWithRetry(action, "testAction", CASE_ID),
                () -> verifyNoInteractions(handler1, handler2)
            );
        }

        @Test
        void shouldInvokeErrorHandler_whenExceptionThrown() throws Exception {
            ThrowingRunnable action = mock(ThrowingRunnable.class);
            Exception exception = new RuntimeException("boom");

            doThrow(exception)
                .when(spyExecutor)
                .runWithRetry(action, "testAction", CASE_ID);

            spyExecutor.runWithRetryWithHandler(action, "testAction", CASE_ID, handler1);

            assertAll(
                () -> verify(spyExecutor).runWithRetry(action, "testAction", CASE_ID),
                () -> verify(handler1).handle(exception, "testAction", CASE_ID),
                () -> verifyNoMoreInteractions(handler1)
            );
        }

        @Test
        void shouldInvokeMultipleHandlers_whenExceptionThrown() throws Exception {
            ThrowingRunnable action = mock(ThrowingRunnable.class);
            Exception exception = new RuntimeException("boom");

            doThrow(exception)
                .when(spyExecutor)
                .runWithRetry(action, "testAction", CASE_ID);

            spyExecutor.runWithRetryWithHandler(action, "testAction", CASE_ID, handler1, handler2);

            assertAll(
                () -> verify(spyExecutor).runWithRetry(action, "testAction", CASE_ID),
                () -> verify(handler1).handle(exception, "testAction", CASE_ID),
                () -> verify(handler2).handle(exception, "testAction", CASE_ID),
                () -> verifyNoMoreInteractions(handler1)
            );
        }

        @Test
        void shouldThrowIllegalStateException_whenNoHandlerProvided() throws Exception {
            ThrowingRunnable action = mock(ThrowingRunnable.class);
            Exception exception = new RuntimeException("boom");

            doThrow(exception)
                .when(spyExecutor)
                .runWithRetry(action, "testAction", CASE_ID);

            assertThatThrownBy(() ->
                spyExecutor.runWithRetryWithHandler(action, "testAction", CASE_ID)
            )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("no handler provided");
        }

        @Test
        void shouldSuppressException_whenExceptionThrown() throws Exception {
            ThrowingRunnable action = mock(ThrowingRunnable.class);
            Exception exception = new RuntimeException("boom");

            doThrow(exception)
                .when(spyExecutor)
                .runWithRetry(action, "testAction", CASE_ID);

            spyExecutor.runWithRetrySuppressException(action, "testAction", CASE_ID);

            assertAll(
                () -> verify(spyExecutor).runWithRetry(action, "testAction", CASE_ID),
                () -> assertThat(logs.getErrors()).containsExactly(("%s - unexpected exception when executing testAction in suppress mode. "
                        + "This indicates a bug or retry exhausted in RetryExecutor.").formatted(CASE_ID))
            );
        }
    }

    @Nested
    class SupplyWithRetryWithHandlerTest {

        @Mock
        private RetryErrorHandler handler1;
        @Mock
        private RetryErrorHandler handler2;
        @Spy
        RetryExecutor spyExecutor;

        @Test
        void shouldExecuteSuccessfully_whenNoExceptionThrown() {
            Object ret = mock(Object.class);
            ThrowingSupplier<?> action = () -> ret;

            Optional<?> actual = spyExecutor.supplyWithRetryWithHandler(action, "testAction", CASE_ID,
                handler1, handler2);

            ArgumentCaptor<ThrowingSupplier<Object>> captor = getThrowingSupplierCaptor();
            assertAll(
                () -> verify(spyExecutor).supplyWithRetry(captor.capture(), eq("testAction"), eq(CASE_ID)),
                () -> assertThat(captor.getValue().get()).isEqualTo(actual.orElseThrow()),
                () -> verifyNoInteractions(handler1, handler2)
            );
        }

        @Test
        void shouldInvokeErrorHandler_whenExceptionThrown() throws Exception {
            ThrowingSupplier<?> action = mock(ThrowingSupplier.class);
            Exception exception = new RuntimeException("boom");

            doThrow(exception)
                .when(spyExecutor)
                .supplyWithRetry(action, "testAction", CASE_ID);

            Optional<?> actual = spyExecutor.supplyWithRetryWithHandler(action, "testAction", CASE_ID, handler1);

            assertAll(
                () -> verify(spyExecutor).supplyWithRetry(action, "testAction", CASE_ID),
                () -> assertThat(actual).isEmpty(),
                () -> verify(handler1).handle(exception, "testAction", CASE_ID),
                () -> verifyNoMoreInteractions(handler1)
            );
        }

        @Test
        void shouldInvokeMultipleHandlers_whenExceptionThrown() throws Exception {
            ThrowingSupplier<?> action = mock(ThrowingSupplier.class);
            Exception exception = new RuntimeException("boom");

            doThrow(exception)
                .when(spyExecutor)
                .supplyWithRetry(action, "testAction", CASE_ID);

            Optional<?> actual = spyExecutor.supplyWithRetryWithHandler(action, "testAction", CASE_ID, handler1, handler2);

            assertAll(
                () -> verify(spyExecutor).supplyWithRetry(action, "testAction", CASE_ID),
                () -> assertThat(actual).isEmpty(),
                () -> verify(handler1).handle(exception, "testAction", CASE_ID),
                () -> verify(handler2).handle(exception, "testAction", CASE_ID),
                () -> verifyNoMoreInteractions(handler1, handler2)
            );
        }

        @Test
        void shouldThrowIllegalStateException_whenNoHandlerProvided() throws Exception {
            ThrowingSupplier<?> action = mock(ThrowingSupplier.class);
            Exception exception = new RuntimeException("boom");

            doThrow(exception)
                .when(spyExecutor)
                .supplyWithRetry(action, "testAction", CASE_ID);

            assertThatThrownBy(() ->
                spyExecutor.supplyWithRetryWithHandler(action, "testAction", CASE_ID)
            )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("no handler provided");
        }

        @Test
        void shouldSuppressException_whenExceptionThrown() throws Exception {
            ThrowingSupplier<?> action = mock(ThrowingSupplier.class);
            Exception exception = new RuntimeException("boom");

            doThrow(exception)
                .when(spyExecutor)
                .supplyWithRetry(action, "testAction", CASE_ID);

            Optional<?> actual = spyExecutor.supplyWithRetrySuppressException(action, "testAction", CASE_ID);
            assertAll(
                () -> verify(spyExecutor).supplyWithRetry(action, "testAction", CASE_ID),
                () -> assertThat(actual).isEmpty(),
                () -> assertThat(logs.getErrors())
                    .containsExactly(("%s - unexpected exception when executing testAction in suppress mode. "
                        + "This indicates a bug or retry exhausted in RetryExecutor.").formatted(CASE_ID))
            );

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
