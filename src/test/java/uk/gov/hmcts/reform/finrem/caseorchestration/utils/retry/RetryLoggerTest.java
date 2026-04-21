package uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;

class RetryLoggerTest {

    @TestLogs
    private final TestLogger logs = new TestLogger(RetryLogger.class);

    private RetryLogger retryLogger;

    @Mock
    private RetryContext context;

    @Mock
    private RetryCallback<Object, Throwable> callback;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        retryLogger = new RetryLogger();
    }

    @Test
    void shouldLogWarningOnError() {
        when(context.getAttribute("actionName")).thenReturn("granting access");
        when(context.getAttribute("caseId")).thenReturn(CASE_ID);
        when(context.getRetryCount()).thenReturn(2);

        Throwable throwable = new RuntimeException("boom");

        retryLogger.onError(context, callback, throwable);

        assertAll(
            () -> verify(context).getAttribute("actionName"),
            () -> verify(context).getAttribute("caseId"),
            () -> verify(context).getRetryCount(),
            () -> assertThat(logs.getWarns())
                .containsExactly("1234567890 - Attempt #2 for action (granting access) failed:"),

            // throwable message
            () -> assertThat(logs.getWarnThrowableMessages())
                .contains("boom")
        );
    }

    @Test
    void shouldLogErrorOnCloseWhenThrowablePresent() {
        when(context.getAttribute("actionName")).thenReturn("granting access");
        when(context.getAttribute("caseId")).thenReturn(CASE_ID);
        when(context.getRetryCount()).thenReturn(3);

        Throwable throwable = new RuntimeException("final failure");

        retryLogger.close(context, callback, throwable);

        assertAll(
            // interactions
            () -> verify(context).getAttribute("actionName"),
            () -> verify(context).getAttribute("caseId"),
            () -> verify(context).getRetryCount(),

            // log message
            () -> assertThat(logs.getErrors())
                .anyMatch(log -> log.contains("1234567890 - All 3 retry attempts failed for action (granting access)")),

            // throwable class
            () -> assertThat(logs.getErrorThrowableClassNames())
                .contains(RuntimeException.class.getName()),

            // throwable message
            () -> assertThat(logs.getErrorThrowableMessages())
                .contains("final failure")
        );
    }

    @Test
    void shouldDoNothingOnCloseWhenThrowableIsNull() {
        retryLogger.close(context, callback, null);

        verify(context, never()).getAttribute(anyString());
        verify(context, never()).getRetryCount();
    }
}
