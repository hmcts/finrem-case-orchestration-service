package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.exceptions;

import uk.gov.service.notify.NotificationClientException;

public class SendEmailException extends RuntimeException {
    public SendEmailException(NotificationClientException exception) {
        super(exception);
    }
}
