package uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitorcreatecase.mandatorydatavalidation;


import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class CreateCaseMandatoryDataValidatorTest {

    @Test
    void givenAllValidationFails_whenValidateCalled_thenReturnsErrors() {
        List<MandatoryDataValidator> mandatoryDataValidators = List.of(
            mockValidator(List.of("Validation Error 1")),
            mockValidator(List.of("Validation Error 2", "Validation Error 3"))
        );
        FinremCaseData caseData = FinremCaseData.builder().build();

        CreateCaseMandatoryDataValidator validator = new CreateCaseMandatoryDataValidator(mandatoryDataValidators);
        List<String> validationErrors = validator.validate(caseData);

        assertThat(validationErrors.size()).isEqualTo(3);
        assertThat(validationErrors).containsExactlyInAnyOrder("Validation Error 1", "Validation Error 2",
            "Validation Error 3");
    }

    @Test
    void givenSomeValidationFails_whenValidateCalled_thenReturnsErrors() {
        List<MandatoryDataValidator> mandatoryDataValidators = List.of(
            mockValidator(List.of("Validation Error 1")),
            mockValidator(Collections.emptyList())
        );
        FinremCaseData caseData = FinremCaseData.builder().build();

        CreateCaseMandatoryDataValidator validator = new CreateCaseMandatoryDataValidator(mandatoryDataValidators);
        List<String> validationErrors = validator.validate(caseData);

        assertThat(validationErrors.size()).isEqualTo(1);
        assertThat(validationErrors).containsExactly("Validation Error 1");
    }

    @Test
    void givenNoValidationFails_whenValidateCalled_thenReturnsEmptyList() {
        List<MandatoryDataValidator> mandatoryDataValidators = List.of(
            mockValidator(Collections.emptyList()),
            mockValidator(Collections.emptyList())
        );
        FinremCaseData caseData = FinremCaseData.builder().build();

        CreateCaseMandatoryDataValidator validator = new CreateCaseMandatoryDataValidator(mandatoryDataValidators);
        List<String> validationErrors = validator.validate(caseData);

        assertThat(validationErrors).isEmpty();
    }

    private MandatoryDataValidator mockValidator(List<String> validationMessage) {
        MandatoryDataValidator mockValidator = Mockito.mock(MandatoryDataValidator.class);
        when(mockValidator.validate(any(FinremCaseData.class))).thenReturn(validationMessage);
        return mockValidator;
    }
}
