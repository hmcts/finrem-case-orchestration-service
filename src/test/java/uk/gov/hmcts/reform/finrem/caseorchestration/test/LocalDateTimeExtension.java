package uk.gov.hmcts.reform.finrem.caseorchestration.test;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * JUnit 5 extension to mock LocalDateTime.now() to return a fixed time.
 *
 * <p>
 * Usage: Add the following annotation to your test class:
 * <pre>
 *     {@code @RegisterExtension LocalDateTimeExtension timeExtension = new LocalDateTimeExtension(LocalDateTime.of(1970, 1, 1, 10, 30, 0);}
 * </pre>
 * This will set the return value of any calls to LocalDateTime.now() to 10:30am 1st January 1970 for all tests in the class
 * </p>
 */
public class LocalDateTimeExtension implements InvocationInterceptor {

    private final LocalDateTime fixed;

    public LocalDateTimeExtension(LocalDateTime fixed) {
        this.fixed = fixed;
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext,
                                    ExtensionContext extensionContext) throws Throwable {
        try (MockedStatic<LocalDateTime> mockedStatic = Mockito.mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS)) {
            mockedStatic.when(LocalDateTime::now).thenReturn(fixed);

            invocation.proceed();
        }
    }

    @Override
    public void interceptTestTemplateMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext,
                                            ExtensionContext extensionContext) throws Throwable {
        try (MockedStatic<LocalDateTime> mockedStatic = Mockito.mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS)) {
            mockedStatic.when(LocalDateTime::now).thenReturn(fixed);

            invocation.proceed();
        }
    }
}
