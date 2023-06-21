package uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@EqualsAndHashCode
@JsonPropertyOrder(value = {"caseReference"})
@AllArgsConstructor
@NoArgsConstructor
public class CaseReference {

    @JsonProperty
    private String caseReference;

}
