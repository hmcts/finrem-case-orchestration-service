package uk.gov.hmcts.reform.finrem.caseorchestration.error;

public class AssignCaseAccessException extends RuntimeException {
    public AssignCaseAccessException(String message) {
        super(message);
    }
}
