package uk.gov.hmcts.reform.finrem.caseorchestration.error;

public class NotificationClientRuntimeException extends RuntimeException {
    public NotificationClientRuntimeException(String message) {
        super(message);
    }
}
