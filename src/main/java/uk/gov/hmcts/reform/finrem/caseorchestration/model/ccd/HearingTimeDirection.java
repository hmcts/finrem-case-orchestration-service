package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum HearingTimeDirection {
    STANDARD_TIME("standardTime"),
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
