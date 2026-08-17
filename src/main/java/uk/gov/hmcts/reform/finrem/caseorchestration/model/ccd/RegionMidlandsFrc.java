package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_midlands_FRCList", generate = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum RegionMidlandsFrc {
    @CCD(label = "Nottingham FRC")
    NOTTINGHAM("nottingham"),
    @CCD(label = "Birmingham FRC")
    BIRMINGHAM("birmingham");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static RegionMidlandsFrc forValue(String value) {
        return Arrays.stream(RegionMidlandsFrc.values())
            .filter(option -> option.getValue().equalsIgnoreCase(value))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
