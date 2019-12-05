package uk.gov.hmcts.reform.finrem.caseorchestration.model.scan.validation.out;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class OcrValidationResponse {

    @JsonProperty("warnings")
    private final List<String> warnings;

    @JsonProperty("errors")
    private final List<String> errors;

    @JsonProperty("status")
    private final ValidationStatus status;

    @JsonCreator
    public OcrValidationResponse(OcrValidationResult ocrValidationResult) {
        this.warnings = ocrValidationResult.getWarnings();
        this.errors = ocrValidationResult.getErrors();
        this.status = ocrValidationResult.getStatus();
    }

}