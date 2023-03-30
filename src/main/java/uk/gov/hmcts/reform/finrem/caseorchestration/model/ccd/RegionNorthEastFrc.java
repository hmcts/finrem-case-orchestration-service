package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum RegionNorthEastFrc {
    @JsonProperty("cleveland")
    CLEVELAND("cleveland"),
    @JsonProperty("cleaveland")
    CLEAVELAND("cleaveland"),
    @JsonProperty("nwyorkshire")
    NW_YORKSHIRE("nwyorkshire"),
    @JsonProperty("hsyorkshire")
    HS_YORKSHIRE("hsyorkshire");

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
