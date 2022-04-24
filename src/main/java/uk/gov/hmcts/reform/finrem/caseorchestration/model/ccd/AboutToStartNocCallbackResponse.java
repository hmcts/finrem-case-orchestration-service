package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AboutToStartNocCallbackResponse implements CallbackResponse {
    private Map<String, Object> data;

    @JsonProperty("data_classification")
    private Map<String, Object> dataClassification;

    private List<String> errors;

    private List<String> warnings;
}
