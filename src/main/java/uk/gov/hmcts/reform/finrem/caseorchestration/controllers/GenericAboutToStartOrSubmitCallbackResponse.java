package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenericAboutToStartOrSubmitCallbackResponse<D> {

    private D data;

    @JsonProperty("data_classification")
    private Map<String, Object> dataClassification;

    @JsonProperty("security_classification")
    private Map<String, Object> securityClassification;

    private List<String> errors;

    private List<String> warnings;

    private String state;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("confirmation_header")
    private String confirmationHeader;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("confirmation_body")
    private String confirmationBody;

    public List<String> getErrors() {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        return errors;
    }

    public List<String> getWarnings() {
        if (warnings == null) {
            warnings = new ArrayList<>();
        }
        return warnings;
    }

    @JsonIgnore
    public boolean hasErrors() {
        return !getErrors().isEmpty();
    }

    @JsonIgnore
    public boolean hasWarnings() {
        return !getWarnings().isEmpty();
    }
}
