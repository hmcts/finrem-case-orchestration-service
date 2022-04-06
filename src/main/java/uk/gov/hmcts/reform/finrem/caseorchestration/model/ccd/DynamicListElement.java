package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class DynamicListElement {

    /**
     * Property that maps to the value attribute of the option tag.
     */
    @JsonProperty("code")
    private final String code;

    /**
     * Property that maps to the label attribute of the option tag.
     */
    @JsonProperty("label")
    private final String label;

}
