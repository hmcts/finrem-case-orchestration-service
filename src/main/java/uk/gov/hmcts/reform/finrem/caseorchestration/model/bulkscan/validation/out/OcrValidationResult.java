package uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.validation.out;

import lombok.Getter;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.validation.ValidationStatus;

import java.util.List;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

@Getter
public class OcrValidationResult {

    private final List<String> warnings;
    private final List<String> errors;
    private final ValidationStatus status;

    public OcrValidationResult(
        List<String> warnings,
        List<String> errors
    ) {
        if (isNotEmpty(errors)) {
            this.status = ValidationStatus.ERRORS;
        } else {
            this.status = ValidationStatus.SUCCESS;
        }

        this.warnings = warnings;
        this.errors = errors;
    }
}
