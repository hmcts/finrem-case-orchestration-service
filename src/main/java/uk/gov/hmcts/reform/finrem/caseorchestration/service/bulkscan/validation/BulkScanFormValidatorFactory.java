package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.UnsupportedFormTypeException;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.BulkScanForms.FORM_A;

@Component
public class BulkScanFormValidatorFactory {

    @Autowired
    private FormAValidator formAValidator;

    private static Map<String, BulkScanFormValidator> validators;

    @PostConstruct
    public void initBean() {
        validators = new HashMap<>();
        validators.put(FORM_A, formAValidator);
    }

    public BulkScanFormValidator getValidator(final String formType) throws UnsupportedFormTypeException {
        if (!validators.containsKey(formType)) {
            throw new UnsupportedFormTypeException(formType);
        }

        return validators.get(formType);
    }
}
