package uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.framework.AopContext;
import org.springframework.util.function.ThrowingSupplier;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogs;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.getThrowingSupplierCaptor;

@ExtendWith(MockitoExtension.class)
class RetryExecutorTest {
    @TestLogs
    private final TestLogger logs = new TestLogger(RetryExecutor.class);

    @Spy
    private RetryExecutor spyExecutor;
    private MockedStatic<AopContext> aopContextMock;

    @BeforeEach
    void setUp() {
        aopContextMock = mockStatic(AopContext.class);
        aopContextMock.when(AopContext::currentProxy)
            .thenReturn(spyExecutor);
    }

    @AfterEach
    void tearDown() {
        aopContextMock.close();
    }

    @Nested
    class RunWithRetryWithHandlerTest {

        @Mock
        private RetryErrorHandler handler1;
        @Mock
        private RetryErrorHandler handler2;

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
        void shouldThrowIllegalStateException_whenNoHandlerProvided() {
            ThrowingSupplier<?> action = mock(ThrowingSupplier.class);

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
}
