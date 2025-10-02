package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.exceptions.InvalidEmailAddressException;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.exceptions.SendEmailException;
import uk.gov.service.notify.NotificationClientException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NotificationClientExceptionResolverTest {

    @Test
    void givenInvalidEmailError_whenResolve_thenThrowInvalidEmailAddressException() {
        NotificationClientException exception = mock(NotificationClientException.class);
        when(exception.getHttpResult()).thenReturn(400);

        String message = """
            {
                "errors" : [ {
                "error" : "ValidationError",
                    "message" : "email_address Not a valid email address"
            } ],
                "status_code" : 400
            }
            """;
        when(exception.getMessage()).thenReturn(message);

        NotificationClientExceptionResolver resolver = new NotificationClientExceptionResolver();
        assertThatThrownBy(() -> resolver.resolve(exception))
            .isInstanceOf(InvalidEmailAddressException.class);
    }

    @Test
    void given400Error_whenResolve_thenThrowSendEmailException() {
        NotificationClientException exception = mock(NotificationClientException.class);
        when(exception.getHttpResult()).thenReturn(400);
        when(exception.getMessage()).thenReturn("Cannot send to this recipient using a team-only API key");

        NotificationClientExceptionResolver resolver = new NotificationClientExceptionResolver();
        assertThatThrownBy(() -> resolver.resolve(exception))
            .isInstanceOf(SendEmailException.class);
    }

    @Test
    void given500Error_whenResolve_thenThrowSendEmailException() {
        NotificationClientException exception = mock(NotificationClientException.class);
        when(exception.getHttpResult()).thenReturn(500);
        when(exception.getMessage()).thenReturn("Internal server error");

        NotificationClientExceptionResolver resolver = new NotificationClientExceptionResolver();
        assertThatThrownBy(() -> resolver.resolve(exception))
            .isInstanceOf(SendEmailException.class);
    }
}
