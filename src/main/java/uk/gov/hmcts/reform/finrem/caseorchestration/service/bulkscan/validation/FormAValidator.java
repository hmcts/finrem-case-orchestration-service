package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.bsp.common.model.validation.BulkScanValidationPatterns.CCD_EMAIL_REGEX;
import static uk.gov.hmcts.reform.bsp.common.model.validation.BulkScanValidationPatterns.CCD_PHONE_NUMBER_REGEX;
import static uk.gov.hmcts.reform.bsp.common.service.PostcodeValidator.validatePostcode;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.ADDRESS_OF_PROPERTIES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.APPLICANT_FULL_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.APPLICANT_INTENDS_TO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.APPLYING_FOR_CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.CHILD_SUPPORT_AGENCY_CALCULATION_MADE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.CHILD_SUPPORT_AGENCY_CALCULATION_REASON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.DISCHARGE_PERIODICAL_PAYMENT_SUBSTITUTE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.DIVORCE_STAGE_REACHED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.HWF_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.MORTGAGE_DETAILS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.NATURE_OF_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.ORDER_FOR_CHILDREN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.ORDER_FOR_CHILDREN_NO_AGREEMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.PROVISION_MADE_FOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.helper.BulkScanHelper.dischargePeriodicalPaymentSubstituteChecklistToCcdFieldNames;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.helper.BulkScanHelper.getCommaSeparatedValuesFromOcrDataField;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.helper.BulkScanHelper.natureOfApplicationChecklistToCcdFieldNames;

@Component
public class FormAValidator extends BulkScanFormValidator {

    private static final String HWF_NUMBER_6_DIGITS_REGEX = "\\d{6}";

    private static final String EMPTY_STRING = "";

    private static final List<String> MANDATORY_FIELDS = asList(
        DIVORCE_CASE_NUMBER,
        APPLICANT_FULL_NAME,
        RESPONDENT_FULL_NAME,
        PROVISION_MADE_FOR,
        NATURE_OF_APPLICATION,
        APPLICANT_INTENDS_TO,
        APPLYING_FOR_CONSENT_ORDER,
        DIVORCE_STAGE_REACHED,
        APPLICANT_REPRESENTED
    );

    public List<String> getMandatoryFields() {
        return MANDATORY_FIELDS;
    }

    private static final Map<String, List<String>> ALLOWED_VALUES_PER_FIELD = new HashMap<>();

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
        ALLOWED_VALUES_PER_FIELD.put(APPLYING_FOR_CONSENT_ORDER, Collections.singletonList("Yes"));
        ALLOWED_VALUES_PER_FIELD.put(DIVORCE_STAGE_REACHED, asList("Decree Nisi", "Decree Absolute"));
        ALLOWED_VALUES_PER_FIELD.put(APPLICANT_REPRESENTED, asList(
            "I am not represented by a solicitor in these proceedings",
            "I am not represented by a solicitor in these proceedings but am receiving advice from a solicitor",
            "I am represented by a solicitor in these proceedings, who has signed Section 5"
        ));

        ALLOWED_VALUES_PER_FIELD.put(ORDER_FOR_CHILDREN, asList(
            "there is a written agreement made before 5 April 1993 about maintenance for the benefit of children",
            "there is a written agreement made on or after 5 April 1993 about maintenance for the benefit of children",
            "there is no agreement, but the applicant is applying for payments"
        ));
        ALLOWED_VALUES_PER_FIELD.put(ORDER_FOR_CHILDREN_NO_AGREEMENT, asList(
            "for a stepchild or stepchildren",
            "in addition to child support maintenance already paid under a Child Support Agency assessment",
            "to meet expenses arising from a child’s disability",
            "to meet expenses incurred by a child in being educated or training for work",
            "when either the child or the person with care of the child or the absent parent of the child is not habitually resident in the United Kingdom"
        ));
        ALLOWED_VALUES_PER_FIELD.put(CHILD_SUPPORT_AGENCY_CALCULATION_MADE, asList(
            "Yes",
            "No",
            EMPTY_STRING
        ));
    }

    @Override
    protected Map<String, List<String>> getAllowedValuesPerField() {
        return ALLOWED_VALUES_PER_FIELD;
    }

    @Override
    protected List<String> runPostProcessingValidation(Map<String, String> fieldsMap) {

        return Stream.of(
            validateHwfNumber(fieldsMap, HWF_NUMBER),
            validateHasAtLeastTwoNames(fieldsMap, APPLICANT_FULL_NAME),
            validateHasAtLeastTwoNames(fieldsMap, RESPONDENT_FULL_NAME),
            validateNonMandatoryCommaSeparatedField(fieldsMap,
                NATURE_OF_APPLICATION, natureOfApplicationChecklistToCcdFieldNames),
            validateNonMandatoryCommaSeparatedField(fieldsMap,
                DISCHARGE_PERIODICAL_PAYMENT_SUBSTITUTE, dischargePeriodicalPaymentSubstituteChecklistToCcdFieldNames),
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
            ? Collections.singletonList("HWFNumber is usually 6 digits")
            : emptyList();
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
