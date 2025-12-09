package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.exceptions.InvalidEmailAddressException;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.exceptions.SendEmailException;
import uk.gov.service.notify.NotificationClientException;

@Service
public class NotificationClientExceptionResolver {

    /**
     * Resolves NotificationClientException to more specific exceptions.
     *
     * @param exception the NotificationClientException to resolve
     */
    public void resolve(NotificationClientException exception) {
        if (isInvalidEmailError(exception)) {
            throw new InvalidEmailAddressException(exception);
        } else {
            throw new SendEmailException(exception);
        }
    }

    private boolean isInvalidEmailError(NotificationClientException exception) {
        return exception.getHttpResult() == 400 && exception.getMessage().contains("email_address Not a valid email address");
    }
}
