package uk.gov.hmcts.reform.finrem.caseorchestration.test;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.time.LocalDate;

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
