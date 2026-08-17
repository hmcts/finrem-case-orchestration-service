package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_fl_hearingTimeDirections", generate = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum HearingTimeDirection {
    @CCD(label = "The application can be listed for the standard time")
    STANDARD_TIME("standardTime"),
    @CCD(
            label = "Additional time is needed (Or add in another directions about the hearing, e.g. directions for listing of MPS application)"
    )
    ADDITIONAL_TIME_REQ("additionalTimeReq");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static HearingTimeDirection forValue(String value) {
        return Arrays.stream(HearingTimeDirection.values())
            .filter(option -> option.getValue().equalsIgnoreCase(value))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
