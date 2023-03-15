package uk.gov.hmcts.reform.finrem.caseorchestration.error;

public class DocumentConversionException extends RuntimeException {
    public DocumentConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
