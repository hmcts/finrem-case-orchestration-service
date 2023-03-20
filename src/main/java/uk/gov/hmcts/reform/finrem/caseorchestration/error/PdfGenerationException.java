package uk.gov.hmcts.reform.finrem.caseorchestration.error;

public class PdfGenerationException extends RuntimeException {

    public PdfGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
