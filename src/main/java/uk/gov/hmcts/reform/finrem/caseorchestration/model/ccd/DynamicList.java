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
public class DynamicList {

    /**
     * Constructs a new {@link DynamicList} instance from a given string value.
     * <p>
     * This constructor is primarily used for deserialization purposes, allowing
     * Jackson to create a {@link DynamicList} object from a string representation.
     * The provided string value is used to initialize the {@code code} property
     * of the {@link DynamicListElement} that represents the selected value.
     * </p>
     *
     * @param value the string value used to set the {@code code} property of the
     *              {@link DynamicListElement} representing the selected value.
     */
    public DynamicList(String value) {
        this.value = new DynamicListElement();
        this.value.setCode(value);
    }

    /**
     * The selected value for the dropdown.
     */
    @JsonProperty("value")
    private DynamicListElement value;

    /**
     * List of options for the dropdown.
     */
    @JsonProperty("list_items")
    private List<DynamicListElement> listItems;

    @JsonIgnore
    public String getValueCode() {
        return value == null ? null : value.getCode();
    }
}
