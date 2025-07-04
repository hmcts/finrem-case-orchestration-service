package uk.gov.hmcts.reform.finrem.caseorchestration.model.fee;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeeValue {
    @JsonProperty("FeeDescription")
    private String feeDescription;

    @JsonProperty("FeeVersion")
    private String feeVersion;

    @JsonProperty("FeeCode")
    private String feeCode;

    @JsonProperty("FeeAmount")
    private String feeAmount;
}
