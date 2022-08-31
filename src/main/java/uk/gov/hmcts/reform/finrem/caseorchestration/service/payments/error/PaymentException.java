package uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.error;

public class PaymentException extends RuntimeException {

    public PaymentException(Exception exception) {
        super(exception);
    }
}
