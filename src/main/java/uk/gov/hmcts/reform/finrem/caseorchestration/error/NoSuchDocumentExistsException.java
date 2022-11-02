package uk.gov.hmcts.reform.finrem.caseorchestration.error;

public class NoSuchDocumentExistsException extends RuntimeException {
    public NoSuchDocumentExistsException(String message) {
        super(message);
    }
}
