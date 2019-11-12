package uk.gov.hmcts.reform.finrem.caseorchestration.error;

public class UnauthenticatedException extends RuntimeException {

    public UnauthenticatedException(String message) {
        super(message);
    }
}