package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public enum HearingMode {
    IN_PERSON("In_Person", "In Person"),
    VIDEO_CALL("Video_Call", "Remote - Video call"),
    PHONE_CALL("Phone_Call", "Remote - Phone call"),
    HYBRID("Hybrid", "Hybrid - In person and remote"),;

    private final String value;
    @Getter
    private final String displayValue;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static HearingMode forValue(String value) {
        return Arrays.stream(HearingMode.values())
            .filter(option -> option.getValue().equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }
}
