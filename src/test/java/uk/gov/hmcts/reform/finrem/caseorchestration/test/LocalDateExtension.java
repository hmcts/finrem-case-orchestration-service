package uk.gov.hmcts.reform.finrem.caseorchestration.test;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.time.LocalDate;

/**
 * JUnit 5 extension to mock LocalDate.now() to return a fixed date.
 *
 * <p>
 * Usage: Add the following annotation to your test class:
 * <pre>
 *      {@code @RegisterExtension LocalDateExtension dateExtension = new LocalDateExtension(LocalDate.of(1970, 1, 1);}
 * </pre>
 * This will set the return value of any calls to LocalDate.now() to 1st January 1970 for all tests in the class
 * </p>
 */
public class LocalDateExtension implements InvocationInterceptor {

    private final LocalDate fixed;

    public LocalDateExtension(LocalDate fixed) {
        this.fixed = fixed;
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext,
                                    ExtensionContext extensionContext) throws Throwable {
        try (MockedStatic<LocalDate> mockedStatic = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            mockedStatic.when(LocalDate::now).thenReturn(fixed);

            invocation.proceed();
        }
    }

    @Override
    public void interceptTestTemplateMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext,
                                            ExtensionContext extensionContext) throws Throwable {
        try (MockedStatic<LocalDate> mockedStatic = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            mockedStatic.when(LocalDate::now).thenReturn(fixed);

            invocation.proceed();
        }
    }
}
