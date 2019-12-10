package uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.validation.out;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

@Getter
public class OcrValidationResult {

    private final List<String> warnings;
    private final List<String> errors;
    private final ValidationStatus status;

    private OcrValidationResult(List<String> warnings, List<String> errors, ValidationStatus status) {
        this.warnings = warnings;
        this.errors = errors;
        this.status = status;
    }

    public static Builder builder() {
        return new OcrValidationResult.Builder();
    }

    public static class Builder {
        private List<String> warnings = new ArrayList<>();
        private List<String> errors = new ArrayList<>();
        private ValidationStatus status;

        private Builder() {
        }

        public OcrValidationResult build() {
            this.status = determineStatus();
            return new OcrValidationResult(this.warnings, this.errors, this.status);
        }

        private ValidationStatus determineStatus() {
            ValidationStatus correctStatus;

            if (isNotEmpty(this.errors)) {
                correctStatus = ValidationStatus.ERRORS;
            } else if (isNotEmpty(this.warnings)) {
                correctStatus = ValidationStatus.WARNINGS;
            } else {
                correctStatus = ValidationStatus.SUCCESS;
            }

            return correctStatus;
        }

        public Builder addError(String errorMessage) {
            this.errors.add(errorMessage);
            return this;
        }

        public Builder addWarning(String warningMessage) {
            this.warnings.add(warningMessage);
            return this;
        }
    }
}
