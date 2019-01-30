package uk.gov.hmcts.reform.finrem.caseorchestration.error;

public class InvalidCaseDataException extends RuntimeException {

    private static final long serialVersionUID = 0;
    private int status;

    public InvalidCaseDataException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int status() {
        return this.status;
    }
}


