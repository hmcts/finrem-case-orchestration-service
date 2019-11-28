package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.OcrDataField;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class FormAValidatorTest {

    private FormAValidator formAValidator;

    @Before
    public void init() {
        formAValidator = new FormAValidator();
    }

    @Test
    public void whenValidating_missingMandatoryFieldsCauseValidationErrors() {
        formAValidator.validate(Collections.emptyList());
        assertThat(formAValidator.getErrors()).isEmpty();
        assertThat(formAValidator.getWarnings()).contains("Mandatory field \"PetitionerFirstName\" is missing");
        assertThat(formAValidator.getWarnings()).contains("Mandatory field \"PetitionerLastName\" is missing");
    }

    @Test
    public void givenAllMadatoryFieldsPresent_whenValidating_thereAreNoErrorsOrWarnings() {
        OcrDataField firstName = new OcrDataField("PetitionerFirstName", "John");
        OcrDataField lastName = new OcrDataField("PetitionerLastName", "Doe");
        formAValidator.validate(Arrays.asList(firstName, lastName));
        assertThat(formAValidator.getWarnings()).isEmpty();
        assertThat(formAValidator.getErrors()).isEmpty();
    }
}
