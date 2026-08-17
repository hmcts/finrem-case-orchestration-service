package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_region_list", generate = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum Region {
    @CCD(label = "Midlands")
    MIDLANDS("midlands"),
    @CCD(label = "London")
    LONDON("london"),
    @CCD(label = "North West")
    NORTHWEST("northwest"),
    @CCD(label = "North East")
    NORTHEAST("northeast"),
    @CCD(label = "South East")
    SOUTHEAST("southeast"),
    @CCD(label = "South West")
    SOUTHWEST("southwest"),
    @CCD(label = "Wales")
    WALES("wales"),
    @CCD(label = "High Court Family Division")
    HIGHCOURT("highcourt");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }
}
