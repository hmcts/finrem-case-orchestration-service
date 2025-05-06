package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public enum HearingMode {
    IN_PERSON("In_Person"),
    VIDEO_CALL("Video_Call"),
    PHONE_CALL("Phone_Call");

    private final String value;

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
