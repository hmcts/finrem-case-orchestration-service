package uk.gov.hmcts.reform.finrem.caseorchestration.error;

public class UnsuccessfulDocumentGenerateException extends RuntimeException {
    public UnsuccessfulDocumentGenerateException(String msg, Exception e) {
        super(msg, e);
    }
}
