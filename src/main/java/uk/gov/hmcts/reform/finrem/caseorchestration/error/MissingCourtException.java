package uk.gov.hmcts.reform.finrem.caseorchestration.error;

public class MissingCourtException extends RuntimeException {
    public MissingCourtException(String message) {
        super(message);
    }
}
