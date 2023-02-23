package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class DynamicRadioListElement {

    /**
     * Property that maps to the value attribute of the option tag.
     */
    @JsonProperty("code")
    private String code;

    /**
     * Property that maps to the label attribute of the option tag.
     */
    @JsonProperty("label")
    private String label;

}
