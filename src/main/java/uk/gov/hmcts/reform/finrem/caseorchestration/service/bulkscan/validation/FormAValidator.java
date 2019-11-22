package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation;

import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.Arrays.asList;

@Component
public class FormAValidator extends BulkScanFormValidator {

    private static final List<String> MANDATORY_FIELDS = asList("PetitionerFirstName", "PetitionerLastName");

    // intentionally not overriding #validate() as the only required validation for BSP-76 is validating mandatory fields

    public List<String> getMandatoryFields() {
        return MANDATORY_FIELDS;
    }
}
