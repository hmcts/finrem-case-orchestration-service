package uk.gov.hmcts.reform.finrem.caseorchestration.error;

public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
