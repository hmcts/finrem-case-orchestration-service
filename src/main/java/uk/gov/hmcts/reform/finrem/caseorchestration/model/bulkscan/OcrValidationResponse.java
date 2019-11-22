package uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class OcrValidationResponse {

    @JsonProperty("warnings")
    public final List<String> warnings;

    @JsonProperty("errors")
    public final List<String> errors;

    @JsonProperty("status")
    public final ValidationStatus status;

    @JsonCreator
    public OcrValidationResponse(OcrValidationResult ocrValidationResult) {
        this.warnings = ocrValidationResult.getWarnings();
        this.errors = ocrValidationResult.getErrors();
        this.status = ocrValidationResult.getStatus();
    }
}
