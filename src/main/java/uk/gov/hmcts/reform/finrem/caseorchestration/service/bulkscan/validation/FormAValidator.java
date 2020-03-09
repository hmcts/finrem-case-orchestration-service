package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.reform.bsp.common.model.validation.BulkScanValidationPatterns.CCD_EMAIL_REGEX;
import static uk.gov.hmcts.reform.bsp.common.model.validation.BulkScanValidationPatterns.CCD_PHONE_NUMBER_REGEX;
import static uk.gov.hmcts.reform.bsp.common.service.PostcodeValidator.validatePostcode;

@Component
public class FormAValidator extends BulkScanFormValidator {

    private static final List<String> MANDATORY_FIELDS = asList(
        "ApplicantRepresented"
    );

    private static final Map<String, List<String>> ALLOWED_VALUES_PER_FIELD = new HashMap<>();

    static {
        ALLOWED_VALUES_PER_FIELD.put("ApplicantRepresented",
                asList("I am not represented by a solicitor in these proceedings",
                        "I am not represented by a solicitor in these proceedings but am receiving advice from a solicitor",
                        "I am represented by a solicitor in these proceedings, who has signed Section 5"
                )
        );
    }

    public List<String> getMandatoryFields() {
        return MANDATORY_FIELDS;
    }

    @Override
    protected Map<String, List<String>> getAllowedValuesPerField() {
        return ALLOWED_VALUES_PER_FIELD;
    }

    @Override
    protected List<String> runPostProcessingValidation(Map<String, String> fieldsMap) {
        List<String> errorMessages = Stream.of(
            validateFieldMatchesRegex(fieldsMap, "ApplicantSolicitorPhone", CCD_PHONE_NUMBER_REGEX),
            validateFieldMatchesRegex(fieldsMap, "ApplicantPhone", CCD_PHONE_NUMBER_REGEX),
            validateFieldMatchesRegex(fieldsMap, "ApplicantSolicitorEmail", CCD_EMAIL_REGEX),
            validateFieldMatchesRegex(fieldsMap, "ApplicantEmail", CCD_EMAIL_REGEX),
            validatePostcode(fieldsMap, "ApplicantSolicitorAddressPostcode"),
            validatePostcode(fieldsMap, "ApplicantAddressPostcode"),
            validatePostcode(fieldsMap, "RespondentAddressPostcode"),
            validatePostcode(fieldsMap, "RespondentSolicitorAddressPostcode")
        )
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

        return errorMessages;
    }

    private static List<String> validateFieldMatchesRegex(Map<String, String> fieldsMap, String fieldKey, String validationRegex) {
        List<String> validationMessages = new ArrayList<>();

        if (fieldsMap.containsKey(fieldKey)) {
            String valueToValidate = fieldsMap.get(fieldKey);
            if (!valueToValidate.matches(validationRegex)) {
                validationMessages.add(fieldKey + " is not in a valid format");
            }
        }
        return validationMessages;
    }
}
