package uk.gov.hmcts.reform.finrem.caseorchestration.model.scan.common;

public class UnauthenticatedException extends RuntimeException {

    public UnauthenticatedException(String message) {
        super(message);
    }
}
