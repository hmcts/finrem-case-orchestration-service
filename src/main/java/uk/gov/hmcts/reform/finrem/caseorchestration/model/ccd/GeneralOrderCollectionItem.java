package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class GeneralOrderCollectionItem extends CollectionElement<GeneralOrder> {

    @JsonProperty("value")
    private GeneralOrder generalOrder;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        GeneralOrderCollectionItem that = (GeneralOrderCollectionItem) o;
        return Objects.equals(generalOrder, that.generalOrder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), generalOrder);
    }
}
