package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.exceptions;

import uk.gov.service.notify.NotificationClientException;

public class InvalidEmailAddressException extends RuntimeException {
    public InvalidEmailAddressException(NotificationClientException exception) {
        super(exception);
    }
}
