package uk.gov.hmcts.reform.finrem.caseorchestration.error;

public class DocumentDeleteException extends RuntimeException {
    public DocumentDeleteException(String message, Throwable cause) {
        super(message, cause);
    }
}
