package uk.gov.hmcts.reform.finrem.caseorchestration.service.ccd;

public class RetryFailureException extends RuntimeException {

    public RetryFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
