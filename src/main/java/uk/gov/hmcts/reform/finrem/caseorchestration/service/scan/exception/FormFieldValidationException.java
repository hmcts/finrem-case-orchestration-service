package uk.gov.hmcts.reform.finrem.caseorchestration.service.scan.exception;

public class FormFieldValidationException extends RuntimeException {

    public FormFieldValidationException(String validationErrorMessage) {
        super(validationErrorMessage);
    }

}