package uk.gov.hmcts.reform.finrem.caseorchestration.error;

import static java.lang.String.format;

public class UnsupportedFormTypeException extends RuntimeException {

    public UnsupportedFormTypeException(String formType) {
        super(format("\"%s\" form type is not supported", formType));
    }

}