package uk.gov.hmcts.reform.finrem.caseorchestration.error;

public class UnsuccessfulDocumentGenerateException extends RuntimeException {
    public UnsuccessfulDocumentGenerateException(String msg, Exception exception) {
        super(msg, exception);
    }
}
