package uk.gov.hmcts.reform.finrem.caseorchestration.error;

public class DocumentDownloadException extends RuntimeException {
    public DocumentDownloadException(String message, Throwable cause) {
        super(message, cause);
    }
}
