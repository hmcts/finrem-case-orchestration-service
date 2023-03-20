package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum RegionNorthWestFrc {
    @JsonProperty("liverpool")
    LIVERPOOL("liverpool"),
    @JsonProperty("manchester")
    MANCHESTER("manchester"),
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
