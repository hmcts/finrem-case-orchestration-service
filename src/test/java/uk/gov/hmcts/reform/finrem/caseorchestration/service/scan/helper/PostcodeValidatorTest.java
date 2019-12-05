package uk.gov.hmcts.reform.finrem.caseorchestration.service.scan.helper;

import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.scan.helper.PostcodeValidator.validatePostcode;

public class PostcodeValidatorTest {

    private static final String d8PetitionerPostcodeKey = "D8PetitionerPostcode";
    private static final String postcodeValidationErrorMessage = "is usually 6 or 7 characters long";

    @Test
    public void shouldNotProduceErrorMessagesWhenPostcodeLengthIsValid() {
        String[] validPostcodes = {"M1 1AA", "L1 0AP", "B151TT", "SW15 5PU", "CH5 3QW", "SE279TU", "GL51 0EX"};
        for (String validPostcode : validPostcodes) {
            Map<String, String> validPostcodeFieldMap = new HashMap<>();
            validPostcodeFieldMap.put(d8PetitionerPostcodeKey, validPostcode);

            List<String> actualValidationMessages = validatePostcode(validPostcodeFieldMap, d8PetitionerPostcodeKey);

            assertThat(actualValidationMessages, is(emptyList()));
        }
    }

    @Test
    public void shouldProduceWarningWhenPostcodeHasLessThan6Characters() {
        String[] invalidPostcodes = {"N", "B1", "SE4", "BT7Q", "SW15P", "M11 A", "CV30A"};

        for (String invalidPostcode : invalidPostcodes) {
            Map<String, String> invalidPostcodeFieldMap = new HashMap<>();
            invalidPostcodeFieldMap.put(d8PetitionerPostcodeKey, invalidPostcode);

            List<String> actualValidationMessages = validatePostcode(invalidPostcodeFieldMap, d8PetitionerPostcodeKey);

            assertThat(actualValidationMessages, hasItem(String.format("%s %s", d8PetitionerPostcodeKey, postcodeValidationErrorMessage)));
        }
    }

    @Test
    public void shouldProduceWarningWhenPostcodeHasMoreThan8Characters() {
        String[] invalidPostcodes = {"SW15 1PXX", "M11 12HEQ", "BT12   8TR", "BIRMINGHAM1", "LT1REEEEE"};

        for (String invalidPostcode : invalidPostcodes) {
            Map<String, String> invalidPostcodeFieldMap = new HashMap<>();
            invalidPostcodeFieldMap.put(d8PetitionerPostcodeKey, invalidPostcode);

            List<String> actualValidationMessages = validatePostcode(invalidPostcodeFieldMap, d8PetitionerPostcodeKey);

            assertThat(actualValidationMessages, hasItem(String.format("%s %s", d8PetitionerPostcodeKey, postcodeValidationErrorMessage)));
        }
    }
}
