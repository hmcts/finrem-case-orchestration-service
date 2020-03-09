package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.APPLICANT_FULL_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.APPLICANT_INTENDS_TO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.APPLYING_FOR_CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.DISCHARGE_PERIODICAL_PAYMENT_SUBSTITUTE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.DIVORCE_STAGE_REACHED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.HWF_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.NATURE_OF_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.PROVISION_MADE_FOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.helper.BulkScanHelper.dischargePeriodicalPaymentSubstituteChecklistToCcdFieldNames;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.helper.BulkScanHelper.getCommaSeparatedValuesFromOcrDataField;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.helper.BulkScanHelper.natureOfApplicationChecklistToCcdFieldNames;

@Component
public class FormAValidator extends BulkScanFormValidator {

    private static final String HWF_NUMBER_6_DIGITS_REGEX = "\\d{6}";

    private static final List<String> MANDATORY_FIELDS = asList(
            DIVORCE_CASE_NUMBER,
            APPLICANT_FULL_NAME,
            RESPONDENT_FULL_NAME,
            PROVISION_MADE_FOR,
            NATURE_OF_APPLICATION,
            APPLICANT_INTENDS_TO,
            APPLYING_FOR_CONSENT_ORDER,
            DIVORCE_STAGE_REACHED
    );

    private static final Map<String, List<String>> ALLOWED_VALUES_PER_FIELD = new HashMap<>();

    public List<String> getMandatoryFields() {
        return MANDATORY_FIELDS;
    }

    static {
        ALLOWED_VALUES_PER_FIELD.put(PROVISION_MADE_FOR, asList(
                "in connection with matrimonial or civil partnership proceedings",
                "under paragraphs 1 or 2 of Schedule 1 to the Children Act 1989"
        ));
        ALLOWED_VALUES_PER_FIELD.put(APPLICANT_INTENDS_TO, asList(
                "ApplyToCourtFor",
                "ProceedWithApplication",
                "ApplyToVary",
                "ApplyToDischargePeriodicalPaymentOrder"
        ));
        ALLOWED_VALUES_PER_FIELD.put(APPLYING_FOR_CONSENT_ORDER, asList("Yes"));
        ALLOWED_VALUES_PER_FIELD.put(DIVORCE_STAGE_REACHED, asList("Decree Nisi", "Decree Absolute"));
    }
    
    @Override
    protected Map<String, List<String>> getAllowedValuesPerField() {
        return ALLOWED_VALUES_PER_FIELD;
    }

    @Override
    protected List<String> runPostProcessingValidation(Map<String, String> fieldsMap) {
        List<String> errorMessages = Stream.of(
            validateHwfNumber(fieldsMap, HWF_NUMBER),
            validateHasAtLeastTwoNames(fieldsMap, APPLICANT_FULL_NAME),
            validateHasAtLeastTwoNames(fieldsMap, RESPONDENT_FULL_NAME),
            validateNonMandatoryCommaSeparatedField(fieldsMap,
                NATURE_OF_APPLICATION, natureOfApplicationChecklistToCcdFieldNames),
            validateNonMandatoryCommaSeparatedField(fieldsMap,
                DISCHARGE_PERIODICAL_PAYMENT_SUBSTITUTE, dischargePeriodicalPaymentSubstituteChecklistToCcdFieldNames)
        )
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
        
        return errorMessages;
    }

    private static List<String> validateNonMandatoryCommaSeparatedField(Map<String, String> fieldsMap, String commaSeparatedFieldKey,
                                                                        Map<String, String> validOcrFieldNamesToCcdFieldNames) {
        List<String> validationWarningMessages = new ArrayList<>();

        String commaSeparatedFieldValue = fieldsMap.getOrDefault(commaSeparatedFieldKey, "");

        if (StringUtils.isEmpty(commaSeparatedFieldValue)) {
            return validationWarningMessages;
        }

        boolean allOcrFieldsCanBeMapped = getCommaSeparatedValuesFromOcrDataField(commaSeparatedFieldValue)
            .stream()
            .map(validOcrFieldNamesToCcdFieldNames::containsKey)
            .reduce(Boolean::logicalAnd)
            .orElse(false);

        if (!allOcrFieldsCanBeMapped) {
            validationWarningMessages.add(
                String.format("%s contains a value that is not accepted", commaSeparatedFieldKey)
            );
        }

        return validationWarningMessages;
    }
    
    private static List<String> validateHasAtLeastTwoNames(Map<String, String> fieldsMap, String fieldName) {
        String fieldValue = fieldsMap.get(fieldName);
        return fieldValue != null && Arrays.stream(fieldValue.split(" "))
            .filter(StringUtils::isNotBlank)
            .count() < 2
            ? asList(String.format("%s must contain a firstname and a lastname", fieldName))
            : emptyList();
    }

    private static List<String> validateHwfNumber(Map<String, String> fieldsMap, String fieldName) {
        String hwfNumber = fieldsMap.get(fieldName);
        return hwfNumber != null && !hwfNumber.matches(HWF_NUMBER_6_DIGITS_REGEX)
            ? asList("HWFNumber is usually 6 digits")
            : emptyList();
    }
}
