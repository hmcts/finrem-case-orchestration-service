package uk.gov.hmcts.reform.finrem.caseorchestration.error;

public class IllegalUserException extends RuntimeException {
    public IllegalUserException(String message) {
        super(message);
    }
}
