package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.OcrDataField;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public abstract class BulkScanFormValidator {

    private final List<String> errors = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();

    protected List<OcrDataField> ocrDataFields;

    protected abstract List<String> getMandatoryFields();

    public List<String> getErrors() {
        return errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void validate(List<OcrDataField> ocrDataFields) {
        this.ocrDataFields = ocrDataFields;
        validateMandatoryFields();
    }

    private void validateMandatoryFields() {
        warnings.addAll(getWarningsForMissingMandatoryFields());
    }

    protected Map<String, String> getFilledFormFields() {
        return ocrDataFields.stream()
            .filter(field -> isNotBlank(field.getValue()))
            .collect(toMap(OcrDataField::getName, OcrDataField::getValue));
    }

    private List<String> getWarningsForMissingMandatoryFields() {
        Map<String, String> filledFormFields = getFilledFormFields();

        return getMandatoryFields().stream()
            .filter(field -> !filledFormFields.containsKey(field))
            .map(field -> String.format("Mandatory field \"%s\" is missing", field))
            .collect(Collectors.toList());
    }
}
