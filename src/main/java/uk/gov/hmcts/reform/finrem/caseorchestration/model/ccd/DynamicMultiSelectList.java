package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class DynamicMultiSelectList {
    /**
     * The selected value for the dropdown.
     */
    @JsonProperty("value")
    private DynamicMultiSelectListElement value;

    /**
     * List of options for the dropdown.
     */
    @JsonProperty("list_items")
    private List<DynamicMultiSelectListElement> listItems;

    @JsonIgnore
    public String getValueCode() {
        return value == null ? null : value.getCode();
    }
}
