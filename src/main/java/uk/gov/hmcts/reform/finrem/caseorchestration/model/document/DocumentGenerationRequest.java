package uk.gov.hmcts.reform.finrem.caseorchestration.model.document;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;

import java.util.Map;

@Data
@Builder
@Schema(description = "Request body model for Document Generation Request")
public class DocumentGenerationRequest {
    @Schema(description = "Name of the template", required = true)
    @JsonProperty(value = "template", required = true)
    @NotBlank
    private final String template;
    @Schema(description = "Name of the file")
    @JsonProperty(value = "fileName", required = true)
    private final String fileName;
    @JsonProperty(value = "values", required = true)
    @Schema(description = "Placeholder key / value pairs", required = true)
    private final Map<String, Object> values;
}
