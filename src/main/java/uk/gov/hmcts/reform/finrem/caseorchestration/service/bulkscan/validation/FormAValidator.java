package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.helper.BulkScanHelper.getCommaSeparatedValuesFromOcrDataField;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.helper.BulkScanHelper.maimUrgencyChecklistToCcdFieldNames;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.helper.BulkScanHelper.miamDomesticViolenceChecklistToCcdFieldNames;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.helper.BulkScanHelper.miamExemptionsChecklistToCcdFieldNames;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.helper.BulkScanHelper.miamOtherGroundsChecklistToCcdFieldNames;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.helper.BulkScanHelper.miamPreviousAttendanceChecklistToCcdFieldNames;

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
    protected Map<String, List<String>> getAllowedValuesPerField() {
        return ALLOWED_VALUES_PER_FIELD;
    }

    @Override
    protected List<String> runPostProcessingValidation(Map<String, String> fieldsMap) {

        return Stream.of(
            validateNonMandatoryCommaSeparatedField(fieldsMap,
                "MIAMExemptionsChecklist", miamExemptionsChecklistToCcdFieldNames),
            validateNonMandatoryCommaSeparatedField(fieldsMap,
                "MIAMDomesticViolenceChecklist", miamDomesticViolenceChecklistToCcdFieldNames),
            validateNonMandatoryCommaSeparatedField(fieldsMap,
                "MIAMUrgencyChecklist", maimUrgencyChecklistToCcdFieldNames),
            validateNonMandatoryCommaSeparatedField(fieldsMap,
                "MIAMPreviousAttendanceChecklist", miamPreviousAttendanceChecklistToCcdFieldNames),
            validateNonMandatoryCommaSeparatedField(fieldsMap,
                "MIAMOtherGroundsChecklist", miamOtherGroundsChecklistToCcdFieldNames)
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
                String.format("%s contains a value that is not accepted.", commaSeparatedFieldKey)
            );
        }

        return validationWarningMessages;
    }
}