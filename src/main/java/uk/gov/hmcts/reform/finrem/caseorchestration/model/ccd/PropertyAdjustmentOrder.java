package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_PropertyAdjustmentOrder", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PropertyAdjustmentOrder {
    @CCD(label = "Property address", searchable = false, typeOverride = FieldType.TextArea)
    @JsonProperty("propertAddress")
    private String propertyAddress;
    @CCD(
            label = "Name(s) and address(es) of any mortgage(s) for property",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String nameForProperty;
}
