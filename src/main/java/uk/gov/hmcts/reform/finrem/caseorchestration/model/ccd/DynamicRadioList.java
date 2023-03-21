package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class DynamicRadioList {
    /**
     * The selected value for the dropdown.
     */
    @JsonProperty("value")
    private DynamicRadioListElement value;

    /**
     * List of options for the dropdown.
     */
    @JsonProperty("list_items")
    private List<DynamicRadioListElement> listItems;

    @JsonIgnore
    public String getValueCode() {
        return value == null ? null : value.getCode();
    }
}
