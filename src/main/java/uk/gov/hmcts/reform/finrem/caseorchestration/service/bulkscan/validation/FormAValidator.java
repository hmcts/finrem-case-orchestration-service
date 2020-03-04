package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class FormAValidator extends BulkScanFormValidator {

    private static final List<String> MANDATORY_FIELDS = Collections.emptyList();

    private static final Map<String, List<String>> ALLOWED_VALUES_PER_FIELD = new HashMap<>();

    public List<String> getMandatoryFields() {
        return MANDATORY_FIELDS;
    }

    @Override
    protected Map<String, List<String>> getAllowedValuesPerField() {
        return ALLOWED_VALUES_PER_FIELD;
    }

    @Override
    protected List<String> runPostProcessingValidation(Map<String, String> fieldsMap) {
        return new ArrayList<>();
    }
}
