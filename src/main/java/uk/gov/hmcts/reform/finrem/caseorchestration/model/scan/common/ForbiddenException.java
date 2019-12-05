package uk.gov.hmcts.reform.finrem.caseorchestration.model.scan.common;

public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
