package uk.gov.hmcts.reform.finrem.caseorchestration.error;

public class NoSuchDocumentFoundException extends RuntimeException {
    public NoSuchDocumentFoundException(String message) {
        super(message);
    }
}
