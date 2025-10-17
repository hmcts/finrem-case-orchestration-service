package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

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
public class DynamicMultiSelectList {
    /**
     * The selected value for the dropdown.
     */
    @JsonProperty("value")
    private List<DynamicMultiSelectListElement> value;

    /**
     * List of options for the dropdown.
     */
    @JsonProperty("list_items")
    private List<DynamicMultiSelectListElement> listItems;

    public List<DynamicMultiSelectListElement> getValue() {
        return value != null ? value : null;
    }

    /**
     * Set the multi-select list by selecting items that match the given list of codes.
     *
     * <p>
     * This method filters the existing {@code listItems} to include only those whose code
     * is present in the provided {@code codes} list. The filtered items are then
     * set as the current selected values of the list.
     * </p>
     *
     * @param codes a list of codes to set in the multi-select list
     * @return the updated {@link DynamicMultiSelectList} instance with the preset values applied
     */
    public DynamicMultiSelectList setValueByCodes(List<String> codes) {
        this.setValue(this.listItems.stream().filter(d -> codes.contains(d.getCode())).toList());
        return this;
    }
}
