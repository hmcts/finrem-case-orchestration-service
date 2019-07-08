package uk.gov.hmcts.reform.finrem.caseorchestration.error;


public class NoSuchFieldExistsException extends RuntimeException {

    private static final long serialVersionUID = 0;

    public NoSuchFieldExistsException(String message) {
        super(message);
    }
}

