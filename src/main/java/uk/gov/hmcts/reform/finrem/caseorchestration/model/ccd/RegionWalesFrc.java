package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_wales_frc_list", generate = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum RegionWalesFrc {
    @CCD(label = "Newport FRC")
    NEWPORT("newport"),
    @CCD(label = "Swansea FRC")
    SWANSEA("swansea"),
    @CCD(label = "North Wales FRC")
    NORTH_WALES("northwales");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static RegionWalesFrc forValue(String value) {
        return Arrays.stream(RegionWalesFrc.values())
            .filter(option -> option.getValue().equalsIgnoreCase(value))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
