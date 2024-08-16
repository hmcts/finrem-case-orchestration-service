package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler.removesolicitorfromcase;

public class RemoveSolicitorFromCaseException extends RuntimeException {
    public RemoveSolicitorFromCaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public RemoveSolicitorFromCaseException(String message) {
        super(message);
    }
}
