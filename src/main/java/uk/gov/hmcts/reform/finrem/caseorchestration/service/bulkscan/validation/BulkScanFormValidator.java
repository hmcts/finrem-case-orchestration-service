package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.bsp.common.model.validation.in.OcrDataField;
import uk.gov.hmcts.reform.bsp.common.model.validation.out.OcrValidationResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.helper.BulkScanHelper.produceMapWithoutEmptyEntries;

public abstract class BulkScanFormValidator {

    protected abstract List<String> getMandatoryFields();

    protected abstract List<String> runPostProcessingValidation(Map<String, String> fieldsMap);

    public OcrValidationResult validateBulkScanForm(List<OcrDataField> ocrDataFields) {
        OcrValidationResult.Builder validationResultBuilder = OcrValidationResult.builder();

        Map<String, String> fieldsMap = produceMapWithoutEmptyEntries(ocrDataFields);

        List<String> validationMessagesForMissingMandatory = produceErrorsForMissingMandatoryFields(fieldsMap);
        validationMessagesForMissingMandatory.forEach(validationResultBuilder::addWarning);

        List<String> validationMessagesForValuesNotAllowed = produceErrorsForValuesNotAllowed(fieldsMap);
        validationMessagesForValuesNotAllowed.forEach(validationResultBuilder::addWarning);

        List<String> validationMessagesFromPostProcessingValidation = runPostProcessingValidation(fieldsMap);
        validationMessagesFromPostProcessingValidation.forEach(validationResultBuilder::addWarning);

        return validationResultBuilder.build();
    }

    private List<String> produceErrorsForMissingMandatoryFields(Map<String, String> fieldsMap) {
        return getMandatoryFields().stream()
            .filter(f -> !fieldsMap.containsKey(f))
            .map(f -> String.format("Mandatory field \"%s\" is missing", f))
            .collect(Collectors.toList());
    }

    private List<String> produceErrorsForValuesNotAllowed(Map<String, String> fieldsMap) {
        List<String> validationErrorMessages = new ArrayList<>();

        getAllowedValuesPerField().forEach((fieldName, allowedValues) -> {
            if (fieldsMap.containsKey(fieldName)) {
                String ocrFieldValue = fieldsMap.get(fieldName);
                if (!allowedValues.contains(ocrFieldValue)) {
                    String errorMessage = produceErrorMessageForValueNotAllowed(fieldName, allowedValues);
                    validationErrorMessages.add(errorMessage);
                }
            }
        });

        return validationErrorMessages;
    }

    protected abstract Map<String, List<String>> getAllowedValuesPerField();

    private String produceErrorMessageForValueNotAllowed(String fieldName, List<String> allowedValues) {
        StringBuilder errorMessage = new StringBuilder();

        int arraySize = allowedValues.size();
        
        if (arraySize == 1) {
            return String.format("%s only accepts value of \"%s\"", fieldName, allowedValues.get(0));
        }
        
        for (int i = 1; i <= arraySize; i++) {
            String allowedValue = allowedValues.get(i - 1);

            if (StringUtils.isNotBlank(allowedValue)) {
                errorMessage.append("\"");
                errorMessage.append(allowedValue);
                errorMessage.append("\"");
            } else {
                errorMessage.append("left blank");
            }

            boolean lastItem = i == arraySize;
            if (!lastItem) {
                boolean itemBeforeLast = i == arraySize - 1;
                if (!itemBeforeLast) {
                    errorMessage.append(", ");
                } else {
                    errorMessage.append(" or ");
                }
            }
        }

        return format("%s must be %s", fieldName, errorMessage.toString());
    }
}
