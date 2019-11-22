package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.BulkScanForms;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

@Component
public class BulkScanFormValidatorFactory {

    private final Map<String, BulkScanFormValidator> validators;

    public BulkScanFormValidatorFactory(FormAValidator formAValidator) {
        validators = new HashMap<>();
        validators.put(BulkScanForms.FORM_A, formAValidator);
    }

    public BulkScanFormValidator getValidator(final String formType) {
        if (!validators.containsKey(formType)) {
            throw new UnsupportedOperationException(format("\"%s\" form type is not supported", formType));
        }

        return validators.get(formType);
    }
}
