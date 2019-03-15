package uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.validation;

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
public class PBAValidationResponse {
    @JsonProperty(value = "pbaNumberValid")
    private boolean pbaNumberValid;
}
