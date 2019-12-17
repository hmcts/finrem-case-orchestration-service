package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

@Component
public class FormAValidator extends BulkScanFormValidator {

    private static final List<String> MANDATORY_FIELDS = asList(
        "claimingExemptionMIAM",
        "familyMediatorMIAM",
        "applicantAttendedMIAM"
    );

    private static final Map<String, List<String>> ALLOWED_VALUES_PER_FIELD = new HashMap<>();

    static {
        List<String> yesNoValues = Collections.unmodifiableList(asList("Yes", "No"));
        ALLOWED_VALUES_PER_FIELD.put("claimingExemptionMIAM", yesNoValues);
        ALLOWED_VALUES_PER_FIELD.put("familyMediatorMIAM", yesNoValues);
        ALLOWED_VALUES_PER_FIELD.put("applicantAttendedMIAM", yesNoValues);
    }

    public List<String> getMandatoryFields() {
        return MANDATORY_FIELDS;
    }

    @Override
    protected List<String> runPostProcessingValidation(Map<String, String> fieldsMap) {
        return Collections.emptyList();
    }

    @Override
    protected Map<String, List<String>> getAllowedValuesPerField() {
        return ALLOWED_VALUES_PER_FIELD;
    }

}
