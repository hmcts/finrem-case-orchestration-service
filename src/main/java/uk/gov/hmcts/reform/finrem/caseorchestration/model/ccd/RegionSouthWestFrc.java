package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_sw_frc_list", generate = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum RegionSouthWestFrc {
    @CCD(label = "Devon and Cornwall FRC")
    DEVON("devon"),
    @CCD(label = "Dorset and Hampshire FRC")
    DORSET("dorset"),
    @CCD(label = "Bristol FRC")
    BRISTOL("bristol");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static RegionSouthWestFrc forValue(String value) {
        return Arrays.stream(RegionSouthWestFrc.values())
            .filter(option -> option.getValue().equalsIgnoreCase(value))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
