package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import java.util.List;
import java.util.Map;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Jacksonized
public class OldAboutToStartOrSubmitCallbackResponse implements CallbackResponse {

    private Map<String, Object> data;

    @JsonProperty("data_classification")
    private Map<String, Object> dataClassification;

    @JsonProperty("security_classification")
    private Map<String, Object> securityClassification;

    private List<String> errors;

    private List<String> warnings;

    private String  state;

}
