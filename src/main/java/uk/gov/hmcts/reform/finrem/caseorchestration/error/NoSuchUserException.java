package uk.gov.hmcts.reform.finrem.caseorchestration.error;

public class NoSuchUserException extends RuntimeException {
    public NoSuchUserException(String message) {
        super(message);
    }
}
