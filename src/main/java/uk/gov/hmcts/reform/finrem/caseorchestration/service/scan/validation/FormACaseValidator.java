package uk.gov.hmcts.reform.finrem.caseorchestration.service.scan.validation;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

@Component
public class FormACaseValidator extends BulkScanFormValidator {

    private static final List<String> MANDATORY_FIELDS = asList(
        "D8PetitionerFirstName",
        "D8PetitionerLastName"
    );

    public List<String> getMandatoryFields() {
        return MANDATORY_FIELDS;
    }

    @Override
    protected Map<String, List<String>> getAllowedValuesPerField() {
        return Collections.emptyMap();
    }

    @Override
    protected List<String> runPostProcessingValidation(Map<String, String> fieldsMap) {
        return Collections.emptyList();
    }
}