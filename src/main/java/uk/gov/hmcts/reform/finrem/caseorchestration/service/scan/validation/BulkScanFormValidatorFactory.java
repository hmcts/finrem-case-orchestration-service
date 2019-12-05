package uk.gov.hmcts.reform.finrem.caseorchestration.service.scan.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.scan.exception.UnsupportedFormTypeException;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.service.scan.BulkScanForms.FORM_A;

@Component
public class BulkScanFormValidatorFactory {

    @Autowired
    private FormACaseValidator formACaseValidator;

    private static Map<String, BulkScanFormValidator> validators;

    @PostConstruct
    public void initBean() {
        validators = new HashMap<>();
        validators.put(FORM_A, formACaseValidator);
    }

    public BulkScanFormValidator getValidator(final String formType) throws UnsupportedFormTypeException {
        if (!validators.containsKey(formType)) {
            throw new UnsupportedFormTypeException(formType);
        }

        return validators.get(formType);
    }
}