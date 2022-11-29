package uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.error;

public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(String message) {
        super(message);
    }

}
