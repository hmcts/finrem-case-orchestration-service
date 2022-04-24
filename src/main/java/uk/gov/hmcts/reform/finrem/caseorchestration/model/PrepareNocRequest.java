package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class PrepareNocRequest {
    @JsonProperty("case_data")
    private Map<String, Object> caseData;

    public static PrepareNocRequest prepareNocRequest(Map<String, Object> caseData) {
        return PrepareNocRequest.builder().caseData(caseData).build();
    }
}
