package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_nw_frc_list", generate = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum RegionNorthWestFrc {
    @CCD(label = "Liverpool FRC")
    @JsonProperty("liverpool")
    LIVERPOOL("liverpool"),
    @CCD(label = "Manchester FRC")
    @JsonProperty("manchester")
    MANCHESTER("manchester"),
    @CCD(label = "Lancashire and Cumbria FRC")
    @JsonProperty("lancashire")
    LANCASHIRE("lancashire");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static RegionNorthEastFrc forValue(String value) {
        return Arrays.stream(RegionNorthEastFrc.values())
            .filter(option -> option.getValue().equalsIgnoreCase(value))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
