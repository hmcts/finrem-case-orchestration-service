package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
